import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { hasProjectManagerDashboardPermission } from '@/config/permissions'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/403.vue'),
    meta: { title: '无权限访问' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { 
          title: '个人工作台',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { 
          title: '个人信息',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'personal-time-statistics',
        name: 'PersonalTimeStatistics',
        component: () => import('@/views/PersonalTimeStatistics.vue'),
        meta: { 
          title: '个人工时统计',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'user-performance-ranking',
        name: 'UserPerformanceRanking',
        component: () => import('@/views/UserPerformanceRanking.vue'),
        meta: { 
          title: '员工工作表现排名',
          roles: ['ADMIN', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-manager',
        name: 'ProjectManagerDashboard',
        component: () => import('@/views/ProjectManagerDashboard.vue'),
        meta: { 
          title: '经理工作台',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/projects',
        name: 'ProjectManagerProjects',
        component: () => import('@/views/ProjectManagerProjects.vue'),
        meta: { 
          title: '管理的项目',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/approvals',
        name: 'ProjectManagerApprovals',
        component: () => import('@/views/ProjectManagerApprovals.vue'),
        meta: { 
          title: '工时审批',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/team-performance',
        name: 'ProjectManagerTeamPerformance',
        component: () => import('@/views/ProjectManagerTeamPerformance.vue'),
        meta: { 
          title: '团队绩效',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/reports',
        name: 'ProjectManagerReports',
        component: () => import('@/views/ProjectManagerReports.vue'),
        meta: { 
          title: '项目报表',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/team',
        name: 'ProjectManagerTeam',
        component: () => import('@/views/ProjectManagerTeam.vue'),
        meta: { 
          title: '团队管理',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'project-manager/team-overview',
        name: 'ProjectManagerTeamOverview',
        component: () => import('@/views/TeamOverview.vue'),
        meta: {
          title: '团队员工工作情况',
          roles: ['ADMIN', 'DEPARTMENT_MANAGER', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/Projects.vue'),
        meta: { 
          title: '项目管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-status-management',
        name: 'ProjectStatusManagement',
        component: () => import('@/views/ProjectStatusManagement.vue'),
        meta: { 
          title: '项目状态管理',
          roles: ['ADMIN', 'PROJECT_MANAGER']
        }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/Tasks.vue'),
        meta: { 
          title: '任务管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'time-entries',
        name: 'TimeEntries',
        component: () => import('@/views/TimeEntries.vue'),
        meta: { 
          title: '工时记录',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { 
          title: '用户管理', 
          roles: ['ADMIN', 'DEPARTMENT_MANAGER'] 
        }
      },
      {
        path: 'organizations',
        name: 'Organizations',
        component: () => import('@/views/Organizations.vue'),
        meta: { 
          title: '组织管理', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'organizations-test',
        name: 'OrganizationsTest',
        component: () => import('@/views/OrganizationsTest.vue'),
        meta: { 
          title: '组织管理测试', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'token-test',
        name: 'TokenTest',
        component: () => import('@/views/TokenTest.vue'),
        meta: { 
          title: 'Token测试', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'navigation-debug',
        name: 'NavigationDebug',
        component: () => import('@/views/NavigationDebug.vue'),
        meta: { 
          title: '导航调试', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'error-debug',
        name: 'ErrorDebug',
        component: () => import('@/views/ErrorDebug.vue'),
        meta: { 
          title: '错误调试', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'navigation-test',
        name: 'NavigationTest',
        component: () => import('@/views/NavigationTest.vue'),
        meta: { 
          title: '导航测试', 
          roles: ['ADMIN'] 
        }
      },
      {
        path: 'approvals',
        name: 'Approvals',
        component: () => import('@/views/Approvals.vue'),
        meta: { 
          title: '工时审核', 
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER'] 
        }
      },
      {
        path: 'project-statistics',
        name: 'ProjectStatistics',
        component: () => import('@/views/ProjectStatistics.vue'),
        meta: { 
          title: '项目统计分析',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER'],
          requiresProjectManagerPermission: true
        }
      },
      {
        path: 'project-costs',
        name: 'ProjectCosts',
        component: () => import('@/views/ProjectCosts.vue'),
        meta: { 
          title: '项目成本管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-milestones',
        name: 'ProjectMilestones',
        component: () => import('@/views/ProjectMilestones.vue'),
        meta: { 
          title: '项目里程碑',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-team',
        name: 'ProjectTeam',
        component: () => import('@/views/ProjectTeamFixed.vue'),
        meta: { 
          title: '项目团队管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-permissions',
        name: 'ProjectPermissions',
        component: () => import('@/views/ProjectTeamEnhanced.vue'),
        meta: { 
          title: '项目权限管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-reports',
        name: 'ProjectReports',
        component: () => import('@/views/ProjectReports.vue'),
        meta: { 
          title: '项目报表查看',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'PRODUCT_MANAGER', 'DEPARTMENT_MANAGER', 'DEVELOPER', 'TESTER']
        }
      },
      {
        path: 'time-tracking-statistics',
        name: 'TimeTrackingStatistics',
        component: () => import('@/views/TimeTrackingStatistics.vue'),
        meta: {
          title: '工时统计管理',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },

      {
        path: 'project-member-debug',
        name: 'ProjectMemberDebug',
        component: () => import('@/views/ProjectMemberDebug.vue'),
        meta: { 
          title: '成员显示调试',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'project-team-debug',
        name: 'ProjectTeamDebug',
        component: () => import('@/views/ProjectTeamDebug.vue'),
        meta: { 
          title: '项目团队调试',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'api-test',
        name: 'APITest',
        component: () => import('@/views/APITest.vue'),
        meta: { 
          title: 'API接口测试',
          roles: ['ADMIN']
        }
      },
      {
        path: 'team-management-test',
        name: 'TeamManagementTest',
        component: () => import('@/views/TeamManagementTest.vue'),
        meta: { 
          title: '团队管理测试',
          roles: ['ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER']
        }
      },
      {
        path: 'test',
        name: 'Test',
        component: () => import('@/views/Test.vue'),
        meta: { 
          title: '权限测试',
          roles: ['ADMIN']
        }
      },
      {
        path: 'system-validation',
        name: 'SystemValidation',
        component: () => import('@/views/SystemValidation.vue'),
        meta: { 
          title: '系统验证',
          roles: ['ADMIN']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  console.log('=== 路由守卫检查 ===')
  console.log('从:', from.path)
  console.log('到:', to.path)
  console.log('用户Token:', userStore.token ? '存在' : '不存在')
  console.log('用户信息:', userStore.user)
  console.log('用户角色:', userStore.user?.role)
  console.log('需要的角色:', to.meta.roles)
  
  // 登录页面直接通过
  if (to.path === '/login') {
    console.log('访问登录页面')
    // 如果已登录，重定向到首页
    if (userStore.token) {
      console.log('已登录，重定向到首页')
      next('/')
    } else {
      console.log('未登录，允许访问登录页')
      next()
    }
    return
  }
  
  // 403页面直接通过
  if (to.path === '/403') {
    console.log('访问403页面，直接通过')
    next()
    return
  }
  
  // 检查是否已登录
  if (!userStore.token || !userStore.user) {
    console.log('❌ 用户未登录，重定向到登录页')
    console.log('Token存在:', !!userStore.token)
    console.log('User存在:', !!userStore.user)
    next('/login')
    return
  }
  
  // 特殊处理项目经理工作台及其子页面
  if (to.path.startsWith('/project-manager')) {
    console.log('🔍 检查项目经理工作台权限')
    const userRole = userStore.user?.role
    
    try {
      const hasPermission = await hasProjectManagerDashboardPermission(userRole)
      console.log('项目经理工作台权限检查结果:', hasPermission)
      
      if (hasPermission) {
        console.log('✅ 项目经理工作台权限检查通过')
        next()
      } else {
        console.log('❌ 项目经理工作台权限检查失败，重定向到工作台')
        next('/dashboard')
      }
    } catch (error) {
      console.error('项目经理工作台权限检查异常:', error)
      next('/dashboard')
    }
    return
  }
  
  // 检查页面权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userStore.user?.role
    console.log('🔒 权限检查:', { 
      userRole, 
      requiredRoles: to.meta.roles,
      hasRole: userRole && to.meta.roles.includes(userRole),
      requiresProjectManagerPermission: to.meta.requiresProjectManagerPermission
    })
    
    if (!userRole) {
      console.log('❌ 用户角色为空，重定向到工作台')
      next('/dashboard')
      return
    }
    
    // 检查基本角色权限
    const hasBasicRole = to.meta.roles.includes(userRole)
    
    // 如果需要项目经理权限，进行额外检查
    if (to.meta.requiresProjectManagerPermission) {
      console.log('🔍 检查项目经理权限')
      try {
        const hasProjectManagerPermission = await hasProjectManagerDashboardPermission(userRole)
        console.log('项目经理权限检查结果:', hasProjectManagerPermission)
        
        if (hasBasicRole && hasProjectManagerPermission) {
          console.log('✅ 基本角色和项目经理权限检查都通过')
          next()
        } else if (hasBasicRole) {
          console.log('✅ 有基本角色权限，但无项目经理权限，允许访问')
          next()
        } else {
          console.log('❌ 权限检查失败，重定向到工作台')
          next('/dashboard')
        }
      } catch (error) {
        console.error('项目经理权限检查异常:', error)
        if (hasBasicRole) {
          console.log('✅ 权限检查异常，但有基本角色权限，允许访问')
          next()
        } else {
          console.log('❌ 权限检查异常且无基本角色权限，重定向到工作台')
          next('/dashboard')
        }
      }
      return
    }
    
    // 普通权限检查
    if (!hasBasicRole) {
      console.log('❌ 权限检查失败，重定向到工作台')
      console.log(`用户角色 "${userRole}" 不在允许的角色列表 [${to.meta.roles.join(', ')}] 中`)
      next('/dashboard')
      return
    }
  }
  
  console.log('✅ 路由守卫通过，允许访问')
  next()
})

export default router