import type { AiChatRequestPayload } from '../types/auth'
import { useUserStore } from '../store/modules/user'
import router from '../router'

let refreshingPromise: Promise<string> | null = null

function toErrorMessage(text: string) {
  if (!text) return 'AI stream request failed'
  try {
    const parsed = JSON.parse(text) as { message?: string }
    return parsed.message || text
  } catch {
    return text
  }
}

async function refreshAccessToken() {
  const userStore = useUserStore()
  if (!userStore.refreshToken) {
    userStore.clearSession()
    void router.replace('/login')
    throw new Error('登录已过期，请重新登录')
  }

  if (!refreshingPromise) {
    refreshingPromise = (async () => {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: userStore.refreshToken }),
      })

      const raw = await response.text()
      if (!response.ok) {
        userStore.clearSession()
        void router.replace('/login')
        throw new Error(toErrorMessage(raw) || '刷新登录状态失败，请重新登录')
      }

      const payload = JSON.parse(raw) as {
        data?: { accessToken?: string; refreshToken?: string; role?: 'SUPER_ADMIN' | 'ADMIN' | 'EMPLOYEE' }
        message?: string
      }
      const accessToken = payload.data?.accessToken
      const refreshToken = payload.data?.refreshToken
      const role = payload.data?.role || userStore.profile?.role || 'EMPLOYEE'
      if (!accessToken || !refreshToken) {
        userStore.clearSession()
        void router.replace('/login')
        throw new Error(payload.message || '刷新登录状态失败，请重新登录')
      }

      userStore.setTokens({ accessToken, refreshToken, role })
      return accessToken
    })().finally(() => {
      refreshingPromise = null
    })
  }

  return refreshingPromise
}

async function doStreamRequest(options: {
  payload: AiChatRequestPayload
  accessToken: string
  signal?: AbortSignal
}) {
  const { payload, accessToken, signal } = options
  return fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(payload),
    signal,
  })
}

export async function streamChatApi(options: {
  payload: AiChatRequestPayload
  onToken: (token: string) => void
  signal?: AbortSignal
}) {
  const { payload, onToken, signal } = options
  const userStore = useUserStore()

  if (!userStore.accessToken) {
    throw new Error('当前未登录，无法使用 AI 聊天')
  }

  let response = await doStreamRequest({
    payload,
    accessToken: userStore.accessToken,
    signal,
  })

  if (response.status === 401) {
    const nextAccessToken = await refreshAccessToken()
    response = await doStreamRequest({ payload, accessToken: nextAccessToken, signal })
    if (response.status === 401) {
      throw new Error('AI 接口鉴权失败，请稍后重试')
    }
  }

  if (!response.ok || !response.body) {
    if (response.status === 401) {
      throw new Error('AI 接口鉴权失败，请稍后重试')
    }
    const message = await response.text()
    throw new Error(toErrorMessage(message))
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }

    buffer += decoder.decode(value, { stream: true })

    let lineBreakIndex = buffer.indexOf('\n')
    while (lineBreakIndex >= 0) {
      const rawLine = buffer.slice(0, lineBreakIndex)
      buffer = buffer.slice(lineBreakIndex + 1)

      const line = rawLine.trim()
      if (line.startsWith('data:')) {
        const token = line.slice(5).trim()
        if (token && token !== '[DONE]') {
          onToken(token)
        }
      }

      lineBreakIndex = buffer.indexOf('\n')
    }
  }
}
