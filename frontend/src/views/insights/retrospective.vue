<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getRetrospectiveApi } from '../../api/insight'
import type { Retrospective } from '../../types/auth'

const router = useRouter()
const period = ref<'week' | 'month'>('week')
const loading = ref(false)
const dataRef = ref<Retrospective | null>(null)

const load = async () => {
  loading.value = true
  try {
    const { data } = await getRetrospectiveApi(period.value)
    dataRef.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载复盘模板失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="retro-page">
    <header class="header">
      <div>
        <h2>智能复盘模板生成器</h2>
        <p>自动生成结构化复盘，沉淀亮点、问题与行动。</p>
      </div>
      <div class="actions">
        <el-radio-group v-model="period" size="small" @change="load">
          <el-radio-button label="week">周复盘</el-radio-button>
          <el-radio-button label="month">月复盘</el-radio-button>
        </el-radio-group>
        <el-button @click="router.push('/')">返回工作台</el-button>
      </div>
    </header>

    <el-card v-loading="loading" class="summary-card" v-if="dataRef">
      <template #header>复盘概览</template>
      <p>周期：{{ dataRef.startDate }} ~ {{ dataRef.endDate }}</p>
      <p>主题：{{ dataRef.theme }}</p>
      <p>总结：{{ dataRef.summary }}</p>
    </el-card>

    <section class="grid" v-if="dataRef">
      <el-card>
        <template #header>做得好的</template>
        <ul>
          <li v-for="item in dataRef.whatWentWell" :key="item">{{ item }}</li>
        </ul>
      </el-card>
      <el-card>
        <template #header>可改进项</template>
        <ul>
          <li v-for="item in dataRef.toImprove" :key="item">{{ item }}</li>
        </ul>
      </el-card>
      <el-card>
        <template #header>下一步行动</template>
        <ul>
          <li v-for="item in dataRef.nextActions" :key="item">{{ item }}</li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.retro-page { padding: 24px; }
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
.summary-card { margin-top: 16px; }
.grid { margin-top: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; }
@media (max-width: 1200px) { .grid { grid-template-columns: 1fr; } }
@media (max-width: 960px) { .actions { flex-wrap: wrap; } }
</style>
