import request from '../utils/request'
import type { ApiResponse, GoalBoard, SprintGoalItem, SprintGoalSavePayload } from '../types/auth'

export function saveGoalApi(payload: SprintGoalSavePayload) {
  return request.post<ApiResponse<SprintGoalItem>>('/goals', payload)
}

export function listGoalsApi() {
  return request.get<ApiResponse<SprintGoalItem[]>>('/goals')
}

export function deleteGoalApi(id: number) {
  return request.delete<ApiResponse<null>>(`/goals/${id}`)
}

export function getGoalBoardApi(days = 14) {
  return request.get<ApiResponse<GoalBoard>>('/goals/board', { params: { days } })
}
