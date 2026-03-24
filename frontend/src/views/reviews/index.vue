<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { listCompanyOptionsApi } from '../../api/company'
import { deleteReviewApi, listCompanyReviewsApi, listReviewAuditLogsApi, submitReviewApi, updateReviewApi, verifyReviewApi } from '../../api/review'
import { useUserStore } from '../../store/modules/user'
import type { CompanyReviewItem, CompanyReviewSubmitPayload, ReviewAuditLogItem } from '../../types/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const reviews = ref<CompanyReviewItem[]>([])
const editReviewId = ref<number | null>(null)

const isManager = computed(() => ['SUPER_ADMIN', 'ADMIN'].includes(userStore.profile?.role || ''))
const isEmployee = computed(() => userStore.profile?.role === 'EMPLOYEE')
const employedCompany = computed(() => userStore.profile?.employedCompany?.trim() || '')
const canSubmitReview = computed(() => !isEmployee.value || !!employedCompany.value)
const canEditReview = (row: CompanyReviewItem) => row.userId === userStore.profile?.id
const canDeleteReview = (row: CompanyReviewItem) => isManager.value || row.userId === userStore.profile?.id

const form = reactive<CompanyReviewSubmitPayload>({
  companyName: '',
  cultureScore: 4,
  teamScore: 4,
  growthScore: 4,
  salaryScore: 4,
  balanceScore: 4,
  anonymousMode: true,
  publicVisible: true,
  reviewContent: '',
})

const tableState = reactive({
  keyword: '',
  status: '',
  sortProp: 'createdAt',
  sortOrder: 'descending' as 'ascending' | 'descending',
  page: 1,
  pageSize: 10,
})

const auditDrawerVisible = ref(false)
const auditLoading = ref(false)
const auditLogs = ref<ReviewAuditLogItem[]>([])
const auditTitle = ref('审核记录')
const companyOptions = ref<string[]>([])

const loadReviews = async () => {
  loading.value = true
  try {
    const { data } = await listCompanyReviewsApi()
    reviews.value = data.data
    tableState.page = 1
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  if (!canSubmitReview.value) {
    ElMessage.warning('你当前未设置在职公司，无法提交评价')
    return
  }
  if (!form.companyName) {
    ElMessage.warning('请先输入公司名')
    return
  }
  form.publicVisible = true
  try {
    if (editReviewId.value) {
      await updateReviewApi(editReviewId.value, form)
      ElMessage.success('评价更新成功')
    } else {
      await submitReviewApi(form)
      ElMessage.success(isEmployee.value ? '评价提交成功（员工评价免审核）' : '评价提交成功，待审核')
    }
    editReviewId.value = null
    await loadReviews()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '提交失败')
  }
}

const editReview = (row: CompanyReviewItem) => {
  editReviewId.value = row.id
  form.companyName = row.companyName
  form.cultureScore = row.cultureScore
  form.teamScore = row.teamScore
  form.growthScore = row.growthScore
  form.salaryScore = row.salaryScore
  form.balanceScore = row.balanceScore
  form.anonymousMode = row.anonymousMode === 1
  form.publicVisible = true
  form.reviewContent = row.reviewContent || ''
}

const cancelEdit = () => {
  editReviewId.value = null
  form.companyName = employedCompany.value || ''
  form.cultureScore = 4
  form.teamScore = 4
  form.growthScore = 4
  form.salaryScore = 4
  form.balanceScore = 4
  form.anonymousMode = true
  form.publicVisible = true
  form.reviewContent = ''
}

const removeReview = async (row: CompanyReviewItem) => {
  await ElMessageBox.confirm('确认删除该评价吗？', '删除评价', { type: 'warning' })
  try {
    await deleteReviewApi(row.id)
    ElMessage.success('评价删除成功')
    await loadReviews()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

const verifyReview = async (row: CompanyReviewItem, status: 'APPROVED' | 'REJECTED') => {
  try {
    const prompt = await ElMessageBox.prompt('可选：填写审核备注', `${status === 'APPROVED' ? '通过' : '拒绝'}评价`, {
      inputPlaceholder: '例如：信息完整，评分合理',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: '',
    }).catch(() => null)

    if (!prompt) {
      return
    }

    await verifyReviewApi(row.id, status, prompt.value || undefined)
    ElMessage.success('审核完成')
    await loadReviews()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '审核失败')
  }
}

const openAuditLogs = async (row: CompanyReviewItem) => {
  auditTitle.value = `评价 #${row.id} 审核记录`
  auditDrawerVisible.value = true
  auditLoading.value = true
  try {
    const { data } = await listReviewAuditLogsApi(row.id)
    auditLogs.value = data.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '获取审核记录失败')
  } finally {
    auditLoading.value = false
  }
}

const filteredReviews = computed(() => {
  return reviews.value.filter((row) => {
    const keyword = tableState.keyword.trim().toLowerCase()
    const keywordMatch = !keyword
      || row.companyName.toLowerCase().includes(keyword)
      || (row.reviewContent || '').toLowerCase().includes(keyword)

    const statusMatch = !tableState.status || row.reviewStatus === tableState.status

    return keywordMatch && statusMatch
  })
})

