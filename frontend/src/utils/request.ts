import axios from 'axios'
import { useUserStore } from '../store/modules/user'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

let refreshing = false
let retryQueue: Array<() => void> = []

const refreshClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

function flushQueue() {
  retryQueue.forEach((run) => run())
  retryQueue = []
}

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.accessToken) {
    config.headers.Authorization = `Bearer ${userStore.accessToken}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  async (error) => {
    const userStore = useUserStore()
    const originalConfig = error.config as { _retry?: boolean; headers?: Record<string, string> }

    if (error.response?.status !== 401 || originalConfig?._retry) {
      return Promise.reject(error)
    }

    if (!userStore.refreshToken) {
      userStore.clearSession()
      router.replace('/login')
      return Promise.reject(error)
    }

    if (refreshing) {
      return new Promise((resolve) => {
        retryQueue.push(() => resolve(request(originalConfig)))
      })
    }

    originalConfig._retry = true
    refreshing = true
    try {
      const { data } = await refreshClient.post('/auth/refresh', { refreshToken: userStore.refreshToken })
      userStore.setTokens(data.data)
      if (originalConfig.headers) {
        originalConfig.headers.Authorization = `Bearer ${userStore.accessToken}`
      }
      flushQueue()
      return request(originalConfig)
    } catch (refreshError) {
      userStore.clearSession()
      router.replace('/login')
      return Promise.reject(refreshError)
    } finally {
      refreshing = false
    }
  },
)

export default request
