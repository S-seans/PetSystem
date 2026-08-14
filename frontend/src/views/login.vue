<template>
  <div class="login-page">
    <div class="bg-blob b1"></div>
    <div class="bg-blob b2"></div>

    <div class="login-card">
      <div class="brand">🐾 爱心<i>领养</i></div>
      <h2 class="title">欢迎回来</h2>
      <p class="subtitle">登录后即可申请领养心仪的毛孩子</p>

      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" label-position="top" class="login-form">
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="请输入账号"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item v-if="captchaEnabled" label="验证码" prop="code">
          <div class="code-row">
            <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            />
            <img :src="codeUrl" @click="getCode" class="code-img" title="点击刷新验证码" />
          </div>
        </el-form-item>

        <div class="remember-row">
          <el-checkbox v-model="loginForm.rememberMe" class="remember-check">记住密码</el-checkbox>
        </div>

        <el-form-item class="submit-item">
          <el-button
            :loading="loading"
            size="large"
            class="submit-btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
          <div class="register-link" v-if="register">
            <router-link class="link-type" :to="'/register'">没有账号？立即注册 →</router-link>
          </div>
          <div class="home-link">
            <el-button class="btn-ghost btn-home" @click="goHome">🏠 回到主界面</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <div class="el-login-footer">
      <span>© {{ year }} 爱心宠物领养平台 · 用爱终结流浪</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'

const title = import.meta.env.VITE_APP_TITLE
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const year = new Date().getFullYear()

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关（已临时关闭：调试阶段免验证码登录；恢复：改回 true 并恢复 getCode() 中的覆盖逻辑）
const captchaEnabled = ref(false)
// 注册开关
const register = ref(true)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function goHome() {
  router.push('/adopt/public')
}

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    // 临时关闭验证码：忽略后端返回的开关状态，始终不显示验证码输入框
    captchaEnabled.value = false
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  Cookies.remove("username")
  Cookies.remove("password")
  Cookies.remove("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style scoped>
.login-page {
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

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
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

.login-form :deep(.el-form-item__label) {
  color: #3d3a35;
  font-weight: 700;
  font-size: 14px;
  line-height: 1.4;
  padding-bottom: 8px;
}
.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #fdfcf9;
  box-shadow: 0 0 0 1px #e0d8cb inset;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e8927c inset, 0 0 0 3px rgba(232, 146, 124, 0.12) !important;
}
.login-form :deep(.el-input__inner) {
  font-size: 15px;
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

/* 记住密码 */
.remember-row {
  margin-bottom: 18px;
}
.remember-check :deep(.el-checkbox__label) {
  color: #8a837a;
  font-size: 13px;
  font-weight: 600;
}
.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #e8927c;
  border-color: #e8927c;
}
.remember-check :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #e8927c;
}

/* 提交 */
.submit-item {
  margin-top: 4px;
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
.register-link {
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

.el-login-footer {
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
