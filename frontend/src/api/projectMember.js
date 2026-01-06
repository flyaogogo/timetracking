import request from '@/utils/request'

/**
 * 获取项目成员列表
 */
export function getProjectMembers(projectId) {
  return request({
    url: `/project-members/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 获取项目成员统计
 */
export function getProjectMemberStatistics(projectId) {
  return request({
    url: `/project-members/project/${projectId}/statistics`,
    method: 'get'
  })
}

/**
 * 添加项目成员
 */
export function addProjectMember(data) {
  return request({
    url: '/project-members',
    method: 'post',
    data
  })
}

/**
 * 更新项目成员
 */
export function updateProjectMember(id, data) {
  return request({
    url: `/project-members/${id}`,
    method: 'put',
    data
  })
}

/**
 * 移除项目成员
 */
export function removeProjectMember(id) {
  return request({
    url: `/project-members/${id}`,
    method: 'delete'
  })
}

/**
 * 批量添加项目成员
 */
export function batchAddProjectMembers(data) {
  return request({
    url: '/project-members/batch',
    method: 'post',
    data
  })
}