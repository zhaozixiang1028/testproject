import request from '../utils/request'
import type { ApiResponse, LoginPayload, Profile, RegisterPayload, TokenResponse } from '../types/auth'

export function loginApi(payload: LoginPayload) {
  return request.post<ApiResponse<TokenResponse>>('/auth/login', payload)
}

export function refreshTokenApi(refreshToken: string) {
  return request.post<ApiResponse<TokenResponse>>('/auth/refresh', { refreshToken })
}

export function profileApi() {
  return request.get<ApiResponse<Profile>>('/auth/profile')
}

export function logoutApi() {
  return request.post<ApiResponse<null>>('/auth/logout')
}

export function registerApi(payload: RegisterPayload) {
  return request.post<ApiResponse<Profile>>('/auth/register', payload)
}

export function updateMyEmploymentApi(employedCompany?: string) {
  return request.put<ApiResponse<Profile>>('/auth/profile/employment', { employedCompany })
}

export function changeMyPasswordApi(oldPassword: string, newPassword: string) {
  return request.put<ApiResponse<null>>('/auth/profile/password', { oldPassword, newPassword })
}

