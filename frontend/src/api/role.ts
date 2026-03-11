import request from '../utils/request'
import type { ApiResponse, RoleOption } from '../types/auth'

export function listRolesApi() {
  return request.get<ApiResponse<RoleOption[]>>('/roles')
}
