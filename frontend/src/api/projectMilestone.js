import request from '@/utils/request'

/**
 * 获取项目里程碑列表
 */
export function getProjectMilestones(projectId) {
  return request({
    url: `/project-milestones/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 获取项目里程碑统计
 */
export function getProjectMilestoneStatistics(projectId) {
  return request({
    url: `/project-milestones/project/${projectId}/statistics`,
    method: 'get'
  })
}

/**
 * 创建项目里程碑
 */
export function createProjectMilestone(data) {
  return request({
    url: '/project-milestones',
    method: 'post',
    data
  })
}

/**
 * 更新项目里程碑
 */
export function updateProjectMilestone(id, data) {
  return request({
    url: `/project-milestones/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除项目里程碑
 */
export function deleteProjectMilestone(id) {
  return request({
    url: `/project-milestones/${id}`,
    method: 'delete'
  })
}

/**
 * 批量保存项目里程碑
 */
export function batchSaveProjectMilestones(projectId, milestones) {
  return request({
    url: `/project-milestones/project/${projectId}/batch`,
    method: 'post',
    data: milestones
  })
}