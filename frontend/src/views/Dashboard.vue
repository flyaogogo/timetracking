<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon project">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.projectCount }}</div>
              <div class="stats-label">项目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon task">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.taskCount }}</div>
              <div class="stats-label">涉及任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon time">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalHours }}</div>
              <div class="stats-label">项目工时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.pendingCount }}</div>
              <div class="stats-label">待审核工时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 内容区域 -->
    <el-row :gutter="20" class="content-row">
      <!-- 我的项目 -->
      <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>我的项目 (Top10)</span>
              <el-button type="text" @click="$router.push({ path: '/projects', query: { userOnly: 'true' } })">查看更多</el-button>
            </div>
          </template>
          
          <div v-if="myProjects.length === 0" class="empty-data">
            <el-empty description="暂无参与的项目" />
          </div>
          
          <div v-else class="project-list">
            <div v-for="project in myProjects" :key="project.id" class="project-item">
              <div class="project-info">
                <div class="project-name">{{ project.projectName }}</div>
                <div class="project-role">{{ getRoleText(project.role) }}</div>
              </div>
              <div class="project-status">
                <el-tag :type="getProjectStatusType(project.status)" size="small">
                  {{ getProjectStatusText(project.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 我的任务 -->
      <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>我的任务 (Top10)</span>
              <el-button type="text" @click="$router.push({ path: '/tasks', query: { userOnly: 'true' } })">查看更多</el-button>
            </div>
          </template>
          
          <div v-if="myTasks.length === 0" class="empty-data">
            <el-empty description="暂无任务" />
          </div>
          
          <div v-else class="task-list">
            <div v-for="task in myTasks" :key="task.id" class="task-item">
              <div class="task-info">
                <div class="task-name">{{ task.taskName }}</div>
                <div class="task-project">{{ task.projectName }}</div>
              </div>
              <div class="task-status">
                <el-tag :type="getTaskStatusType(task.status)" size="small">
                  {{ getTaskStatusText(task.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最近工时 -->
      <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>最近工时 (Top10)</span>
              <el-button type="text" @click="$router.push({ path: '/time-entries', query: { userOnly: 'true' } })">查看更多</el-button>
            </div>
          </template>
          
          <div v-if="recentTimeEntries.length === 0" class="empty-data">
            <el-empty description="暂无工时记录" />
          </div>
          
          <div v-else class="time-list">
            <div v-for="entry in recentTimeEntries" :key="entry.id" class="time-entry-item">
              <div class="entry-info">
                <div class="entry-date">{{ entry.workDate }}</div>
                <div class="entry-project">{{ entry.projectName }}</div>
              </div>
              <div class="entry-hours">
                {{ entry.duration }}h
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 待审核工时 -->
      <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>待审核工时 (Top10)</span>
              <el-button type="text" @click="$router.push({ path: '/approvals', query: { managerOnly: 'true' } })">查看更多</el-button>
            </div>
          </template>
          
          <div v-if="pendingApprovals.length === 0" class="empty-data">
            <el-empty description="暂无待审核工时" />
          </div>
          
          <div v-else class="approval-list">
            <div v-for="approval in pendingApprovals" :key="approval.id" class="approval-item">
              <div class="approval-info">
                <div class="approval-user">{{ approval.userName }}</div>
                <div class="approval-project">{{ approval.projectName }} - {{ approval.workDate }}</div>
              </div>
              <div class="approval-hours">
                {{ approval.duration }}h
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserDashboardData, getUserTaskStats, getUserTimeStats, getUserProjects } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const stats = ref({
  projectCount: 0,
  taskCount: 0,
  totalHours: 0,
  pendingCount: 0
})

const myProjects = ref([])
const myTasks = ref([])
const recentTimeEntries = ref([])
const pendingApprovals = ref([])

const getTaskStatusType = (status) => {
  const statusMap = {
    'TODO': 'info',
    'IN_PROGRESS': 'warning',
    'REVIEW': 'primary',
    'COMPLETED': 'success',
    'PAUSED': 'warning',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getTaskStatusText = (status) => {
  const statusMap = {
    'TODO': '待开始',
    'IN_PROGRESS': '进行中',
    'REVIEW': '待审核',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getProjectStatusType = (status) => {
  const statusMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'PAUSED': 'warning',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getProjectStatusText = (status) => {
  const statusMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getRoleText = (role) => {
  const roleMap = {
    'MANAGER': '项目经理',
    'PROJECT_MANAGER': '项目经理',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员',
    'DESIGNER': '设计师',
    'PRODUCT_MANAGER': '产品经理',
    'TECH_LEADER': '技术负责人',
    'OTHER': '其他'
  }
  return roleMap[role] || role
}

const loadDashboardData = async () => {
  try {
    // 检查用户是否已登录
    if (!userStore.user?.id) {
      console.log('用户未登录，跳过数据加载')
      return
    }
    
    const userId = userStore.user.id
    
    // 加载工作台数据
    try {
      const dashboardResponse = await getUserDashboardData(userId)
      if (dashboardResponse.code === 200) {
        const data = dashboardResponse.data
        
        // 更新统计数据
        stats.value = {
          projectCount: data.projectCount || 0,
          taskCount: data.taskCount || 0,
          totalHours: parseFloat(data.totalHours || 0).toFixed(1),
          pendingCount: data.pendingCount || 0
        }
        
        // 更新任务列表
        myTasks.value = data.recentTasks || []
        
        // 更新工时记录
        recentTimeEntries.value = data.recentTimeEntries || []
        
        // 更新项目列表
        myProjects.value = data.recentProjects || []
        
        // 更新待审核工时
        pendingApprovals.value = data.pendingApprovals || []
      }
    } catch (error) {
      console.error('加载工作台数据失败:', error)
      // 设置默认值，不显示错误
      stats.value = {
        projectCount: 0,
        taskCount: 0,
        totalHours: '0.0',
        pendingCount: 0
      }
      myTasks.value = []
      recentTimeEntries.value = []
      myProjects.value = []
      pendingApprovals.value = []
    }
    
    // 加载用户参与的项目
    try {
      const projectsResponse = await getUserProjects(userId)
      if (projectsResponse.code === 200) {
        // 如果工作台数据中没有项目数据，则使用这个接口的数据
        if (!myProjects.value || myProjects.value.length === 0) {
          myProjects.value = (projectsResponse.data || []).slice(0, 10) // 只取前10个
        }
      }
    } catch (error) {
      console.error('加载用户项目失败:', error)
      if (!myProjects.value || myProjects.value.length === 0) {
        myProjects.value = []
      }
    }
    
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    // 设置默认值
    myTasks.value = []
    recentTimeEntries.value = []
    stats.value = {
      projectCount: 0,
      taskCount: 0,
      totalHours: '0.0',
      pendingCount: 0
    }
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
  margin-bottom: 16px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 8px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.stats-icon.project {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.task {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.time {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
  min-width: 0;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.content-row {
  margin-top: 20px;
}

.content-card {
  margin-bottom: 16px;
  height: 400px;
}

.content-card .el-card__body {
  height: calc(100% - 57px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-list,
.time-list,
.project-list,
.approval-list {
  height: 100%;
  overflow-y: auto;
}

.task-item, .time-entry-item, .project-item, .approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child, .time-entry-item:last-child, .project-item:last-child, .approval-item:last-child {
  border-bottom: none;
}

.task-name, .entry-date, .project-name, .approval-user {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.task-project, .entry-project, .project-role, .approval-project {
  font-size: 12px;
  color: #666;
}

.entry-hours {
  font-weight: bold;
  color: #409EFF;
}

.entry-hours {
  font-weight: bold;
  color: #409EFF;
}

.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-card {
    height: 100px;
    margin-bottom: 12px;
  }
  
  .stats-content {
    padding: 4px;
  }
  
  .stats-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
    margin-right: 12px;
  }
  
  .stats-number {
    font-size: 24px;
  }
  
  .stats-label {
    font-size: 12px;
  }
  
  .content-card {
    height: 350px;
    margin-bottom: 12px;
  }
  
  .task-item, .time-entry-item {
    padding: 10px 0;
  }
  
  .task-name, .entry-date {
    font-size: 14px;
  }
  
  .task-project, .entry-project {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .stats-card {
    height: 90px;
  }
  
  .stats-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    margin-right: 10px;
  }
  
  .stats-number {
    font-size: 20px;
  }
  
  .content-card {
    height: 300px;
  }
}
</style>