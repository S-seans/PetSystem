<template>
  <div class="health-layout">
    <PublicHeader />

    <div class="container">
      <div class="page-head">
        <h2>💚 宠物健康记录</h2>
        <p>每一只毛孩子的健康，我们都用心记录 —— 共 {{ total }} 条健康记录</p>
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

      <main v-loading="loading" class="health-main">
        <div v-for="item in list" :key="item.healthId" class="health-card">
          <div class="health-top">
            <div class="pet-avatar">{{ petEmoji(item.petName) }}</div>
            <div class="health-info">
              <div class="pet-name">{{ item.petName || '未知宠物' }}</div>
              <div class="health-date">📅 {{ formatDate(item.recordDate) }}</div>
            </div>
            <span class="status-badge" :class="statusClass(item.healthStatus)">
              {{ healthStatusText(item.healthStatus) }}
            </span>
          </div>

          <div class="health-meta">
            <span class="meta-pill">💉 疫苗：{{ item.vaccineName || '无记录' }}</span>
            <span class="meta-pill">✂️ 绝育：{{ item.isSterilized === 1 ? '是' : '否' }}</span>
          </div>

          <p class="desc">{{ item.description || '暂无详细描述' }}</p>
        </div>

        <el-empty v-if="!loading && list.length === 0" description="暂无健康记录" />
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
import { listPublicHealth } from '@/api/public/health'
import { healthStatusText, HEALTH_STATUS } from '@/utils/business'
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

function statusClass(status) {
  if (status === HEALTH_STATUS.HEALTHY || status === HEALTH_STATUS.GOOD) return 'st-good'
  if (status === HEALTH_STATUS.SICK) return 'st-sick'
  if (status === HEALTH_STATUS.DEAD) return 'st-dead'
  if (status === HEALTH_STATUS.RECOVERING) return 'st-recovering'
  return 'st-default'
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
  listPublicHealth({ ...queryParams }).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).catch(() => {
    proxy.$modal.msgError('获取健康记录失败')
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
.health-layout {
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

.health-main {
  min-height: 200px;
}
.health-card {
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 20px;
  padding: 22px 24px;
  margin-bottom: 18px;
  transition: box-shadow 0.2s;
}
.health-card:hover {
  box-shadow: 0 12px 28px rgba(90, 80, 70, 0.08);
}
.health-top {
  display: flex;
  align-items: center;
  gap: 16px;
}
.pet-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e6f4e9, #c8e6d0);
  display: grid;
  place-items: center;
  font-size: 30px;
  flex-shrink: 0;
}
.health-info {
  flex: 1;
  min-width: 0;
}
.pet-name {
  font-size: 20px;
  font-weight: 900;
  color: #2f2b26;
}
.health-date {
  margin-top: 6px;
  font-size: 13px;
  color: #a49c91;
}

/* 健康状态徽章 */
.status-badge {
  flex-shrink: 0;
  font-size: 12.5px;
  font-weight: 800;
  padding: 6px 14px;
  border-radius: 999px;
  white-space: nowrap;
}
.st-good { color: #16a34a; background: #e6f6ea; border: 1px solid #c9ecd2; }
.st-sick { color: #dc2626; background: #ffe9e9; border: 1px solid #f7caca; }
.st-recovering { color: #b45309; background: #fff3d6; border: 1px solid #f5dfa8; }
.st-dead { color: #6b7280; background: #f3f4f6; border: 1px solid #e0e2e7; }
.st-default { color: #6b645b; background: #faf7f2; border: 1px solid #ece5da; }

.health-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 16px;
}
.meta-pill {
  background: #faf7f2;
  border: 1px solid #ece5da;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12.5px;
  color: #6b645b;
  font-weight: 600;
}

.desc {
  margin: 14px 0 0;
  font-size: 14px;
  color: #6b645b;
  line-height: 1.8;
  background: #fdfcf9;
  border: 1px solid #f4eee5;
  border-radius: 14px;
  padding: 14px 16px;
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
