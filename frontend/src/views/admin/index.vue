<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/modules/user'
import { listRolesApi } from '../../api/role'
import { listCompanyOptionsApi } from '../../api/company'
import { createUserApi, listUsersApi, resetUserPasswordApi, updateUserEmploymentApi, updateUserRoleApi, updateUserStatusApi } from '../../api/user'
import type { CreateUserPayload, RoleOption, UserItem, UserRole } from '../../types/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const users = ref<UserItem[]>([])
const roles = ref<RoleOption[]>([])
const companyOptions = ref<string[]>([])

const createForm = reactive<CreateUserPayload>({
  username: '',
  password: '',
  nickname: '',
  role: 'EMPLOYEE',
  employedCompany: '',
})

const createLoading = ref(false)

const fetchManagerData = async () => {
  loading.value = true
  try {
    const [userResp, roleResp, companyResp] = await Promise.all([listUsersApi(), listRolesApi(), listCompanyOptionsApi()])
    users.value = userResp.data.data
    roles.value = roleResp.data.data
    companyOptions.value = companyResp.data.data || []
    if (roles.value.length && !roles.value.find((item) => item.code === createForm.role)) {
      createForm.role = roles.value[0]?.code || 'EMPLOYEE'
    }
  } finally {
    loading.value = false
  }
}

const createUser = async () => {
  if (!createForm.username || !createForm.password || !createForm.nickname) {
    ElMessage.warning('请填写完整信息')
    return
  }

  createLoading.value = true
  try {
    await createUserApi(createForm)
    ElMessage.success('用户创建成功')
    createForm.username = ''
    createForm.password = ''
    createForm.nickname = ''
    createForm.employedCompany = ''
    await fetchManagerData()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '创建失败')
  } finally {
    createLoading.value = false
  }
}

const updateEmployment = async (row: UserItem, employedCompany?: string) => {
  try {
    await updateUserEmploymentApi(row.id, employedCompany || undefined)
    ElMessage.success('在职公司更新成功')
    await fetchManagerData()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '更新在职公司失败')
  }
}

const updateRole = async (row: UserItem, role: string) => {
  try {
    await updateUserRoleApi(row.id, role as UserRole)
    ElMessage.success('角色更新成功')
    await fetchManagerData()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '更新角色失败')
  }
}

const handleRoleChange = (row: UserItem, value: unknown) => {
  if (typeof value === 'string') {
    updateRole(row, value)
  }
}

const toggleStatus = async (row: UserItem) => {
  const targetStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatusApi(row.id, targetStatus)
    ElMessage.success('状态更新成功')
    await fetchManagerData()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '更新状态失败')
  }
}

const resetPassword = async (row: UserItem) => {
  const { value } = await ElMessageBox.prompt(`请输入 ${row.username} 的新密码`, '重置密码', {
    inputPlaceholder: '至少6位',
    inputType: 'password',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })

  try {
    await resetUserPasswordApi(row.id, value)
    ElMessage.success('密码重置成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '重置失败')
  }
}

const canBack = computed(() => !!userStore.profile)

onMounted(fetchManagerData)
</script>

<template>
  <div class="admin-page">
    <header class="header">
      <div>
        <h2>用户与角色管理</h2>
        <p>当前角色：{{ userStore.profile?.role }}</p>
      </div>
      <el-button v-if="canBack" @click="router.push('/')">返回工作台</el-button>
    </header>

    <section class="manager-panel">
      <el-card>
        <template #header>创建用户</template>
        <el-form label-position="top" class="create-form">
          <el-form-item label="用户名">
            <el-input v-model="createForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="createForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="createForm.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="createForm.role" placeholder="请选择角色" style="width: 100%">
              <el-option v-for="item in roles" :key="item.code" :label="item.label" :value="item.code" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="createForm.role === 'EMPLOYEE'" label="在职公司">
            <el-select
              v-model="createForm.employedCompany"
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
          <el-button type="primary" :loading="createLoading" @click="createUser">创建</el-button>
        </el-form>
      </el-card>

      <el-card>
        <template #header>用户管理</template>
        <el-table :data="users" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column label="在职公司" min-width="180">
            <template #default="scope">
              <el-input
                v-if="scope.row.role === 'EMPLOYEE'"
                :model-value="scope.row.employedCompany || ''"
                size="small"
                clearable
                placeholder="未设置"
                @change="updateEmployment(scope.row, ($event as string) || undefined)"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="150">
            <template #default="scope">
              <el-select :model-value="scope.row.role" size="small" style="width: 130px" @change="handleRoleChange(scope.row, $event)">
                <el-option v-for="item in roles" :key="item.code" :label="item.label" :value="item.code" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <div class="table-actions">
                <el-button link type="primary" @click="toggleStatus(scope.row)">
                  {{ scope.row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button link type="warning" @click="resetPassword(scope.row)">重置密码</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.admin-page {
  padding: 24px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header p {
  margin: 8px 0 0;
  color: #5f7583;
}

.manager-panel {
  margin-top: 20px;
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 16px;
}

.table-actions {
  display: flex;
  gap: 4px;
}

@media (max-width: 960px) {
  .manager-panel {
    grid-template-columns: 1fr;
  }
}
</style>
