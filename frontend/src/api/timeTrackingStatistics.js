import request from '@/utils/request'

/**
 * 工时统计API
 */

// 更新任务的实际工时和进度
export function updateTaskActualHours(taskId) {
  return request({
    url: `/time-tracking-statistics/tasks/${taskId}/update-hours`,
    method: 'post'
  })
}

// 更新项目的实际工时汇总
export function updateProjectActualHours(projectId) {
  return request({
    url: `/time-tracking-statistics/projects/${projectId}/update-hours`,
    method: 'post'
  })
}

// 批量更新所有任务的实际工时和进度
export function updateAllTasksActualHours() {
  return request({
    url: '/time-tracking-statistics/tasks/update-all-hours',
    method: 'post'
  })
}

// 批量更新所有项目的实际工时汇总
export function updateAllProjectsActualHours() {
  return request({
    url: '/time-tracking-statistics/projects/update-all-hours',
    method: 'post'
  })
}

// 获取任务工时统计详情
export function getTaskTimeStatistics(taskId) {
  return request({
    url: `/time-tracking-statistics/tasks/${taskId}/statistics`,
    method: 'get'
  })
}

// 获取项目工时统计详情
export function getProjectTimeStatistics(projectId) {
  return request({
    url: `/time-tracking-statistics/projects/${projectId}/statistics`,
    method: 'get'
  })
}