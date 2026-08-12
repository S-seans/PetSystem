import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { connectSse, disconnect } from '@/utils/sse'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/adopt/public']

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
      } else {
        if (useUserStore().roles.length === 0 || usePermissionStore().addRoutes.length === 0) {
          isRelogin.show = true
          // 判断当前用户是否已拉取完user_info信息
          useUserStore().getInfo().then(() => {
            isRelogin.show = false
            // 非管理员禁止进入后台管理界面
            if (!isAdminUser() && isBackendPath(to.path)) {
              next('/adopt/public')
              NProgress.done()
              return
            }
            usePermissionStore().generateRoutes().then(accessRoutes => {
              // 根据roles权限生成可访问的路由表
              accessRoutes.forEach(route => {
                if (!isHttp(route.path)) {
                  router.addRoute(route) // 动态添加可访问路由表
                }
              })
              next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
            })
          }).catch(err => {
            useUserStore().logOut().then(() => {
              ElMessage.error(err)
              next({ path: '/' })
            })
          })
        } else {
          // 角色信息已加载，非管理员禁止进入后台管理界面
          if (!isAdminUser() && isBackendPath(to.path)) {
            next('/adopt/public')
            NProgress.done()
            return
          }
          next()
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
