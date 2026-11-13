<template>
  <div class="chat-page">
    <el-container class="chat-container">
      <el-aside width="260px" class="session-panel">
        <div class="session-header">
          <el-button type="primary" size="small" @click="handleNewSession">新建会话</el-button>
        </div>
        <el-scrollbar class="session-list">
          <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: s.id === currentSessionId }"
            @click="selectSession(s.id)"
          >
            <div class="session-title">{{ s.title || '新会话' }}</div>
            <div class="session-time">{{ formatTime(s.updatedAt) }}</div>
          </div>
          <el-empty v-if="!sessions.length" description="暂无会话" :image-size="60" />
        </el-scrollbar>
      </el-aside>

      <el-container class="chat-main-wrap">
        <el-main class="chat-main">
          <div ref="messageListRef" class="message-list">
            <div v-if="!messages.length" class="chat-empty">
              <el-empty description="开始和 AI 助手对话，例如：今天车怎么样？" />
            </div>
            <div v-for="m in messages" :key="m.id" class="message-row" :class="m.role">
              <div class="bubble">{{ m.content }}</div>
            </div>
            <div v-if="sending" class="message-row assistant">
              <div class="bubble loading">正在思考…</div>
            </div>
          </div>

          <div class="input-bar">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              placeholder="输入消息，Enter 发送 / Shift+Enter 换行"
              @keydown.enter.exact.prevent="handleSend"
            />
            <el-button
              type="primary"
              :loading="sending"
              :disabled="!input.trim() || !currentSessionId"
              @click="handleSend"
            >
              发送
            </el-button>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { createSession, listSessions, getMessages, sendMessage } from '../api/chat'

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const messageListRef = ref(null)

async function loadSessions() {
  const res = await listSessions()
  sessions.value = res.data || []
  if (!currentSessionId.value && sessions.value.length) {
    selectSession(sessions.value[0].id)
  }
}

async function selectSession(id) {
  currentSessionId.value = id
  const res = await getMessages(id)
  messages.value = res.data || []
  await scrollToBottom()
}

async function handleNewSession() {
  const res = await createSession()
  const s = res.data
  sessions.value.unshift(s)
  currentSessionId.value = s.id
  messages.value = []
  input.value = ''
}

async function handleSend() {
  const text = input.value.trim()
  if (!text || !currentSessionId.value || sending.value) return
  messages.value.push({ id: 'tmp-' + Date.now(), role: 'user', content: text })
  input.value = ''
  sending.value = true
  await scrollToBottom()
  try {
    const res = await sendMessage(currentSessionId.value, text)
    messages.value.push(res.data)
    await loadSessions()
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

function scrollToBottom() {
  return nextTick(() => {
    const el = messageListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

onMounted(loadSessions)
</script>

<style scoped>
.chat-page {
  height: 100%;
}
.chat-container {
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}
.session-panel {
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}
.session-header {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
}
.session-list {
  flex: 1;
  overflow: hidden;
}
.session-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}
.session-item:hover {
  background: #f0f6ff;
}
.session-item.active {
  background: #e8f3ff;
}
.session-title {
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.session-time {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.chat-main-wrap {
  display: flex;
  flex-direction: column;
}
.chat-main {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: 100%;
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}
.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.message-row {
  display: flex;
  margin-bottom: 12px;
}
.message-row.user {
  justify-content: flex-end;
}
.message-row.assistant {
  justify-content: flex-start;
}
.bubble {
  max-width: 72%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.message-row.user .bubble {
  background: #409eff;
  color: #fff;
}
.message-row.assistant .bubble {
  background: #f2f3f5;
  color: #303133;
}
.bubble.loading {
  color: #909399;
}
.input-bar {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
