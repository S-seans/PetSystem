<template>
  <div class="apps-layout">
    <PublicHeader />

    <div class="container">
      <div class="page-head">
        <h2>我的领养申请</h2>
        <p>共 {{ total }} 条申请，可随时撤销待审核的申请</p>
      </div>

      <main v-loading="loading" class="apps-main">
        <div v-for="item in list" :key="item.requestId" class="app-card">
          <div class="app-top">
            <div class="app-pet">
              <div class="pet-icon">{{ petEmoji(item.petName) }}</div>
              <div class="pet-name-wrap">
                <span class="pet-name">{{ item.petName || '未知宠物' }}</span>
                <span class="pet-id">申请号 #{{ item.requestId }}</span>
              </div>
            </div>
            <span class="status-tag" :class="'st-' + item.status">{{ statusText(item.status) }}</span>
          </div>

          <p class="reason">{{ item.reason || '未填写领养理由' }}</p>

          <div class="app-meta">
            <span v-if="item.reviewRemark" class="meta-line">
              <span class="meta-label">审核备注</span>{{ item.reviewRemark }}
            </span>
            <span class="meta-line">
              <span class="meta-label">提交时间</span>{{ formatTime(item.createTime) }}
            </span>
            <span v-if="item.reviewTime" class="meta-line">
              <span class="meta-label">审核时间</span>{{ formatTime(item.reviewTime) }}
            </span>
          </div>

          <div class="app-foot">
            <el-button v-if="item.status === ADOPTION_STATUS.PENDING" class="btn-ghost btn-withdraw" :loading="withdrawingId === item.requestId" @click="withdraw(item)">撤销申请</el-button>
            <span v-else class="locked-tip">{{ statusTip(item.status) }}</span>
          </div>
        </div>

        <el-empty v-if="!loading && list.length === 0" description="还没有领养申请，去宠物列表看看吧" />
      </main>

      <div class="pagination-wrap" v-if="!loading && total > 0">
        <el-pagination
          layout="prev, pager, next"
          background
          :total="total"
          :page-size="queryParams.pageSize"
          :current-page="queryParams.pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <footer class="site-footer">
      <p>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue'
import { listMyAdoption, delMyAdoption } from '@/api/adoption/adoption'
import { ADOPTION_STATUS, adoptionStatusText } from '@/utils/business'
import PublicHeader from '@/components/PublicHeader'

const { proxy } = getCurrentInstance()
const year = new Date().getFullYear()

// 页面卸载时取消在途请求，避免快速切页堆积“孤儿请求”
const abortCtrl = new AbortController()
onBeforeUnmount(() => abortCtrl.abort())

const list = ref([])
const total = ref(0)
const loading = ref(true)
const withdrawingId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

function statusText(status) {
  return adoptionStatusText(status)
}

function statusTip(status) {
  if (status === ADOPTION_STATUS.PASS) return '申请已通过，等待办理领养手续'
  if (status === ADOPTION_STATUS.OUT) return '宠物已被领养，感谢你的爱心'
  if (status === ADOPTION_STATUS.REJECT) return '申请未通过，可联系救助站了解原因'
  return ''
}

function petEmoji(petName) {
  const name = petName || ''
  if (/猫/.test(name)) return '🐱'
  if (/犬|狗/.test(name)) return '🐶'
  if (/兔/.test(name)) return '🐰'
  if (/鼠|仓鼠/.test(name)) return '🐹'
  return '🐾'
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  const pad = n => (n < 10 ? '0' + n : n)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function getList() {
  loading.value = true
  listMyAdoption({ ...queryParams }, { signal: abortCtrl.signal }).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).catch(err => {
    if (err && (err.code === 'ERR_CANCELED' || err.name === 'CanceledError')) return
    list.value = []
    total.value = 0
    proxy.$modal.msgError('获取申请列表失败')
  }).finally(() => {
    loading.value = false
  })
}

function handlePageChange(page) {
  queryParams.pageNum = page
  getList()
}

function withdraw(item) {
  proxy.$modal.confirm(`确定撤销对「${item.petName || '该宠物'}」的领养申请吗？`).then(() => {
    withdrawingId.value = item.requestId
    delMyAdoption(item.requestId).then(() => {
      proxy.$modal.msgSuccess('撤销成功')
      getList()
    }).catch(() => {
      proxy.$modal.msgError('撤销失败，请稍后重试')
    }).finally(() => {
      withdrawingId.value = null
    })
  }).catch(() => {})
}

onMounted(getList)
</script>

<style scoped>
.apps-layout {
  min-height: 100vh;
  background: #faf7f2;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Segoe UI", Roboto, sans-serif;
  color: #3d3a35;
  display: flex;
  flex-direction: column;
}
.container {
  max-width: 820px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
  flex: 1;
}
.page-head {
  margin: 40px 0 24px;
}
.page-head h2 {
  font-size: 28px;
  font-weight: 900;
  color: #2f2b26;
}
.page-head p {
  margin-top: 8px;
  font-size: 14px;
  color: #a49c91;
}

.apps-main {
  min-height: 200px;
}
.app-card {
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 20px;
  padding: 20px 24px;
  margin-bottom: 18px;
  transition: box-shadow 0.2s;
}
.app-card:hover {
  box-shadow: 0 12px 28px rgba(90, 80, 70, 0.08);
}
.app-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.app-pet {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}
.pet-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffe9de, #ffd6c2);
  display: grid;
  place-items: center;
  font-size: 24px;
  flex-shrink: 0;
}
.pet-name-wrap {
  min-width: 0;
}
.pet-name {
  display: block;
  font-size: 17px;
  font-weight: 800;
  color: #2f2b26;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pet-id {
  display: block;
  font-size: 12px;
  color: #b0a99e;
  margin-top: 2px;
}

/* 状态标签 */
.status-tag {
  font-size: 12px;
  font-weight: 700;
  padding: 5px 14px;
  border-radius: 999px;
  white-space: nowrap;
}
.st-pending { color: #b45309; background: #fff3d6; }
.st-pass { color: #16a34a; background: #e6f6ea; }
.st-out { color: #2563eb; background: #e6f0ff; }
.st-reject { color: #dc2626; background: #ffe9e9; }

.reason {
  margin: 14px 0;
  font-size: 14px;
  color: #6b645b;
  line-height: 1.7;
  background: #fdfcf9;
  border: 1px solid #f4eee5;
  border-radius: 12px;
  padding: 12px 14px;
}

.app-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #8a837a;
}
.meta-line {
  display: block;
}
.meta-label {
  color: #b0a99e;
  margin-right: 8px;
}

.app-foot {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ece5da;
  display: flex;
  justify-content: flex-end;
}
.btn-ghost {
  background: transparent !important;
  border: 1.5px solid #e8927c !important;
  color: #e8927c !important;
  border-radius: 999px !important;
  font-weight: 700;
}
.btn-ghost:hover {
  background: #e8927c !important;
  color: #fff !important;
}
.btn-withdraw {
  padding: 8px 20px;
  font-size: 13px;
}
.locked-tip {
  font-size: 13px;
  color: #b0a99e;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin: 24px 0 40px;
}
.pagination-wrap :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #e8927c;
}

/* 页脚 */
.site-footer {
  text-align: center;
  padding: 20px 0 40px;
  color: #b0a99e;
  font-size: 13px;
}
</style>
