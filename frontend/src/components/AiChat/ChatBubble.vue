<template>
  <Teleport to="body">
    <!-- 聊天气泡 -->
    <div class="ai-bubble" :class="{ 'is-open': visible }" @click="toggle">
      <div v-if="!visible" class="ai-bubble-btn">
        <span class="ai-bubble-icon">✨</span>
        <span class="ai-bubble-tooltip">智能助手</span>
      </div>
      <div v-else class="ai-bubble-btn ai-bubble-btn-close">
        <el-icon><Close /></el-icon>
      </div>
    </div>

    <!-- 聊天面板 -->
    <Transition name="ai-pop">
      <div v-if="visible" class="ai-panel">
        <header class="ai-panel-head">
          <div class="ai-title">
            <span class="ai-title-icon">🤖</span>
            <div>
              <div class="ai-title-name">智能客服助手</div>
              <div class="ai-title-sub">关于本系统 / 宠物领养问题都可以问我</div>
            </div>
          </div>
          <el-button text class="ai-clear" title="清空对话" @click="clearChat">
            <el-icon><Delete /></el-icon>
          </el-button>
        </header>

        <main ref="msgBox" class="ai-msgs">
          <el-empty
            v-if="!messages.length"
            :image-size="70"
            description="你好，我是智能客服助手，请问有什么可以帮你？"
          >
            <div class="ai-chips">
              <button v-for="q in quickQuestions" :key="q" class="ai-chip" @click="send(q)">{{ q }}</button>
            </div>
          </el-empty>

          <template v-else>
            <div
              v-for="(m, i) in messages"
              :key="i"
              class="ai-msg"
              :class="m.role === 'user' ? 'is-user' : 'is-ai'"
            >
              <div class="ai-avatar" :class="m.role === 'user' ? 'ai-avatar-user' : 'ai-avatar-ai'">
                {{ m.role === 'user' ? '我' : '🤖' }}
              </div>
              <div class="ai-bubble-msg">
                <span v-if="!m.content && m.role === 'assistant' && streaming" class="ai-typing">
                  <i></i><i></i><i></i>
                </span>
                <span v-else class="ai-text">{{ m.content }}</span>
              </div>
            </div>
          </template>
        </main>

        <footer class="ai-panel-foot">
          <div class="ai-input-row">
            <el-input
              v-model="input"
              type="textarea"
              :rows="1"
              resize="none"
              maxlength="500"
              placeholder="请输入你的问题，Enter 发送"
              class="ai-input"
              @keydown.enter.exact.prevent="send(input)"
            />
            <el-button
              class="ai-send"
              :loading="streaming"
              :disabled="streaming || !input.trim()"
              @click="send(input)"
            >发送</el-button>
          </div>
          <div class="ai-tip">AI 回答仅供参考，领养详情请以系统实际数据为准</div>
        </footer>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { Close, Delete } from '@element-plus/icons-vue'
import { streamChat } from '@/api/ai/chat'

const visible = ref(false)
const input = ref('')
const streaming = ref(false)
const messages = ref([])
const msgBox = ref(null)

const quickQuestions = [
  '系统有哪些功能？',
  '有哪些可领养的宠物？',
  '怎么申请领养宠物？'
]

function toggle() {
  visible.value = !visible.value
}

function clearChat() {
  messages.value = []
}

function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

function send(text) {
  const question = (text || '').trim()
  if (!question || streaming.value) return
  input.value = ''

  messages.value.push({ role: 'user', content: question })
  messages.value.push({ role: 'assistant', content: '' })
  scrollToBottom()

  streaming.value = true
  const history = messages.value
    .filter((m, idx) => m.role === 'user' || (m.role === 'assistant' && m.content))
    .slice(0, -1)
    .map(m => ({ role: m.role, content: m.content }))
    .slice(-20)

  const last = () => messages.value[messages.value.length - 1]

  streamChat(
    { message: question, history },
    {
      onContent: chunk => {
        last().content += chunk
        scrollToBottom()
      },
      onDone: () => {
        streaming.value = false
        scrollToBottom()
      },
      onError: msg => {
        last().content = last().content || `抱歉，${msg}`
        streaming.value = false
        scrollToBottom()
      }
    }
  )
}

watch(visible, v => {
  if (v) scrollToBottom()
})
</script>

