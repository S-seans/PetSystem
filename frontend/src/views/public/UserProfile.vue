<template>
  <div class="profile-layout">
    <PublicHeader />

    <div class="container">
      <div class="page-head">
        <h2>个人中心</h2>
        <p>管理你的个人资料、头像与登录密码</p>
      </div>

      <div class="profile-grid">
        <!-- 左侧信息卡 -->
        <aside class="info-card">
          <div class="avatar-wrap">
            <el-avatar :size="96" :src="userStore.avatar || undefined" class="avatar">
              <span class="avatar-fallback">{{ (userStore.nickName || userStore.name || 'U')[0] }}</span>
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatarFile"
              accept="image/*"
            >
              <el-button class="btn-ghost btn-sm">更换头像</el-button>
            </el-upload>
          </div>
          <ul class="info-list">
            <li>
              <span class="label">账号</span>
              <span class="value">{{ profile.userName || userStore.name || '-' }}</span>
            </li>
            <li>
              <span class="label">昵称</span>
              <span class="value">{{ profile.nickName || '-' }}</span>
            </li>
            <li>
              <span class="label">所属角色</span>
              <span class="value">{{ roleGroup || '-' }}</span>
            </li>
            <li>
              <span class="label">创建日期</span>
              <span class="value">{{ profile.createTime || '-' }}</span>
            </li>
          </ul>
        </aside>

        <!-- 右侧资料编辑 -->
        <main class="edit-card">
          <div class="section-title">基本资料</div>
          <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-position="top" class="warm-form">
            <div class="form-row">
              <el-form-item label="用户昵称" prop="nickName" class="form-col">
                <el-input v-model="profileForm.nickName" maxlength="30" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phonenumber" class="form-col">
                <el-input v-model="profileForm.phonenumber" maxlength="11" placeholder="请输入手机号码" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="邮箱" prop="email" class="form-col">
                <el-input v-model="profileForm.email" maxlength="50" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="性别" class="form-col">
                <el-radio-group v-model="profileForm.sex">
                  <el-radio value="0">男</el-radio>
                  <el-radio value="1">女</el-radio>
                  <el-radio value="2">保密</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <div class="form-actions">
              <el-button class="btn-primary btn-save" :loading="savingProfile" @click="submitProfile">保存修改</el-button>
            </div>
          </el-form>

          <el-divider />

          <div class="section-title">修改密码</div>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top" class="warm-form">
            <div class="form-row">
              <el-form-item label="旧密码" prop="oldPassword" class="form-col">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword" class="form-col">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位，不含特殊符号" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword" class="form-col">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
            </div>
            <div class="form-actions">
              <el-button class="btn-primary btn-save" :loading="savingPwd" @click="submitPwd">修改密码</el-button>
            </div>
          </el-form>
        </main>
      </div>
    </div>

    <footer class="site-footer">
      <p>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile, updateUserProfile, updateUserPwd, uploadAvatar } from '@/api/system/user'
import useUserStore from '@/store/modules/user'
import PublicHeader from '@/components/PublicHeader'

const { proxy } = getCurrentInstance()
const router = useRouter()
const userStore = useUserStore()
const baseApi = import.meta.env.VITE_APP_BASE_API
const year = new Date().getFullYear()

const profile = ref({})
const roleGroup = ref('')

const profileRef = ref(null)
const pwdRef = ref(null)
const savingProfile = ref(false)
const savingPwd = ref(false)

const profileForm = reactive({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '2'
})

