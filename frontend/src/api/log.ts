import request from '../utils/request'
import type { ApiResponse, WorkLogItem, WorkLogSavePayload, WorkLogStatistics } from '../types/auth'

export function saveDailyLogApi(payload: WorkLogSavePayload) {
  return request.post<ApiResponse<WorkLogItem>>('/logs/daily', payload)
}

export function listLogsApi(params?: { startDate?: string; endDate?: string }) {
  return request.get<ApiResponse<WorkLogItem[]>>('/logs', { params })
}

export function getLogDetailApi(id: number) {
  return request.get<ApiResponse<WorkLogItem>>(`/logs/${id}`)
}

export function deleteLogApi(id: number) {
  return request.delete<ApiResponse<null>>(`/logs/${id}`)
}

export function getLogStatisticsApi(period: 'day' | 'week' | 'month') {
  return request.get<ApiResponse<WorkLogStatistics>>('/logs/statistics', { params: { period } })
}

export function exportLogsApi(params?: { startDate?: string; endDate?: string }) {
  return request.get('/logs/export', { params, responseType: 'blob' })
}