<style scoped>
/* 气泡 */
.ai-bubble {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3001;
}
.ai-bubble-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  cursor: pointer;
  background: linear-gradient(135deg, #ff9d84, #e8927c);
  color: #fff;
  font-size: 24px;
  box-shadow: 0 8px 22px rgba(232, 146, 124, 0.45);
  transition: transform 0.2s;
}
.ai-bubble-btn:hover {
  transform: scale(1.06);
}
.ai-bubble-btn-close {
  background: #3d3a35;
  font-size: 20px;
  box-shadow: 0 8px 22px rgba(60, 55, 50, 0.3);
}
.ai-bubble-tooltip {
  position: absolute;
  right: 66px;
  top: 50%;
  transform: translateY(-50%);
  background: #2f2b26;
  color: #fff;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}
.ai-bubble-btn:hover .ai-bubble-tooltip {
  opacity: 1;
}

/* 面板 */
.ai-panel {
  position: fixed;
  right: 24px;
  bottom: 92px;
  z-index: 3000;
  width: 380px;
  max-width: calc(100vw - 32px);
  height: 560px;
  max-height: calc(100vh - 140px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(60, 55, 50, 0.18);
  border: 1px solid #f0eae0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.ai-pop-enter-from,
.ai-pop-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

/* 头部 */
.ai-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 12px;
  background: linear-gradient(135deg, #fff6f0, #ffece2);
  border-bottom: 1px solid #f5ece2;
}
.ai-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.ai-title-icon {
  font-size: 26px;
}
.ai-title-name {
  font-weight: 800;
  font-size: 15px;
  color: #2f2b26;
}
.ai-title-sub {
  font-size: 11px;
  color: #b0a99e;
  margin-top: 2px;
}
.ai-clear {
  color: #b0a99e;
  font-size: 16px;
}
.ai-clear:hover {
  color: #e8927c;
}

/* 消息区 */
.ai-msgs {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #faf7f2;
}
.ai-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.ai-msg.is-user {
  flex-direction: row-reverse;
}
.ai-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 800;
}
.ai-avatar-ai {
  background: #ffe9de;
  font-size: 15px;
}
.ai-avatar-user {
  background: #2f2b26;
  color: #fff;
}
.ai-bubble-msg {
  max-width: 76%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13.5px;
  line-height: 1.7;
  word-break: break-word;
}
.ai-msg.is-ai .ai-bubble-msg {
  background: #fff;
  border: 1px solid #f0eae0;
  border-top-left-radius: 4px;
  color: #3d3a35;
}
.ai-msg.is-user .ai-bubble-msg {
  background: #e8927c;
  color: #fff;
  border-top-right-radius: 4px;
}
.ai-text {
  white-space: pre-wrap;
}

/* 打字动画 */
.ai-typing {
  display: inline-flex;
  gap: 4px;
  padding: 4px 2px;
}
.ai-typing i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #e8927c;
  animation: ai-blink 1.2s infinite both;
}
.ai-typing i:nth-child(2) {
  animation-delay: 0.2s;
}
.ai-typing i:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes ai-blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}

/* 快捷问题 */
.ai-chips {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}
.ai-chip {
  padding: 9px 14px;
  border-radius: 999px;
  border: 1.5px solid #ece5da;
  background: #fff;
  color: #6b645b;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}
.ai-chip:hover {
  border-color: #e8927c;
  color: #e8927c;
}

/* 底部输入 */
.ai-panel-foot {
  padding: 12px 14px 14px;
  background: #fff;
  border-top: 1px solid #f0eae0;
}
.ai-input-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.ai-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #faf7f2;
  border-color: #ece5da;
  font-size: 13.5px;
  line-height: 1.6;
}
.ai-input :deep(.el-textarea__inner:focus) {
  border-color: #e8927c;
}
.ai-send {
  height: 40px;
  border-radius: 12px;
  background: #e8927c !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700;
}
.ai-send:hover:not(.is-disabled) {
  background: #dd7f66 !important;
}
.ai-tip {
  margin-top: 8px;
  font-size: 11px;
  color: #c0b8ac;
  text-align: center;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .ai-panel {
    right: 12px;
    left: 12px;
    bottom: 80px;
    width: auto;
  }
  .ai-bubble {
    right: 16px;
    bottom: 16px;
  }
}
</style>