const profileRules = {
  nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phonenumber: [
    { required: true, message: '手机号码不能为空', trigger: 'blur' },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const equalToPassword = (rule, value, callback) => {
  if (pwdForm.newPassword !== value) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^[^<>"'|\\]+$/, message: '不能包含非法字符：< > " \' \\ |', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    { required: true, validator: equalToPassword, trigger: 'blur' }
  ]
}

function getUser() {
  getUserProfile().then(response => {
    profile.value = response.data || {}
    roleGroup.value = response.roleGroup || ''
    profileForm.nickName = profile.value.nickName || ''
    profileForm.phonenumber = profile.value.phonenumber || ''
    profileForm.email = profile.value.email || ''
    profileForm.sex = profile.value.sex || '2'
  }).catch(() => {
    proxy.$modal.msgError('获取个人信息失败')
  })
}

function submitProfile() {
  profileRef.value.validate(valid => {
    if (!valid) return
    savingProfile.value = true
    updateUserProfile({ ...profileForm }).then(() => {
      proxy.$modal.msgSuccess('资料修改成功')
      userStore.nickName = profileForm.nickName
      userStore.getInfo().catch(() => {})
      getUser()
    }).catch(() => {
      proxy.$modal.msgError('修改失败，请稍后重试')
    }).finally(() => {
      savingProfile.value = false
    })
  })
}

function submitPwd() {
  pwdRef.value.validate(valid => {
    if (!valid) return
    savingPwd.value = true
    updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword).then(() => {
      proxy.$modal.msgSuccess('密码修改成功，请重新登录')
      userStore.logOut().then(() => {
        router.push('/login')
      })
    }).catch(() => {
      proxy.$modal.msgError('修改失败，请检查旧密码是否正确')
    }).finally(() => {
      savingPwd.value = false
    })
  })
}

function beforeAvatarUpload(file) {
  if (file.type.indexOf('image/') === -1) {
    proxy.$modal.msgError('文件格式错误，请上传图片类型，如：JPG、PNG')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    proxy.$modal.msgError('图片大小不能超过 5MB')
    return false
  }
  return true
}

function uploadAvatarFile(options) {
  const formData = new FormData()
  formData.append('avatarfile', options.file)
  uploadAvatar(formData).then(response => {
    userStore.avatar = baseApi + response.imgUrl
    proxy.$modal.msgSuccess('头像修改成功')
  }).catch(() => {
    proxy.$modal.msgError('头像上传失败，请稍后重试')
  })
}

onMounted(getUser)
</script>

<style scoped>
.profile-layout {
  min-height: 100vh;
  background: #faf7f2;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Segoe UI", Roboto, sans-serif;
  color: #3d3a35;
  display: flex;
  flex-direction: column;
}
.container {
  max-width: 960px;
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

.profile-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  align-items: start;
  margin-bottom: 40px;
}
@media (max-width: 800px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

/* 左侧信息卡 */
.info-card {
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 22px;
  padding: 28px 24px;
  text-align: center;
}
.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}
.avatar {
  background: linear-gradient(135deg, #ffd6c2, #e8927c) !important;
  color: #fff !important;
  font-weight: 800;
}
.avatar-fallback {
  font-size: 34px;
}
.avatar-uploader {
  display: block;
}
.info-list {
  list-style: none;
  padding: 0;
  margin: 24px 0 0;
  text-align: left;
  border-top: 1px dashed #ece5da;
  padding-top: 16px;
}
.info-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  font-size: 14px;
}
.info-list .label {
  color: #a49c91;
  flex-shrink: 0;
}
.info-list .value {
  font-weight: 700;
  color: #3d3a35;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧编辑卡 */
.edit-card {
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 22px;
  padding: 28px;
}
.section-title {
  font-size: 17px;
  font-weight: 800;
  color: #2f2b26;
  margin-bottom: 18px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}
@media (max-width: 560px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
.form-col {
  margin-bottom: 4px;
}
.warm-form :deep(.el-form-item__label) {
  color: #3d3a35;
  font-weight: 700;
  font-size: 14px;
  line-height: 1.4;
  padding-bottom: 8px;
}
.warm-form :deep(.el-input__wrapper),
.warm-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #fdfcf9;
}
.warm-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e0d8cb inset;
}
.warm-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e8927c inset, 0 0 0 3px rgba(232, 146, 124, 0.12) !important;
}
.form-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
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
.btn-save {
  padding: 10px 28px;
  font-size: 14px;
}
.btn-ghost {
  background: transparent !important;
  border: 1.5px solid #e0d8cb !important;
  color: #6b645b !important;
  border-radius: 999px !important;
  font-weight: 700;
}
.btn-ghost:hover {
  border-color: #e8927c !important;
  color: #e8927c !important;
}
.btn-sm {
  padding: 8px 18px;
  font-size: 13px;
}
.warm-form :deep(.el-radio__label) {
  color: #3d3a35;
  font-weight: 600;
}
.warm-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #e8927c;
  border-color: #e8927c;
}
.warm-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #e8927c;
}
.warm-form :deep(.el-divider) {
  margin: 28px 0;
  border-color: #f0eae0;
}

/* 页脚 */
.site-footer {
  text-align: center;
  padding: 20px 0 40px;
  color: #b0a99e;
  font-size: 13px;
}
</style>
