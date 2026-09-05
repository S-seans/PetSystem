import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, removeToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { connectSse, disconnect } from '@/utils/sse'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/adopt/public', '/adopt/records', '/adopt/health']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

// 后台管理界面路由（仅管理员可进入）
const backendPrefixes = ['/system', '/monitor', '/tool', '/pet', '/adoption', '/health', '/success', '/user']
const isBackendPath = (path) => {
  if (path === '/' || path === '/index') return true
  return backendPrefixes.some(prefix => path === prefix || path.startsWith(prefix + '/'))
}

// 是否管理员
const isAdminUser = () => {
  const roles = useUserStore().roles || []
  return roles.some(r => r === 'admin' || r === 'administrator')
}

// ---------- 并发去重：用户信息 / 后台动态路由只准备一次 ----------
let infoTask = null      // 进行中的 getInfo
let adminTask = null     // 进行中的“角色 + 后台菜单”准备
let adminReady = false   // 后台动态路由是否已挂载完成

function ensureGetInfo() {
  if (useUserStore().roles.length > 0) return Promise.resolve()
  if (infoTask) return infoTask
  infoTask = useUserStore().getInfo().finally(() => { infoTask = null })
  return infoTask
}

// 后台页专用：保证已拿到角色信息并挂载完动态路由；非管理员直接放行拦截（返回 false）
function prepareAdminRoutes() {
  if (adminReady) return Promise.resolve(true)
  if (adminTask) return adminTask
  adminTask = ensureGetInfo()
    .then(() => {
      if (!isAdminUser()) return false
      if (usePermissionStore().addRoutes.length === 0) {
        return usePermissionStore().generateRoutes().then(accessRoutes => {
          accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
              router.addRoute(route) // 动态添加可访问路由表
            }
          })
          adminReady = true
          return true
        })
      }
      adminReady = true
      return true
    })
    .finally(() => { adminTask = null })
  return adminTask
}

// 登出失败（后端不可达）等场景：本地清空登录态，回到公开站点
function resetLocalSession() {
  removeToken()
  const userStore = useUserStore()
  userStore.token = ''
  userStore.roles = []
  userStore.permissions = []
  disconnect()
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      disconnect()
      next({ path: '/' })
      NProgress.done()
    } else {
      // 只要已登录就建立 SSE 实时连接（幂等），确保被挤占时能收到强制下线推送
      connectSse()
      if (isWhiteList(to.path)) {
        next()
      } else if (!isBackendPath(to.path)) {
        // 站内静态页（constantRoutes 已注册、非后台菜单）：登录即可进入，
        // 不再重复触发 getInfo + generateRoutes（后台菜单重建），避免切页时大量重复请求。
        // 顺带补齐用户信息用于顶部头像/昵称展示，失败不阻塞跳转。
        ensureGetInfo().catch(() => {})
        next()
      } else {
        // 后台管理页：需要角色信息与动态路由菜单
        const routesReady = usePermissionStore().addRoutes.length > 0
        if (routesReady) {
          if (!isAdminUser()) {
            next('/adopt/public')
            NProgress.done()
            return
          }
          next()
        } else {
          isRelogin.show = true
          prepareAdminRoutes().then(allowed => {
            isRelogin.show = false
            if (!allowed) {
              next('/adopt/public')
              NProgress.done()
              return
            }
            // hack方法 确保addRoutes已完成，重新发起本次导航
            next({ ...to, replace: true })
          }).catch(err => {
            isRelogin.show = false
            const msg = (err && err.message) ? err.message : '登录信息获取失败，请重新登录'
            resetLocalSession()
            ElMessage.error(msg)
            next({ path: '/adopt/public', replace: true })
            NProgress.done()
          })
        }
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else if (to.path === '/' || to.path === '/index') {
      // 未登录时访问首页重定向到公开宠物展示页
      next('/adopt/public')
      NProgress.done()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 其他受保护页面重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
