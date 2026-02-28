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

/**
 * 获取用户指定月份的工时统计
 */
export function getUserMonthlyStats(userId, year, month) {
  return request({
    url: `/dashboard/user/${userId}/monthly-stats`,
    method: 'get',
    params: {
      year,
      month
    }
  })
}

/**
 * 获取用户指定年度的工时统计
 */
export function getUserYearlyStats(userId, year) {
  return request({
    url: `/dashboard/user/${userId}/yearly-stats`,
    method: 'get',
    params: {
      year
    }
  })
}

/**
 * 获取用户月度工时趋势（最近12个月）
 */
export function getUserMonthlyTrend(userId) {
  return request({
    url: `/dashboard/user/${userId}/monthly-trend`,
    method: 'get'
  })
}

/**
 * 获取用户项目工时分布
 */
export function getUserProjectHoursDistribution(userId, year, month) {
  return request({
    url: `/dashboard/user/${userId}/project-distribution`,
    method: 'get',
    params: {
      year,
      month
    }
  })
}

/**
 * 获取所有用户的月度工时统计
 */
export function getAllUsersMonthlyStats(year, month) {
  return request({
    url: '/dashboard/all-users/monthly-stats',
    method: 'get',
    params: {
      year,
      month
    }
  })
}

/**
 * 获取所有用户的年度工时统计
 */
export function getAllUsersYearlyStats(year) {
  return request({
    url: '/dashboard/all-users/yearly-stats',
    method: 'get',
    params: {
      year
    }
  })
}

/**
 * 获取所有用户的季度工时统计
 */
export function getAllUsersQuarterlyStats(year, quarter) {
  return request({
    url: '/dashboard/all-users/quarterly-stats',
    method: 'get',
    params: {
      year,
      quarter
    }
  })
}