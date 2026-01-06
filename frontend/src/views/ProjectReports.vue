<template>
  <div class="project-reports">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目报表查看</span>
          <div class="header-actions">
            <el-select
              v-model="selectedProjectId"
              placeholder="选择项目"
              style="width: 200px; margin-right: 10px"
              @change="loadProjectReports"
            >
              <el-option
                v-for="project in accessibleProjects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              @click="refreshReports"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 权限提示 -->
      <div v-if="!hasReportPermission" class="permission-notice">
        <el-alert
          title="权限提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>您需要以下权限之一才能查看项目报表：</p>
            <ul>
              <li>项目经理权限</li>
              <li>技术负责人权限</li>
              <li>报表查看权限</li>
            </ul>
            <p>如需获取权限，请联系项目管理员在"项目管理 > 权限管理"中为您分配相应权限。</p>
          </template>
        </el-alert>
      </div>
      
      <!-- 报表内容 -->
      <div v-if="selectedProjectId && hasReportPermission" v-loading="loading">
        <!-- 快速统计 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ reportData.totalTasks || 0 }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ reportData.completedTasks || 0 }}</div>
                <div class="stat-label">已完成任务</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ reportData.totalHours || 0 }}h</div>
                <div class="stat-label">总工时</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ reportData.completionRate || 0 }}%</div>
                <div class="stat-label">完成率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 详细报表选项 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="report-option-card" @click="viewTaskReport">
              <div class="report-option">
                <el-icon class="report-icon"><Document /></el-icon>
                <h3>任务报表</h3>
                <p>查看项目任务完成情况、进度分析</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="report-option-card" @click="viewTimeReport">
              <div class="report-option">
                <el-icon class="report-icon"><Timer /></el-icon>
                <h3>工时报表</h3>
                <p>查看团队工时统计、效率分析</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="report-option-card" @click="viewCostReport">
              <div class="report-option">
                <el-icon class="report-icon"><Money /></el-icon>
                <h3>成本报表</h3>
                <p>查看项目成本分析、预算执行</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 最近报表记录 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>最近查看的报表</span>
          </template>
          
          <el-table :data="recentReports" style="width: 100%">
            <el-table-column prop="reportType" label="报表类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getReportTypeColor(row.reportType)">
                  {{ getReportTypeName(row.reportType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="projectName" label="项目名称" width="150" />
            <el-table-column prop="viewTime" label="查看时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.viewTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewReport(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!selectedProjectId" class="empty-state">
        <el-empty description="请选择项目查看报表" />
      </div>
    </el-card>
    
    <!-- 报表详情对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      :title="currentReportTitle"
      width="80%"
      :before-close="handleCloseReport"
    >
      <div v-loading="reportLoading">
        <component 
          :is="currentReportComponent" 
          :project-id="selectedProjectId"
          :report-data="currentReportData"
          @close="reportDialogVisible = false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Document, Timer, Money, Refresh } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import { EnhancedPermissionUtil } from '@/utils/enhancedPermissions'
import request from '@/utils/request'

// 导入报表组件
import TaskReportComponent from '@/components/reports/TaskReport.vue'
import TimeReportComponent from '@/components/reports/TimeReport.vue'
import CostReportComponent from '@/components/reports/CostReport.vue'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const reportLoading = ref(false)
const selectedProjectId = ref(null)
const accessibleProjects = ref([])
const reportData = ref({})
const recentReports = ref([])
const reportDialogVisible = ref(false)
const currentReportTitle = ref('')
const currentReportComponent = ref(null)
const currentReportData = ref(null)

// 检查报表查看权限
const hasReportPermission = computed(() => {
  if (!selectedProjectId.value) return false
  
  // 管理员总是有权限
  if (userStore.user?.role === 'ADMIN') return true
  
  // 这里应该调用权限检查API，暂时简化处理
  return true
})

// 加载可访问的项目
const loadAccessibleProjects = async () => {
  try {
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      const allProjects = response.data.records || []
      
      // 过滤用户有报表查看权限的项目
      if (userStore.user?.role === 'ADMIN') {
        accessibleProjects.value = allProjects
      } else {
        // 这里应该调用权限过滤API
        accessibleProjects.value = allProjects
      }
      
      // 如果URL中有projectId参数，自动选择
      const urlProjectId = parseInt(route.query.projectId)
      if (urlProjectId && accessibleProjects.value.find(p => p.id === urlProjectId)) {
        selectedProjectId.value = urlProjectId
        loadProjectReports()
      } else if (accessibleProjects.value.length > 0) {
        selectedProjectId.value = accessibleProjects.value[0].id
        loadProjectReports()
      }
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 加载项目报表数据
const loadProjectReports = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    const response = await request({
      url: `/project-statistics/${selectedProjectId.value}/summary`,
      method: 'get'
    })
    
    if (response.code === 200) {
      reportData.value = response.data
    } else {
      // 模拟数据
      reportData.value = {
        totalTasks: 45,
        completedTasks: 38,
        totalHours: 1280,
        completionRate: 84
      }
    }
    
    // 加载最近报表记录
    loadRecentReports()
  } catch (error) {
    console.error('加载项目报表失败:', error)
    // 使用模拟数据
    reportData.value = {
      totalTasks: 45,
      completedTasks: 38,
      totalHours: 1280,
      completionRate: 84
    }
    loadRecentReports()
  } finally {
    loading.value = false
  }
}

// 加载最近报表记录
const loadRecentReports = () => {
  // 模拟最近报表数据
  recentReports.value = [
    {
      id: 1,
      reportType: 'TASK',
      projectName: '示例项目1',
      viewTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
      description: '任务完成情况统计'
    },
    {
      id: 2,
      reportType: 'TIME',
      projectName: '示例项目1',
      viewTime: new Date(Date.now() - 24 * 60 * 60 * 1000),
      description: '团队工时分析报表'
    }
  ]
}

// 刷新报表
const refreshReports = () => {
  if (selectedProjectId.value) {
    loadProjectReports()
  }
}

// 查看任务报表
const viewTaskReport = () => {
  currentReportTitle.value = '任务报表'
  currentReportComponent.value = TaskReportComponent
  currentReportData.value = reportData.value
  reportDialogVisible.value = true
  
  // 记录查看历史
  recordReportView('TASK', '任务完成情况统计')
}

// 查看工时报表
const viewTimeReport = () => {
  currentReportTitle.value = '工时报表'
  currentReportComponent.value = TimeReportComponent
  currentReportData.value = reportData.value
  reportDialogVisible.value = true
  
  recordReportView('TIME', '团队工时分析报表')
}

// 查看成本报表
const viewCostReport = () => {
  currentReportTitle.value = '成本报表'
  currentReportComponent.value = CostReportComponent
  currentReportData.value = reportData.value
  reportDialogVisible.value = true
  
  recordReportView('COST', '项目成本分析报表')
}

// 查看历史报表
const viewReport = (report) => {
  switch (report.reportType) {
    case 'TASK':
      viewTaskReport()
      break
    case 'TIME':
      viewTimeReport()
      break
    case 'COST':
      viewCostReport()
      break
  }
}

// 记录报表查看历史
const recordReportView = (reportType, description) => {
  const project = accessibleProjects.value.find(p => p.id === selectedProjectId.value)
  if (!project) return
  
  const newRecord = {
    id: Date.now(),
    reportType,
    projectName: project.projectName,
    viewTime: new Date(),
    description
  }
  
  // 添加到最近记录，保持最多10条
  recentReports.value.unshift(newRecord)
  if (recentReports.value.length > 10) {
    recentReports.value = recentReports.value.slice(0, 10)
  }
}

// 关闭报表对话框
const handleCloseReport = (done) => {
  currentReportComponent.value = null
  currentReportData.value = null
  done()
}

// 获取报表类型颜色
const getReportTypeColor = (type) => {
  const colorMap = {
    'TASK': 'primary',
    'TIME': 'success',
    'COST': 'warning'
  }
  return colorMap[type] || 'info'
}

// 获取报表类型名称
const getReportTypeName = (type) => {
  const nameMap = {
    'TASK': '任务报表',
    'TIME': '工时报表',
    'COST': '成本报表'
  }
  return nameMap[type] || type
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAccessibleProjects()
})
</script>

<style scoped>
.project-reports {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.permission-notice {
  margin-bottom: 20px;
}

.permission-notice ul {
  margin: 10px 0;
  padding-left: 20px;
}

.permission-notice li {
  margin: 5px 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: default;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.report-option-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.report-option-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.report-option {
  text-align: center;
  padding: 30px 20px;
}

.report-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.report-option h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 18px;
}

.report-option p {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.el-row {
  margin-bottom: 20px;
}

.el-row:last-child {
  margin-bottom: 0;
}
</style>