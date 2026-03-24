<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getReviewAlertsApi } from '../../api/insight'
import type { ReviewAlertSummary } from '../../types/auth'

const router = useRouter()
const days = ref(14)
const loading = ref(false)
const summary = ref<ReviewAlertSummary | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const handleResize = () => {
  chart?.resize()
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !summary.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const source = summary.value.warnings
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    xAxis: {
      type: 'category',
      data: source.map((item) => item.companyName),
      axisLabel: { rotate: 20 },
      axisLine: { lineStyle: { color: '#6f8ea5' } },
    },
    yAxis: {
      type: 'value',
      name: '百分比(%)',
    },
    series: [
      {
        name: '下降幅度',
        type: 'bar',
        data: source.map((item) => Number(item.dropRate)),
        itemStyle: { color: '#ff8a3d', borderRadius: [8, 8, 0, 0] },
        universalTransition: true,
      },
      {
        name: '负面占比',
        type: 'line',
        smooth: true,
        data: source.map((item) => Number(item.negativeRate)),
        lineStyle: { color: '#ff4d6d', width: 3 },
        universalTransition: true,
      },
    ],
    animationDuration: 420,
    animationDurationUpdate: 520,
    animationEasingUpdate: 'cubicOut',
    grid: { left: 44, right: 20, top: 40, bottom: 55 },
  })
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await getReviewAlertsApi(days.value)
    summary.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载评价预警失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

watch(summary, () => {
  void renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<template>
  <div class="alerts-page">
    <header class="header">
      <div>
        <h2>评价异动预警</h2>
        <p>识别评分下滑和负面集中公司，提供管理预警。</p>
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

    <section class="stats" v-if="summary">
      <el-card>
        <p>预警数量</p>
        <h3>{{ summary.totalWarnings }}</h3>
      </el-card>
      <el-card>
        <p>监测窗口</p>
        <h3>近 {{ summary.days }} 天</h3>
      </el-card>
    </section>

    <el-card v-loading="loading" class="table-card">
      <template #header>预警列表</template>
      <el-table :data="summary?.warnings || []">
        <el-table-column prop="companyName" label="公司" min-width="180" />
        <el-table-column prop="recentCount" label="近期样本" width="100" />
        <el-table-column prop="currentAvg" label="当前均分" width="110" />
        <el-table-column prop="previousAvg" label="历史均分" width="110" />
        <el-table-column prop="dropRate" label="下降幅度(%)" width="120" />
        <el-table-column prop="negativeRate" label="负面占比(%)" width="120" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.level === 'HIGH' ? 'danger' : 'warning'">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="chart-card" v-if="summary && summary.warnings.length">
      <template #header>风险对比图</template>
      <div ref="chartRef" class="chart-box" />
    </el-card>
  </div>
</template>

<style scoped>
.alerts-page { padding: 24px; }
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
.stats {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.table-card { margin-top: 16px; }
.chart-card { margin-top: 16px; }
.chart-box {
  height: 300px;
  border-radius: 12px;
  background: rgba(244, 251, 255, 0.8);
}
@media (max-width: 960px) { .stats { grid-template-columns: 1fr; } .actions { flex-wrap: wrap; } }
</style>
