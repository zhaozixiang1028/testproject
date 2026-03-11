export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'EMPLOYEE'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  nickname: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  role: UserRole
}

export interface Profile {
  id: number
  username: string
  nickname: string
  role: UserRole
  employedCompany?: string | null
}

export interface RoleOption {
  code: UserRole
  label: string
  description: string
}

export interface UserItem {
  id: number
  username: string
  nickname: string
  employedCompany?: string | null
  role: UserRole
  status: number
  createTime: string
  updateTime: string
}

export interface CreateUserPayload {
  username: string
  password: string
  nickname: string
  role: UserRole
  employedCompany?: string
}

export interface WorkLogItem {
  id: number
  userId: number
  workDate: string
  title: string
  content?: string
  projectName?: string
  taskType?: string
  priorityLevel?: string
  startTime?: string
  endTime?: string
  workHours: number
  tags?: string
  attachmentUrls?: string
  createdAt: string
  updatedAt: string
}

export interface WorkLogSavePayload {
  id?: number
  workDate: string
  title: string
  content?: string
  projectName?: string
  taskType?: string
  priorityLevel?: string
  startTime?: string
  endTime?: string
  tags?: string
  attachmentUrls?: string
}

export interface WorkLogStatistics {
  period: string
  totalHours: number
  totalLogs: number
  projectHours: Array<{ projectName: string; hours: number }>
}

export interface CompanyReviewItem {
  id: number
  userId: number | null
  username: string
  companyName: string
  cultureScore: number
  teamScore: number
  growthScore: number
  salaryScore: number
  balanceScore: number
  anonymousMode: number
  publicVisible: number
  reviewContent?: string
  reviewStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
}

export interface CompanyReviewSubmitPayload {
  companyName: string
  cultureScore: number
  teamScore: number
  growthScore: number
  salaryScore: number
  balanceScore: number
  anonymousMode: boolean
  publicVisible: boolean
  reviewContent?: string
}

export interface ReviewAuditLogItem {
  id: number
  reviewId: number
  operatorUserId: number
  operatorUsername: string
  oldStatus: string
  newStatus: string
  remark?: string
  createdAt: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
