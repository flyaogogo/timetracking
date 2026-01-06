// 权限配置
export const ROLES = {
  ADMIN: 'ADMIN',
  PROJECT_MANAGER: 'PROJECT_MANAGER', 
  PRODUCT_MANAGER: 'PRODUCT_MANAGER',
  DEPARTMENT_MANAGER: 'DEPARTMENT_MANAGER',
  DEVELOPER: 'DEVELOPER',
  TESTER: 'TESTER'
}

// 菜单权限配置
export const MENU_PERMISSIONS = {
  dashboard: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  projectManagerDashboard: [ROLES.ADMIN, ROLES.PROJECT_MANAGER],
  projects: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  projectCosts: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  projectMilestones: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  projectPermissions: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  projectReports: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  tasks: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  timeEntries: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  approvals: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  timeTrackingStatistics: [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  users: [ROLES.ADMIN, ROLES.DEPARTMENT_MANAGER],
  organizations: [ROLES.ADMIN],
  test: [ROLES.ADMIN] // 测试页面仅管理员可见
}

// 页面权限配置
export const PAGE_PERMISSIONS = {
  '/dashboard': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  '/project-manager': [ROLES.ADMIN, ROLES.PROJECT_MANAGER],
  '/projects': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  '/project-permissions': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  '/project-reports': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  '/tasks': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  '/time-entries': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.PRODUCT_MANAGER, ROLES.DEPARTMENT_MANAGER, ROLES.DEVELOPER, ROLES.TESTER],
  '/approvals': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  '/time-tracking-statistics': [ROLES.ADMIN, ROLES.PROJECT_MANAGER, ROLES.DEPARTMENT_MANAGER],
  '/users': [ROLES.ADMIN, ROLES.DEPARTMENT_MANAGER],
  '/organizations': [ROLES.ADMIN],
  '/test': [ROLES.ADMIN] // 测试页面仅管理员可访问
}

// 检查菜单权限
export function hasMenuPermission(menuKey, userRole, additionalCheck = null) {
  if (!userRole || !menuKey) return false
  const permissions = MENU_PERMISSIONS[menuKey]
  const hasRolePermission = permissions && permissions.includes(userRole)
  
  // 如果有额外的权限检查函数，也要通过
  if (additionalCheck && typeof additionalCheck === 'function') {
    return hasRolePermission || additionalCheck()
  }
  
  return hasRolePermission
}

// 检查页面权限
export function hasPagePermission(path, userRole) {
  if (!userRole || !path) return false
  const permissions = PAGE_PERMISSIONS[path]
  return permissions && permissions.includes(userRole)
}

// 获取用户可访问的菜单
export function getAccessibleMenus(userRole) {
  if (!userRole) return []
  const menus = []
  
  Object.keys(MENU_PERMISSIONS).forEach(menuKey => {
    if (hasMenuPermission(menuKey, userRole)) {
      menus.push(menuKey)
    }
  })
  
  return menus
}

// 检查是否为项目级别的项目经理
export async function isProjectLevelManager() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return false
    
    const response = await fetch('http://localhost:8080/api/project-manager/check-permission', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.code === 200) {
        // 只有真正担任项目经理的用户才返回true
        return result.data.isProjectManager && result.data.managedProjectsCount > 0
      }
    }
    return false
  } catch (error) {
    console.error('检查项目级别项目经理权限失败:', error)
    return false
  }
}

// 检查项目经理工作台权限（只检查项目级别，不再检查全局角色）
export async function hasProjectManagerDashboardPermission(userRole) {
  // 只检查项目级别权限，不再基于全局角色给予权限
  // 这确保只有真正被设置为项目经理的用户才能看到项目经理工作台
  return await isProjectLevelManager()
}