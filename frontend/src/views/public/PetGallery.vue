<template>
  <div class="public-layout">
    <!-- 顶部导航 -->
    <PublicHeader />

    <div class="container">
      <!-- Hero 区 -->
      <section class="hero">
        <div class="hero-text">
          <h1>遇见你，<br>是它们<em>一生</em>的幸运</h1>
          <p>这里有被救助的毛孩子们，正翘首以盼一个温暖的家。领养代替购买，让爱不再流浪。</p>
          <div class="hero-btns">
            <el-button class="btn-primary btn-lg" @click="scrollToPets">浏览待领养 →</el-button>
            <el-button class="btn-outline btn-lg" @click="goLogin" v-if="!isLoggedIn">登录后申请领养</el-button>
          </div>
          <div class="hero-note">🏠 实名认证 · 定期回访 · 健康保障</div>
        </div>
        <div class="hero-visual">
          <div class="circle cv1">🐱</div>
          <div class="circle cv2">🐰</div>
          <div class="circle cv3">🐶</div>
          <div class="float-note">❤ 用心呵护每一个生命</div>
        </div>
      </section>

      <!-- 标题 + 分类 -->
      <div class="section-head">
        <h2>等待领养的<span>毛孩子</span></h2>
        <a href="javascript:void(0)" class="section-link">共 {{ displayTotal }} 只小可爱</a>
      </div>

      <div class="cats">
        <button
          v-for="cat in categories"
          :key="cat.key"
          class="cat"
          :class="{ active: activeCat === cat.key }"
          @click="switchCategory(cat.key)"
        >{{ cat.label }}</button>
      </div>

      <!-- 卡片网格 -->
      <main class="grid" v-loading="loading">
        <article
          v-for="pet in displayPets"
          :key="pet.petId"
          class="card"
          @click="showDetail(pet)"
        >
          <div class="thumb" :style="thumbStyle(pet)">
            <el-image
              :src="pet.imageUrl ? (isExternal(pet.imageUrl) ? pet.imageUrl : baseApi + pet.imageUrl) : ''"
              fit="cover"
              class="pet-img"
              lazy
            >
              <template #error>
                <div class="thumb-fallback">{{ petEmoji(pet) }}</div>
              </template>
            </el-image>
            <span class="gender-badge">{{ pet.gender === '1' ? '♂' : '♀' }}</span>
          </div>
          <div class="body">
            <div class="row">
              <span class="name">{{ pet.name }}</span>
              <span class="avail">● 可领养</span>
            </div>
            <div class="meta">
              <span>{{ pet.breed || '未知品种' }}</span>·
              <span>{{ formatPetAge(pet.age) }}</span>·
              <span>{{ pet.weight ?? '?' }}kg</span>
            </div>
            <p class="desc">{{ pet.description || '暂无描述' }}</p>
            <div class="cta">
              <el-button class="btn-primary btn-block" @click.stop="handleAdopt(pet)">申请领养</el-button>
            </div>
          </div>
        </article>
      </main>

      <el-empty
        v-if="!loading && displayPets.length === 0"
        :description="activeCat === 'all' ? '暂无待领养的宠物' : '该分类下暂无宠物'"
      />

      <!-- 分页 -->
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
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentPet?.name"
      width="620px"
      destroy-on-close
      class="pet-dialog"
      align-center
    >
      <div v-if="currentPet" class="detail-content">
        <div class="detail-img">
          <el-image
            :src="currentPet.imageUrl ? (isExternal(currentPet.imageUrl) ? currentPet.imageUrl : baseApi + currentPet.imageUrl) : ''"
            fit="contain"
          >
            <template #error>
              <div class="detail-img-fallback">{{ petEmoji(currentPet) }}</div>
            </template>
          </el-image>
        </div>
        <div class="detail-info">
          <div class="detail-head">
            <h3>{{ currentPet.name }}</h3>
            <span class="avail">● {{ currentPet.status || '可领养' }}</span>
          </div>
          <div class="detail-meta">
            <div class="meta-item">
              <span class="meta-label">品种</span>
              <span class="meta-value">{{ currentPet.breed || '未知' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">性别</span>
              <span class="meta-value">{{ currentPet.gender === '1' ? '公' : '母' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">年龄</span>
              <span class="meta-value">{{ formatPetAge(currentPet.age) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">体重</span>
              <span class="meta-value">{{ currentPet.weight ?? '?' }}kg</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">救助日期</span>
              <span class="meta-value">{{ currentPet.rescueDate || '未知' }}</span>
            </div>
          </div>
          <p class="detail-desc">{{ currentPet.description || '暂无描述' }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-outline" @click="detailVisible = false">关闭</el-button>
          <el-button class="btn-primary" @click="handleAdopt(currentPet)">申请领养</el-button>
        </div>
      </template>
    </el-dialog>

    <footer class="site-footer">
      <p>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import { isExternal } from '@/utils/validate'
import { listPublicPets } from '@/api/public/pet'
import { CAT_BREEDS, DOG_BREEDS, RABBIT_BREEDS, matchBreed } from '@/utils/petBreeds'
import { formatPetAge } from '@/utils/petAge'
import PublicHeader from '@/components/PublicHeader'

const router = useRouter()
const baseApi = import.meta.env.VITE_APP_BASE_API

const pets = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(true)
const detailVisible = ref(false)
const currentPet = ref(null)
const activeCat = ref('all')

const isCat = p => matchBreed(p.breed, CAT_BREEDS) || /猫/.test(p.breed || '')
const isDog = p => matchBreed(p.breed, DOG_BREEDS) || /犬|狗/.test(p.breed || '')
const isRabbit = p => matchBreed(p.breed, RABBIT_BREEDS) || /兔/.test(p.breed || '')

const categories = [
  { label: '全部', key: 'all', match: () => true },
  { label: '🐱 猫咪', key: 'cat', match: isCat },
  { label: '🐶 狗狗', key: 'dog', match: isDog },
  { label: '🐰 兔兔', key: 'rabbit', match: isRabbit },
  { label: '🐹 其他', key: 'other', match: p => !isCat(p) && !isDog(p) && !isRabbit(p) }
]

const year = new Date().getFullYear()
const isLoggedIn = computed(() => !!getToken())

const displayPets = computed(() =>
  activeCat.value === 'all' ? pets.value : pets.value.filter(categories.find(c => c.key === activeCat.value).match)
)
const displayTotal = computed(() => displayPets.value.length)

function thumbStyle(pet) {
  const palettes = [
    'linear-gradient(135deg, #ffe9de, #ffd6c2)',
    'linear-gradient(135deg, #e6f4e9, #c8e6d0)',
    'linear-gradient(135deg, #fff3d6, #ffe3a8)',
    'linear-gradient(135deg, #e8ecff, #c7d0ff)',
    'linear-gradient(135deg, #f3e8ff, #ddc8ff)'
  ]
  const idx = (pet.petId || 0) % palettes.length
  return { background: palettes[idx] }
}

function petEmoji(pet) {
  if (!pet) return '🐾'
  if (matchBreed(pet.breed, CAT_BREEDS) || /猫/.test(pet.breed || '')) return pet.gender === '1' ? '🐱' : '🐈'
  if (matchBreed(pet.breed, DOG_BREEDS) || /犬|狗/.test(pet.breed || '')) return pet.gender === '1' ? '🐕' : '🐶'
  if (matchBreed(pet.breed, RABBIT_BREEDS) || /兔/.test(pet.breed || '')) return '🐰'
  if (/鼠|仓鼠/.test(pet.breed || '')) return '🐹'
  return '🐾'
}

function switchCategory(key) {
  activeCat.value = key
}

function showDetail(pet) {
  currentPet.value = pet
  detailVisible.value = true
}

function handleAdopt(pet) {
  if (!pet) return
  if (!getToken()) {
    router.push(`/login?redirect=/adopt/public`)
    return
  }
  router.push(`/adopt/apply/${pet.petId}`)
}

function goLogin() {
  router.push('/login?redirect=/adopt/public')
}

function scrollToPets() {
  const el = document.querySelector('.section-head')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
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
  background: #faf7f2;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Segoe UI", Roboto, sans-serif;
  color: #3d3a35;
}
.container {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 按钮通用 */
.btn-outline {
  background: transparent !important;
  border: 1.5px solid #e0d8cb !important;
  color: #6b645b !important;
  border-radius: 999px !important;
  font-weight: 700;
}
.btn-outline:hover {
  border-color: #e8927c !important;
  color: #e8927c !important;
}
.btn-primary {
  background: #e8927c !important;
  color: #fff !important;
  border: none !important;
  border-radius: 999px !important;
  font-weight: 700;
  box-shadow: 0 6px 16px rgba(232, 146, 124, 0.35);
}
.btn-primary:hover {
  background: #dd7f66 !important;
  color: #fff !important;
}
.btn-lg {
  padding: 13px 28px;
  font-size: 15px;
}
.btn-block {
  width: 100%;
  padding: 10px 0;
  font-size: 14px;
}

/* Hero */
.hero {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  gap: 40px;
  padding: 60px 0 28px;
}
.hero-text h1 {
  font-size: 46px;
  line-height: 1.2;
  font-weight: 900;
  color: #2f2b26;
  letter-spacing: 1px;
}
.hero-text h1 em {
  font-style: normal;
  color: #e8927c;
  position: relative;
}
.hero-text h1 em::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 2px;
  height: 10px;
  background: rgba(232, 146, 124, 0.25);
  z-index: -1;
  border-radius: 4px;
}
.hero-text p {
  margin: 18px 0 26px;
  color: #8a837a;
  font-size: 16px;
  line-height: 1.8;
}
.hero-btns {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}
.hero-note {
  margin-top: 18px;
  font-size: 13px;
  color: #b0a99e;
}
.hero-visual {
  position: relative;
  height: 320px;
}
.hero-visual .circle {
  position: absolute;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 54px;
  box-shadow: 0 18px 40px rgba(90, 80, 70, 0.12);
  transition: transform 0.3s;
}
.hero-visual .circle:hover {
  transform: scale(1.06) rotate(-4deg);
}
.cv1 { width: 170px; height: 170px; background: #ffe9de; top: 0; left: 0; }
.cv2 { width: 130px; height: 130px; background: #e6f4e9; top: 8px; right: 10px; font-size: 44px; }
.cv3 { width: 150px; height: 150px; background: #fff3d6; bottom: 0; left: 90px; }
.float-note {
  position: absolute;
  bottom: 6px;
  right: 0;
  background: #fff;
  border-radius: 16px;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 700;
  color: #6b645b;
  box-shadow: 0 8px 20px rgba(90, 80, 70, 0.1);
}

/* 标题 + 分类 */
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 36px 0 18px;
  gap: 16px;
  flex-wrap: wrap;
}
.section-head h2 {
  font-size: 26px;
  font-weight: 900;
  color: #2f2b26;
}
.section-head h2 span {
  color: #e8927c;
}
.section-link {
  font-size: 14px;
  color: #a49c91;
  font-weight: 600;
  text-decoration: none;
  cursor: default;
}
.cats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 26px;
}
.cat {
  padding: 10px 22px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  background: #fff;
  border: 1.5px solid #ece5da;
  color: #8a837a;
  transition: all 0.2s;
}
.cat.active {
  background: #2f2b26;
  color: #fff;
  border-color: #2f2b26;
}
.cat:hover:not(.active) {
  border-color: #e8927c;
  color: #e8927c;
}

/* 卡片网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  min-height: 200px;
}
@media (max-width: 1000px) { .grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 760px) { .grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .grid { grid-template-columns: 1fr; } }
.card {
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #f0eae0;
  transition: transform 0.25s, box-shadow 0.25s;
}
.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 36px rgba(90, 80, 70, 0.12);
}
.thumb {
  height: 200px;
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
}
.pet-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.thumb-fallback {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 80px;
}
.gender-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 800;
  color: #fff;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(6px);
}
.body {
  padding: 16px 18px 20px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 18px;
  font-weight: 800;
  color: #2f2b26;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.avail {
  font-size: 11px;
  font-weight: 700;
  color: #16a34a;
  background: #e6f6ea;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}
.meta {
  margin: 8px 0 6px;
  font-size: 13px;
  color: #a49c91;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: center;
}
.desc {
  font-size: 13px;
  color: #7a736a;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 44px;
}
.cta {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ece5da;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 34px;
}
.pagination-wrap :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 50%;
}
.pagination-wrap :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #e8927c;
}

/* 详情弹窗 */
.pet-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}
.pet-dialog :deep(.el-dialog__header) {
  background: #fff6f0;
  color: #2f2b26;
  font-weight: 800;
}
.detail-content {
  padding: 4px;
}
.detail-img {
  height: 260px;
  border-radius: 16px;
  overflow: hidden;
  background: #ffe9de;
  display: grid;
  place-items: center;
}
.detail-img :deep(.el-image) {
  width: 100%;
  height: 100%;
}
.detail-img-fallback {
  font-size: 96px;
}
.detail-info {
  padding: 20px 8px 4px;
}
.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.detail-head h3 {
  font-size: 22px;
  font-weight: 900;
  color: #2f2b26;
}
.detail-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 18px 0;
}
@media (max-width: 560px) {
  .detail-meta { grid-template-columns: repeat(2, 1fr); }
}
.meta-item {
  background: #faf7f2;
  border: 1px solid #f0eae0;
  border-radius: 14px;
  padding: 10px 14px;
}
.meta-label {
  display: block;
  font-size: 12px;
  color: #b0a99e;
  margin-bottom: 4px;
}
.meta-value {
  font-size: 15px;
  font-weight: 700;
  color: #3d3a35;
}
.detail-desc {
  font-size: 14px;
  color: #7a736a;
  line-height: 1.8;
  background: #fffbf7;
  border-radius: 14px;
  padding: 14px 16px;
  margin: 0;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.pet-dialog :deep(.el-button + .el-button) {
  margin-left: 0;
}

/* 页脚 */
.site-footer {
  text-align: center;
  padding: 30px 0 40px;
  color: #b0a99e;
  font-size: 13px;
}

/* 响应式 Hero */
@media (max-width: 820px) {
  .hero {
    grid-template-columns: 1fr;
    text-align: center;
    padding: 40px 0 20px;
  }
  .hero-btns {
    justify-content: center;
  }
  .hero-visual {
    display: none;
  }
}
</style>
