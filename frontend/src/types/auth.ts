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
  moodScore?: number
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
  moodScore?: number
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

export interface WeeklyReport {
  period: 'week' | 'month'
  startDate: string
  endDate: string
  totalLogs: number
  totalHours: string
  summary: string
  highlights: string[]
  risks: string[]
  nextPlans: string[]
}

export interface MoodPoint {
  date: string
  moodScore: number
}

export interface MoodTrend {
  days: number
  avgMood: string
  lowestMoodDay?: MoodPoint
  series: MoodPoint[]
  advice: string[]
}

export interface SkillItem {
  skill: string
  mentionCount: number
  relatedHours: string
}

export interface SkillPortrait {
  days: number
  topSkills: SkillItem[]
  growthSuggestions: string[]
}

export interface ReviewAlertItem {
  companyName: string
  recentCount: number
  currentAvg: string
  previousAvg: string
  dropRate: string
  negativeRate: string
  level: 'HIGH' | 'MEDIUM'
}

export interface ReviewAlertSummary {
  days: number
  totalWarnings: number
  warnings: ReviewAlertItem[]
}

export interface SprintGoalItem {
  id: number
  userId: number
  title: string
  description?: string
  relatedProject?: string
  targetHours: string
  completedHours: string
  completionRate: string
  status: 'OPEN' | 'DONE' | 'PAUSED'
  startDate: string
  endDate: string
  createdAt: string
  updatedAt: string
}

export interface SprintGoalSavePayload {
  id?: number
  title: string
  description?: string
  relatedProject?: string
  targetHours: number
  status?: 'OPEN' | 'DONE' | 'PAUSED'
  startDate: string
  endDate: string
}

export interface GoalBoard {
  days: number
  totalGoals: number
  completedGoals: number
  avgCompletionRate: string
  goals: SprintGoalItem[]
  highRiskGoals: SprintGoalItem[]
}

export interface RiskRadarItem {
  source: string
  level: 'LOW' | 'MEDIUM' | 'HIGH'
  title: string
  message: string
  metric: string
}

export interface RiskRadar {
  days: number
  overallLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  overallScore: number
  items: RiskRadarItem[]
}

export interface Retrospective {
  period: 'week' | 'month'
  startDate: string
  endDate: string
  theme: string
  summary: string
  whatWentWell: string[]
  toImprove: string[]
  nextActions: string[]
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

export type AiRole = 'user' | 'assistant' | 'system'

export interface AiMessage {
  id: string
  role: AiRole
  content: string
  createdAt: string
}

export interface AiSession {
  id: string
  title: string
  messages: AiMessage[]
  updatedAt: string
}

export interface AiChatRequestPayload {
  sessionId: string
  prompt: string
  messages: Array<{ role: AiRole; content: string }>
}

export interface AiConfigCheckResponse {
  keyConfigured: boolean
  baseUrl: string
  model: string
  providerReachable: boolean
  authPassed: boolean
  modelAvailable: boolean
  upstreamStatus?: number | null
  message: string
}
