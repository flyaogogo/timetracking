<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="logo">
          <h3 v-if="!isCollapsed">工时管理系统</h3>
          <h3 v-else>TMS</h3>
        </div>
        
        <el-menu
          :default-active="$route.path"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse="isCollapsed"
          :collapse-transition="false"
          @select="handleMenuSelect"
        >
          <!-- 个人工作台 -->
          <el-menu-item 
            v-if="hasMenuPermission('dashboard')"
            index="/dashboard"
          >
            <el-icon><Odometer /></el-icon>
            <template #title>个人工作台</template>
          </el-menu-item>
          
          <!-- 经理工作台 -->
          <el-menu-item 
            v-if="showProjectManagerDashboard"
            index="/project-manager"
          >
            <el-icon><UserFilled /></el-icon>
            <template #title>经理工作台</template>
          </el-menu-item>
          
          <!-- 项目管理 -->
          <el-sub-menu 
            v-if="hasMenuPermission('projects')"
            index="project"
          >
            <template #title>
              <el-icon><Folder /></el-icon>
              <span>项目管理</span>
            </template>
            
            <el-menu-item index="/projects">
              <el-icon><List /></el-icon>
              <template #title>项目列表</template>
            </el-menu-item>
            
            <el-menu-item index="/project-statistics">
              <el-icon><TrendCharts /></el-icon>
              <template #title>统计分析</template>
            </el-menu-item>
            
            <el-menu-item 
              v-if="hasMenuPermission('projects')"
              index="/project-costs"
            >
              <el-icon><Money /></el-icon>
              <template #title>成本管理</template>
            </el-menu-item>
            
            <el-menu-item 
              v-if="hasMenuPermission('projects')"
              index="/project-milestones"
            >
              <el-icon><Flag /></el-icon>
              <template #title>里程碑</template>
            </el-menu-item>
            
            <el-menu-item 
              v-if="hasMenuPermission('projectPermissions')"
              index="/project-permissions"
            >
              <el-icon><Key /></el-icon>
              <template #title>权限管理</template>
            </el-menu-item>
            
            <el-menu-item 
              v-if="hasMenuPermission('projectReports')"
              index="/project-reports"
            >
              <el-icon><TrendCharts /></el-icon>
              <template #title>报表查看</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 任务管理 -->
          <el-menu-item 
            v-if="hasMenuPermission('tasks')"
            index="/tasks"
          >
            <el-icon><List /></el-icon>
            <template #title>任务管理</template>
          </el-menu-item>
          
          <!-- 工时记录 -->
          <el-menu-item 
            v-if="hasMenuPermission('timeEntries')"
            index="/time-entries"
          >
            <el-icon><Timer /></el-icon>
            <template #title>工时记录</template>
          </el-menu-item>
          
          <!-- 个人工时统计 -->
          <el-menu-item 
            v-if="hasMenuPermission('timeEntries')"
            index="/personal-time-statistics"
          >
            <el-icon><TrendCharts /></el-icon>
            <template #title>个人工时统计</template>
          </el-menu-item>
          
          <!-- 工时审核 -->
          <el-menu-item 
            v-if="hasMenuPermission('approvals')"
            index="/approvals"
          >
            <el-icon><Check /></el-icon>
            <template #title>工时审核</template>
          </el-menu-item>
          
          <!-- 工时统计管理 -->
          <el-menu-item 
            v-if="hasMenuPermission('timeTrackingStatistics')"
            index="/time-tracking-statistics"
          >
            <el-icon><TrendCharts /></el-icon>
            <template #title>工时统计管理</template>
          </el-menu-item>
          
          <!-- 用户管理 -->
          <el-menu-item 
            v-if="hasMenuPermission('users')"
            index="/users"
          >
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          
          <!-- 组织管理 -->
          <el-menu-item 
            v-if="hasMenuPermission('organizations')"
            index="/organizations"
          >
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>组织管理</template>
          </el-menu-item>
          
          <!-- 测试页面 - 临时，生产环境需要移除 -->
          <el-menu-item 
            v-if="hasMenuPermission('test')"
            index="/test"
          >
            <el-icon><Setting /></el-icon>
            <template #title>权限测试</template>
          </el-menu-item>
          
          <!-- 系统验证 - 开发调试用 -->
          <el-menu-item 
            v-if="hasMenuPermission('test')"
            index="/system-validation"
          >
            <el-icon><Monitor /></el-icon>
            <template #title>系统验证</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-button 
              type="text" 
              @click="toggleSidebar"
              class="sidebar-toggle"
            >
              <el-icon><Expand v-if="isCollapsed" /><Fold v-else /></el-icon>
            </el-button>
            <span class="page-title">{{ $route.meta.title || '个人工作台' }}</span>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><Avatar /></el-icon>
                <span class="user-name">{{ userStore.user?.realName || userStore.user?.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Odometer, 
  Folder, 
  List, 
  Timer, 
  Check, 
  User, 
  UserFilled,
  OfficeBuilding, 
  Setting, 
  Expand, 
  Fold, 
  Avatar, 
  ArrowDown,
  TrendCharts,
  Money,
  Flag,
  Monitor,
  Key
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { hasMenuPermission as checkMenuPermission, hasProjectManagerDashboardPermission } from '@/config/permissions'

const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const screenWidth = ref(window.innerWidth)
const showProjectManagerDashboard = ref(false)

// 计算侧边栏宽度
const sidebarWidth = computed(() => {
  if (isCollapsed.value) {
    return '64px'
  }
  return screenWidth.value < 1200 ? '180px' : '200px'
})

// 检查菜单权限
const hasMenuPermission = (menuKey) => {
  const userRole = userStore.user?.role
  if (!userRole) return false
  
  // 使用统一的权限检查函数
  return checkMenuPermission(menuKey, userRole)
}

// 检查项目经理工作台权限
const checkProjectManagerDashboardPermission = async () => {
  const userRole = userStore.user?.role
  if (!userRole) {
    showProjectManagerDashboard.value = false
    return
  }
  
  try {
    const hasPermission = await hasProjectManagerDashboardPermission(userRole)
    showProjectManagerDashboard.value = hasPermission
    console.log('项目经理工作台权限检查结果:', hasPermission)
  } catch (error) {
    console.error('检查项目经理工作台权限失败:', error)
    showProjectManagerDashboard.value = false
  }
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 处理窗口大小变化
const handleResize = () => {
  screenWidth.value = window.innerWidth
  
  // 小屏幕自动折叠侧边栏
  if (screenWidth.value < 768) {
    isCollapsed.value = true
  } else if (screenWidth.value > 1200) {
    isCollapsed.value = false
  }
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('=== 菜单选择事件 ===')
  console.log('选择的菜单index:', index)
  console.log('当前路径:', router.currentRoute.value.path)
  console.log('用户角色:', userStore.user?.role)
  
  // 执行导航
  if (index !== router.currentRoute.value.path) {
    console.log('🚀 开始导航到:', index)
    router.push(index)
      .then(() => {
        console.log('✅ 导航成功')
      })
      .catch(err => {
        console.error('❌ 导航失败详细信息:', err)
        console.error('错误类型:', typeof err)
        console.error('错误消息:', err.message)
        console.error('错误堆栈:', err.stack)
        
        // 显示更详细的错误信息
        if (err.message) {
          ElMessage.error(`页面导航失败: ${err.message}`)
        } else {
          ElMessage.error('页面导航失败，请查看控制台获取详细信息')
        }
      })
  } else {
    console.log('ℹ️ 已在当前页面，无需导航')
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人信息页面
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        userStore.logout()
        ElMessage.success('退出成功')
        router.push('/login')
      } catch {
        // 用户取消
      }
      break
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize() // 初始化检查
  
  // 检查项目经理工作台权限
  if (userStore.user) {
    checkProjectManagerDashboardPermission()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: #304156;
  overflow: hidden;
  transition: width 0.3s ease;
}

.sidebar-collapsed {
  width: 64px !important;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: white;
  border-bottom: 1px solid #434a5a;
  transition: all 0.3s ease;
}

.logo h3 {
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  margin-right: 16px;
  font-size: 18px;
  color: #666;
}

.sidebar-toggle:hover {
  color: #409EFF;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
}

.user-info .el-icon {
  font-size: 16px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 16px;
  }
  
  .user-name {
    display: none;
  }
  
  .main-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 12px;
  }
  
  .main-content {
    padding: 12px;
  }
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: #2b2f3a;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #434a5a;
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #5a6169;
}
</style>