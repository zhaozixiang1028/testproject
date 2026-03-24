import request from '../utils/request'
import type {
  ApiResponse,
  MoodTrend,
  Retrospective,
  ReviewAlertSummary,
  RiskRadar,
  SkillPortrait,
  WeeklyReport,
} from '../types/auth'

export function getWeeklyReportApi(period: 'week' | 'month' = 'week') {
  return request.get<ApiResponse<WeeklyReport>>('/insights/weekly-report', { params: { period } })
}

export function getMoodTrendApi(days = 14) {
  return request.get<ApiResponse<MoodTrend>>('/insights/mood-trend', { params: { days } })
}

export function getSkillPortraitApi(days = 30) {
  return request.get<ApiResponse<SkillPortrait>>('/insights/skills', { params: { days } })
}

export function getReviewAlertsApi(days = 14) {
  return request.get<ApiResponse<ReviewAlertSummary>>('/insights/review-alerts', { params: { days } })
}

export function getRiskRadarApi(days = 14) {
  return request.get<ApiResponse<RiskRadar>>('/insights/risk-radar', { params: { days } })
}

export function getRetrospectiveApi(period: 'week' | 'month' = 'week') {
  return request.get<ApiResponse<Retrospective>>('/insights/retrospective', { params: { period } })
}
