import request from '@/utils/request'

/**
 * 获取项目成本列表
 */
export function getProjectCosts(params) {
  return request({
    url: `/project-costs/project/${params.projectId}`,
    method: 'get',
    params: {
      costType: params.costType,
      current: params.current,
      size: params.size
    }
  })
}

/**
 * 获取项目成本统计
 */
export function getProjectCostStatistics(projectId) {
  return request({
    url: `/project-costs/project/${projectId}/statistics`,
    method: 'get'
  })
}

/**
 * 创建项目成本
 */
export function createProjectCost(data) {
  return request({
    url: '/project-costs',
    method: 'post',
    data
  })
}

/**
 * 更新项目成本
 */
export function updateProjectCost(id, data) {
  return request({
    url: `/project-costs/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除项目成本
 */
export function deleteProjectCost(id) {
  return request({
    url: `/project-costs/${id}`,
    method: 'delete'
  })
}

/**
 * 审批项目成本
 */
export function approveProjectCost(id, status) {
  return request({
    url: `/project-costs/${id}/approve`,
    method: 'post',
    params: { status }
  })
}