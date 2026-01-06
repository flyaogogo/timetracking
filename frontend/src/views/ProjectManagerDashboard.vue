<template>
  <div class="project-manager-dashboard">
    <div class="dashboard-header">
      <h1>项目经理工作台</h1>
      <p>管理您的项目，审批工时，监控进度</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.managedProjectsCount || 0 }}</div>
            <div class="stat-label">管理项目</div>
          </div>
          <el-icon class="stat-icon"><Folder /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.pendingApprovalsCount || 0 }}</div>
            <div class="stat-label">待审批工时</div>
          </div>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.taskStats?.totalTasks || 0 }}</div>
            <div class="stat-label">总任务数</div>
          </div>
          <el-icon class="stat-icon"><List /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.taskStats?.completedTasks || 0 }}</div>
            <div class="stat-label">已完成任务</div>
          </div>
          <el-icon class="stat-icon"><Check /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card class="content-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="batchApprove">
              <el-icon><Check /></el-icon>
              批量审批工时
            </el-button>
            <el-button type="info" @click="viewTeamPerformance">
              <el-icon><TrendCharts /></el-icon>
              团队绩效
            </el-button>
            <el-button type="warning" @click="viewProjectReports">
              <el-icon><DataAnalysis /></el-icon>
              项目报表
            </el-button>
            <el-button type="success" @click="manageTeam">
              <el-icon><User /></el-icon>
              团队管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-content">
      <!-- 管理的项目 -->
      <el-col :span="12">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>项目经理的项目</span>
              <el-button type="primary" size="small" @click="viewAllProjects">查看全部</el-button>
            </div>
          </template>
          <div class="project-list">
            <div 
              v-for="project in dashboardData.managedProjects?.slice(0, 5)" 
              :key="project.id"
              class="project-item"
              @click="viewProject(project.id)"
            >
              <div class="project-info">
                <div class="project-name">{{ project.projectName }}</div>
                <div class="project-status">
                  <el-tag :type="getStatusType(project.status)" size="small">
                    {{ getStatusText(project.status) }}
                  </el-tag>
                </div>
              </div>
              <div class="project-progress">
                <el-progress 
                  :percentage="calculateProgress(project)" 
                  :stroke-width="6"
                  :show-text="false"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待审批工时 -->
      <el-col :span="12">
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>待审批工时</span>
              <el-button type="primary" size="small" @click="viewAllApprovals">查看全部</el-button>
            </div>
          </template>
          <div class="approval-list">
            <div 
              v-for="entry in dashboardData.pendingApprovals?.slice(0, 5)" 
              :key="entry.id"
              class="approval-item"
            >
              <div class="approval-info">
                <div class="user-name">{{ entry.userRealName }}</div>
                <div class="project-name">{{ entry.projectName }}</div>
                <div class="work-date">{{ formatDate(entry.workDate) }}</div>
              </div>
              <div class="approval-actions">
                <div class="duration">{{ entry.duration }}h</div>
                <el-button-group size="small">
                  <el-button type="success" @click="approveEntry(entry.id)">
                    <el-icon><Check /></el-icon>
                  </el-button>
                  <el-button type="danger" @click="rejectEntry(entry.id)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-button-group>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Folder, Clock, List, Check, Close, 
  TrendCharts, DataAnalysis, User 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const dashboardData = ref({})

// 获取工作台数据
const getDashboardData = async () => {
  try {
    const response = await request({
      url: '/project-manager/dashboard',
      method: 'get'
    })
    
    if (response.code === 200) {
      dashboardData.value = response.data
    } else {
      ElMessage.error(response.message || '获取工作台数据失败')
    }
  } catch (error) {
    ElMessage.error('获取工作台数据失败')
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 计算项目进度
const calculateProgress = (project) => {
  if (project.actualHours && project.plannedHours) {
    return Math.min(100, Math.round((project.actualHours / project.plannedHours) * 100))
  }
  return 0
}

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}

// 查看项目
const viewProject = (projectId) => {
  // 跳转到对应项目的统计分析页面，并传递项目ID参数
  router.push(`/project-statistics?projectId=${projectId}`)
}

// 查看所有项目
const viewAllProjects = () => {
  router.push('/project-manager/projects')
}

// 查看所有待审批
const viewAllApprovals = () => {
  router.push('/project-manager/approvals')
}

// 审批工时记录
const approveEntry = async (entryId) => {
  try {
    const response = await request({
      url: `/time-entries/${entryId}/approve`,
      method: 'post',
      data: { comment: '批准' }
    })
    
    if (response.code === 200) {
      ElMessage.success('审批成功')
      getDashboardData() // 刷新数据
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    ElMessage.error('审批失败')
  }
}

// 拒绝工时记录
const rejectEntry = async (entryId) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝工时记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    })
    
    const response = await request({
      url: `/time-entries/${entryId}/reject`,
      method: 'post',
      data: { comment: reason }
    })
    
    if (response.code === 200) {
      ElMessage.success('拒绝成功')
      getDashboardData() // 刷新数据
    } else {
      ElMessage.error(response.message || '拒绝失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝失败')
    }
  }
}

// 批量审批
const batchApprove = () => {
  router.push('/project-manager/approvals')
}

// 查看团队绩效
const viewTeamPerformance = () => {
  router.push('/project-manager/team-performance')
}

// 查看项目报表
const viewProjectReports = () => {
  router.push('/project-manager/reports')
}

// 团队管理
const manageTeam = () => {
  router.push('/project-manager/team')
}

onMounted(() => {
  getDashboardData()
})
</script>

<style scoped>
.project-manager-dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
}

.dashboard-header h1 {
  margin: 0 0 8px 0;
  color: #303133;
}

.dashboard-header p {
  margin: 0;
  color: #909399;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  font-size: 48px;
  color: #E4E7ED;
}

.dashboard-content {
  margin-bottom: 20px;
}

.content-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #EBEEF5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-list, .approval-list {
  max-height: 300px;
  overflow-y: auto;
}

.project-item, .approval-item {
  padding: 12px 0;
  border-bottom: 1px solid #F5F7FA;
  cursor: pointer;
  transition: background-color 0.3s;
}

.project-item:hover {
  background-color: #F5F7FA;
}

.project-item:last-child, .approval-item:last-child {
  border-bottom: none;
}

.project-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.project-name {
  font-weight: 500;
  color: #303133;
}

.project-progress {
  margin-top: 8px;
}

.approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.approval-info {
  flex: 1;
}

.approval-info .user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.approval-info .project-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.approval-info .work-date {
  font-size: 12px;
  color: #C0C4CC;
}

.approval-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.duration {
  font-weight: bold;
  color: #409EFF;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
}
</style>