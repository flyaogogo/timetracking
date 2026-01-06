import request from '@/utils/request'

/**
 * 获取用户工作台数据
 */
export function getUserDashboardData(userId) {
  return request({
    url: `/dashboard/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户参与的项目列表
 */
export function getUserProjects(userId) {
  return request({
    url: `/dashboard/user/${userId}/projects`,
    method: 'get'
  })
}

/**
 * 获取用户的任务统计
 */
export function getUserTaskStats(userId) {
  return request({
    url: `/dashboard/user/${userId}/task-stats`,
    method: 'get'
  })
}

/**
 * 获取用户的工时统计
 */
export function getUserTimeStats(userId) {
  return request({
    url: `/dashboard/user/${userId}/time-stats`,
    method: 'get'
  })
}