<script setup lang="ts">
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { logoutApi } from '../../api/auth'
import { useUserStore } from '../../store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => ['SUPER_ADMIN', 'ADMIN'].includes(userStore.profile?.role || ''))
const isEmployee = computed(() => userStore.profile?.role === 'EMPLOYEE')

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
        <p v-if="isEmployee" class="employment-line">
          在职公司：{{ userStore.profile?.employedCompany || '未设置' }}
        </p>
      </div>
      <div class="actions">
        <el-tag type="success">角色：{{ userStore.profile?.role }}</el-tag>
        <el-button @click="logout">退出登录</el-button>
      </div>
    </header>

    <section class="board">
      <div class="main-col">
        <el-card class="group-card">
          <template #header>
            <div class="group-head">
              <h3>核心功能</h3>
              <span>日常操作入口</span>
            </div>
          </template>

          <div class="modules">
            <el-card class="module-card" @click="router.push('/logs')">
              <h3>工作日志</h3>
              <p>记录每日任务、工时和项目进展，支持统计与导出。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/goals')">
              <h3>目标冲刺看板</h3>
              <p>设定目标并自动计算完成度，让日志和结果形成闭环。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/reviews')">
              <h3>公司评价</h3>
              <p>提交多维评分，查看公司评价，并进行管理员审核。</p>
            </el-card>

            <el-card v-if="isEmployee" class="module-card" @click="router.push('/profile')">
              <h3>个人资料</h3>
              <p>维护在职公司，影响你可提交的公司评价范围。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/password')">
              <h3>修改密码</h3>
              <p>校验当前密码后更新账号密码，修改成功后重新登录。</p>
            </el-card>

            <el-card v-if="isAdmin" class="module-card" @click="router.push('/admin')">
              <h3>用户与角色管理</h3>
              <p>创建账号、修改角色、启停账号、重置密码。</p>
            </el-card>
          </div>
        </el-card>

        <el-card class="group-card">
          <template #header>
            <div class="group-head">
              <h3>智能洞察</h3>
              <span>数据分析与建议</span>
            </div>
          </template>

          <div class="modules">
            <el-card class="module-card" @click="router.push('/insights/report')">
              <h3>智能周报助手</h3>
              <p>自动汇总关键成果、风险与后续计划，快速生成周报。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/insights/mood')">
              <h3>职场情绪温度计</h3>
              <p>根据日志情绪分追踪状态趋势，给出健康节奏建议。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/insights/skills')">
              <h3>技能画像与成长地图</h3>
              <p>识别高频能力标签，形成个人成长方向和行动建议。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/insights/risk-radar')">
              <h3>风险雷达中心</h3>
              <p>聚合情绪、工时、目标、评价信号，快速发现风险点。</p>
            </el-card>

            <el-card class="module-card" @click="router.push('/insights/retrospective')">
              <h3>智能复盘模板生成器</h3>
              <p>自动提炼本周做得好、待改进与可执行行动清单。</p>
            </el-card>

            <el-card v-if="isAdmin" class="module-card" @click="router.push('/insights/review-alerts')">
              <h3>评价异动预警</h3>
              <p>监测评分异常波动与负面占比，辅助管理快速响应。</p>
            </el-card>
          </div>
        </el-card>
      </div>

      <aside class="side-col">
        <el-card class="profile-card">
          <h3>账户概览</h3>
          <p><strong>账号：</strong>{{ userStore.profile?.username }}</p>
          <p><strong>昵称：</strong>{{ userStore.profile?.nickname || '-' }}</p>
          <p><strong>角色：</strong>{{ userStore.profile?.role }}</p>
          <p v-if="isEmployee"><strong>在职公司：</strong>{{ userStore.profile?.employedCompany || '未设置' }}</p>
          <div class="side-actions">
            <el-button size="small" @click="router.push('/logs')">去写日志</el-button>
            <el-button size="small" type="primary" plain @click="router.push('/insights/report')">看周报</el-button>
          </div>
        </el-card>

        <el-card class="tips-card">
          <h3>本周建议</h3>
          <ul>
            <li>优先完成高优任务，再安排复盘时间。</li>
            <li>至少保持每 2 天一次日志更新，保证趋势数据连续。</li>
            <li>周末前导出周报，沉淀成果和风险处理策略。</li>
          </ul>
        </el-card>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 24px;
  animation: reveal-up 0.5s ease;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.82);
  box-shadow: var(--shadow-soft);
  position: relative;
  overflow: hidden;
}

.header::after {
  content: "";
  position: absolute;
  inset: 0;
  background: var(--theme-dashboard);
  opacity: 0.45;
  pointer-events: none;
}

.header > * {
  position: relative;
  z-index: 1;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header h2 {
  margin: 0;
  font-size: 30px;
  letter-spacing: 0.8px;
}

.header p {
  margin: 8px 0 0;
  color: #526c83;
}

.employment-line {
  margin: 8px 0 0;
  color: #4a6d85;
}

.board {
  margin-top: 18px;
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 16px;
  align-items: start;
}

.main-col {
  display: grid;
  gap: 16px;
}

.group-card {
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-soft);
}

.group-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.group-head h3 {
  margin: 0;
  font-size: 21px;
}

.group-head span {
  color: #5a788f;
  font-size: 13px;
}

.modules {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.module-card {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
  overflow: hidden;
  border-radius: 18px;
  border: none;
  animation: reveal-up 0.45s ease;
}

.module-card:nth-child(1) { animation-delay: 0.03s; }
.module-card:nth-child(2) { animation-delay: 0.07s; }
.module-card:nth-child(3) { animation-delay: 0.11s; }
.module-card:nth-child(4) { animation-delay: 0.15s; }
.module-card:nth-child(5) { animation-delay: 0.19s; }
.module-card:nth-child(6) { animation-delay: 0.23s; }

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-pop);
  filter: saturate(1.06);
}

.module-card h3 {
  margin: 0;
  font-size: 20px;
}

.module-card p {
  margin: 10px 0 0;
  color: #38556f;
  min-height: 36px;
}

.module-card:nth-child(1) { background: linear-gradient(140deg, #ffe9cf 0%, #ffe082 100%); }
.module-card:nth-child(2) { background: linear-gradient(140deg, #d9f6ff 0%, #96e6ff 100%); }
.module-card:nth-child(3) { background: linear-gradient(140deg, #f2e8ff 0%, #d5c8ff 100%); }
.module-card:nth-child(4) { background: linear-gradient(140deg, #ffe2ea 0%, #ffb9cb 100%); }
.module-card:nth-child(5) { background: linear-gradient(140deg, #ddffe9 0%, #99edb2 100%); }
.module-card:nth-child(6) { background: linear-gradient(140deg, #ffe6cb 0%, #ffc88c 100%); }

.side-col {
  display: grid;
  gap: 16px;
}

.profile-card,
.tips-card {
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 16px 32px rgba(16, 45, 72, 0.14);
}

.profile-card h3,
.tips-card h3 {
  margin: 0 0 10px;
  font-size: 19px;
}

.profile-card p {
  margin: 8px 0;
  color: #38546c;
}

.side-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.tips-card ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 10px;
  color: #35516b;
}

.dashboard :deep(.el-tag.el-tag--success) {
  border: none;
  color: #14334b;
  background: linear-gradient(130deg, #8df5bd 0%, #49e0d5 100%);
}

@media (max-width: 1200px) {
  .board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .modules {
    grid-template-columns: 1fr;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
