import router from './index'
import { useUserStore } from '../store/modules/user'
import { profileApi } from '../api/auth'

let profileLoaded = false

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.path === '/login' && userStore.isLoggedIn) {
    return '/'
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!userStore.isLoggedIn) {
    return '/login'
  }

  if (!profileLoaded) {
    try {
      const { data } = await profileApi()
      userStore.setProfile(data.data)
      profileLoaded = true
    } catch (error) {
      userStore.clearSession()
      return '/login'
    }
  }

  if (to.meta.roles?.length && userStore.role && !to.meta.roles.includes(userStore.role)) {
    return '/'
  }

  return true
})
