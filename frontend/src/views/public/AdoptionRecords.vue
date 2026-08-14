<template>
  <div class="records-layout">
    <PublicHeader />

    <div class="container">
      <div class="page-head">
        <h2>📖 领养记录</h2>
        <p>每一个被领养的小生命，都值得被记住 —— 共 {{ total }} 段温暖故事</p>
      </div>

      <!-- 搜索 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.petName"
          placeholder="按宠物名称搜索"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button class="btn-primary" @click="handleSearch">搜索</el-button>
      </div>

      <main v-loading="loading" class="records-main">
        <div v-for="item in list" :key="item.successId" class="record-card">
          <div class="record-top">
            <div class="pet-avatar">{{ petEmoji(item.petName) }}</div>
            <div class="record-info">
              <div class="pet-name">{{ item.petName || '未知宠物' }}</div>
              <div class="pet-sub">
                <span class="meta-pill">🙋 领养人：{{ item.userName || '未知' }}</span>
                <span class="meta-pill">📅 领养日期：{{ formatDate(item.adoptTime) }}</span>
              </div>
            </div>
            <span class="status-badge">💝 已领养</span>
          </div>

          <p v-if="item.remark" class="story">{{ item.remark }}</p>
          <p v-else class="story story-empty">愿这只小可爱在新家幸福一生 🐾</p>

          <div class="record-foot">
            <span v-if="item.followUpDate" class="follow-up">📌 回访日期：{{ formatDate(item.followUpDate) }}</span>
            <span class="record-time">记录于 {{ formatDate(item.createTime) }}</span>
          </div>
        </div>

        <el-empty v-if="!loading && list.length === 0" description="暂无领养记录，快去看看待领养的小可爱吧" />
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
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { listPublicSuccess } from '@/api/public/success'
import PublicHeader from '@/components/PublicHeader'

const { proxy } = getCurrentInstance()
const year = new Date().getFullYear()

const list = ref([])
const total = ref(0)
const loading = ref(true)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  petName: null
})

function petEmoji(petName) {
  const name = petName || ''
  if (/猫/.test(name)) return '🐱'
  if (/犬|狗/.test(name)) return '🐶'
  if (/兔/.test(name)) return '🐰'
  if (/鼠|仓鼠/.test(name)) return '🐹'
  return '🐾'
}

function formatDate(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  const pad = n => (n < 10 ? '0' + n : n)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function getList() {
  loading.value = true
  listPublicSuccess({ ...queryParams }).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).catch(() => {
    proxy.$modal.msgError('获取领养记录失败')
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  queryParams.pageNum = 1
  getList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
  getList()
}

onMounted(getList)
</script>

<style scoped>
.records-layout {
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

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.search-input {
  flex: 1;
  max-width: 320px;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 0 0 1px #e0d8cb inset;
}
.btn-primary {
  background: #e8927c !important;
  color: #fff !important;
  border: none !important;
  border-radius: 999px !important;
  font-weight: 700;
  padding: 0 26px;
}
.btn-primary:hover {
  background: #dd7f66 !important;
}

.records-main {
  min-height: 200px;
}
.record-card {
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 20px;
  padding: 22px 24px;
  margin-bottom: 18px;
  transition: box-shadow 0.2s;
}
.record-card:hover {
  box-shadow: 0 12px 28px rgba(90, 80, 70, 0.08);
}
.record-top {
  display: flex;
  align-items: center;
  gap: 16px;
}
.pet-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffe9de, #ffd6c2);
  display: grid;
  place-items: center;
  font-size: 30px;
  flex-shrink: 0;
}
.record-info {
  flex: 1;
  min-width: 0;
}
.pet-name {
  font-size: 20px;
  font-weight: 900;
  color: #2f2b26;
}
.pet-sub {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.meta-pill {
  background: #faf7f2;
  border: 1px solid #ece5da;
  border-radius: 999px;
  padding: 5px 13px;
  font-size: 12.5px;
  color: #6b645b;
  font-weight: 600;
}
.status-badge {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 800;
  color: #e8927c;
  background: #fff3ec;
  border: 1px solid #fadbcb;
  padding: 6px 14px;
  border-radius: 999px;
  white-space: nowrap;
}

.story {
  margin: 16px 0 0;
  font-size: 14px;
  color: #6b645b;
  line-height: 1.8;
  background: #fdfcf9;
  border: 1px solid #f4eee5;
  border-radius: 14px;
  padding: 14px 16px;
}
.story-empty {
  color: #b0a99e;
  font-style: italic;
}

.record-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
  font-size: 12.5px;
  color: #b0a99e;
}
.follow-up {
  color: #8a837a;
  font-weight: 600;
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
