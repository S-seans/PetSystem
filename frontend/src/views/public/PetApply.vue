<template>
  <div class="apply-layout">
    <PublicHeader />

    <div class="container">
      <div class="apply-card">
        <div class="card-head">
          <h2>申请领养</h2>
          <p>请如实填写领养理由，我们将尽快审核并与您联系。</p>
        </div>

        <!-- 宠物信息 -->
        <div class="pet-summary" v-if="pet">
          <div class="pet-avatar" :style="thumbStyle(pet)">
            <el-image
              :src="pet.imageUrl ? (isExternal(pet.imageUrl) ? pet.imageUrl : baseApi + pet.imageUrl) : ''"
              fit="cover"
            >
              <template #error>
                <div class="pet-avatar-fallback">{{ petEmoji(pet) }}</div>
              </template>
            </el-image>
          </div>
          <div class="pet-info">
            <div class="pet-head">
              <span class="pet-name">{{ pet.name }}</span>
              <span class="avail">● 可领养</span>
            </div>
            <div class="pet-meta">
              <span class="meta-pill">🐾 {{ pet.breed || '未知品种' }}</span>
              <span class="meta-pill">{{ pet.gender === '1' ? '♂ 公' : '♀ 母' }}</span>
              <span class="meta-pill">🎂 {{ formatPetAge(pet.age) }}</span>
              <span class="meta-pill">⚖ {{ pet.weight ?? '?' }}kg</span>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="pet-summary pet-summary-empty">
          <span>未找到该宠物信息</span>
        </div>
        <div v-else class="pet-summary pet-summary-loading">
          <span>正在加载宠物信息...</span>
        </div>

        <!-- 领养理由表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="apply-form">
          <el-form-item label="领养理由" prop="reason">
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="5"
              placeholder="请描述您的领养理由、饲养条件和居住环境，越详细越容易通过审核..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <div class="form-actions">
            <el-button class="btn-ghost btn-flex" @click="goBack">返回</el-button>
            <el-button class="btn-primary btn-flex" :loading="submitting" @click="submitForm">
              {{ submitting ? '提交中...' : '提交申请' }}
            </el-button>
          </div>
        </el-form>
      </div>
    </div>

    <footer class="site-footer">
      <p>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicPet } from '@/api/public/pet'
import { addAdoption } from '@/api/adoption/adoption'
import useUserStore from '@/store/modules/user'
import { isExternal } from '@/utils/validate'
import { formatPetAge } from '@/utils/petAge'
import PublicHeader from '@/components/PublicHeader'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const baseApi = import.meta.env.VITE_APP_BASE_API

const pet = ref(null)
const loading = ref(true)
const submitting = ref(false)
const formRef = ref(null)
const year = new Date().getFullYear()

const form = reactive({
  reason: ''
})

const rules = {
  reason: [
    { required: true, message: '请填写领养理由', trigger: 'blur' },
    { max: 500, message: '不超过500字', trigger: 'blur' }
  ]
}

function thumbStyle(p) {
  const palettes = [
    'linear-gradient(135deg, #ffe9de, #ffd6c2)',
    'linear-gradient(135deg, #e6f4e9, #c8e6d0)',
    'linear-gradient(135deg, #fff3d6, #ffe3a8)',
    'linear-gradient(135deg, #e8ecff, #c7d0ff)',
    'linear-gradient(135deg, #f3e8ff, #ddc8ff)'
  ]
  return { background: palettes[(p.petId || 0) % palettes.length] }
}

function petEmoji(p) {
  if (!p) return '🐾'
  if (/猫/.test(p.breed || '')) return p.gender === '1' ? '🐱' : '🐈'
  if (/犬|狗/.test(p.breed || '')) return p.gender === '1' ? '🐕' : '🐶'
  if (/兔/.test(p.breed || '')) return '🐰'
  if (/鼠|仓鼠/.test(p.breed || '')) return '🐹'
  return '🐾'
}

