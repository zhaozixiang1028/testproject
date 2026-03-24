<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Plus,
  Delete,
  Close,
  VideoPause,
  RefreshRight,
  DocumentCopy,
  EditPen,
  Download,
} from '@element-plus/icons-vue'
import { useAiStore } from '../store/modules/ai'
import { useUserStore } from '../store/modules/user'

const aiStore = useAiStore()
const userStore = useUserStore()
const prompt = ref('')
const listRef = ref<HTMLElement | null>(null)
const searchText = ref('')
const canShowAi = computed(() => userStore.isLoggedIn)

const sessions = computed(() => aiStore.sortedSessions)
const currentSession = computed(() => aiStore.activeSession)
const filteredSessions = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) {
    return sessions.value
  }
  return sessions.value.filter((session) => {
    const titleMatch = session.title.toLowerCase().includes(keyword)
    const contentMatch = session.messages.some((item) => item.content.toLowerCase().includes(keyword))
    return titleMatch || contentMatch
  })
})

watch(
  () => currentSession.value?.messages.length,
  () => {
    nextTick(() => {
      if (listRef.value) {
        listRef.value.scrollTop = listRef.value.scrollHeight
      }
    })
  },
)

watch(
  () => aiStore.panelVisible,
  (visible) => {
    if (visible && !currentSession.value) {
      aiStore.createSession()
    }
  },
)

watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (!loggedIn) {
      aiStore.panelVisible = false
    }
  },
)

function togglePanel() {
  aiStore.panelVisible = !aiStore.panelVisible
}

function createSession() {
  aiStore.createSession()
}

function switchSession(id: string) {
  aiStore.setActiveSession(id)
}

async function removeSession(id: string) {
  try {
    await ElMessageBox.confirm('确定删除该会话吗？', '删除会话', { type: 'warning' })
    aiStore.deleteSession(id)
  } catch {
    // User canceled this action.
  }
}

async function renameSession(id: string, title: string) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的会话标题', '重命名会话', {
      inputValue: title,
      inputPlaceholder: '例如：周报优化讨论',
      confirmButtonText: '保存',
      cancelButtonText: '取消',
    })
    aiStore.renameSession(id, value)
  } catch {
    // User canceled this action.
  }
}

function resetPrompt() {
  prompt.value = ''
}

async function send() {
  const text = prompt.value.trim()
  if (!text) {
    return
  }
  prompt.value = ''
  await aiStore.sendMessage(text)
}

async function retryLast() {
  const session = currentSession.value
  if (!session) return
  const lastUserMessage = [...session.messages].reverse().find((item) => item.role === 'user')
  if (!lastUserMessage) {
    ElMessage.info('暂无可重试的问题')
    return
  }
  await aiStore.sendMessage(lastUserMessage.content)
}

function stopGenerate() {
  aiStore.stopGenerating()
}

async function copyContent(content: string) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function copySessionSummary() {
  const session = currentSession.value
  if (!session || !session.messages.length) {
    ElMessage.warning('当前会话没有可分享内容')
    return
  }

  const blocks = session.messages.slice(-8).map((item) => {
    const role = item.role === 'user' ? '用户' : 'AI'
    return `- ${role}: ${item.content.replace(/\s+/g, ' ').trim()}`
  })
  const summary = [
    `【${session.title}】会话摘要`,
    `时间：${new Date().toLocaleString()}`,
    '',
    ...blocks,
  ].join('\n')

  await copyContent(summary)
}

function onInputKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    void send()
  }
}

