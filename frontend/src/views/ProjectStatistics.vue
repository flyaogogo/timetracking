<template>
  <div class="project-statistics">
    <el-card>
      <!-- 项目选择器 -->
      <div class="toolbar">
        <div class="search-box">
          <el-select
            v-model="selectedProjectId"
            placeholder="选择项目"
            style="width: 300px"
            @change="loadProjectStatistics"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="`${project.projectName} (${project.projectCode})`"
              :value="project.id"
              :disabled="false"
              :class="{ 
                'recommended-option': project.isRecommended,
                'normal-option': project.canFullAccess && !project.isRecommended,
                'limited-option': !project.canFullAccess
              }"
            >
              <span :class="{ 
                'recommended-text': project.isRecommended,
                'normal-text': project.canFullAccess && !project.isRecommended,
                'limited-text': !project.canFullAccess 
              }">
                {{ project.projectName }} ({{ project.projectCode }})
                <el-tag v-if="project.isProjectManager" type="success" size="small" style="margin-left: 8px;">项目经理</el-tag>
                <el-tag v-else-if="project.canFullAccess" type="primary" size="small" style="margin-left: 8px;">全权限</el-tag>
                <el-tag v-else type="info" size="small" style="margin-left: 8px;">参与者</el-tag>
              </span>
            </el-option>
          </el-select>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计数据展示 -->
    <div v-if="statistics" class="statistics-content">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon progress">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ formatPercentage(statistics.progressStats?.progressPercentage) }}</div>
                <div class="stats-label">项目进度</div>
                <div class="stats-detail">{{ statistics.progressStats?.completedTasks }}/{{ statistics.progressStats?.totalTasks }} 任务</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon cost">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ formatPercentage(statistics.financialStats?.costUtilization) }}</div>
                <div class="stats-label">成本使用率</div>
                <div class="stats-detail">{{ formatCurrency(statistics.financialStats?.actualCost) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon schedule">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ getScheduleStatusText(statistics.scheduleStats?.scheduleStatus) }}</div>
                <div class="stats-label">工期状态</div>
                <div class="stats-detail">
                  <span v-if="statistics.scheduleStats?.delayDays">
                    {{ statistics.scheduleStats.delayDays > 0 ? '延期' : '提前' }} {{ Math.abs(statistics.scheduleStats.delayDays) }} 天
                  </span>
                  <span v-else>按计划进行</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon team">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ statistics.teamStats?.teamSize || 0 }}</div>
                <div class="stats-label">团队规模</div>
                <div class="stats-detail">{{ statistics.teamStats?.activeMembers || 0 }} 人活跃</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细分析 -->
      <el-row :gutter="20" class="content-row">
        <!-- 进度分析 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="content-card">
            <template #header>
              <div class="card-header">
                <span>进度分析</span>
              </div>
            </template>
            
            <div class="analysis-section">
              <div class="progress-chart">
                <el-progress
                  type="circle"
                  :percentage="parseFloat(statistics.progressStats?.progressPercentage || 0)"
                  :width="100"
                  :stroke-width="8"
                />
                <div class="progress-details">
                  <div class="detail-item">
                    <span class="label">预估工时</span>
                    <span class="value">{{ statistics.progressStats?.estimatedHours || 0 }}h</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">实际工时</span>
                    <span class="value">{{ statistics.progressStats?.actualHours || 0 }}h</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">剩余工时</span>
                    <span class="value">{{ statistics.progressStats?.remainingHours || 0 }}h</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 成本分析 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="content-card">
            <template #header>
              <div class="card-header">
                <span>成本分析</span>
              </div>
            </template>
            
            <div class="analysis-section">
              <div class="cost-grid">
                <div class="cost-item">
                  <div class="cost-label">合同金额</div>
                  <div class="cost-value contract">{{ formatCurrency(statistics.financialStats?.contractAmount) }}</div>
                </div>
                <div class="cost-item">
                  <div class="cost-label">预算金额</div>
                  <div class="cost-value budget">{{ formatCurrency(statistics.financialStats?.budgetAmount) }}</div>
                </div>
                <div class="cost-item">
                  <div class="cost-label">实际成本</div>
                  <div class="cost-value actual">{{ formatCurrency(statistics.financialStats?.actualCost) }}</div>
                </div>
                <div class="cost-item">
                  <div class="cost-label">利润率</div>
                  <div class="cost-value profit" :class="getProfitClass(statistics.financialStats?.profitMargin)">
                    {{ formatPercentage(statistics.financialStats?.profitMargin) }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 里程碑和团队 -->
      <el-row :gutter="20" class="content-row">
        <!-- 里程碑进度 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="content-card">
            <template #header>
              <div class="card-header">
                <span>里程碑进度</span>
              </div>
            </template>
            
            <div class="milestone-container">
              <div v-if="!statistics.milestones || statistics.milestones.length === 0" class="empty-milestone">
                <el-empty description="暂无里程碑数据" :image-size="80" />
              </div>
              <div v-else class="milestone-list">
                <div
                  v-for="milestone in statistics.milestones.slice(0, 4)"
                  :key="milestone.milestoneName"
                  class="milestone-item"
                  :class="getMilestoneClass(milestone.status)"
                >
                  <div class="milestone-info">
                    <div class="milestone-name">{{ milestone.milestoneName }}</div>
                    <div class="milestone-date">{{ milestone.plannedDate }}</div>
                  </div>
                  <div class="milestone-status">
                    <el-tag :type="getMilestoneTagType(milestone.status)" size="small">
                      {{ getMilestoneStatusText(milestone.status) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 团队统计 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="content-card">
            <template #header>
              <div class="card-header">
                <span>团队统计</span>
              </div>
            </template>
            
            <div class="team-stats">
              <div class="team-overview">
                <div class="team-item">
                  <div class="team-label">团队规模</div>
                  <div class="team-value">{{ statistics.teamStats?.teamSize || 0 }}人</div>
                </div>
                <div class="team-item">
                  <div class="team-label">活跃成员</div>
                  <div class="team-value">{{ statistics.teamStats?.activeMembers || 0 }}人</div>
                </div>
                <div class="team-item">
                  <div class="team-label">日均工时</div>
                  <div class="team-value">{{ statistics.teamStats?.avgDailyHours || 0 }}h</div>
                </div>
                <div class="team-item">
                  <div class="team-label">工作效率</div>
                  <div class="team-value">良好</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 无数据状态 -->
    <el-empty v-else-if="!loading" description="请选择项目查看统计数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getProjectList } from '@/api/project'
import { getProjectStatistics } from '@/api/projectStatistics'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const selectedProjectId = ref(null)
const projects = ref([])
const statistics = ref(null)

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({ current: 1, size: 100 })
    if (response.code === 200) {
      const projectList = response.data.records || []
      
      // 获取用户角色和项目经理权限信息
      const userRole = userStore.user?.role
      const managerPermissions = await getUserProjectManagerPermissions()
      
      // 根据用户角色决定项目权限策略
      projects.value = projectList.map(project => {
        const isProjectManager = managerPermissions.includes(project.id)
        
        // 权限逻辑：
        // 1. ADMIN - 可以查看所有项目，不置灰
        // 2. PROJECT_MANAGER - 可以查看所有项目，不置灰
        // 3. 其他角色 - 只有项目经理权限的项目不置灰，其他置灰但可选
        let canFullAccess = false
        let isRecommended = false
        
        if (userRole === 'ADMIN' || userRole === 'PROJECT_MANAGER') {
          // 全局管理员和项目经理可以访问所有项目
          canFullAccess = true
          isRecommended = isProjectManager // 推荐管理的项目
        } else {
          // 其他角色用户
          canFullAccess = isProjectManager
          isRecommended = isProjectManager
        }
        
        return {
          ...project,
          isProjectManager,
          canFullAccess,
          isRecommended
        }
      })
      
      // 如果URL中有projectId参数，自动选择该项目
      const projectIdFromQuery = route.query.projectId
      if (projectIdFromQuery && projects.value.length > 0) {
        const projectExists = projects.value.find(p => p.id == projectIdFromQuery)
        if (projectExists) {
          selectedProjectId.value = parseInt(projectIdFromQuery)
          loadProjectStatistics()
          return
        }
      }
      
      // 默认选择逻辑
      if (projects.value.length > 0 && !selectedProjectId.value) {
        // 优先选择推荐的项目（用户管理的项目）
        const recommendedProject = projects.value.find(p => p.isRecommended)
        if (recommendedProject) {
          selectedProjectId.value = recommendedProject.id
        } else if (projects.value.length > 0) {
          // 如果没有推荐项目，选择第一个项目
          selectedProjectId.value = projects.value[0].id
        }
        loadProjectStatistics()
      }
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 获取用户的项目经理权限
const getUserProjectManagerPermissions = async () => {
  try {
    const response = await request({
      url: '/project-manager/managed-projects',
      method: 'get'
    })
    
    if (response.code === 200) {
      return (response.data || []).map(project => project.id)
    }
    return []
  } catch (error) {
    console.error('获取项目经理权限失败:', error)
    return []
  }
}

// 加载项目统计数据
const loadProjectStatistics = async () => {
  if (!selectedProjectId.value) {
    statistics.value = null
    return
  }
  
  loading.value = true
  try {
    const response = await getProjectStatistics(selectedProjectId.value)
    if (response.code === 200) {
      statistics.value = response.data
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  if (selectedProjectId.value) {
    loadProjectStatistics()
  }
}

// 格式化百分比
const formatPercentage = (value) => {
  if (value == null) return '0%'
  return `${parseFloat(value).toFixed(1)}%`
}

// 格式化货币
const formatCurrency = (value) => {
  if (value == null) return '¥0'
  return `¥${parseFloat(value).toLocaleString()}`
}

// 获取工期状态文本
const getScheduleStatusText = (status) => {
  const statusMap = {
    'ON_SCHEDULE': '按期进行',
    'AHEAD': '提前进行',
    'DELAYED': '进度延期'
  }
  return statusMap[status] || '未知'
}

// 获取利润率样式类
const getProfitClass = (profitMargin) => {
  if (profitMargin == null) return ''
  const margin = parseFloat(profitMargin)
  if (margin >= 20) return 'high-profit'
  if (margin >= 10) return 'medium-profit'
  if (margin >= 0) return 'low-profit'
  return 'negative-profit'
}

// 获取里程碑样式类
const getMilestoneClass = (status) => {
  return `milestone-${status.toLowerCase()}`
}

// 获取里程碑标签类型
const getMilestoneTagType = (status) => {
  const typeMap = {
    'COMPLETED': 'success',
    'IN_PROGRESS': 'warning',
    'DELAYED': 'danger',
    'PENDING': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取里程碑状态文本
const getMilestoneStatusText = (status) => {
  const statusMap = {
    'COMPLETED': '已完成',
    'IN_PROGRESS': '进行中',
    'DELAYED': '已延期',
    'PENDING': '待开始'
  }
  return statusMap[status] || status
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'MANAGER': '项目经理',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员',
    'DESIGNER': '设计师'
  }
  return roleMap[role] || role
}

// 获取成本类型颜色
const getCostTypeColor = (type) => {
  const colorMap = {
    'LABOR': 'primary',
    'EQUIPMENT': 'success',
    'TRAVEL': 'warning',
    'OUTSOURCING': 'info',
    'OTHER': 'default'
  }
  return colorMap[type] || 'default'
}

// 获取成本类型文本
const getCostTypeText = (type) => {
  const typeMap = {
    'LABOR': '人工成本',
    'EQUIPMENT': '设备成本',
    'TRAVEL': '差旅费用',
    'OUTSOURCING': '外包费用',
    'OTHER': '其他费用'
  }
  return typeMap[type] || type
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-statistics {
  padding: 0;
}

/* 工具栏样式 - 与Projects页面保持一致 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
}

.actions {
  display: flex;
  gap: 12px;
}

/* 项目选择器样式 */
.recommended-option {
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.recommended-text {
  color: #409EFF !important;
  font-weight: 500;
}

.normal-option {
  background-color: #ffffff;
}

.normal-text {
  color: #303133 !important;
}

.limited-option {
  background-color: #fafafa;
  opacity: 0.8;
}

.limited-text {
  color: #909399 !important;
}

/* 统计内容区域 */
.statistics-content {
  margin-top: 20px;
}

/* 统计卡片 - 与Dashboard风格一致 */
.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

.stats-icon.progress {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.cost {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.schedule {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.team {
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
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 2px;
}

.stats-detail {
  font-size: 12px;
  color: #999;
}

/* 内容区域 - 与Dashboard风格一致 */
.content-row {
  margin-bottom: 20px;
}

.content-card {
  margin-bottom: 16px;
  height: 350px;
}

.content-card .el-card__body {
  height: calc(100% - 57px);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 分析区域 */
.analysis-section {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.progress-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  height: 100%;
}

.progress-details {
  flex: 1;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  color: #666;
  font-size: 14px;
}

.detail-item .value {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

/* 成本网格 */
.cost-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  height: 100%;
  align-content: center;
}

.cost-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.cost-item:hover {
  background: #f0f0f0;
  border-color: #d0d0d0;
}

.cost-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.cost-value {
  font-size: 16px;
  font-weight: bold;
}

.cost-value.contract { color: #409EFF; }
.cost-value.budget { color: #67C23A; }
.cost-value.actual { color: #E6A23C; }
.cost-value.profit.high-profit { color: #67C23A; }
.cost-value.profit.medium-profit { color: #409EFF; }
.cost-value.profit.low-profit { color: #E6A23C; }
.cost-value.profit.negative-profit { color: #F56C6C; }

/* 里程碑容器 */
.milestone-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.empty-milestone {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

.milestone-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: 10px 0;
}

.milestone-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  min-height: 60px;
}

.milestone-item:last-child {
  border-bottom: none;
}

.milestone-info {
  flex: 1;
}

.milestone-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.milestone-date {
  font-size: 12px;
  color: #666;
}

.milestone-status {
  flex-shrink: 0;
}

/* 团队统计 */
.team-stats {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.team-overview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  width: 100%;
}

.team-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.team-item:hover {
  background: #f0f0f0;
  border-color: #d0d0d0;
}

.team-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.team-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

/* 空数据状态 */
.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

/* 里程碑状态样式 */
.milestone-item.milestone-completed {
  background-color: rgba(103, 194, 58, 0.1);
}

.milestone-item.milestone-in_progress {
  background-color: rgba(230, 162, 60, 0.1);
}

.milestone-item.milestone-delayed {
  background-color: rgba(245, 108, 108, 0.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .cost-grid,
  .team-overview {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .progress-chart {
    flex-direction: column;
    gap: 20px;
  }
  
  .content-card {
    height: 320px;
  }
}

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
  
  .stats-detail {
    font-size: 11px;
  }
  
  .content-card {
    height: 280px;
    margin-bottom: 12px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .search-box,
  .actions {
    justify-content: center;
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
    height: 250px;
  }
  
  .cost-item,
  .team-item {
    padding: 15px;
  }
}
</style>