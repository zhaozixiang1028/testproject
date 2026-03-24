<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMoodTrendApi } from '../../api/insight'
import type { MoodTrend } from '../../types/auth'

const router = useRouter()
const days = ref(14)
const loading = ref(false)
const trend = ref<MoodTrend | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const handleResize = () => {
  chart?.resize()
}

const avgMoodNum = computed(() => Number(trend.value?.avgMood || 0))
const avgMoodPercent = computed(() => Math.min(100, Math.round((avgMoodNum.value / 5) * 100)))

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !trend.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trend.value.series.map((item) => item.date),
      axisLine: { lineStyle: { color: '#6f8ea5' } },
    },
    yAxis: {
      type: 'value',
      min: 1,
      max: 5,
      interval: 1,
      name: '情绪分',
    },
    series: [
      {
        type: 'line',
        data: trend.value.series.map((item) => item.moodScore),
        smooth: true,
        areaStyle: {
          color: 'rgba(59, 180, 255, 0.2)',
        },
        lineStyle: { width: 3, color: '#3bb4ff' },
        symbolSize: 8,
        universalTransition: true,
      },
    ],
    animationDuration: 420,
    animationDurationUpdate: 520,
    animationEasingUpdate: 'cubicOut',
    grid: { left: 44, right: 20, top: 20, bottom: 26 },
  })
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await getMoodTrendApi(days.value)
    trend.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载情绪趋势失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

watch(trend, () => {
  void renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<template>
  <div class="mood-page">
    <header class="header">
      <div>
        <h2>职场情绪温度计</h2>
        <p>基于日志情绪分，识别近期状态趋势。</p>
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

    <section v-loading="loading" class="grid" v-if="trend">
      <el-card>
        <template #header>平均情绪分</template>
        <el-progress :percentage="avgMoodPercent" :stroke-width="18" status="success" />
        <h3>{{ trend.avgMood }} / 5</h3>
      </el-card>
      <el-card>
        <template #header>最低情绪日</template>
        <h3>{{ trend.lowestMoodDay?.date || '暂无' }}</h3>
        <p>分值：{{ trend.lowestMoodDay?.moodScore ?? '-' }}</p>
      </el-card>
      <el-card>
        <template #header>趋势点位</template>
        <el-table :data="trend.series" size="small" max-height="220">
          <el-table-column prop="date" label="日期" />
          <el-table-column prop="moodScore" label="情绪分" width="90" />
        </el-table>
      </el-card>
    </section>

    <el-card class="chart-card" v-if="trend">
      <template #header>情绪趋势曲线</template>
      <div ref="chartRef" class="chart-box" />
    </el-card>

    <el-card class="advice" v-if="trend">
      <template #header>建议</template>
      <ul>
        <li v-for="item in trend.advice" :key="item">{{ item }}</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.mood-page { padding: 24px; }
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
.grid { margin-top: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.chart-card { margin-top: 16px; }
.chart-box {
  height: 280px;
  border-radius: 12px;
  background: rgba(244, 251, 255, 0.8);
}
.advice { margin-top: 16px; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; }
@media (max-width: 960px) { .grid { grid-template-columns: 1fr; } .actions { flex-wrap: wrap; } }
</style>
