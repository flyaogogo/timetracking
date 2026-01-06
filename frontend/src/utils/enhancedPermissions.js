import { useUserStore } from '@/stores/user'
import { projectPermissionApi } from '@/api/projectPermission'

/**
 * 增强权限工具类 - 支持项目级权限控制
 */
export class EnhancedPermissionUtil {
  
  /**
   * 权限类型枚举
   */
  static PERMISSION_TYPES = {
    PROJECT_MANAGEMENT: 'PROJECT_MANAGEMENT',
    TASK_MANAGEMENT: 'TASK_MANAGEMENT',
    TIMESHEET_APPROVAL: 'TIMESHEET_APPROVAL',
    REPORT_VIEW: 'REPORT_VIEW',
    COST_MANAGEMENT: 'COST_MANAGEMENT',
    MILESTONE_MANAGEMENT: 'MILESTONE_MANAGEMENT',
    TEAM_MANAGEMENT: 'TEAM_MANAGEMENT'
  }

  /**
   * 全局角色枚举
   */
  static GLOBAL_ROLES = {
    ADMIN: 'ADMIN',
    PROJECT_MANAGER: 'PROJECT_MANAGER',
    PRODUCT_MANAGER: 'PRODUCT_MANAGER',
    DEPARTMENT_MANAGER: 'DEPARTMENT_MANAGER',
    DEVELOPER: 'DEVELOPER',
    TESTER: 'TESTER'
  }

  /**
   * 权限缓存
   */
  static permissionCache = new Map()
  static cacheExpiry = new Map()
  static CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

  /**
   * 检查用户是否有全局管理员权限
   */
  static isAdmin() {
    const userStore = useUserStore()
    return userStore.user?.role === this.GLOBAL_ROLES.ADMIN
  }

  /**
   * 检查用户是否有全局项目经理权限
   */
  static isGlobalProjectManager() {
    const userStore = useUserStore()
    return userStore.user?.role === this.GLOBAL_ROLES.PROJECT_MANAGER
  }

  /**
   * 检查用户在指定项目中是否有特定权限
   */
  static async hasProjectPermission(projectId, permissionType) {
    if (!projectId || !permissionType) {
      return false
    }

    // 1. 检查全局管理员权限
    if (this.isAdmin()) {
      return true
    }

    // 2. 检查缓存
    const cacheKey = `${projectId}_${permissionType}`
    if (this.isCacheValid(cacheKey)) {
      return this.permissionCache.get(cacheKey)
    }

    try {
      // 3. 从服务器获取权限信息
      const response = await projectPermissionApi.getPermissionSummary(projectId)
      if (response.code === 200) {
        const summary = response.data
        
        // 缓存权限信息
        this.cachePermissionSummary(projectId, summary)
        
        // 检查特定权限
        return this.checkPermissionFromSummary(summary, permissionType)
      }
    } catch (error) {
      console.error('检查项目权限失败:', error)
    }

    return false
  }

  /**
   * 检查用户是否为项目经理（全局角色或项目内角色）
   */
  static async isProjectManager(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    try {
      const response = await projectPermissionApi.getPermissionSummary(projectId)
      if (response.code === 200) {
        return response.data.isProjectManager || false
      }
    } catch (error) {
      console.error('检查项目经理权限失败:', error)
    }

    return false
  }

  /**
   * 检查用户是否为技术负责人
   */
  static async isTechLeader(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局管理员权限
    if (this.isAdmin()) {
      return true
    }

    // 2. 检查项目内权限
    try {
      const response = await projectPermissionApi.getPermissionSummary(projectId)
      if (response.code === 200) {
        return response.data.isTechLeader || false
      }
    } catch (error) {
      console.error('检查技术负责人权限失败:', error)
    }

    return false
  }

