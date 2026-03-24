<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteGoalApi, getGoalBoardApi, listGoalsApi, saveGoalApi } from '../../api/goal'
import type { GoalBoard, SprintGoalItem, SprintGoalSavePayload } from '../../types/auth'

const router = useRouter()
const loading = ref(false)
const boardDays = ref(14)
const board = ref<GoalBoard | null>(null)
const goals = ref<SprintGoalItem[]>([])
const editId = ref<number | undefined>(undefined)

const form = reactive<SprintGoalSavePayload>({
  title: '',
  description: '',
  relatedProject: '',
  targetHours: 40,
  status: 'OPEN',
  startDate: new Date().toISOString().slice(0, 10),
  endDate: new Date(Date.now() + 7 * 24 * 3600 * 1000).toISOString().slice(0, 10),
})

const load = async () => {
  loading.value = true
  try {
    const [boardResp, goalsResp] = await Promise.all([getGoalBoardApi(boardDays.value), listGoalsApi()])
    board.value = boardResp.data.data
    goals.value = goalsResp.data.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editId.value = undefined
  form.title = ''
  form.description = ''
  form.relatedProject = ''
  form.targetHours = 40
  form.status = 'OPEN'
  form.startDate = new Date().toISOString().slice(0, 10)
  form.endDate = new Date(Date.now() + 7 * 24 * 3600 * 1000).toISOString().slice(0, 10)
}

const submit = async () => {
  if (!form.title || !form.startDate || !form.endDate || form.targetHours <= 0) {
    ElMessage.warning('请完善目标信息')
    return
  }
  try {
    await saveGoalApi({ ...form, id: editId.value })
    ElMessage.success(editId.value ? '目标更新成功' : '目标创建成功')
    resetForm()
    await load()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '保存目标失败')
  }
}

const editGoal = (row: SprintGoalItem) => {
  editId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.relatedProject = row.relatedProject || ''
  form.targetHours = Number(row.targetHours)
  form.status = row.status
  form.startDate = row.startDate
  form.endDate = row.endDate
}

const removeGoal = async (row: SprintGoalItem) => {
  await ElMessageBox.confirm('确认删除该目标吗？', '提示', { type: 'warning' })
  try {
    await deleteGoalApi(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="goals-page">
    <header class="header">
      <div>
        <h2>目标冲刺看板</h2>
        <p>用目标驱动日志沉淀，自动追踪完成度。</p>
      </div>
      <div class="actions">
        <el-select v-model="boardDays" style="width: 120px" @change="load">
          <el-option :value="14" label="近14天" />
          <el-option :value="30" label="近30天" />
          <el-option :value="60" label="近60天" />
        </el-select>
        <el-button @click="router.push('/')">返回工作台</el-button>
      </div>
    </header>

    <section class="stats" v-if="board">
      <el-card>
        <p>目标总数</p>
        <h3>{{ board.totalGoals }}</h3>
      </el-card>
      <el-card>
        <p>已完成</p>
        <h3>{{ board.completedGoals }}</h3>
      </el-card>
      <el-card>
        <p>平均完成率</p>
        <h3>{{ board.avgCompletionRate }}%</h3>
      </el-card>
    </section>

    <section class="layout" v-loading="loading">
      <el-card>
        <template #header>{{ editId ? '编辑目标' : '新增目标' }}</template>
        <el-form label-position="top">
          <el-form-item label="目标名称"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="目标描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="关联项目"><el-input v-model="form.relatedProject" placeholder="可选" /></el-form-item>
          <el-form-item label="目标工时"><el-input-number v-model="form.targetHours" :min="1" :step="1" style="width: 100%" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="OPEN" value="OPEN" />
              <el-option label="DONE" value="DONE" />
              <el-option label="PAUSED" value="PAUSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="submit">{{ editId ? '更新' : '保存' }}</el-button>
            <el-button @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </el-card>

      <el-card>
        <template #header>目标列表</template>
        <el-table :data="goals" stripe>
          <el-table-column prop="title" label="目标" min-width="160" />
          <el-table-column prop="relatedProject" label="项目" width="120" />
          <el-table-column prop="targetHours" label="目标工时" width="100" />
          <el-table-column prop="completedHours" label="完成工时" width="100" />
          <el-table-column prop="completionRate" label="完成率(%)" width="110" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column prop="endDate" label="截止日期" width="110" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="editGoal(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="removeGoal(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.goals-page { padding: 24px; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 18px 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-soft);
}
.actions { display: flex; gap: 10px; align-items: center; }
.stats { margin-top: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.layout { margin-top: 16px; display: grid; grid-template-columns: minmax(320px, 420px) minmax(0, 1fr); gap: 16px; }
.form-actions { display: flex; gap: 10px; }
@media (max-width: 1200px) { .layout, .stats { grid-template-columns: 1fr; } }
@media (max-width: 960px) { .actions { flex-wrap: wrap; } }
</style>
