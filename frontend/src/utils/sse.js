import { getToken } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import router from '@/router'

let eventSource = null
let connectTimer = null

const RECONNECT_DELAY = 15000

function sseUrl() {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const token = getToken()
  return `${base}/sse/subscribe?token=${encodeURIComponent(token)}`
}

function clearConnectTimer() {
  if (connectTimer) {
    clearTimeout(connectTimer)
    connectTimer = null
  }
}

function scheduleReconnect() {
  clearConnectTimer()
  // 页面不可见时不重连，回到可见状态后由 connectSse() 补建
  if (document.visibilityState === 'hidden') return
  connectTimer = setTimeout(() => {
    connectTimer = null
    if (getToken()) {
      start()
    }
  }, RECONNECT_DELAY)
}

function start() {
  // 先关闭可能残留的旧连接，杜绝多路 EventSource 并行堆叠
  disconnect()
  if (!getToken()) return
  if (typeof EventSource === 'undefined') return

  const source = new EventSource(sseUrl())
  eventSource = source

  source.addEventListener('kicked', (event) => {
    if (eventSource !== source) return
    // 先断开，避免弹窗期间被旧 token 的连接持续占用
    disconnect()
    ElMessageBox.alert(event.data || '您的账户已在其他地方登录，您已被强制下线！', '系统提示', {
      type: 'warning',
      confirmButtonText: '确定',
      closeOnClickModal: false,
      showClose: false,
    }).then(() => {
      useUserStore().logOut()
        .then(() => router.push('/login'))
        .catch(() => router.push('/login'))
    }).catch(() => {})
  })

  // 统一接管错误重连：主动关闭，阻止浏览器默认约 3s 一次的疯狂自连，
  // 改为受控退避（15s 后单次重试），避免后端短暂重启/断网时连接堆积。
  source.onerror = () => {
    if (eventSource !== source) return
    disconnect()
    scheduleReconnect()
  }
}

/**
 * 建立 / 保持 SSE 长连接（幂等）。
 * 在任意路由守卫/页面挂载处调用即可，不会因为频繁切换页面而重复建连。
 */
export function connectSse() {
  if (!getToken()) return
  if (typeof EventSource === 'undefined') return
  const es = eventSource
  // 复用仍在工作的连接；CONNECTING 表示浏览器已在尝试，也无需新建
  if (es && es.readyState !== EventSource.CLOSED) return
  clearConnectTimer()
  start()
}

export function disconnect() {
  clearConnectTimer()
  if (eventSource) {
    eventSource.onopen = null
    eventSource.onerror = null
    eventSource.close()
    eventSource = null
  }
}
