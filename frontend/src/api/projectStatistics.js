import request from '@/utils/request'

// 获取项目完整统计分析
export const getProjectStatistics = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}`,
    method: 'get'
  })
}

// 获取项目进度统计
export const getProgressStats = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}/progress`,
    method: 'get'
  })
}

// 获取项目财务统计
export const getFinancialStats = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}/financial`,
    method: 'get'
  })
}

// 获取项目工期统计
export const getScheduleStats = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}/schedule`,
    method: 'get'
  })
}

// 获取项目团队统计
export const getTeamStats = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}/team`,
    method: 'get'
  })
}

// 获取项目质量统计
export const getQualityStats = (projectId) => {
  return request({
    url: `/project-statistics/${projectId}/quality`,
    method: 'get'
  })
}