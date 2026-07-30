<template>
  <div class="apply-layout">
    <div class="apply-card">
      <h2>申请领养 — {{ pet?.name || '加载中...' }}</h2>

      <el-descriptions v-if="pet" :column="2" border style="margin-bottom: 24px">
        <el-descriptions-item label="宠物名称">{{ pet.name }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ pet.breed || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ pet.gender === '1' ? '公' : '母' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ pet.age ?? '?' }}个月</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="领养理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请描述您的领养理由和饲养条件..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
          <el-button @click="$router.push('/adopt/public')">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicPet } from '@/api/public/pet'
import { addAdoption } from '@/api/adoption/adoption'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pet = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  reason: ''
})

const rules = {
  reason: [
    { required: true, message: '请填写领养理由', trigger: 'blur' },
    { max: 500, message: '不超过500字', trigger: 'blur' }
  ]
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
  })
})
</script>

<style scoped>
.apply-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  padding: 60px 20px;
}
.apply-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 640px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.apply-card h2 {
  margin: 0 0 24px;
  font-size: 24px;
  color: #333;
  text-align: center;
}
</style>
