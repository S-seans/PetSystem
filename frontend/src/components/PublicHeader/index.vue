<template>
  <header class="site-header">
    <nav class="nav">
      <div class="brand" @click="goPublic">🐾 爱心<i>领养</i></div>
      <div class="nav-right">
        <!-- 未登录 -->
        <el-button v-if="!isLoggedIn" class="btn-ghost" @click="goLogin">登录 / 注册</el-button>

        <!-- 已登录 - 管理员 -->
        <el-button v-else-if="isAdmin" class="btn-admin" @click="goAdmin">进入管理后台</el-button>

        <!-- 已登录 - 普通用户 -->
        <el-dropdown v-else trigger="hover" popper-class="user-menu" @command="handleCommand">
          <div class="user-box">
            <el-avatar :size="34" :src="userStore.avatar || undefined" class="user-avatar">
              <span class="avatar-fallback">{{ nickName[0] || 'U' }}</span>
            </el-avatar>
            <span class="user-name">{{ nickName || userStore.name }}</span>
            <el-icon class="user-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="applications">
                <el-icon><Document /></el-icon>我的领养申请
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, User, Document, SwitchButton } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!getToken())
const isAdmin = computed(() =>
  (userStore.roles || []).some(r => r === 'admin' || r === 'administrator')
)
const nickName = computed(() => userStore.nickName || userStore.name || '用户')

function goPublic() {
  router.push('/adopt/public')
}

function goLogin() {
  router.push('/login?redirect=/adopt/public')
}

function goAdmin() {
  router.push('/index')
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/adopt/profile')
  } else if (command === 'applications') {
    router.push('/adopt/my-applications')
  } else if (command === 'logout') {
    handleLogout()
  }
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      router.push('/adopt/public')
    })
  }).catch(() => {})
}

onMounted(() => {
  // 公开页在白名单内，路由守卫不会拉取用户信息，登录后需自行拉取以填充头像/昵称/角色
  if (getToken() && userStore.roles.length === 0) {
    userStore.getInfo().catch(() => {})
  }
})
</script>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(250, 247, 242, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #efe9df;
}
.nav {
  max-width: 1160px;
  margin: 0 auto;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  font-weight: 800;
  font-size: 21px;
  color: #2f2b26;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.brand i {
  font-style: normal;
  color: #e8927c;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 按钮 */
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
.btn-admin {
  background: #2f2b26 !important;
  color: #fff !important;
  border: none !important;
  border-radius: 999px !important;
  font-weight: 700;
}
.btn-admin:hover {
  background: #e8927c !important;
  color: #fff !important;
}

/* 用户下拉 */
.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  border: 1.5px solid #ece5da;
  background: #fff;
  transition: border-color 0.2s;
}
.user-box:hover {
  border-color: #e8927c;
}
.user-avatar {
  background: linear-gradient(135deg, #ffd6c2, #e8927c) !important;
  color: #fff !important;
  font-weight: 800;
}
.avatar-fallback {
  font-size: 15px;
}
.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #3d3a35;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-arrow {
  font-size: 12px;
  color: #b0a99e;
}
</style>

<style>
/* 下拉菜单美化（非 scoped，作用于全局弹出层） */
.user-menu .el-dropdown-menu {
  border-radius: 14px;
  padding: 6px;
  box-shadow: 0 12px 28px rgba(90, 80, 70, 0.14);
  border: 1px solid #f0eae0;
}
.user-menu .el-dropdown-menu__item {
  border-radius: 9px;
  padding: 9px 14px;
  color: #3d3a35;
  font-weight: 600;
}
.user-menu .el-dropdown-menu__item .el-icon {
  margin-right: 8px;
  color: #b0a99e;
}
.user-menu .el-dropdown-menu__item:hover {
  background: #fff3ec;
  color: #e8927c;
}
</style>