  /**
   * 检查用户是否可以审核工时
   */
  static async canApproveTimesheet(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.TIMESHEET_APPROVAL) ||
           await this.isProjectManager(projectId)
  }

  /**
   * 检查用户是否可以管理任务
   */
  static async canManageTasks(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.TASK_MANAGEMENT) ||
           await this.isProjectManager(projectId) ||
           await this.isTechLeader(projectId)
  }

  /**
   * 检查用户是否可以查看报表
   */
  static async canViewReports(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.REPORT_VIEW) ||
           await this.isProjectManager(projectId) ||
           await this.isTechLeader(projectId)
  }

  /**
   * 检查用户是否可以管理成本
   */
  static async canManageCosts(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.COST_MANAGEMENT) ||
           await this.isProjectManager(projectId)
  }

  /**
   * 检查用户是否可以管理里程碑
   */
  static async canManageMilestones(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.MILESTONE_MANAGEMENT) ||
           await this.isProjectManager(projectId) ||
           await this.isTechLeader(projectId)
  }

  /**
   * 检查用户是否可以管理团队
   */
  static async canManageTeam(projectId) {
    if (!projectId) {
      return false
    }

    // 1. 检查全局权限
    if (this.isAdmin() || this.isGlobalProjectManager()) {
      return true
    }

    // 2. 检查项目内权限
    return await this.hasProjectPermission(projectId, this.PERMISSION_TYPES.TEAM_MANAGEMENT) ||
           await this.isProjectManager(projectId)
  }

  /**
   * 检查用户是否可以授予权限
   */
  static async canGrantPermission(projectId) {
    if (!projectId) {
      return false
    }

    return this.isAdmin() || await this.isProjectManager(projectId)
  }

  /**
   * 获取用户在项目中的权限摘要
   */
  static async getUserProjectPermissions(projectId) {
    if (!projectId) {
      return null
    }

    try {
      const response = await projectPermissionApi.getPermissionSummary(projectId)
      if (response.code === 200) {
        return response.data
      }
    } catch (error) {
      console.error('获取用户项目权限失败:', error)
    }

    return null
  }

  /**
   * 获取用户可访问的项目列表（基于权限过滤）
   */
  static async getAccessibleProjects(allProjects) {
    if (this.isAdmin()) {
      return allProjects // 管理员可以访问所有项目
    }

    const accessibleProjects = []
    
    for (const project of allProjects) {
      try {
        const summary = await this.getUserProjectPermissions(project.id)
        if (summary && (summary.isProjectManager || summary.isTechLeader || summary.permissions.length > 0)) {
          accessibleProjects.push(project)
        }
      } catch (error) {
        console.error(`检查项目 ${project.id} 权限失败:`, error)
      }
    }

    return accessibleProjects
  }

  /**
   * 检查缓存是否有效
   */
  static isCacheValid(key) {
    const expiry = this.cacheExpiry.get(key)
    if (!expiry || Date.now() > expiry) {
      this.permissionCache.delete(key)
      this.cacheExpiry.delete(key)
      return false
    }
    return this.permissionCache.has(key)
  }

  /**
   * 缓存权限摘要信息
   */
  static cachePermissionSummary(projectId, summary) {
    const expiry = Date.now() + this.CACHE_DURATION
    
    // 缓存各种权限
    Object.keys(this.PERMISSION_TYPES).forEach(permissionType => {
      const cacheKey = `${projectId}_${permissionType}`
      const hasPermission = this.checkPermissionFromSummary(summary, permissionType)
      this.permissionCache.set(cacheKey, hasPermission)
      this.cacheExpiry.set(cacheKey, expiry)
    })

    // 缓存角色信息
    this.permissionCache.set(`${projectId}_isProjectManager`, summary.isProjectManager || false)
    this.permissionCache.set(`${projectId}_isTechLeader`, summary.isTechLeader || false)
    this.cacheExpiry.set(`${projectId}_isProjectManager`, expiry)
    this.cacheExpiry.set(`${projectId}_isTechLeader`, expiry)
  }

  /**
   * 从权限摘要中检查特定权限
   */
  static checkPermissionFromSummary(summary, permissionType) {
    if (!summary) return false

    switch (permissionType) {
      case this.PERMISSION_TYPES.PROJECT_MANAGEMENT:
        return summary.isProjectManager || false
      case this.PERMISSION_TYPES.TASK_MANAGEMENT:
        return summary.canManageTasks || false
      case this.PERMISSION_TYPES.TIMESHEET_APPROVAL:
        return summary.canApproveTimesheet || false
      case this.PERMISSION_TYPES.REPORT_VIEW:
        return summary.canViewReports || false
      case this.PERMISSION_TYPES.COST_MANAGEMENT:
        return summary.canManageCosts || false
      case this.PERMISSION_TYPES.MILESTONE_MANAGEMENT:
        return summary.canManageMilestones || false
      case this.PERMISSION_TYPES.TEAM_MANAGEMENT:
        return summary.canManageTeam || false
      default:
        return false
    }
  }

  /**
   * 清除权限缓存
   */
  static clearCache() {
    this.permissionCache.clear()
    this.cacheExpiry.clear()
  }

  /**
   * 清除特定项目的权限缓存
   */
  static clearProjectCache(projectId) {
    const keysToDelete = []
    
    for (const key of this.permissionCache.keys()) {
      if (key.startsWith(`${projectId}_`)) {
        keysToDelete.push(key)
      }
    }
    
    keysToDelete.forEach(key => {
      this.permissionCache.delete(key)
      this.cacheExpiry.delete(key)
    })
  }
}

/**
 * Vue 3 Composition API 钩子函数
 */
export function useProjectPermissions(projectId) {
  const checkPermission = async (permissionType) => {
    return await EnhancedPermissionUtil.hasProjectPermission(projectId.value || projectId, permissionType)
  }

  const isProjectManager = async () => {
    return await EnhancedPermissionUtil.isProjectManager(projectId.value || projectId)
  }

  const isTechLeader = async () => {
    return await EnhancedPermissionUtil.isTechLeader(projectId.value || projectId)
  }

  const canApproveTimesheet = async () => {
    return await EnhancedPermissionUtil.canApproveTimesheet(projectId.value || projectId)
  }

  const canManageTasks = async () => {
    return await EnhancedPermissionUtil.canManageTasks(projectId.value || projectId)
  }

  const canViewReports = async () => {
    return await EnhancedPermissionUtil.canViewReports(projectId.value || projectId)
  }

  const canManageCosts = async () => {
    return await EnhancedPermissionUtil.canManageCosts(projectId.value || projectId)
  }

  const canManageMilestones = async () => {
    return await EnhancedPermissionUtil.canManageMilestones(projectId.value || projectId)
  }

  const canManageTeam = async () => {
    return await EnhancedPermissionUtil.canManageTeam(projectId.value || projectId)
  }

  const canGrantPermission = async () => {
    return await EnhancedPermissionUtil.canGrantPermission(projectId.value || projectId)
  }

  const getPermissionSummary = async () => {
    return await EnhancedPermissionUtil.getUserProjectPermissions(projectId.value || projectId)
  }

  return {
    checkPermission,
    isProjectManager,
    isTechLeader,
    canApproveTimesheet,
    canManageTasks,
    canViewReports,
    canManageCosts,
    canManageMilestones,
    canManageTeam,
    canGrantPermission,
    getPermissionSummary,
    PERMISSION_TYPES: EnhancedPermissionUtil.PERMISSION_TYPES
  }
}

export default EnhancedPermissionUtil