<script setup lang="ts">
import * as echarts from 'echarts'
import jsPDF from 'jspdf'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getWeeklyReportApi } from '../../api/insight'
import type { WeeklyReport } from '../../types/auth'

const router = useRouter()
const period = ref<'week' | 'month'>('week')
const loading = ref(false)
const report = ref<WeeklyReport | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const handleResize = () => {
  chart?.resize()
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !report.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const overview = report.value
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    xAxis: {
      type: 'category',
      data: ['亮点', '风险', '计划'],
      axisLine: { lineStyle: { color: '#6f8ea5' } },
    },
    yAxis: [
      {
        type: 'value',
        name: '条目数',
      },
      {
        type: 'value',
        name: '规模',
      },
    ],
    series: [
      {
        name: '条目数',
        type: 'bar',
        barWidth: 28,
        data: [overview.highlights.length, overview.risks.length, overview.nextPlans.length],
        itemStyle: { borderRadius: [8, 8, 0, 0], color: '#3bb4ff' },
        universalTransition: true,
      },
      {
        name: '日志规模',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: [overview.totalLogs, overview.totalLogs, overview.totalLogs],
        lineStyle: { width: 3, color: '#ff8a3d' },
        universalTransition: true,
      },
    ],
    animationDuration: 450,
    animationDurationUpdate: 550,
    animationEasingUpdate: 'cubicOut',
    grid: { left: 42, right: 30, top: 44, bottom: 28 },
  })
}

const exportChartPng = () => {
  if (!chart) {
    ElMessage.warning('图表还未准备好')
    return
  }
  const dataUrl = chart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' })
  const link = document.createElement('a')
  link.href = dataUrl
  link.download = `weekly-report-${period.value}.png`
  link.click()
}

