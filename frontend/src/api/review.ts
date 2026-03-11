import request from '../utils/request'
import type { ApiResponse, CompanyReviewItem, CompanyReviewSubmitPayload, ReviewAuditLogItem } from '../types/auth'

export function submitReviewApi(payload: CompanyReviewSubmitPayload) {
  return request.post<ApiResponse<CompanyReviewItem>>('/reviews/submit', payload)
}

export function updateReviewApi(id: number, payload: CompanyReviewSubmitPayload) {
  return request.put<ApiResponse<CompanyReviewItem>>(`/reviews/${id}`, payload)
}

export function listCompanyReviewsApi(companyName?: string) {
  return request.get<ApiResponse<CompanyReviewItem[]>>('/reviews', {
    params: companyName ? { companyName } : undefined,
  })
}

export function verifyReviewApi(id: number, reviewStatus: 'APPROVED' | 'REJECTED', remark?: string) {
  return request.put<ApiResponse<CompanyReviewItem>>(`/reviews/verify/${id}`, { reviewStatus, remark })
}

export function listReviewAuditLogsApi(id: number) {
  return request.get<ApiResponse<ReviewAuditLogItem[]>>(`/reviews/${id}/audit-logs`)
}

export function deleteReviewApi(id: number) {
  return request.delete<ApiResponse<null>>(`/reviews/${id}`)
}