function goBack() {
  router.push('/adopt/public')
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitting.value = true
    addAdoption({
      petId: parseInt(route.params.petId),
      userId: userStore.id,
      reason: form.reason,
      status: 'pending'
    }).then(() => {
      proxy.$modal.msgSuccess('领养申请提交成功！请等待管理员审核。')
      router.push('/adopt/public')
    }).catch(() => {
      proxy.$modal.msgError('提交失败，请稍后重试')
    }).finally(() => {
      submitting.value = false
    })
  })
}

onMounted(() => {
  const petId = route.params.petId
  if (!petId) {
    router.push('/adopt/public')
    return
  }
  getPublicPet(petId).then(res => {
    pet.value = res.data
  }).catch(() => {
    proxy.$modal.msgError('无法获取宠物信息')
    router.push('/adopt/public')
  }).finally(() => {
    loading.value = false
  })
})
</script>

<style scoped>
.apply-layout {
  min-height: 100vh;
  background: #faf7f2;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Segoe UI", Roboto, sans-serif;
  color: #3d3a35;
  display: flex;
  flex-direction: column;
}
.container {
  max-width: 760px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
  flex: 1;
}

/* 卡片 */
.apply-card {
  background: #fff;
  border-radius: 26px;
  padding: 40px;
  margin: 40px 0;
  border: 1px solid #f0eae0;
  box-shadow: 0 18px 40px rgba(90, 80, 70, 0.08);
}
.card-head {
  text-align: center;
  margin-bottom: 28px;
}
.card-head h2 {
  font-size: 28px;
  font-weight: 900;
  color: #2f2b26;
}
.card-head p {
  margin-top: 10px;
  font-size: 14px;
  color: #a49c91;
}

/* 宠物摘要 */
.pet-summary {
  display: flex;
  gap: 20px;
  align-items: center;
  background: #faf7f2;
  border: 1px solid #f0eae0;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 28px;
}
.pet-summary-empty,
.pet-summary-loading {
  justify-content: center;
  color: #a49c91;
  font-size: 14px;
}
.pet-avatar {
  width: 110px;
  height: 110px;
  border-radius: 18px;
  overflow: hidden;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  position: relative;
}
.pet-avatar :deep(.el-image) {
  width: 100%;
  height: 100%;
}
.pet-avatar-fallback {
  font-size: 48px;
}
.pet-info {
  flex: 1;
  min-width: 0;
}
.pet-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.pet-name {
  font-size: 22px;
  font-weight: 900;
  color: #2f2b26;
}
.avail {
  font-size: 12px;
  font-weight: 700;
  color: #16a34a;
  background: #e6f6ea;
  padding: 4px 12px;
  border-radius: 999px;
  white-space: nowrap;
}
.pet-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.meta-pill {
  background: #fff;
  border: 1px solid #ece5da;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  color: #6b645b;
  font-weight: 600;
}

/* 表单 */
.apply-form {
  margin-top: 4px;
}
.apply-form :deep(.el-form-item__label) {
  color: #3d3a35;
  font-weight: 700;
  font-size: 14px;
  line-height: 1.4;
  padding-bottom: 8px;
}
.apply-form :deep(.el-textarea__inner) {
  border-radius: 14px;
  border-color: #e0d8cb;
  background: #fdfcf9;
  font-family: inherit;
  padding: 12px 14px;
  transition: border-color 0.2s;
}
.apply-form :deep(.el-textarea__inner:focus) {
  border-color: #e8927c;
  box-shadow: 0 0 0 2px rgba(232, 146, 124, 0.15);
}
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
.btn-flex {
  flex: 1;
  padding: 12px 0;
  font-size: 14px;
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
.btn-primary.is-loading {
  opacity: 0.85;
}

/* 页脚 */
.site-footer {
  text-align: center;
  padding: 20px 0 40px;
  color: #b0a99e;
  font-size: 13px;
}

@media (max-width: 560px) {
  .apply-card {
    padding: 24px 20px;
  }
  .pet-summary {
    flex-direction: column;
    text-align: center;
  }
  .pet-meta {
    justify-content: center;
  }
  .pet-head {
    justify-content: center;
  }
}
</style>
