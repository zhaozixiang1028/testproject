import type { AiChatRequestPayload, AiConfigCheckResponse, ApiResponse } from '../types/auth'
import { useUserStore } from '../store/modules/user'

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

function mapAiFailureMessage(raw: string, status?: number) {
  const message = toErrorMessage(raw)
  const lowerMessage = message.toLowerCase()

  const isAppUnauthorized =
    status === 401 && (lowerMessage === 'unauthorized' || lowerMessage.includes('登录凭证已失效'))

  if (isAppUnauthorized) {
    return '登录凭证已失效，请重新登录'
  }

  const isAuthFailure =
    lowerMessage.includes('ai api key is not configured') ||
    lowerMessage.includes('api key is not configured') ||
    lowerMessage.includes('app_ai_api_key') ||
    lowerMessage.includes('dashscope_api_key') ||
    lowerMessage.includes('ai 上游鉴权失败') ||
    lowerMessage.includes('unauthorized') ||
    lowerMessage.includes('invalid api key') ||
    lowerMessage.includes('invalid_api_key') ||
    lowerMessage.includes('incorrect api key') ||
    lowerMessage.includes('api key is invalid') ||
    lowerMessage.includes('authentication') ||
    lowerMessage.includes('鉴权') ||
    lowerMessage.includes('密钥无效')

  if (isAuthFailure) {
    return 'AI 服务鉴权失败（API Key 未配置或无效），请联系管理员检查后端 APP_AI_API_KEY 或 DASHSCOPE_API_KEY。'
  }

  if (status === 401) {
    return 'AI 聊天接口未授权，请刷新登录态后重试。'
  }

  const isModelUnavailable =
    status === 404 ||
    lowerMessage.includes('model') && (lowerMessage.includes('not found') || lowerMessage.includes('not exist') || lowerMessage.includes('not support')) ||
    lowerMessage.includes('模型不存在') ||
    lowerMessage.includes('模型不可用')

  if (isModelUnavailable) {
    return 'AI 模型不可用，请检查 APP_AI_MODEL 与网关配置。'
  }

  const isRateLimited =
    status === 429 ||
    lowerMessage.includes('rate limit') ||
    lowerMessage.includes('too many requests') ||
    lowerMessage.includes('quota') ||
    lowerMessage.includes('限流') ||
    lowerMessage.includes('请求过多')

  if (isRateLimited) {
    return 'AI 服务限流，请稍后重试。'
  }

  const isTimeout =
    status === 408 ||
    lowerMessage.includes('timeout') ||
    lowerMessage.includes('timed out') ||
    lowerMessage.includes('etimedout') ||
    lowerMessage.includes('econnaborted') ||
    lowerMessage.includes('networkerror') ||
    lowerMessage.includes('failed to fetch') ||
    lowerMessage.includes('超时')

  if (isTimeout) {
    return 'AI 服务超时，请稍后重试。'
  }

  try {
    const parsed = JSON.parse(raw) as { code?: number; message?: string }
    if (parsed.message && parsed.message.trim()) {
      return mapAiFailureMessage(parsed.message, status)
    }
  } catch {
    // Keep fallback behavior for non-JSON responses.
  }

  return message || '抱歉，当前服务繁忙，请稍后重试。'
}

async function refreshAccessToken() {
  const userStore = useUserStore()
  if (!userStore.refreshToken) {
    throw new Error('登录凭证已失效，请重新登录')
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
        if (response.status === 401) {
          throw new Error('登录凭证已失效，请重新登录')
        }
        throw new Error(toErrorMessage(raw) || '刷新登录状态失败，请稍后重试')
      }

      const payload = JSON.parse(raw) as {
        code?: number
        data?: { accessToken?: string; refreshToken?: string; role?: 'SUPER_ADMIN' | 'ADMIN' | 'EMPLOYEE' }
        message?: string
      }
      if (payload.code && payload.code !== 0) {
        if (payload.code === 401) {
          throw new Error('登录凭证已失效，请重新登录')
        }
        throw new Error(payload.message || '刷新登录状态失败，请稍后重试')
      }
      const accessToken = payload.data?.accessToken
      const refreshToken = payload.data?.refreshToken
      const role = payload.data?.role || userStore.profile?.role || 'EMPLOYEE'
      if (!accessToken || !refreshToken) {
        throw new Error(payload.message || '刷新登录状态失败，请稍后重试')
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

async function doConfigCheckRequest(accessToken: string) {
  return fetch('/api/ai/config/check', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })
}

async function canUseAiWithToken(accessToken: string) {
  try {
    const response = await doConfigCheckRequest(accessToken)
    if (!response.ok) {
      return false
    }
    const raw = await response.text()
    const parsed = JSON.parse(raw) as ApiResponse<AiConfigCheckResponse>
    return parsed.code === 0 && !!parsed.data && parsed.data.authPassed && parsed.data.providerReachable
  } catch {
    return false
  }
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

  let response: Response
  try {
    response = await doStreamRequest({
      payload,
      accessToken: userStore.accessToken,
      signal,
    })
  } catch (error: any) {
    if (error?.name === 'AbortError') {
      throw error
    }
    throw new Error('AI 服务超时，请稍后重试。')
  }

  if (response.status === 401) {
    let nextAccessToken = ''
    try {
      nextAccessToken = await refreshAccessToken()
    } catch (error: any) {
      throw new Error(error?.message || 'AI 聊天接口未授权，请刷新登录态后重试。')
    }
    try {
      response = await doStreamRequest({ payload, accessToken: nextAccessToken, signal })
    } catch (error: any) {
      if (error?.name === 'AbortError') {
        throw error
      }
      throw new Error('AI 服务超时，请稍后重试。')
    }
    if (response.status === 401) {
      const stillUsable = await canUseAiWithToken(nextAccessToken)
      if (stillUsable) {
        try {
          response = await doStreamRequest({ payload, accessToken: nextAccessToken, signal })
        } catch (error: any) {
          if (error?.name === 'AbortError') {
            throw error
          }
          throw new Error('AI 服务超时，请稍后重试。')
        }
      }
      if (response.status === 401) {
        throw new Error('AI 聊天接口未授权，请刷新登录态后重试。')
      }
    }
  }

  if (!response.ok || !response.body) {
    if (response.status === 401) {
      throw new Error('AI 聊天接口未授权，请刷新登录态后重试。')
    }
    const message = await response.text()
    throw new Error(mapAiFailureMessage(message, response.status))
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

export async function checkAiConfigApi() {
  const userStore = useUserStore()
  if (!userStore.accessToken) {
    throw new Error('当前未登录，无法执行 AI 配置自检')
  }

  let response = await doConfigCheckRequest(userStore.accessToken)
  if (response.status === 401) {
    let nextAccessToken = ''
    try {
      nextAccessToken = await refreshAccessToken()
    } catch {
      throw new Error('登录凭证已失效，请重新登录')
    }
    response = await doConfigCheckRequest(nextAccessToken)
  }

  const raw = await response.text()
  if (!response.ok) {
    throw new Error(mapAiFailureMessage(raw, response.status))
  }

  try {
    const parsed = JSON.parse(raw) as ApiResponse<AiConfigCheckResponse>
    if (parsed.code !== 0 || !parsed.data) {
      throw new Error(parsed.message || 'AI 配置自检失败')
    }
    return parsed.data
  } catch (error: any) {
    if (error instanceof SyntaxError) {
      throw new Error('AI 配置自检返回格式异常')
    }
    throw error
  }
}
