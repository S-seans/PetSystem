<template>
  <div class="register-page">
    <div class="bg-blob b1"></div>
    <div class="bg-blob b2"></div>

    <div class="register-card">
      <div class="brand">🐾 爱心<i>领养</i></div>
      <h2 class="title">创建账号</h2>
      <p class="subtitle">加入我们，给毛孩子一个温暖的家</p>

      <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-position="top" class="register-form">
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入您的昵称"
          />
        </el-form-item>

        <el-form-item label="手机号码" prop="phonenumber">
          <el-input
            v-model="registerForm.phonenumber"
            type="text"
            size="large"
            maxlength="11"
            auto-complete="off"
            placeholder="请输入手机号码"
          />
        </el-form-item>

        <el-form-item label="邮箱（选填）" prop="email">
          <el-input
            v-model="registerForm.email"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入邮箱（选填）"
          />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="registerForm.sex" class="sex-group">
            <el-radio value="0">男</el-radio>
            <el-radio value="1">女</el-radio>
            <el-radio value="2">保密</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="请输入密码（5-20位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="请再次输入密码"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item v-if="captchaEnabled" label="验证码" prop="code">
          <div class="code-row">
            <el-input
              v-model="registerForm.code"
              size="large"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleRegister"
            />
            <img :src="codeUrl" @click="getCode" class="code-img" title="点击刷新验证码" />
          </div>
        </el-form-item>

        <el-form-item class="submit-item">
          <el-button
            :loading="loading"
            size="large"
            class="submit-btn"
            @click.prevent="handleRegister"
          >
            <span v-if="!loading">注 册</span>
            <span v-else>注 册 中...</span>
          </el-button>
          <div class="login-link">
            <router-link class="link-type" :to="'/login'">使用已有账户登录 →</router-link>
          </div>
          <div class="home-link">
            <el-button class="btn-ghost btn-home" @click="goHome">🏠 回到主界面</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <div class="el-register-footer">
      <span>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import { validEmail } from "@/utils/validate"

const title = import.meta.env.VITE_APP_TITLE
const router = useRouter()
const { proxy } = getCurrentInstance()
const year = new Date().getFullYear()

const registerForm = ref({
  nickname: "",
  phonenumber: "",
  email: "",
  sex: "2",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value || value.trim() === "") {
    callback()
  } else if (!validEmail(value)) {
    callback(new Error("请输入正确的邮箱地址"))
  } else {
    callback()
  }
}

const registerRules = {
  nickname: [
    { required: true, trigger: "blur", message: "请输入您的昵称" },
    { min: 2, max: 20, message: "昵称长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  phonenumber: [
    { required: true, trigger: "blur", message: "请输入手机号码" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
  ],
  email: [
    { validator: validateEmail, trigger: "blur" }
  ],
  sex: [
    { required: true, trigger: "change", message: "请选择性别" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)

function goHome() {
  router.push('/adopt/public')
}

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        const account = res.data
        ElMessageBox.alert("<font color='red'>注册成功！您的账号是：" + account + "</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #faf7f2;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Segoe UI", Roboto, sans-serif;
  color: #3d3a35;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}
.bg-blob {
  position: fixed;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  z-index: 0;
}
.b1 { width: 360px; height: 360px; background: #ffd6c2; top: -100px; left: -100px; }
.b2 { width: 320px; height: 320px; background: #e6f4e9; bottom: -100px; right: -100px; }

.register-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  background: #fff;
  border: 1px solid #f0eae0;
  border-radius: 26px;
  padding: 36px 38px 30px;
  box-shadow: 0 18px 44px rgba(90, 80, 70, 0.1);
}
.brand {
  font-weight: 800;
  font-size: 22px;
  color: #2f2b26;
  letter-spacing: 1px;
  text-align: center;
}
.brand i {
  font-style: normal;
  color: #e8927c;
}
.title {
  margin: 18px 0 6px;
  text-align: center;
  font-size: 26px;
  font-weight: 900;
  color: #2f2b26;
}
.subtitle {
  margin: 0 0 24px;
  text-align: center;
  font-size: 13px;
  color: #a49c91;
}

.register-form :deep(.el-form-item__label) {
  color: #3d3a35;
  font-weight: 700;
  font-size: 14px;
  line-height: 1.4;
  padding-bottom: 8px;
}
.register-form :deep(.el-input__wrapper),
.register-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #fdfcf9;
}
.register-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e0d8cb inset;
}
.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e8927c inset, 0 0 0 3px rgba(232, 146, 124, 0.12) !important;
}
.register-form :deep(.el-input__inner) {
  font-size: 15px;
}

/* 性别选择 */
.sex-group {
  width: 100%;
  display: flex;
  gap: 8px;
}
.sex-group :deep(.el-radio) {
  flex: 1;
  justify-content: center;
  margin-right: 0;
  background: #fdfcf9;
  border: 1.5px solid #e0d8cb;
  border-radius: 12px;
  padding: 9px 0;
  transition: all 0.2s;
}
.sex-group :deep(.el-radio.is-checked) {
  border-color: #e8927c;
  background: #fff3ec;
}
.sex-group :deep(.el-radio__label) {
  font-weight: 700;
  color: #6b645b;
}
.sex-group :deep(.el-radio.is-checked .el-radio__label) {
  color: #e8927c;
}
.sex-group :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #e8927c;
  border-color: #e8927c;
}
.sex-group :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #e8927c;
}

/* 验证码 */
.code-row {
  width: 100%;
  display: flex;
  gap: 12px;
}
.code-row :deep(.el-input) {
  flex: 1;
}
.code-img {
  width: 110px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e0d8cb;
  cursor: pointer;
  object-fit: cover;
}

/* 提交 */
.submit-item {
  margin-top: 24px;
}
.submit-btn {
  width: 100%;
  background: #e8927c !important;
  color: #fff !important;
  border: none !important;
  border-radius: 999px !important;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 4px;
  box-shadow: 0 8px 20px rgba(232, 146, 124, 0.35);
}
.submit-btn:hover {
  background: #dd7f66 !important;
  color: #fff !important;
}
.login-link {
  width: 100%;
  margin-top: 14px;
  text-align: right;
}
.link-type {
  color: #a49c91;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}
.link-type:hover {
  color: #e8927c;
}
.home-link {
  width: 100%;
  margin-top: 12px;
  text-align: center;
}
.btn-home {
  width: 100%;
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

.el-register-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: #b0a99e;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
