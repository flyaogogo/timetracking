import request from '@/utils/request'

/**
 * 获取组织树结构
 */
export function getOrganizationTree() {
  return request({
    url: '/organizations/tree',
    method: 'get'
  })
}

/**
 * 获取所有组织的平铺列表（用于下拉选择）
 */
export function getAllOrganizationsFlat() {
  return request({
    url: '/organizations/flat',
    method: 'get'
  })
}

/**
 * 获取用户所属组织
 */
export function getUserOrganizations(userId) {
  return request({
    url: `/organizations/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取组织统计信息
 */
export function getOrganizationStatistics(orgId) {
  return request({
    url: `/organizations/${orgId}/statistics`,
    method: 'get'
  })
}

/**
 * 创建组织
 */
export function createOrganization(data) {
  return request({
    url: '/organizations',
    method: 'post',
    data
  })
}

/**
 * 更新组织
 */
export function updateOrganization(id, data) {
  return request({
    url: `/organizations/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除组织
 */
export function deleteOrganization(id) {
  return request({
    url: `/organizations/${id}`,
    method: 'delete'
  })
}

/**
 * 获取组织成员列表
 */
export function getOrganizationMembers(orgId, params) {
  return request({
    url: `/organizations/${orgId}/members`,
    method: 'get',
    params
  })
}

/**
 * 添加组织成员
 */
export function addOrganizationMember(orgId, data) {
  return request({
    url: `/organizations/${orgId}/members`,
    method: 'post',
    data
  })
}