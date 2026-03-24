<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getSkillPortraitApi } from '../../api/insight'
import type { SkillPortrait } from '../../types/auth'

const router = useRouter()
const days = ref(30)
const loading = ref(false)
const portrait = ref<SkillPortrait | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const handleResize = () => {
  chart?.resize()
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !portrait.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const rows = portrait.value.topSkills
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: rows.map((row) => row.skill),
      axisLabel: { rotate: 25 },
      axisLine: { lineStyle: { color: '#6f8ea5' } },
    },
    yAxis: {
      type: 'value',
      name: '次数',
    },
    series: [
      {
        type: 'bar',
        data: rows.map((row) => row.mentionCount),
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#4fc08d',
        },
        universalTransition: true,
      },
    ],
    animationDuration: 420,
    animationDurationUpdate: 520,
    animationEasingUpdate: 'cubicOut',
    grid: { left: 44, right: 20, top: 24, bottom: 50 },
  })
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await getSkillPortraitApi(days.value)
    portrait.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载技能画像失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

watch(portrait, () => {
  void renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<template>
  <div class="skills-page">
    <header class="header">
      <div>
        <h2>技能画像与成长地图</h2>
        <p>自动提取日志关键词，形成本周期技能分布。</p>
      </div>
      <div class="actions">
        <el-select v-model="days" style="width: 120px" @change="load">
          <el-option :value="30" label="近30天" />
          <el-option :value="60" label="近60天" />
          <el-option :value="90" label="近90天" />
        </el-select>
        <el-button @click="router.push('/')">返回工作台</el-button>
      </div>
    </header>

    <el-card v-loading="loading" class="table-card">
      <template #header>Top 技能</template>
      <el-table :data="portrait?.topSkills || []">
        <el-table-column prop="skill" label="技能关键词" />
        <el-table-column prop="mentionCount" label="出现次数" width="110" />
        <el-table-column prop="relatedHours" label="关联工时" width="120" />
      </el-table>
    </el-card>

    <el-card class="chart-card" v-if="portrait">
      <template #header>技能热度分布</template>
      <div ref="chartRef" class="chart-box" />
    </el-card>

    <el-card class="tips" v-if="portrait">
      <template #header>成长建议</template>
      <ul>
        <li v-for="item in portrait.growthSuggestions" :key="item">{{ item }}</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.skills-page { padding: 24px; }
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
.table-card { margin-top: 16px; }
.chart-card { margin-top: 16px; }
.chart-box {
  height: 300px;
  border-radius: 12px;
  background: rgba(244, 251, 255, 0.8);
}
.tips { margin-top: 16px; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; }
@media (max-width: 960px) { .actions { flex-wrap: wrap; } }
</style>