const exportReportPdf = () => {
  if (!report.value || !chart) {
    ElMessage.warning('暂无可导出内容')
    return
  }

  const pdf = new jsPDF('p', 'mm', 'a4')
  const chartImage = chart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' })
  const current = report.value

  const now = new Date()
  const exportedAt = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  const pageBottom = 284
  let pageNo = 1
  let y = 24

  const drawPageChrome = () => {
    pdf.setDrawColor(196, 214, 232)
    pdf.setLineWidth(0.3)
    pdf.line(10, 16, 200, 16)
    pdf.line(10, 287, 200, 287)

    pdf.setFont('helvetica', 'bold')
    pdf.setFontSize(10)
    pdf.setTextColor(48, 83, 118)
    pdf.text('Weekly Report Template', 12, 13)

    pdf.setFont('helvetica', 'normal')
    pdf.setFontSize(9)
    pdf.setTextColor(90, 110, 130)
    pdf.text(`Exported: ${exportedAt}`, 12, 290)
    pdf.text(`Page ${pageNo}`, 192, 290, { align: 'right' })
  }

  const ensureSpace = (need: number) => {
    if (y + need <= pageBottom) return
    pdf.addPage()
    pageNo += 1
    y = 24
    drawPageChrome()
  }

  const writeTitle = (title: string) => {
    ensureSpace(12)
    pdf.setFont('helvetica', 'bold')
    pdf.setFontSize(13)
    pdf.setTextColor(33, 68, 98)
    pdf.text(title, 14, y)
    y += 7
  }

  const writeParagraph = (text: string, indent = 14) => {
    const lines = pdf.splitTextToSize(text || '-', 182 - (indent - 14)) as string[]
    lines.forEach((line) => {
      ensureSpace(6)
      pdf.setFont('helvetica', 'normal')
      pdf.setFontSize(10.5)
      pdf.setTextColor(38, 48, 58)
      pdf.text(line, indent, y)
      y += 5.4
    })
  }

  drawPageChrome()

  pdf.setFillColor(235, 245, 255)
  pdf.roundedRect(14, y, 182, 36, 3, 3, 'F')
  pdf.setFillColor(209, 230, 252)
  pdf.roundedRect(152, y + 6, 38, 24, 2, 2, 'F')
  pdf.setFont('helvetica', 'bold')
  pdf.setFontSize(11)
  pdf.setTextColor(52, 96, 137)
  pdf.text('LOGO', 171, y + 20, { align: 'center' })

  pdf.setFont('helvetica', 'bold')
  pdf.setFontSize(19)
  pdf.setTextColor(30, 64, 94)
  pdf.text('Weekly Insight Report', 20, y + 14)

  pdf.setFont('helvetica', 'normal')
  pdf.setFontSize(10.5)
  pdf.setTextColor(66, 86, 106)
  pdf.text(`Period: ${current.startDate} ~ ${current.endDate}`, 20, y + 23)
  pdf.text(`Entries: ${current.totalLogs}   Hours: ${current.totalHours}`, 20, y + 30)

  y += 44

  writeTitle('Executive Summary')
  writeParagraph(current.summary)
  y += 4

  writeTitle('Chart Overview')
  ensureSpace(88)
  pdf.addImage(chartImage, 'PNG', 14, y, 182, 76)
  y += 82

  writeTitle('Highlights')
  current.highlights.forEach((item, index) => {
    writeParagraph(`${index + 1}. ${item}`)
  })
  y += 3

  writeTitle('Risks')
  current.risks.forEach((item, index) => {
    writeParagraph(`${index + 1}. ${item}`)
  })
  y += 3

  writeTitle('Next Plans')
  current.nextPlans.forEach((item, index) => {
    writeParagraph(`${index + 1}. ${item}`)
  })

  pdf.save(`weekly-report-${period.value}.pdf`)
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await getWeeklyReportApi(period.value)
    report.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '周报生成失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

watch(report, () => {
  void renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<template>
  <div class="report-page">
    <header class="header">
      <div>
        <h2>智能周报助手</h2>
        <p>自动汇总日志并提炼亮点、风险和下阶段计划。</p>
      </div>
      <div class="actions">
        <el-radio-group v-model="period" size="small" @change="load">
          <el-radio-button label="week">周报</el-radio-button>
          <el-radio-button label="month">月报</el-radio-button>
        </el-radio-group>
        <el-button type="primary" plain @click="exportChartPng">导出图表PNG</el-button>
        <el-button type="success" plain @click="exportReportPdf">导出周报PDF</el-button>
        <el-button @click="router.push('/')">返回工作台</el-button>
      </div>
    </header>

    <el-card v-loading="loading" class="summary-card">
      <template #header>总结</template>
      <div v-if="report" class="summary-content">
        <p class="meta">周期：{{ report.startDate }} ~ {{ report.endDate }} ｜ 日志数：{{ report.totalLogs }} ｜ 工时：{{ report.totalHours }}</p>
        <p class="summary-text">{{ report.summary }}</p>
      </div>
      <el-empty v-else description="暂无数据" />
    </el-card>

    <el-card class="chart-card" v-if="report">
      <template #header>周报结构概览</template>
      <div ref="chartRef" class="chart-box" />
    </el-card>

    <section class="grid" v-if="report">
      <el-card>
        <template #header>亮点</template>
        <ul>
          <li v-for="item in report.highlights" :key="item">{{ item }}</li>
        </ul>
      </el-card>
      <el-card>
        <template #header>风险提醒</template>
        <ul>
          <li v-for="item in report.risks" :key="item">{{ item }}</li>
        </ul>
      </el-card>
      <el-card>
        <template #header>下阶段计划</template>
        <ul>
          <li v-for="item in report.nextPlans" :key="item">{{ item }}</li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.report-page { padding: 24px; }
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
.header p { margin: 8px 0 0; color: #4f6b83; }
.actions { display: flex; gap: 10px; align-items: center; }
.summary-card { margin-top: 16px; }
.chart-card { margin-top: 16px; }
.chart-box {
  height: 300px;
  border-radius: 12px;
  background: rgba(244, 251, 255, 0.8);
}
.summary-content .meta { color: #57758b; }
.summary-text { font-size: 16px; margin-top: 8px; }
.grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; }
@media (max-width: 960px) { .grid { grid-template-columns: 1fr; } .actions { flex-wrap: wrap; } }
</style>
