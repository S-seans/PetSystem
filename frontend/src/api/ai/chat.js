import { getToken } from '@/utils/auth'

const baseApi = import.meta.env.VITE_APP_BASE_API || ''

/**
 * AI 流式对话（SSE）
 *
 * @param {Object} params 请求参数
 * @param {string} params.message 用户问题
 * @param {Array}  params.history 最近对话历史 [{role:'user'|'assistant', content}]
 * @param {Object} handlers 回调
 * @param {Function} handlers.onContent 流式增量内容
 * @param {Function} handlers.onDone 完成
 * @param {Function} handlers.onError 错误
 * @param {AbortSignal} [signal] 用于取消请求
 * @returns {AbortController}
 */
export function streamChat({ message, history = [] }, { onContent, onDone, onError }, signal) {
  const controller = new AbortController()
  const usedSignal = signal || controller.signal

  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  let buffer = ''
  let completed = false

  function parseLine(line) {
    if (!line.startsWith('data:')) return
    const payload = line.slice(5).trim()
    if (!payload) return
    let data
    try {
      data = JSON.parse(payload)
    } catch (e) {
      return
    }
    if (data.type === 'content' && data.content) {
      onContent && onContent(data.content)
    } else if (data.type === 'done') {
      completed = true
      onDone && onDone()
    } else if (data.type === 'error') {
      completed = true
      onError && onError(data.content || 'AI 服务异常，请稍后再试')
    }
  }

  function parseBuffer() {
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    lines.forEach(parseLine)
  }

  fetch(`${baseApi}/ai/chat/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ message, history }),
    signal: usedSignal
  })
    .then(response => {
      if (!response.ok || !response.body) {
        throw new Error('AI 服务连接失败')
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')

      function read() {
        return reader.read().then(({ done, value }) => {
          if (done) {
            parseBuffer()
            if (!completed) {
              onError && onError('AI 连接已中断，请稍后再试')
            }
            return
          }
          buffer += decoder.decode(value, { stream: true })
          parseBuffer()
          return read()
        })
      }
      return read()
    })
    .catch(err => {
      if (err && err.name === 'AbortError') return
      onError && onError((err && err.message) || 'AI 服务连接失败')
    })

  return controller
}
