<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { loginApi, profileApi, registerApi } from '../../api/auth'
import { useUserStore } from '../../store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const pageMode = ref<'login' | 'register'>('login')
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  remember: true,
})

const passwordStrength = computed(() => {
  const pwd = formState.password
  if (pwd.length < 8) return '弱'
  if (/[A-Z]/.test(pwd) && /[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) return '强'
  return '中'
})

const submit = async () => {
  if (!formState.username || !formState.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const { data } = await loginApi({ username: formState.username, password: formState.password })
    userStore.setTokens(data.data)

    const profileResp = await profileApi()
    userStore.setProfile(profileResp.data.data)

    ElMessage.success('登录成功')
    await router.replace('/')
  } catch (error: any) {
    const message = error?.response?.data?.message || '登录失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const registerState = reactive({
  username: '',
  password: '',
  nickname: '',
})

const registerLoading = ref(false)

const submitRegister = async () => {
  if (!registerState.username || !registerState.password || !registerState.nickname) {
    ElMessage.warning('请填写完整注册信息')
    return
  }

  registerLoading.value = true
  try {
    await registerApi({
      username: registerState.username,
      password: registerState.password,
      nickname: registerState.nickname,
    })
    ElMessage.success('注册成功，请使用新账号登录')
    registerState.username = ''
    registerState.password = ''
    registerState.nickname = ''
  } catch (error: any) {
    const message = error?.response?.data?.message || '注册失败'
    ElMessage.error(message)
  } finally {
    registerLoading.value = false
  }
}

const openRegisterDialog = async () => {
  await ElMessageBox.alert(
    '新注册用户默认角色为员工（EMPLOYEE）。如需管理员权限，请联系超级管理员在后台分配。',
    '注册说明',
    { confirmButtonText: '我知道了' },
  )
}

const goRegisterMode = () => {
  pageMode.value = 'register'
}

const backToLogin = () => {
  pageMode.value = 'login'
}
</script>

<template>
  <div class="login-page">
    <div class="panel">
      <div class="panel-top" v-if="pageMode === 'register'">
        <el-button text type="primary" @click="backToLogin">返回登录</el-button>
      </div>

      <template v-if="pageMode === 'login'">
        <h1>上班日常系统</h1>
        <p class="subtitle">记录你的每一天，也记录企业真实口碑</p>

        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名">
            <el-input v-model="formState.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model="formState.password"
              placeholder="请输入密码"
              show-password
              autocomplete="current-password"
              @keyup.enter="submit"
            />
            <span class="strength">密码强度：{{ passwordStrength }}</span>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="formState.remember">记住登录状态</el-checkbox>
          </el-form-item>

          <div class="auth-actions">
            <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
            <el-button type="success" class="submit" @click="goRegisterMode">注册</el-button>
          </div>
        </el-form>
      </template>

      <template v-else>
        <h1>注册账号</h1>
        <p class="subtitle">新注册用户默认为员工角色（EMPLOYEE）</p>

        <el-form label-position="top" @submit.prevent="submitRegister">
          <el-form-item label="用户名">
            <el-input v-model="registerState.username" placeholder="新账号用户名" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="registerState.nickname" placeholder="显示昵称" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="registerState.password" type="password" show-password placeholder="至少6位" />
          </el-form-item>
          <div class="register-actions">
            <el-button link type="primary" @click="openRegisterDialog">注册说明</el-button>
            <el-button type="success" :loading="registerLoading" @click="submitRegister">注册</el-button>
          </div>
        </el-form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 14% 18%, rgba(255, 122, 24, 0.36) 0%, rgba(255, 122, 24, 0) 36%),
    radial-gradient(circle at 84% 22%, rgba(0, 166, 251, 0.34) 0%, rgba(0, 166, 251, 0) 40%),
    radial-gradient(circle at 86% 84%, rgba(255, 77, 109, 0.24) 0%, rgba(255, 77, 109, 0) 32%),
    linear-gradient(140deg, #fff4e9 0%, #eef8ff 44%, #f2fff6 100%);
}

.panel {
  width: min(460px, 100%);
  background: rgba(255, 255, 255, 0.76);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  padding: 30px;
  box-shadow: 0 24px 48px rgba(20, 43, 67, 0.18);
  animation: float-in 0.65s ease-out;
}

.panel-top {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

h1 {
  margin: 0;
  font-size: 30px;
  letter-spacing: 1px;
  background: linear-gradient(100deg, #ff7a18 0%, #ff4d6d 48%, #00a6fb 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.subtitle {
  margin: 8px 0 22px;
  color: #4f677d;
  font-weight: 500;
}

.submit {
  width: 100%;
}

.auth-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.auth-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.register-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.strength {
  margin-top: 8px;
  color: #576f84;
  font-size: 12px;
}

.panel :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(58, 92, 126, 0.14) inset;
}

.panel :deep(.el-button--primary) {
  border: none;
  background: var(--brand-ocean);
}

.panel :deep(.el-button--success) {
  border: none;
  background: var(--brand-sunset);
  color: #1d2f44;
}

@keyframes float-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
