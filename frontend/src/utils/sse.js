import { getToken } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import router from '@/router'

let eventSource = null
let errorCount = 0
const MAX_ERROR_COUNT = 5

export function connectSse() {
  const token = getToken()
  if (!token) return
  // 幂等：已有可用连接则直接返回，避免每次导航重复建连
  if (eventSource && eventSource.readyState === EventSource.OPEN) return

  const baseUrl = import.meta.env.VITE_APP_BASE_API || ''
  const url = `${baseUrl}/sse/subscribe?token=${encodeURIComponent(token)}`

  eventSource = new EventSource(url)

  eventSource.addEventListener('kicked', (event) => {
    errorCount = 0
    // 先断开，防止弹窗期间浏览器自动重连（旧 token 已失效，会不断请求）
    disconnect()
    ElMessageBox.alert(event.data, '系统提示', {
      type: 'warning',
      confirmButtonText: '确定',
      closeOnClickModal: false,
      showClose: false,
    }).then(() => {
      useUserStore().logOut().then(() => {
        router.push('/login')
      })
    })
  })

  eventSource.onerror = () => {
    // 连续失败达到阈值（如 token 已失效/被挤占）则停止重连，避免无谓请求
    errorCount++
    if (errorCount >= MAX_ERROR_COUNT) {
      disconnect()
    }
  }
}

export function disconnect() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
    errorCount = 0
  }
}
