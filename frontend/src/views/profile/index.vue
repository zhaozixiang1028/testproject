<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { updateMyEmploymentApi } from '../../api/auth'
import { listCompanyOptionsApi } from '../../api/company'
import { useUserStore } from '../../store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)
const companyOptions = ref<string[]>([])

const form = reactive({
  employedCompany: userStore.profile?.employedCompany || '',
})

const save = async () => {
  saving.value = true
  try {
    const { data } = await updateMyEmploymentApi(form.employedCompany || undefined)
    userStore.setProfile(data.data)
    ElMessage.success('在职公司已更新')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const loadCompanyOptions = async () => {
  try {
    const { data } = await listCompanyOptionsApi()
    companyOptions.value = data.data || []
  } catch {
    companyOptions.value = []
  }
}

onMounted(loadCompanyOptions)
</script>

<template>
  <div class="profile-page">
    <header class="header">
      <div>
        <h2>个人资料</h2>
        <p>员工可在此维护在职公司，决定评价提交范围。</p>
      </div>
      <el-button @click="router.push('/')">返回工作台</el-button>
    </header>

    <el-card class="profile-card">
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input :model-value="userStore.profile?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input :model-value="userStore.profile?.nickname" disabled />
        </el-form-item>
        <el-form-item label="在职公司">
          <el-select
            v-model="form.employedCompany"
            filterable
            allow-create
            default-first-option
            clearable
            placeholder="请选择或输入在职公司"
            style="width: 100%"
          >
            <el-option v-for="name in companyOptions" :key="name" :label="name" :value="name" />
          </el-select>
        </el-form-item>
        <el-alert title="清空在职公司后，你将无法提交公司评价，仅可浏览他人公开评价。" type="warning" :closable="false" />
        <div class="actions">
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
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
  background: var(--theme-profile);
  opacity: 0.45;
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

.profile-card {
  margin-top: 16px;
  max-width: 640px;
  animation: reveal-up 0.62s ease;
}

.profile-card :deep(.el-card) {
  border-radius: 18px;
}

.profile-page :deep(.el-card) {
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 34px rgba(15, 45, 71, 0.14);
}

.profile-page :deep(.el-input__wrapper),
.profile-page :deep(.el-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(56, 89, 121, 0.15) inset;
}

.profile-page :deep(.el-button--primary) {
  border: none;
  background: var(--brand-ocean);
}

.actions {
  margin-top: 14px;
}
</style>
