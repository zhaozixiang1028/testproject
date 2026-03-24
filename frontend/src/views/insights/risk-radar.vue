<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getRiskRadarApi } from '../../api/insight'
import type { RiskRadar } from '../../types/auth'

const router = useRouter()
const days = ref(14)
const loading = ref(false)
const radar = ref<RiskRadar | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !radar.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const values = ['MOOD', 'WORKLOAD', 'GOAL', 'REVIEW', 'SYSTEM'].map((source) => {
    const item = radar.value?.items.find((row) => row.source === source)
    if (!item) return 20
    if (item.level === 'HIGH') return 95
    if (item.level === 'MEDIUM') return 60
    return 30
  })

  chart.setOption({
    radar: {
      indicator: [
        { name: '情绪', max: 100 },
        { name: '负荷', max: 100 },
        { name: '目标', max: 100 },
        { name: '口碑', max: 100 },
        { name: '系统', max: 100 },
      ],
      radius: 92,
    },
    series: [
      {
        type: 'radar',
        data: [{ value: values, name: '风险画像' }],
        areaStyle: { color: 'rgba(255, 122, 24, 0.24)' },
        lineStyle: { color: '#ff7a18', width: 3 },
      },
    ],
    animationDuration: 420,
    animationDurationUpdate: 520,
  })
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await getRiskRadarApi(days.value)
    radar.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载风险雷达失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(radar, () => { void renderChart() })
onMounted(() => window.addEventListener('resize', () => chart?.resize()))
onBeforeUnmount(() => chart?.dispose())
</script>

<template>
  <div class="risk-page">
    <header class="header">
      <div>
        <h2>风险雷达中心</h2>
        <p>合并情绪、工时、目标和评价信号，识别综合风险。</p>
      </div>
      <div class="actions">
        <el-select v-model="days" style="width: 120px" @change="load">
          <el-option :value="14" label="近14天" />
          <el-option :value="30" label="近30天" />
          <el-option :value="60" label="近60天" />
        </el-select>
        <el-button @click="router.push('/')">返回工作台</el-button>
      </div>
    </header>

    <section class="stats" v-if="radar">
      <el-card>
        <p>总体等级</p>
        <h3>{{ radar.overallLevel }}</h3>
      </el-card>
      <el-card>
        <p>综合得分</p>
        <h3>{{ radar.overallScore }}</h3>
      </el-card>
      <el-card>
        <p>风险条目</p>
        <h3>{{ radar.items.length }}</h3>
      </el-card>
    </section>

    <el-card class="chart-card" v-loading="loading" v-if="radar">
      <template #header>风险雷达图</template>
      <div ref="chartRef" class="chart-box" />
    </el-card>

    <el-card class="table-card" v-if="radar">
      <template #header>风险明细</template>
      <el-table :data="radar.items">
        <el-table-column prop="source" label="来源" width="110" />
        <el-table-column prop="level" label="等级" width="100" />
        <el-table-column prop="title" label="风险项" width="160" />
        <el-table-column prop="metric" label="指标" width="100" />
        <el-table-column prop="message" label="说明" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.risk-page { padding: 24px; }
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
.chart-card, .table-card { margin-top: 16px; }
.chart-box { height: 320px; }
@media (max-width: 1200px) { .stats { grid-template-columns: 1fr; } }
@media (max-width: 960px) { .actions { flex-wrap: wrap; } }
</style>
