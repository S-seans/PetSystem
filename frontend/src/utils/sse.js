import { getToken } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import router from '@/router'

let eventSource = null

export function connectSse() {
  const token = getToken()
  if (!token) return

  const baseUrl = import.meta.env.VITE_APP_BASE_API || ''
  const url = `${baseUrl}/sse/subscribe?token=${encodeURIComponent(token)}`

  eventSource = new EventSource(url)

  eventSource.addEventListener('kicked', (event) => {
    ElMessageBox.alert(event.data, '系统提示', {
      type: 'warning',
      confirmButtonText: '确定',
      closeOnClickModal: false,
      showClose: false,
    }).then(() => {
      disconnect()
      useUserStore().logOut().then(() => {
        router.push('/login')
      })
    })
  })

  eventSource.onerror = () => {
  }
}

export function disconnect() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}
