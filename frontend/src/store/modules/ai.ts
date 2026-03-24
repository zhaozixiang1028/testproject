import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { streamChatApi } from '../../api/ai'
import { useUserStore } from './user'
import type { AiChatRequestPayload, AiMessage, AiRole, AiSession } from '../../types/auth'

function createId(prefix = 'id') {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function summarizeTitle(text: string) {
  const clean = text.replace(/\s+/g, ' ').trim()
  if (!clean) return '新会话'
  return clean.length > 16 ? `${clean.slice(0, 16)}...` : clean
}

export const useAiStore = defineStore(
  'ai',
  () => {
    const sessions = ref<AiSession[]>([])
    const activeSessionId = ref('')
    const panelVisible = ref(false)
    const generating = ref(false)
    const errorMessage = ref('')
    const abortController = ref<AbortController | null>(null)

    const sortedSessions = computed(() => {
      return [...sessions.value].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    })

    const activeSession = computed(() => {
      return sessions.value.find((item) => item.id === activeSessionId.value) || null
    })

    function ensureSession() {
      if (activeSession.value) {
        return activeSession.value
      }
      const session: AiSession = {
        id: createId('session'),
        title: '新会话',
        messages: [],
        updatedAt: new Date().toISOString(),
      }
      sessions.value.unshift(session)
      activeSessionId.value = session.id
      return session
    }

    function createSession() {
      const session: AiSession = {
        id: createId('session'),
        title: '新会话',
        messages: [],
        updatedAt: new Date().toISOString(),
      }
      sessions.value.unshift(session)
      activeSessionId.value = session.id
      errorMessage.value = ''
    }

    function setActiveSession(id: string) {
      activeSessionId.value = id
      errorMessage.value = ''
    }

    function deleteSession(id: string) {
      sessions.value = sessions.value.filter((item) => item.id !== id)
      if (activeSessionId.value === id) {
        activeSessionId.value = sessions.value[0]?.id || ''
      }
    }

    function renameSession(id: string, title: string) {
      const session = sessions.value.find((item) => item.id === id)
      if (!session) {
        return
      }
      const nextTitle = title.trim()
      if (!nextTitle) {
        return
      }
      session.title = nextTitle
      session.updatedAt = new Date().toISOString()
    }

    function appendMessage(session: AiSession, role: AiRole, content: string) {
      const message: AiMessage = {
        id: createId('msg'),
        role,
        content,
        createdAt: new Date().toISOString(),
      }
      session.messages.push(message)
      session.updatedAt = new Date().toISOString()
      return message
    }

    function stopGenerating() {
      abortController.value?.abort()
      abortController.value = null
      generating.value = false
    }

    async function sendMessage(prompt: string) {
      const userStore = useUserStore()
      if (!userStore.accessToken) {
        errorMessage.value = '当前未登录，无法使用 AI 聊天'
        return
      }
      if (generating.value) {
        return
      }

      const text = prompt.trim()
      if (!text) {
        return
      }

      const session = ensureSession()
      appendMessage(session, 'user', text)
      const assistantMessage = appendMessage(session, 'assistant', '')

      if (session.title === '新会话') {
        session.title = summarizeTitle(text)
      }

      generating.value = true
      errorMessage.value = ''
      abortController.value = new AbortController()

      const payload: AiChatRequestPayload = {
        sessionId: session.id,
        prompt: text,
        messages: session.messages
          .filter((item) => item.id !== assistantMessage.id)
          .map((item) => ({ role: item.role, content: item.content })),
      }

      try {
        await streamChatApi({
          payload,
          signal: abortController.value.signal,
          onToken: (token) => {
            assistantMessage.content += token
            session.updatedAt = new Date().toISOString()
          },
        })
      } catch (error: any) {
        if (error?.name !== 'AbortError') {
          const message = error?.message || 'AI 请求失败'
          errorMessage.value = message
          if (!assistantMessage.content.trim()) {
            if (message.includes('登录已过期')) {
              assistantMessage.content = '登录状态已过期，请重新登录后继续对话。'
            } else if (message.includes('鉴权失败') || message.includes('Unauthorized')) {
              assistantMessage.content = 'AI 服务鉴权失败，请检查后端 APP_AI_API_KEY 配置是否有效。'
            } else {
              assistantMessage.content = '抱歉，当前服务繁忙，请稍后重试。'
            }
          }
        }
      } finally {
        generating.value = false
        abortController.value = null
        session.updatedAt = new Date().toISOString()
      }
    }

    return {
      sessions,
      activeSessionId,
      panelVisible,
      generating,
      errorMessage,
      sortedSessions,
      activeSession,
      createSession,
      setActiveSession,
      deleteSession,
      renameSession,
      sendMessage,
      stopGenerating,
    }
  },
  {
    persist: {
      key: 'dailywork-ai',
      storage: localStorage,
    },
  },
)
