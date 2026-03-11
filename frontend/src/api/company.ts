import request from '../utils/request'
import type { ApiResponse } from '../types/auth'

export function listCompanyOptionsApi() {
  return request.get<ApiResponse<string[]>>('/companies/options')
}
