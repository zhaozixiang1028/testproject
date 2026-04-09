import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { streamChatApi } from '../../api/ai'
import { useUserStore } from './user'
import type { AiChatRequestPayload, AiMessage, AiRole, AiSession } from '../../types/auth'

const AI_STORAGE_PREFIX = 'dailywork-ai'
const AI_LEGACY_STORAGE_KEY = 'dailywork-ai'

interface AiPersistState {
  sessions: AiSession[]
  activeSessionId: string
}

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
    const userStore = useUserStore()
    const sessions = ref<AiSession[]>([])
    const activeSessionId = ref('')
    const panelVisible = ref(false)
    const generating = ref(false)
    const errorMessage = ref('')
    const abortController = ref<AbortController | null>(null)

    function getStorageOwner() {
      if (!userStore.profile) {
        return ''
      }
      return `user-${userStore.profile.id}`
    }

    function getStorageKey(owner: string) {
      return `${AI_STORAGE_PREFIX}:${owner}`
    }

    function clearRuntimeState() {
      sessions.value = []
      activeSessionId.value = ''
      generating.value = false
      errorMessage.value = ''
      abortController.value = null
    }

    function loadPersistedState(owner: string) {
      if (!owner) {
        clearRuntimeState()
        return
      }
      const raw = localStorage.getItem(getStorageKey(owner))
      if (!raw) {
        clearRuntimeState()
        return
      }
      try {
        const data = JSON.parse(raw) as AiPersistState
        sessions.value = Array.isArray(data.sessions) ? data.sessions : []
        activeSessionId.value = typeof data.activeSessionId === 'string' ? data.activeSessionId : ''
      } catch {
        clearRuntimeState()
      }
    }

    function savePersistedState() {
      const owner = getStorageOwner()
      if (!owner) {
        return
      }
      const payload: AiPersistState = {
        sessions: sessions.value,
        activeSessionId: activeSessionId.value,
      }
      localStorage.setItem(getStorageKey(owner), JSON.stringify(payload))
    }

    function purgeLegacyStorage() {
      if (localStorage.getItem(AI_LEGACY_STORAGE_KEY)) {
        localStorage.removeItem(AI_LEGACY_STORAGE_KEY)
      }
    }

    const sortedSessions = computed(() => {
      return [...sessions.value].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    })

    const activeSession = computed(() => {
      return sessions.value.find((item) => item.id === activeSessionId.value) || null
    })

    function syncForCurrentUser() {
      loadPersistedState(getStorageOwner())
    }

    purgeLegacyStorage()
    syncForCurrentUser()

    watch(
      () => getStorageOwner(),
      (nextOwner, previousOwner) => {
        if (nextOwner !== previousOwner) {
          refreshUserScopedData()
        }
      },
    )

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
      savePersistedState()
    }

    function setActiveSession(id: string) {
      activeSessionId.value = id
      errorMessage.value = ''
      savePersistedState()
    }

    function deleteSession(id: string) {
      sessions.value = sessions.value.filter((item) => item.id !== id)
      if (activeSessionId.value === id) {
        activeSessionId.value = sessions.value[0]?.id || ''
      }
      savePersistedState()
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
      savePersistedState()
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
      savePersistedState()
      return message
    }

    function stopGenerating() {
      abortController.value?.abort()
      abortController.value = null
      generating.value = false
    }

    function refreshUserScopedData() {
      stopGenerating()
      syncForCurrentUser()
    }

    async function sendMessage(prompt: string) {
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
            if (message.includes('登录已过期') || message.includes('登录凭证已失效')) {
              assistantMessage.content = '登录状态已过期，请重新登录后继续对话。'
            } else if (message.includes('鉴权失败') || message.includes('Unauthorized') || message.includes('API Key')) {
              assistantMessage.content = 'AI 服务鉴权失败（API Key 未配置或无效），请联系管理员检查后端 APP_AI_API_KEY 或 DASHSCOPE_API_KEY。'
            } else if (message.includes('AI 聊天接口未授权')) {
              assistantMessage.content = 'AI 聊天接口未授权，请刷新登录态后重试。'
            } else if (message.includes('模型不可用') || message.includes('APP_AI_MODEL') || message.includes('model')) {
              assistantMessage.content = 'AI 模型不可用，请检查 APP_AI_MODEL 与网关配置。'
            } else if (message.includes('限流') || message.includes('请求过多') || message.includes('Too Many Requests')) {
              assistantMessage.content = 'AI 服务限流，请稍后重试。'
            } else if (message.includes('超时') || message.includes('timeout') || message.includes('timed out')) {
              assistantMessage.content = 'AI 服务超时，请稍后重试。'
            } else {
              assistantMessage.content = '抱歉，当前服务繁忙，请稍后重试。'
            }
          }
        }
      } finally {
        generating.value = false
        abortController.value = null
        session.updatedAt = new Date().toISOString()
        savePersistedState()
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
      refreshUserScopedData,
    }
  },
)
