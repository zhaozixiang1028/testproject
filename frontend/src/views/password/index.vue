<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { changeMyPasswordApi, logoutApi } from '../../api/auth'
import { useUserStore } from '../../store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const submit = async () => {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  saving.value = true
  try {
    await changeMyPasswordApi(form.oldPassword, form.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    try {
      await logoutApi()
    } finally {
      userStore.clearSession()
      await router.replace('/login')
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '修改密码失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="password-page">
    <header class="header">
      <div>
        <h2>修改密码</h2>
        <p>修改成功后需要重新登录。</p>
      </div>
      <el-button @click="router.push('/')">返回工作台</el-button>
    </header>

    <el-card class="password-card">
      <el-form label-position="top">
        <el-form-item label="当前密码">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" :loading="saving" @click="submit">确认修改</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.password-page {
  padding: 24px;
  animation: reveal-up 0.52s ease;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 18px 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-soft);
  position: relative;
  overflow: hidden;
}

.header::after {
  content: "";
  position: absolute;
  inset: 0;
  background: var(--theme-password);
  opacity: 0.46;
  pointer-events: none;
}

.header > * {
  position: relative;
  z-index: 1;
}

.header p {
  margin: 8px 0 0;
  color: #4f6b83;
}

.password-card {
  margin-top: 16px;
  max-width: 640px;
  animation: reveal-up 0.62s ease;
}

.password-page :deep(.el-card) {
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 34px rgba(15, 45, 71, 0.14);
}

.password-page :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(56, 89, 121, 0.15) inset;
}

.password-page :deep(.el-button--primary) {
  border: none;
  background: var(--brand-berry);
}

.actions {
  margin-top: 14px;
}
</style>
