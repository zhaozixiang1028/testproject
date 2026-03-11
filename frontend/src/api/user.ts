import request from '../utils/request'
import type { ApiResponse, CreateUserPayload, UserItem, UserRole } from '../types/auth'

export function listUsersApi() {
  return request.get<ApiResponse<UserItem[]>>('/users')
}

export function createUserApi(payload: CreateUserPayload) {
  return request.post<ApiResponse<UserItem>>('/users', payload)
}

export function updateUserRoleApi(id: number, role: UserRole) {
  return request.put<ApiResponse<UserItem>>(`/users/${id}/role`, { role })
}

export function updateUserStatusApi(id: number, status: number) {
  return request.put<ApiResponse<UserItem>>(`/users/${id}/status`, { status })
}

export function updateUserEmploymentApi(id: number, employedCompany?: string) {
  return request.put<ApiResponse<UserItem>>(`/users/${id}/employment`, { employedCompany })
}

export function resetUserPasswordApi(id: number, newPassword: string) {
  return request.put<ApiResponse<null>>(`/users/${id}/password`, { newPassword })
}
