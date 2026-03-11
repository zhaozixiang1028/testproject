import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { Profile, TokenResponse } from '../../types/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    const accessToken = ref('')
    const refreshToken = ref('')
    const profile = ref<Profile | null>(null)

    const isLoggedIn = computed(() => !!accessToken.value)
    const role = computed(() => profile.value?.role)

    function setTokens(payload: TokenResponse) {
      accessToken.value = payload.accessToken
      refreshToken.value = payload.refreshToken
    }

    function setProfile(nextProfile: Profile) {
      profile.value = nextProfile
    }

    function clearSession() {
      accessToken.value = ''
      refreshToken.value = ''
      profile.value = null
    }

    return {
      accessToken,
      refreshToken,
      profile,
      isLoggedIn,
      role,
      setTokens,
      setProfile,
      clearSession,
    }
  },
  {
  persist: {
    key: 'dailywork-user',
    storage: localStorage,
  },
  },
)