function sanitizeFileName(name: string) {
  return name.replace(/[\\/:*?"<>|]/g, '-').trim() || 'ai-chat'
}

function downloadText(content: string, filename: string, mime = 'text/plain;charset=utf-8') {
  const blob = new Blob([content], { type: mime })
  const href = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = href
  a.download = filename
  a.click()
  URL.revokeObjectURL(href)
}

function exportMarkdown() {
  const session = currentSession.value
  if (!session || !session.messages.length) {
    ElMessage.warning('当前会话没有可导出的内容')
    return
  }

  const lines = [`# ${session.title}`, '', `导出时间：${new Date().toLocaleString()}`, '']
  session.messages.forEach((item) => {
    const role = item.role === 'user' ? '你' : 'AI 助手'
    lines.push(`## ${role}`)
    lines.push(item.content || '')
    lines.push('')
  })

  downloadText(lines.join('\n'), `${sanitizeFileName(session.title)}.md`, 'text/markdown;charset=utf-8')
  ElMessage.success('Markdown 已导出')
}

async function exportPdf() {
  const session = currentSession.value
  if (!session || !session.messages.length) {
    ElMessage.warning('当前会话没有可导出的内容')
    return
  }

  try {
    const { jsPDF } = await import('jspdf')
    const doc = new jsPDF({ unit: 'pt', format: 'a4' })
    const pageWidth = doc.internal.pageSize.getWidth()
    const pageHeight = doc.internal.pageSize.getHeight()
    const margin = 40
    const maxTextWidth = pageWidth - margin * 2
    let y = margin

    const ensureSpace = (needed = 20) => {
      if (y + needed > pageHeight - margin) {
        doc.addPage()
        y = margin
      }
    }

    doc.setFontSize(16)
    doc.text(session.title, margin, y)
    y += 24
    doc.setFontSize(10)
    doc.setTextColor(100)
    doc.text(`导出时间：${new Date().toLocaleString()}`, margin, y)
    y += 22

    doc.setTextColor(20)
    session.messages.forEach((item) => {
      const role = item.role === 'user' ? '你' : 'AI 助手'
      ensureSpace(24)
      doc.setFontSize(12)
      doc.setTextColor(31, 41, 55)
      doc.text(role, margin, y)
      y += 16

      doc.setFontSize(10)
      doc.setTextColor(15, 23, 42)
      const wrapped = doc.splitTextToSize(item.content || '', maxTextWidth)
      wrapped.forEach((line: string) => {
        ensureSpace(14)
        doc.text(line, margin, y)
        y += 14
      })
      y += 8
    })

    doc.save(`${sanitizeFileName(session.title)}.pdf`)
    ElMessage.success('PDF 已导出')
  } catch {
    ElMessage.error('PDF 导出失败')
  }
}
</script>

<template>
  <div v-if="canShowAi" class="ai-chat-wrapper">
    <button class="ai-launcher" type="button" @click="togglePanel">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI 助手</span>
    </button>

    <transition name="ai-panel-fade">
      <div v-if="aiStore.panelVisible" class="ai-panel">
        <aside class="ai-sessions">
          <div class="panel-title">会话</div>
          <button class="create-session" type="button" @click="createSession">
            <el-icon><Plus /></el-icon>
            <span>新建</span>
          </button>
          <input
            v-model="searchText"
            class="session-search"
            placeholder="搜索会话标题或内容"
            type="text"
          />

          <div class="session-list">
            <button
              v-for="session in filteredSessions"
              :key="session.id"
              type="button"
              class="session-item"
              :class="{ active: session.id === currentSession?.id }"
              @click="switchSession(session.id)"
            >
              <span class="title">{{ session.title }}</span>
              <span class="time">{{ new Date(session.updatedAt).toLocaleString() }}</span>
              <el-icon class="rename" @click.stop="renameSession(session.id, session.title)"><EditPen /></el-icon>
              <el-icon class="remove" @click.stop="removeSession(session.id)"><Delete /></el-icon>
            </button>
          </div>
        </aside>

        <section class="ai-chat-main">
          <header class="chat-header">
            <div>
              <h3>{{ currentSession?.title || 'AI 对话' }}</h3>
              <p>基于百炼模型流式响应</p>
            </div>
            <button class="ghost" type="button" @click="togglePanel">
              <el-icon><Close /></el-icon>
            </button>
          </header>

          <div class="chat-tools">
            <button class="ghost" type="button" @click="exportMarkdown">
              <el-icon><Download /></el-icon>
              导出 Markdown
            </button>
            <button class="ghost" type="button" @click="exportPdf">
              <el-icon><Download /></el-icon>
              导出 PDF
            </button>
            <button class="ghost" type="button" @click="copySessionSummary">
              <el-icon><DocumentCopy /></el-icon>
              复制摘要
            </button>
          </div>

          <div class="chat-messages" ref="listRef">
            <template v-if="currentSession && currentSession.messages.length">
              <article
                v-for="message in currentSession.messages"
                :key="message.id"
                class="message"
                :class="message.role"
              >
                <div class="meta">{{ message.role === 'user' ? '你' : 'AI 助手' }}</div>
                <div class="bubble">{{ message.content || '...' }}</div>
                <button
                  v-if="message.role === 'assistant' && message.content"
                  class="copy"
                  type="button"
                  @click="copyContent(message.content)"
                >
                  <el-icon><DocumentCopy /></el-icon>
                  复制
                </button>
              </article>
            </template>
            <div v-else class="empty">
              <p>提个问题试试看，比如：</p>
              <p>“帮我总结本周工作亮点，并给出下周优先级建议”</p>
            </div>
          </div>

          <div v-if="aiStore.errorMessage" class="error-text">{{ aiStore.errorMessage }}</div>

          <footer class="chat-input">
            <textarea
              v-model="prompt"
              placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
              rows="3"
              @keydown="onInputKeydown"
            />
            <div class="actions">
              <button class="ghost" type="button" @click="resetPrompt">清空</button>
              <button class="ghost" type="button" @click="retryLast" :disabled="aiStore.generating">
                <el-icon><RefreshRight /></el-icon>
                重试
              </button>
              <button v-if="aiStore.generating" class="warn" type="button" @click="stopGenerate">
                <el-icon><VideoPause /></el-icon>
                停止
              </button>
              <button v-else class="primary" type="button" @click="send">发送</button>
            </div>
          </footer>
        </section>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 3000;
}

.ai-launcher {
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  padding: 12px 16px;
  color: #fff;
  background: linear-gradient(135deg, #0ea5e9, #f97316);
  box-shadow: 0 12px 30px rgba(14, 165, 233, 0.35);
  cursor: pointer;
}

.ai-panel {
  position: absolute;
  right: 0;
  bottom: 58px;
  width: min(920px, calc(100vw - 24px));
  height: min(74vh, 700px);
  border-radius: 18px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 230px 1fr;
  background: linear-gradient(160deg, #fffaf2, #f5fbff);
  box-shadow: 0 20px 48px rgba(2, 6, 23, 0.2);
  border: 1px solid rgba(148, 163, 184, 0.28);
}

.ai-sessions {
  padding: 14px;
  background: rgba(15, 23, 42, 0.92);
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-title {
  font-size: 14px;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #cbd5e1;
}

.create-session {
  border: 1px solid rgba(125, 211, 252, 0.35);
  background: rgba(14, 165, 233, 0.18);
  color: #f8fafc;
  padding: 8px 10px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.session-search {
  width: 100%;
  border: 1px solid rgba(148, 163, 184, 0.6);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.55);
  color: #e2e8f0;
  padding: 8px 10px;
  outline: none;
}

.session-search::placeholder {
  color: #94a3b8;
}

.session-list {
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  border: 1px solid transparent;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.95);
  color: #e2e8f0;
  padding: 10px;
  text-align: left;
  display: grid;
  gap: 4px;
  cursor: pointer;
  position: relative;
}

.session-item.active {
  border-color: rgba(56, 189, 248, 0.65);
  background: rgba(14, 116, 144, 0.4);
}

.session-item .title {
  font-weight: 700;
}

.session-item .time {
  font-size: 12px;
  color: #94a3b8;
}

.session-item .remove {
  position: absolute;
  right: 8px;
  top: 8px;
  opacity: 0.7;
}

.session-item .rename {
  position: absolute;
  right: 30px;
  top: 8px;
  opacity: 0.7;
}

.ai-chat-main {
  display: grid;
  grid-template-rows: auto auto 1fr auto auto;
  min-width: 0;
}

.chat-header {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.chat-tools {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.7);
}

.chat-messages {
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 88%;
  display: grid;
  gap: 6px;
}

.message .meta {
  font-size: 12px;
  color: #64748b;
}

.message .bubble {
  padding: 10px 12px;
  border-radius: 12px;
  white-space: pre-wrap;
  line-height: 1.55;
}

.message.user {
  align-self: flex-end;
}

.message.user .bubble {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: #fff;
}

.message.assistant .bubble {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #0f172a;
}

.message .copy {
  border: none;
  background: transparent;
  color: #475569;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  cursor: pointer;
}

.empty {
  margin: auto;
  text-align: center;
  color: #64748b;
}

.error-text {
  padding: 0 16px 8px;
  color: #dc2626;
  font-size: 12px;
}

.chat-input {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 16px 14px;
  background: rgba(255, 255, 255, 0.88);
}

.chat-input textarea {
  width: 100%;
  resize: none;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
  outline: none;
}

.chat-input textarea:focus {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2);
}

.actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.actions button,
.chat-header .ghost {
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  background: #fff;
  padding: 7px 10px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.actions .primary {
  background: linear-gradient(135deg, #0ea5e9, #f97316);
  color: #fff;
  border: none;
}

.actions .warn {
  background: #ef4444;
  color: #fff;
  border: none;
}

.actions button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.ai-panel-fade-enter-active,
.ai-panel-fade-leave-active {
  transition: all 0.26s ease;
}

.ai-panel-fade-enter-from,
.ai-panel-fade-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}

@media (max-width: 900px) {
  .ai-chat-wrapper {
    right: 8px;
    left: 8px;
    bottom: 8px;
  }

  .ai-launcher {
    margin-left: auto;
  }

  .ai-panel {
    width: 100%;
    height: min(82vh, 760px);
    grid-template-columns: 1fr;
    bottom: 56px;
  }

  .ai-sessions {
    max-height: 170px;
  }
}
</style>