const sortedReviews = computed(() => {
  const list = [...filteredReviews.value]
  const factor = tableState.sortOrder === 'ascending' ? 1 : -1

  list.sort((a, b) => {
    const av = (a as any)[tableState.sortProp]
    const bv = (b as any)[tableState.sortProp]

    if (av == null && bv == null) return 0
    if (av == null) return -1 * factor
    if (bv == null) return 1 * factor

    if (tableState.sortProp === 'reviewStatus') {
      return String(av).localeCompare(String(bv)) * factor
    }

    if (tableState.sortProp === 'score') {
      const scoreA = (a.cultureScore + a.teamScore + a.growthScore + a.salaryScore + a.balanceScore) / 5
      const scoreB = (b.cultureScore + b.teamScore + b.growthScore + b.salaryScore + b.balanceScore) / 5
      return (scoreA - scoreB) * factor
    }

    return String(av).localeCompare(String(bv)) * factor
  })

  return list
})

const pagedReviews = computed(() => {
  const start = (tableState.page - 1) * tableState.pageSize
  return sortedReviews.value.slice(start, start + tableState.pageSize)
})

const avgScore = computed(() => {
  if (!filteredReviews.value.length) return '0.00'
  const sum = filteredReviews.value.reduce((acc, row) => {
    const total = row.cultureScore + row.teamScore + row.growthScore + row.salaryScore + row.balanceScore
    return acc + total / 5
  }, 0)
  return (sum / filteredReviews.value.length).toFixed(2)
})

const handleSortChange = (sort: { prop: string; order: 'ascending' | 'descending' | null }) => {
  tableState.sortProp = sort.prop || 'createdAt'
  tableState.sortOrder = sort.order || 'descending'
}

const handlePageChange = (value: number) => {
  tableState.page = value
}

const handlePageSizeChange = (value: number) => {
  tableState.pageSize = value
}

const resetFilters = () => {
  tableState.keyword = ''
  tableState.status = ''
  tableState.page = 1
}

const loadCompanyOptions = async () => {
  try {
    const { data } = await listCompanyOptionsApi()
    companyOptions.value = data.data || []
  } catch {
    companyOptions.value = []
  }
}

onMounted(() => {
  form.companyName = employedCompany.value || ''
  loadCompanyOptions()
  loadReviews()
})
</script>

