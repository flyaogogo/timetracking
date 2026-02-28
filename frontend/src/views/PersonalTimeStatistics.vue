<template>
  <!-- 版本: 1.0.1 -->
  <div class="personal-time-statistics">
    <template v-if="!userStore.user">
      <div class="login-prompt">
        <el-empty description="请先登录" />
        <el-button type="primary" @click="goToLogin">去登录</el-button>
      </div>
    </template>
    <template v-else>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ isAdminOrManager ? '人员工时统计' : '个人工时统计' }}</h2>
            <div class="header-actions">
              <el-select v-if="isAdminOrManager" v-model="selectedUserId" @change="handleUserChange" style="width: 180px; margin-right: 10px">
                <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.id" />
              </el-select>
              <el-date-picker
                v-if="viewMode === 'monthly'"
                v-model="dateRange"
                type="month"
                placeholder="选择月份"
                @change="handleDateChange"
                style="width: 160px; margin-right: 10px"
              />
              <el-select v-model="viewMode" @change="handleViewModeChange">
                <el-option label="月度统计" value="monthly" />
                <el-option label="年度统计" value="yearly" />
              </el-select>
            </div>
          </div>
        </template>
        
        <!-- 标签页导航 -->
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="统计概览" name="overview">
            <!-- 统计概览 -->
            <div class="stats-overview">
              <!-- 上排统计卡片 -->
              <el-row :gutter="20" class="overview-row" style="margin-top: 15px;">
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><Timer /></el-icon>
                      </div>
                      <div class="stat-value">{{ (stats.totalHours || 0).toFixed(1) }}h</div>
                      <div class="stat-label">总工时</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><Calendar /></el-icon>
                      </div>
                      <div class="stat-value">{{ stats.workDays || 0 }}天</div>
                      <div class="stat-label">工作天数</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><Star /></el-icon>
                      </div>
                      <div class="stat-value">{{ assessmentData.overallScore }}</div>
                      <div class="stat-label">综合评分</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              
              <!-- 下排统计卡片 -->
              <el-row :gutter="20" class="overview-row" style="margin-top: 15px;">
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><DataAnalysis /></el-icon>
                      </div>
                      <div class="stat-value">{{ (100 - assessmentData.taskDelay).toFixed(1) }}%</div>
                      <div class="stat-label">任务完成率</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><Grid /></el-icon>
                      </div>
                      <div class="stat-value">{{ projectDistribution.length }}</div>
                      <div class="stat-label">参与项目数</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-content">
                      <div class="stat-icon">
                        <el-icon class="icon-large"><Clock /></el-icon>
                      </div>
                      <div class="stat-value">{{ assessmentData.workSaturation.toFixed(1) }}%</div>
                      <div class="stat-label">工作饱和度</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            
            <!-- 工时趋势 -->
            <div class="trend-section">
              <el-card class="trend-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>工时趋势（最近12个月）</span>
                  </div>
                </template>
                <div class="trend-container">
                  <el-table :data="trendData" style="width: 100%">
                    <el-table-column prop="month" label="月份" width="120" />
                    <el-table-column prop="totalHours" label="工时 (h)" width="100" />
                  </el-table>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="项目详情" name="projects">
            <!-- 项目工时分布 -->
            <div class="distribution-section">
              <el-card class="distribution-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>项目工时分布</span>
                  </div>
                </template>
                <div class="distribution-container">
                  <el-table :data="projectDistribution" style="width: 100%">
                    <el-table-column prop="projectName" label="项目名称" />
                    <el-table-column prop="hours" label="工时 (h)" width="100" />
                  </el-table>
                </div>
              </el-card>
            </div>
            
            <!-- 项目参与详情 -->
            <div class="project-details-section">
              <el-card class="project-details-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>项目参与详情</span>
                  </div>
                </template>
                <div class="project-details-container">
                  <el-table :data="projectDetails" style="width: 100%">
                    <el-table-column prop="projectName" label="项目名称" />
                    <el-table-column prop="role" label="角色" width="120" />
                    <el-table-column prop="startDate" label="开始日期" width="150" />
                    <el-table-column prop="endDate" label="结束日期" width="150" />
                    <el-table-column prop="totalHours" label="工时 (h)" width="100" />
                  </el-table>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="任务分析" name="tasks">
            <!-- 任务完成情况 -->
            <div class="tasks-section">
              <el-card class="tasks-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>任务完成情况</span>
                  </div>
                </template>
                <div class="tasks-container">
                  <el-table :data="taskStatusData" style="width: 100%">
                    <el-table-column prop="status" label="任务状态" width="120" />
                    <el-table-column prop="count" label="数量" width="100" />
                    <el-table-column prop="percentage" label="占比" width="100" />
                  </el-table>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="考核维度" name="assessment">
            <!-- 工作效率分析 -->
            <div class="efficiency-section">
              <el-card class="efficiency-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>工作效率分析</span>
                  </div>
                </template>
                <div class="efficiency-content">
                  <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                      <div class="efficiency-item">
                        <div class="efficiency-label">月度目标达成率</div>
                        <div class="efficiency-value">{{ efficiencyRate }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                      <div class="efficiency-item">
                        <div class="efficiency-label">工作饱和度</div>
                        <div class="efficiency-value">{{ workSaturation }}%</div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-card>
            </div>
            
            <!-- 考核维度分析 -->
            <div class="assessment-section">
              <el-card class="assessment-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span>考核维度分析</span>
                  </div>
                </template>
                <div class="assessment-content">
                  <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">项目参与度</div>
                        <div class="assessment-value">{{ assessmentData.projectParticipation }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">任务按时完成率</div>
                        <div class="assessment-value">{{ 100 - assessmentData.taskDelay }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">任务难度系数</div>
                        <div class="assessment-value">{{ assessmentData.taskDifficulty }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">工作饱和度</div>
                        <div class="assessment-value">{{ assessmentData.workSaturation }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">加班情况</div>
                        <div class="assessment-value">{{ assessmentData.overtime }}%</div>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
                      <div class="assessment-item">
                        <div class="assessment-label">综合评分</div>
                        <div class="assessment-value overall">{{ assessmentData.overallScore }}分</div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserMonthlyStats, getUserYearlyStats, getUserMonthlyTrend, getUserProjectHoursDistribution } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import { getUserList } from '@/api/user'
import { getUserTasks } from '@/api/task'
import { getProjectList } from '@/api/project'
import {
  Timer,
  Calendar,
  Star,
  DataAnalysis,
  Grid,
  Clock
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 状态管理
const dateRange = ref(new Date())
const viewMode = ref('monthly')
const selectedUserId = ref(null)
const userList = ref([])
const stats = ref({})
const trendData = ref([])
const projectDistribution = ref([])
const projectDetails = ref([])
const taskStatusData = ref([])
const assessmentData = ref({
  projectParticipation: 0,
  taskDelay: 0,
  taskDifficulty: 0,
  workSaturation: 0,
  overtime: 0,
  overallScore: 0
})
const activeTab = ref('overview')

// 角色判断
const isAdminOrManager = computed(() => {
  const role = userStore.user?.role
  return role === 'ADMIN' || role === 'DEPARTMENT_MANAGER'
})

// 当前查询的用户ID
const currentUserId = computed(() => {
  return isAdminOrManager.value && selectedUserId.value ? selectedUserId.value : userStore.user?.id
})

// 计算两个日期之间的工作日数量（排除周六、周日和法定假期）
const getWorkdaysBetween = (startDate, endDate) => {
  let workdays = 0
  const currentDate = new Date(startDate)
  
  // 法定假期数据（示例数据，实际项目中应该从API获取或本地存储）
  // 格式：YYYY-MM-DD
  const holidays = [
    '2026-01-01', // 元旦
    '2026-02-02', // 春节
    '2026-02-03', // 春节
    '2026-02-04', // 春节
    '2026-02-05', // 春节
    '2026-04-04', // 清明节
    '2026-04-05', // 清明节
    '2026-05-01', // 劳动节
    '2026-05-02', // 劳动节
    '2026-05-03', // 劳动节
    '2026-06-25', // 端午节
    '2026-09-27', // 中秋节
    '2026-10-01', // 国庆节
    '2026-10-02', // 国庆节
    '2026-10-03', // 国庆节
    '2026-10-04', // 国庆节
    '2026-10-05', // 国庆节
    '2026-10-06', // 国庆节
    '2026-10-07'  // 国庆节
  ]
  
  while (currentDate <= endDate) {
    const day = currentDate.getDay()
    const dateStr = currentDate.toISOString().split('T')[0]
    
    // 排除周六、周日和法定假期
    if (day !== 0 && day !== 6 && !holidays.includes(dateStr)) {
      workdays++
    }
    currentDate.setDate(currentDate.getDate() + 1)
  }
  
  return workdays
}

// 计算当前日期范围
const getDateRange = () => {
  const now = dateRange.value
  let startDate, endDate

  if (viewMode.value === 'monthly') {
    startDate = new Date(now.getFullYear(), now.getMonth(), 1)
    endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  } else if (viewMode.value === 'yearly') {
    startDate = new Date(now.getFullYear(), 0, 1)
    endDate = new Date(now.getFullYear(), 11, 31)
  }

  return {
    startDate: startDate.toISOString().split('T')[0],
    endDate: endDate.toISOString().split('T')[0],
    workdays: getWorkdaysBetween(startDate, endDate)
  }
}

// 计算效率相关数据
const efficiencyRate = computed(() => {
  // 假设月度目标为160小时
  const targetHours = 160
  const actualHours = stats.value.totalHours || 0
  return Math.min(100, Math.round((actualHours / targetHours) * 100))
})

const workSaturation = computed(() => {
  // 标准工作时间 = 标准工作日 × 8小时
  const dateRange = getDateRange()
  const standardWorkHours = dateRange.workdays * 8
  const totalHours = stats.value.totalHours || 0
  if (standardWorkHours === 0) return 0
  return Math.min(100, parseFloat(((totalHours / standardWorkHours) * 100).toFixed(1)))
})

// 处理日期变更
const handleDateChange = () => {
  loadStats()
}

// 处理视图模式变更
const handleViewModeChange = () => {
  loadStats()
}

// 处理用户选择变更
const handleUserChange = () => {
  loadStats()
}

// 处理标签页点击
const handleTabClick = (tab) => {
  activeTab.value = tab.props.name
}

// 加载用户列表（仅管理员和部门负责人）
const loadUserList = async () => {
  if (isAdminOrManager.value) {
    try {
      // 调用获取用户列表的API
      const response = await getUserList()
      if (response.code === 200) {
        // 处理分页响应，从records中获取用户列表
        const users = response.data.records || []
        userList.value = users.map(user => ({
          id: user.id,
          realName: user.realName
        }))
        
        // 检查URL参数中是否有userId
        const urlUserId = route.query.userId
        if (urlUserId) {
          selectedUserId.value = parseInt(urlUserId)
        } else {
          // 默认选择当前用户
          selectedUserId.value = userStore.user?.id
        }
        
        // 加载统计数据
        loadStats()
      }
    } catch (error) {
      console.error('加载用户列表失败:', error)
    }
  }
}

// 生成模拟项目详情数据
const generateProjectDetails = () => {
  projectDetails.value = [
    {
      projectName: '时间跟踪系统',
      role: '开发工程师',
      startDate: '2025-01-01',
      endDate: '2025-12-31',
      totalHours: 1200
    },
    {
      projectName: '项目管理平台',
      role: '技术负责人',
      startDate: '2025-03-01',
      endDate: '2025-09-30',
      totalHours: 800
    },
    {
      projectName: '员工管理系统',
      role: '开发工程师',
      startDate: '2025-06-01',
      endDate: '2025-12-31',
      totalHours: 600
    }
  ]
}

// 生成模拟任务状态数据
const generateTaskStatusData = () => {
  taskStatusData.value = [
    { status: '待处理', count: 5, percentage: '25%' },
    { status: '进行中', count: 8, percentage: '40%' },
    { status: '已完成', count: 7, percentage: '35%' }
  ]
}

// 计算考核数据
const calculateAssessment = async () => {
  if (!currentUserId.value) return
  
  try {
    const userId = currentUserId.value
    const date = dateRange.value
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    
    // 获取选择时间范围内正在执行的项目总数
    const projectListResponse = await getProjectList({ current: 1, size: 100 })
    const totalProjects = projectListResponse.code === 200 ? projectListResponse.data.records.filter(project => project.status !== 'COMPLETED').length : 10
    
    // 获取员工在各项目的工时分布
    let avgHoursRatio = 60 // 默认值
    const projectDistributionResponse = await getUserProjectHoursDistribution(userId, year, month)
    if (projectDistributionResponse.code === 200 && projectDistributionResponse.data.length > 0) {
      const totalProjectHours = projectDistributionResponse.data.reduce((sum, project) => sum + (project.hours || 0), 0)
      if (totalProjectHours > 0) {
        // 计算各项目工时占比的平均值
        const avgRatio = projectDistributionResponse.data.reduce((sum, project) => {
          const ratio = (project.hours / totalProjectHours) * 100
          return sum + ratio
        }, 0) / projectDistributionResponse.data.length
        avgHoursRatio = avgRatio
      }
    }
    
    // 计算项目参与度：项目参与度 = (参与项目数 ÷ 总项目数) × 50% + (员工在各项目工时占比平均值) × 50%
    const projectCount = projectDistribution.value.length
    const projectParticipation = stats.value.totalHours && stats.value.totalHours > 0 ? 
      parseFloat((((projectCount || 1) / totalProjects * 0.5 + avgHoursRatio / 100 * 0.5) * 100).toFixed(1)) : 0
    
    // 计算工作饱和度：工作饱和度 (%) = (实际工作时间 ÷ 标准工作时间) × 100%
    // 标准工作时间 = 标准工作日 × 8小时
    const dateRange = getDateRange()
    const standardWorkHours = dateRange.workdays * 8
    const workSaturationValue = stats.value.totalHours && stats.value.totalHours > 0 && standardWorkHours > 0 ? 
      parseFloat(((stats.value.totalHours / standardWorkHours) * 100).toFixed(1)) : 0
    
    // 计算加班情况：加班情况 (%) = (加班工时 ÷ 标准工作时间) × 100%
    const overtimeHours = stats.value.overtimeHours || 0
    const overtime = stats.value.totalHours && stats.value.totalHours > 0 && standardWorkHours > 0 ? 
      parseFloat(((overtimeHours / standardWorkHours) * 100).toFixed(1)) : 0
    
    // 计算任务难度系数：任务难度系统=（（简单任务数*LOW+中等任务数*MEDIUM+复杂任务数*HIGH+极复杂任务数*CRITICAL）÷ 总任务数 ）
    // 任务难度分四类：LOW=1:简单，MEDIUM=2：中等, HIGH=3：复杂, CRITICAL=4：极复杂
    let taskDifficulty = 0
    try {
      // 获取当前时间范围
      const dateRange = getDateRange()
      
      // 调用API获取当前员工在选择时间范围内的任务数据
      const taskResponse = await getUserTasks(userId, {
        current: 1,
        size: 100,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })
      
      if (taskResponse.code === 200 && taskResponse.data.records) {
        const tasks = taskResponse.data.records
        const taskCount = tasks.length
        if (taskCount > 0) {
          // 计算任务难度分布
          const taskDifficultyDistribution = tasks.reduce((distribution, task) => {
            const priority = task.priority || 'LOW'
            distribution[priority] = (distribution[priority] || 0) + 1
            return distribution
          }, {})
          
          const totalTasks = taskCount
          if (totalTasks > 0) {
            const weightedSum = (taskDifficultyDistribution.LOW || 0) * 1 + 
                              (taskDifficultyDistribution.MEDIUM || 0) * 2 + 
                              (taskDifficultyDistribution.HIGH || 0) * 3 + 
                              (taskDifficultyDistribution.CRITICAL || 0) * 4
            taskDifficulty = parseFloat((weightedSum / totalTasks).toFixed(1))
          }
        }
      }
    } catch (error) {
      console.error('计算任务难度系数失败:', error)
    }
    
    // 计算任务完成率：结合按时完成任务数和任务完成进度
    let taskCompletionRate = 0
    try {
      // 获取当前时间范围
      const dateRange = getDateRange()
      
      // 调用API获取当前员工在选择时间范围内的任务数据
      const taskResponse = await getUserTasks(userId, {
        current: 1,
        size: 100,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })
      
      if (taskResponse.code === 200 && taskResponse.data.records) {
        const tasks = taskResponse.data.records
        const taskCount = tasks.length
        if (taskCount > 0) {
          // 计算按时完成任务数（进度为100%的任务）
          const completedTasks = tasks.filter(task => task.progress === 100).length
          // 计算平均任务进度
          const avgTaskProgress = tasks.reduce((sum, task) => sum + (task.progress || 0), 0) / taskCount
          
          // 计算任务完成率：结合按时完成任务数和平均任务进度
          // 权重：按时完成任务数占60%，平均任务进度占40%
          // 计算公式：任务完成率 = [(按时完成任务数 ÷ 总任务数) × 60% + (平均任务进度 ÷ 100) × 40%] × 100%
          taskCompletionRate = parseFloat(((completedTasks / taskCount * 0.6 + avgTaskProgress / 100 * 0.4) * 100).toFixed(1))
        }
      }
    } catch (error) {
      console.error('计算任务完成率失败:', error)
    }
    
    // 计算综合评分
    const totalHoursScore = Math.min(25, (stats.value.totalHours / 160) * 25) // 假设目标160小时/月
    const taskCompletionScore = taskCompletionRate ? (taskCompletionRate / 100) * 20 : 0
    const projectScore = projectParticipation ? (projectParticipation / 100) * 15 : 0
    const difficultyScore = taskDifficulty ? (taskDifficulty / 4) * 15 : 0
    const saturationScore = workSaturationValue ? (workSaturationValue / 100) * 15 : 0
    const overtimeScore = overtime ? (overtime / 100) * 10 : 0
    
    const overallScore = Math.round(totalHoursScore + taskCompletionScore + projectScore + difficultyScore + saturationScore + overtimeScore)
    
    assessmentData.value = {
      projectParticipation: projectParticipation,
      taskDelay: 100 - taskCompletionRate, // 任务延迟率 = 100 - 任务完成率
      taskDifficulty: taskDifficulty * 25, // 转换为百分比
      workSaturation: workSaturationValue,
      overtime: overtime,
      overallScore: overallScore
    }
  } catch (error) {
    console.error('计算考核数据失败:', error)
    // 失败时使用默认值
    assessmentData.value = {
      projectParticipation: 0,
      taskDelay: 0,
      taskDifficulty: 0,
      workSaturation: 0,
      overtime: 0,
      overallScore: 0
    }
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 加载统计数据
const loadStats = async () => {
  if (!currentUserId.value) return
  
  const userId = currentUserId.value
  // 检查URL参数中是否有日期和视图模式
  const urlYear = route.query.year
  const urlMonth = route.query.month
  const urlViewMode = route.query.viewMode
  const urlTab = route.query.tab
  
  let year = dateRange.value.getFullYear()
  let month = dateRange.value.getMonth() + 1
  
  if (urlYear && urlMonth) {
    year = parseInt(urlYear)
    month = parseInt(urlMonth)
    // 更新日期选择器
    dateRange.value = new Date(year, month - 1, 1)
  }
  
  if (urlViewMode) {
    viewMode.value = urlViewMode
  }
  
  if (urlTab) {
    activeTab.value = urlTab
  }
  
  try {
    if (viewMode.value === 'monthly') {
      // 加载月度统计
      const monthlyResponse = await getUserMonthlyStats(userId, year, month)
      if (monthlyResponse.code === 200) {
        stats.value = monthlyResponse.data
      }
      
      // 加载项目工时分布
      const distributionResponse = await getUserProjectHoursDistribution(userId, year, month)
      if (distributionResponse.code === 200) {
        projectDistribution.value = distributionResponse.data
      }
    } else {
      // 加载年度统计
      const yearlyResponse = await getUserYearlyStats(userId, year)
      if (yearlyResponse.code === 200) {
        stats.value = yearlyResponse.data
      }
      
      // 加载项目工时分布（使用12月的数据作为年度分布）
      const distributionResponse = await getUserProjectHoursDistribution(userId, year, 12)
      if (distributionResponse.code === 200) {
        projectDistribution.value = distributionResponse.data
      }
    }
    
    // 加载月度趋势
    const trendResponse = await getUserMonthlyTrend(userId)
    if (trendResponse.code === 200) {
      trendData.value = trendResponse.data.map(item => ({
        ...item,
        month: `${item.year}-${String(item.month).padStart(2, '0')}`
      }))
    }
    
    // 生成模拟数据
    generateProjectDetails()
    generateTaskStatusData()
    
    // 计算考核数据
    await calculateAssessment()
  } catch (error) {
    console.error('加载个人工时统计失败:', error)
  }
}

onMounted(() => {
  console.log('=== 个人工时统计页面加载 ===')
  console.log('用户信息:', userStore.user)
  console.log('用户ID:', userStore.user?.id)
  console.log('是否管理员/经理:', isAdminOrManager.value)
  
  if (userStore.user) {
    if (isAdminOrManager.value) {
      console.log('加载用户列表')
      loadUserList()
    } else {
      console.log('直接加载当前用户数据')
      loadStats()
    }
  } else {
    console.log('用户未登录，显示登录提示')
  }
})
</script>

<style scoped>
.personal-time-statistics {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stats-overview {
  margin-bottom: 30px;
}

.stat-card {
  height: 120px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.stat-icon {
  font-size: 32px;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.overview-row {
  margin-bottom: 20px;
}

.trend-section,
.distribution-section,
.project-details-section,
.tasks-section,
.efficiency-section,
.assessment-section {
  margin-bottom: 30px;
}

.trend-card,
.distribution-card,
.project-details-card,
.tasks-card,
.efficiency-card,
.assessment-card {
  min-height: 400px;
}

.trend-container,
.distribution-container,
.project-details-container,
.tasks-container {
  height: calc(100% - 40px);
  overflow-y: auto;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.efficiency-content {
  height: calc(100% - 40px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.efficiency-item {
  text-align: center;
  padding: 20px;
}

.efficiency-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.efficiency-value {
  font-size: 32px;
  font-weight: bold;
  color: #67C23A;
}

.assessment-content {
  padding: 20px 0;
}

.assessment-item {
  text-align: center;
  padding: 20px;
}

.assessment-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.assessment-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.assessment-value.overall {
  color: #E6A23C;
  font-size: 28px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .personal-time-statistics {
    padding: 10px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .efficiency-value {
    font-size: 24px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .el-date-picker {
    width: 100% !important;
  }
  
  .stat-card {
    margin-bottom: 10px;
  }
  
  .trend-card,
  .distribution-card,
  .project-details-card,
  .tasks-card,
  .efficiency-card,
  .assessment-card {
    min-height: 300px;
  }
}
</style>