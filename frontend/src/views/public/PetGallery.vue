<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="header-content">
        <h1 class="site-title">🐾 爱心宠物领养平台</h1>
        <p class="site-desc">用爱终结流浪，给它们一个温暖的家</p>
        <div class="header-actions">
          <el-button type="primary" @click="goLogin" v-if="!isLoggedIn">登录 / 注册</el-button>
          <el-button @click="goAdmin" v-else>进入管理后台</el-button>
        </div>
      </div>
    </header>

    <main class="public-main">
      <div class="section-title">
        <h2>等待领养的宠物</h2>
        <p>共 {{ total }} 只小可爱等待温暖的家</p>
      </div>

      <el-row :gutter="24" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in pets" :key="pet.petId" style="margin-bottom: 24px">
          <el-card :body-style="{ padding: '0' }" shadow="hover" class="pet-card" @click="showDetail(pet)">
            <div class="pet-image-wrapper">
              <el-image
                :src="pet.imageUrl ? (isExternal(pet.imageUrl) ? pet.imageUrl : baseApi + pet.imageUrl) : ''"
                fit="cover"
                class="pet-image"
                lazy
                :preview-src-list="pet.imageUrl ? [isExternal(pet.imageUrl) ? pet.imageUrl : baseApi + pet.imageUrl] : []"
                preview-teleported
              >
                <template #placeholder>
                  <div class="image-placeholder">
                    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
                  </div>
                </template>
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="40"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <el-tag class="pet-gender" :type="pet.gender === '1' ? 'primary' : 'danger'" size="small">
                {{ pet.gender === '1' ? '♂ 公' : '♀ 母' }}
              </el-tag>
            </div>
            <div class="pet-info">
              <h3 class="pet-name">{{ pet.name }}</h3>
              <div class="pet-meta">
                <span>{{ pet.breed || '未知品种' }}</span>
                <span>·</span>
                <span>{{ pet.age ?? '?' }}个月</span>
              </div>
              <div class="pet-actions">
                <el-button type="primary" size="small" @click.stop="handleAdopt(pet)">申请领养</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && pets.length === 0" description="暂无待领养的宠物" />

      <div class="pagination-wrap" v-if="!loading && total > 0">
        <el-pagination
          layout="prev, pager, next"
          background
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </main>

    <el-dialog v-model="detailVisible" :title="currentPet?.name" width="600px" destroy-on-close>
      <div v-if="currentPet" class="detail-content">
        <div class="detail-image">
          <el-image
            :src="currentPet.imageUrl ? (isExternal(currentPet.imageUrl) ? currentPet.imageUrl : baseApi + currentPet.imageUrl) : ''"
            fit="cover"
            style="width: 100%; height: 300px; border-radius: 8px"
          >
            <template #error>
              <div class="image-placeholder" style="height: 300px">
                <el-icon :size="60"><PictureFilled /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <el-descriptions :column="2" border style="margin-top: 16px">
          <el-descriptions-item label="品种">{{ currentPet.breed || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentPet.gender === '1' ? '公' : '母' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentPet.age ?? '?' }}个月</el-descriptions-item>
          <el-descriptions-item label="体重">{{ currentPet.weight ?? '?' }}kg</el-descriptions-item>
          <el-descriptions-item label="救助日期">{{ currentPet.rescueDate || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success">{{ currentPet.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentPet.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleAdopt(currentPet)">申请领养</el-button>
      </template>
    </el-dialog>

    <footer class="public-footer">
      <p>© 2025 爱心宠物领养平台 | 用爱终结流浪</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import { isExternal } from '@/utils/validate'
import { PictureFilled, Loading } from '@element-plus/icons-vue'
import { listPublicPets } from '@/api/public/pet'

const router = useRouter()
const baseApi = import.meta.env.VITE_APP_BASE_API

const pets = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(true)
const detailVisible = ref(false)
const currentPet = ref(null)

const isLoggedIn = computed(() => !!getToken())

function showDetail(pet) {
  currentPet.value = pet
  detailVisible.value = true
}

function handleAdopt(pet) {
  if (!getToken()) {
    router.push(`/login?redirect=/adopt/public`)
    return
  }
  router.push(`/adopt/apply/${pet.petId}`)
}

function goLogin() {
  router.push('/login?redirect=/adopt/public')
}

function goAdmin() {
  router.push('/index')
}

function getList() {
  loading.value = true
  listPublicPets({ pageNum: pageNum.value, pageSize }).then(res => {
    pets.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handlePageChange(page) {
  pageNum.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
  getList()
}

onMounted(getList)
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.public-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  text-align: center;
  padding: 60px 20px;
}
.header-content {
  max-width: 800px;
  margin: 0 auto;
}
.site-title {
  font-size: 36px;
  margin: 0 0 12px;
}
.site-desc {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 24px;
}
.header-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.public-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}
.section-title {
  text-align: center;
  margin-bottom: 36px;
}
.section-title h2 {
  font-size: 28px;
  margin: 0 0 8px;
  color: #333;
}
.section-title p {
  color: #999;
  margin: 0;
}
.pet-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  overflow: hidden;
}
.pet-card:hover {
  transform: translateY(-4px);
}
.pet-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #ebeef5;
}
.pet-image {
  width: 100%;
  height: 100%;
}
.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #c0c4cc;
  background: #f5f7fa;
}
.pet-gender {
  position: absolute;
  top: 8px;
  right: 8px;
}
.pet-info {
  padding: 16px;
}
.pet-name {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}
.pet-meta {
  color: #999;
  font-size: 13px;
  margin-bottom: 12px;
}
.pet-actions {
  text-align: right;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.detail-content {
  padding: 0;
}
.public-footer {
  text-align: center;
  padding: 32px;
  color: #999;
  font-size: 13px;
}
</style>
