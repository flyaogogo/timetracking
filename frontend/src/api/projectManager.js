import request from '@/utils/request'

/**
 * 项目经理专用API
 */

// 获取项目经理工作台数据
export const getDashboard = () => {
  return request({
    url: '/project-manager/dashboard',
    method: 'get'
  })
}

// 获取管理的项目列表
export const getManagedProjects = (params) => {
  return request({
    url: '/project-manager/projects',
    method: 'get',
    params
  })
}

// 获取待审批的工时记录
export const getPendingApprovals = (params) => {
  return request({
    url: '/project-manager/pending-approvals',
    method: 'get',
    params
  })
}

// 获取任务概览
export const getTasksOverview = (projectId) => {
  return request({
    url: '/project-manager/tasks-overview',
    method: 'get',
    params: { projectId }
  })
}

// 获取团队绩效统计
export const getTeamPerformance = (projectId, period) => {
  return request({
    url: '/project-manager/team-performance',
    method: 'get',
    params: { projectId, period }
  })
}

// 批量审批工时记录
export const batchApproveTimeEntries = (ids, comment) => {
  return request({
    url: '/project-manager/batch-approve',
    method: 'post',
    data: { ids, comment }
  })
}

// 获取项目权限摘要
export const getProjectPermissions = (projectId) => {
  return request({
    url: `/project-manager/permissions/${projectId}`,
    method: 'get'
  })
}

// 审批工时记录
export const approveTimeEntry = (entryId, comment) => {
  return request({
    url: `/time-entries/${entryId}/approve`,
    method: 'post',
    data: { comment }
  })
}

// 拒绝工时记录
export const rejectTimeEntry = (entryId, comment) => {
  return request({
    url: `/time-entries/${entryId}/reject`,
    method: 'post',
    data: { comment }
  })
}

export default {
  getDashboard,
  getManagedProjects,
  getPendingApprovals,
  getTasksOverview,
  getTeamPerformance,
  batchApproveTimeEntries,
  getProjectPermissions,
  approveTimeEntry,
  rejectTimeEntry
}