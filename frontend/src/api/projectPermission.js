import request from '@/utils/request'

/**
 * 项目权限管理API
 */

// 获取项目权限列表
export const getProjectPermissions = (projectId, params) => {
  return request({
    url: `/project-permissions/project/${projectId}`,
    method: 'get',
    params
  })
}

// 获取用户权限列表
export const getUserPermissions = (userId, params) => {
  return request({
    url: `/project-permissions/user/${userId}`,
    method: 'get',
    params
  })
}

// 设置用户为项目经理
export const setProjectManager = (projectId, userId, expiryDate = null) => {
  return request({
    url: '/project-permissions/set-project-manager',
    method: 'post',
    data: { projectId, userId, expiryDate }
  })
}

// 设置用户为技术负责人
export const setTechLeader = (projectId, userId, expiryDate = null) => {
  return request({
    url: '/project-permissions/set-tech-leader',
    method: 'post',
    data: { projectId, userId, expiryDate }
  })
}

// 撤销项目经理权限
export const revokeProjectManager = (projectId, userId) => {
  return request({
    url: '/project-permissions/revoke-project-manager',
    method: 'post',
    data: { projectId, userId }
  })
}

// 撤销技术负责人权限
export const revokeTechLeader = (projectId, userId) => {
  return request({
    url: '/project-permissions/revoke-tech-leader',
    method: 'post',
    data: { projectId, userId }
  })
}

// 授予特定权限
export const grantPermission = (projectId, userId, permissionType, expiryDate = null) => {
  return request({
    url: '/project-permissions/grant-permission',
    method: 'post',
    data: { projectId, userId, permissionType, expiryDate }
  })
}

// 批量授予权限
export const grantMultiplePermissions = (projectId, userId, permissions, expiryDate = null) => {
  return request({
    url: '/project-permissions/grant-multiple-permissions',
    method: 'post',
    data: { projectId, userId, permissions, expiryDate }
  })
}

// 撤销特定权限
export const revokePermission = (projectId, userId, permissionType) => {
  return request({
    url: '/project-permissions/revoke-permission',
    method: 'post',
    data: { projectId, userId, permissionType }
  })
}

// 获取用户在项目中的权限摘要
export const getPermissionSummary = (projectId, userId = null) => {
  return request({
    url: `/project-permissions/summary/${projectId}`,
    method: 'get',
    params: userId ? { userId } : {}
  })
}

// 获取即将过期的权限
export const getExpiringPermissions = (days = 7) => {
  return request({
    url: '/project-permissions/expiring',
    method: 'get',
    params: { days }
  })
}

// 清理过期权限
export const cleanupExpiredPermissions = () => {
  return request({
    url: '/project-permissions/cleanup-expired',
    method: 'post'
  })
}

// 复制权限配置
export const copyPermissions = (sourceUserId, targetUserId, projectId) => {
  return request({
    url: '/project-permissions/copy-permissions',
    method: 'post',
    data: { sourceUserId, targetUserId, projectId }
  })
}

// 更新权限过期时间
export const updatePermissionExpiry = (permissionId, expiryDate) => {
  return request({
    url: `/project-permissions/update-expiry/${permissionId}`,
    method: 'put',
    data: { expiryDate }
  })
}

// 权限类型常量
export const PERMISSION_TYPES = {
  PROJECT_MANAGEMENT: 'PROJECT_MANAGEMENT',
  TASK_MANAGEMENT: 'TASK_MANAGEMENT',
  TIMESHEET_APPROVAL: 'TIMESHEET_APPROVAL',
  REPORT_VIEW: 'REPORT_VIEW',
  COST_MANAGEMENT: 'COST_MANAGEMENT',
  MILESTONE_MANAGEMENT: 'MILESTONE_MANAGEMENT',
  TEAM_MANAGEMENT: 'TEAM_MANAGEMENT'
}

// 权限类型描述
export const PERMISSION_DESCRIPTIONS = {
  PROJECT_MANAGEMENT: '项目管理',
  TASK_MANAGEMENT: '任务管理',
  TIMESHEET_APPROVAL: '工时审核',
  REPORT_VIEW: '报表查看',
  COST_MANAGEMENT: '成本管理',
  MILESTONE_MANAGEMENT: '里程碑管理',
  TEAM_MANAGEMENT: '团队管理'
}

// 导出API对象，用于组件中的统一调用
export const projectPermissionApi = {
  getProjectPermissions,
  getUserPermissions,
  setProjectManager,
  setTechLeader,
  revokeProjectManager,
  revokeTechLeader,
  grantPermission,
  grantMultiplePermissions,
  revokePermission,
  getPermissionSummary,
  getExpiringPermissions,
  cleanupExpiredPermissions,
  copyPermissions,
  updatePermissionExpiry
}