<template>
  <div class="reviews-page">
    <header class="header">
      <div>
        <h2>公司评价中心</h2>
        <p>多维评分、匿名模式、管理员审核</p>
      </div>
      <el-button @click="router.push('/')">返回工作台</el-button>
    </header>

    <section class="layout">
      <el-card>
        <template #header>{{ editReviewId ? '编辑评价' : '提交评价' }}</template>
        <el-form label-position="top">
          <el-form-item label="公司名称">
            <el-select
              v-model="form.companyName"
              :disabled="isEmployee && !!employedCompany"
              filterable
              allow-create
              default-first-option
              clearable
              :placeholder="isEmployee && !!employedCompany ? '员工评价固定为在职公司' : '请选择或输入公司名称'"
              style="width: 100%"
            >
              <el-option v-for="name in companyOptions" :key="name" :label="name" :value="name" />
            </el-select>
            <small v-if="isEmployee && !canSubmitReview" class="hint warn">未设置在职公司时，可以查看全部评价，但不能提交员工评价。</small>
            <small v-else-if="isEmployee && employedCompany" class="hint">当前在职公司：{{ employedCompany }}</small>
          </el-form-item>

          <el-form-item label="企业文化"><el-slider v-model="form.cultureScore" :min="1" :max="5" show-stops /></el-form-item>
          <el-form-item label="团队氛围"><el-slider v-model="form.teamScore" :min="1" :max="5" show-stops /></el-form-item>
          <el-form-item label="成长空间"><el-slider v-model="form.growthScore" :min="1" :max="5" show-stops /></el-form-item>
          <el-form-item label="薪酬福利"><el-slider v-model="form.salaryScore" :min="1" :max="5" show-stops /></el-form-item>
          <el-form-item label="工作平衡"><el-slider v-model="form.balanceScore" :min="1" :max="5" show-stops /></el-form-item>

          <el-form-item>
            <el-switch v-model="form.anonymousMode" inline-prompt active-text="匿名" inactive-text="实名" />
          </el-form-item>

          <el-form-item label="补充评价">
            <el-input v-model="form.reviewContent" type="textarea" :rows="4" placeholder="对公司文化、团队氛围、成长空间等补充描述" />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" :disabled="!canSubmitReview" @click="submitReview">{{ editReviewId ? '保存修改' : '提交评价' }}</el-button>
            <el-button v-if="editReviewId" @click="cancelEdit">取消编辑</el-button>
          </div>
        </el-form>
      </el-card>

      <el-card>
        <template #header>
          <div class="review-head">
            <span>评价列表</span>
            <span>筛选后平均分：{{ avgScore }}</span>
          </div>
        </template>

        <div class="filters">
          <el-input v-model="tableState.keyword" placeholder="关键词（公司名/评价内容）" clearable />
          <el-select v-model="tableState.status" placeholder="审核状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
          <el-button @click="resetFilters">重置筛选</el-button>
        </div>

        <el-table :data="pagedReviews" stripe v-loading="loading" @sort-change="handleSortChange">
          <el-table-column prop="companyName" label="公司" min-width="140" />
          <el-table-column prop="score" label="总评" width="90" sortable="custom">
            <template #default="scope">
              {{ ((scope.row.cultureScore + scope.row.teamScore + scope.row.growthScore + scope.row.salaryScore + scope.row.balanceScore) / 5).toFixed(1) }}
            </template>
          </el-table-column>
          <el-table-column prop="reviewStatus" label="状态" width="110" sortable="custom">
            <template #default="scope">
              <el-tag :type="scope.row.reviewStatus === 'APPROVED' ? 'success' : scope.row.reviewStatus === 'REJECTED' ? 'danger' : 'info'">
                {{ scope.row.reviewStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="评价人" width="140" />
          <el-table-column prop="createdAt" label="提交时间" min-width="160" sortable="custom" />
          <el-table-column prop="reviewContent" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="scope">
              <el-button v-if="canEditReview(scope.row)" link type="primary" @click="editReview(scope.row)">编辑</el-button>
              <el-button v-if="isManager && scope.row.reviewStatus === 'PENDING'" link type="success" @click="verifyReview(scope.row, 'APPROVED')">通过</el-button>
              <el-button v-if="isManager && scope.row.reviewStatus === 'PENDING'" link type="danger" @click="verifyReview(scope.row, 'REJECTED')">拒绝</el-button>
              <el-button v-if="isManager" link type="primary" @click="openAuditLogs(scope.row)">审核记录</el-button>
              <el-button v-if="canDeleteReview(scope.row)" link type="danger" @click="removeReview(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            :current-page="tableState.page"
            :page-size="tableState.pageSize"
            :total="sortedReviews.length"
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 50]"
            @update:current-page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </div>
      </el-card>
    </section>

    <el-drawer v-model="auditDrawerVisible" :title="auditTitle" size="45%">
      <div v-loading="auditLoading" class="timeline-wrap">
        <el-empty v-if="!auditLogs.length && !auditLoading" description="暂无审核记录" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="item in auditLogs"
            :key="item.id"
            :timestamp="item.createdAt"
            :type="item.newStatus === 'APPROVED' ? 'success' : item.newStatus === 'REJECTED' ? 'danger' : 'primary'"
          >
            <div class="timeline-card">
              <div class="timeline-line">
                <strong>{{ item.operatorUsername }}</strong>
                <span class="status-chip">{{ item.oldStatus }} -> {{ item.newStatus }}</span>
              </div>
              <p>{{ item.remark || '无备注' }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.reviews-page {
  padding: 24px;
  animation: reveal-up 0.52s ease;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-soft);
  position: relative;
  overflow: hidden;
}

.header::after {
  content: "";
  position: absolute;
  inset: 0;
  background: var(--theme-reviews);
  opacity: 0.45;
  pointer-events: none;
}

.header > * {
  position: relative;
  z-index: 1;
}

.header p {
  margin: 8px 0 0;
  color: #4f6980;
}

.layout {
  margin-top: 16px;
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 16px;
  animation: reveal-up 0.6s ease;
}

.layout :deep(.el-card) {
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 18px 32px rgba(19, 46, 72, 0.14);
}

.layout :deep(.el-card__header) {
  background: linear-gradient(90deg, rgba(255, 122, 24, 0.22), rgba(0, 166, 251, 0.2));
  border-bottom: 1px solid rgba(255, 255, 255, 0.72);
  font-weight: 700;
}

.review-head {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.form-actions {
  display: flex;
  gap: 8px;
}

.hint {
  color: #4f6b81;
  font-size: 12px;
}

.warn {
  color: #c45656;
}

.timeline-wrap {
  min-height: 220px;
}

.timeline-card {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(61, 102, 137, 0.16);
  background: linear-gradient(140deg, #f4fbff 0%, #fdf7ff 100%);
}

.timeline-line {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.timeline-card p {
  margin: 8px 0 0;
  color: #4f6b81;
}

.status-chip {
  color: #2a4255;
  font-size: 12px;
}

.reviews-page :deep(.el-button--primary) {
  border: none;
  background: var(--brand-berry);
}

.reviews-page :deep(.el-input__wrapper),
.reviews-page :deep(.el-select__wrapper),
.reviews-page :deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(47, 83, 117, 0.14) inset;
}

.reviews-page :deep(.el-table th.el-table__cell) {
  background: #f4f9ff;
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
