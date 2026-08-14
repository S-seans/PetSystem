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

    <!-- 主导航栏：主页 / 领养记录 / 我的领养申请 / 宠物健康记录 -->
    <nav class="main-nav">
      <button
        v-for="item in navItems"
        :key="item.path"
        class="nav-btn"
        :class="{ active: isActive(item.path) }"
        @click="go(item.path)"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, User, Document, SwitchButton } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/**
 * 主导航按钮（左右次序与页面内容结构一致）：
 * 主页 → 领养记录 → 我的领养申请 → 宠物健康记录
 */
const navItems = [
  { path: '/adopt/public', label: '主页', icon: '🏠' },
  { path: '/adopt/records', label: '领养记录', icon: '📖' },
  { path: '/adopt/my-applications', label: '我的领养申请', icon: '📝' },
  { path: '/adopt/health', label: '宠物健康记录', icon: '💚' }
]

const isLoggedIn = computed(() => !!getToken())
const isAdmin = computed(() =>
  (userStore.roles || []).some(r => r === 'admin' || r === 'administrator')
)
const nickName = computed(() => userStore.nickName || userStore.name || '用户')

/** 当前路由是否命中某导航项（申请页等详情页归属"主页"高亮） */
function isActive(path) {
  if (path === '/adopt/public') {
    return route.path === path || route.path.startsWith('/adopt/apply')
  }
  return route.path === path
}

function go(path) {
  router.push(path)
}

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
  padding: 14px 24px 10px;
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

/* 主导航栏 */
.main-nav {
  max-width: 1160px;
  margin: 0 auto;
  padding: 2px 24px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border: 1.5px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: #6b645b;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}
.nav-btn .nav-icon {
  font-size: 15px;
}
.nav-btn:hover {
  background: #fff;
  border-color: #e8927c;
  color: #e8927c;
}
.nav-btn.active {
  background: #2f2b26;
  border-color: #2f2b26;
  color: #fff;
  box-shadow: 0 6px 14px rgba(47, 43, 38, 0.18);
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

/* 移动端适配 */
@media (max-width: 640px) {
  .main-nav {
    justify-content: center;
  }
  .nav-btn {
    padding: 8px 14px;
    font-size: 13px;
  }
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
