<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { logoutApi } from '../../api/auth'
import { useUserStore } from '../../store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const logout = async () => {
  await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
  try {
    await logoutApi()
  } finally {
    userStore.clearSession()
    await router.replace('/login')
  }
}
</script>

<template>
  <div class="dashboard">
    <header class="header">
      <div>
        <h2>主工作台</h2>
        <p>欢迎你，{{ userStore.profile?.nickname || userStore.profile?.username }}</p>
        <p v-if="userStore.profile?.role === 'EMPLOYEE'" class="employment-line">
          在职公司：{{ userStore.profile?.employedCompany || '未设置' }}
        </p>
      </div>
      <div class="actions">
        <el-tag type="success">角色：{{ userStore.profile?.role }}</el-tag>
        <el-button @click="logout">退出登录</el-button>
      </div>
    </header>

    <section class="modules">
      <el-card class="module-card" @click="router.push('/logs')">
        <h3>工作日志</h3>
        <p>记录每日任务、工时和项目进展，支持统计与导出。</p>
      </el-card>

      <el-card class="module-card" @click="router.push('/reviews')">
        <h3>公司评价</h3>
        <p>提交多维评分，查看公司评价，并进行管理员审核。</p>
      </el-card>

      <el-card v-if="userStore.profile?.role === 'EMPLOYEE'" class="module-card" @click="router.push('/profile')">
        <h3>个人资料</h3>
        <p>维护在职公司，影响你可提交的公司评价范围。</p>
      </el-card>

      <el-card class="module-card" @click="router.push('/password')">
        <h3>修改密码</h3>
        <p>校验当前密码后更新账号密码，修改成功后重新登录。</p>
      </el-card>

      <el-card v-if="['SUPER_ADMIN', 'ADMIN'].includes(userStore.profile?.role || '')" class="module-card" @click="router.push('/admin')">
        <h3>用户与角色管理</h3>
        <p>创建账号、修改角色、启停账号、重置密码。</p>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 24px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.employment-line {
  margin: 8px 0 0;
  color: #5f7583;
}

.modules {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.module-card {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.module-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(18, 35, 48, 0.12);
}

.module-card h3 {
  margin: 0;
}

.module-card p {
  margin: 10px 0 0;
  color: #5f7583;
  min-height: 42px;
}
</style>
