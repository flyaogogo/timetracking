<template>
  <div class="project-manager-reports">
    <div class="page-header">
      <h2>项目报表</h2>
      <p>查看您管理项目的详细报表和分析</p>
    </div>

    <el-row :gutter="20">
      <!-- 报表筛选 -->
      <el-col :span="24">
        <el-card class="filter-card">
          <div class="filter-content">
            <el-select
              v-model="selectedProject"
              placeholder="选择项目"
              style="width: 200px"
              @change="handleProjectChange"
            >
              <el-option
                v-for="project in managedProjects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px; margin-left: 16px"
              @change="handleDateChange"
            />
            
            <el-button type="primary" @click="generateReport" style="margin-left: 16px">
              生成报表
            </el-button>
            
            <el-button type="success" @click="exportReport" style="margin-left: 8px">
              导出Excel
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="selectedProject && reportData" v-loading="loading">
      <!-- 项目概览 -->
      <el-row :gutter="20" class="report-overview">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-number">{{ reportData.totalHours || 0 }}</div>
              <div class="overview-label">总工时</div>
              <div class="overview-trend">
                <span :class="getTrendClass(reportData.hoursTrend)">
                  {{ reportData.hoursTrend > 0 ? '+' : '' }}{{ reportData.hoursTrend || 0 }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-number">{{ reportData.totalCost || 0 }}</div>
              <div class="overview-label">总成本</div>
              <div class="overview-trend">
                <span :class="getTrendClass(reportData.costTrend)">
                  {{ reportData.costTrend > 0 ? '+' : '' }}{{ reportData.costTrend || 0 }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-number">{{ reportData.completionRate || 0 }}%</div>
              <div class="overview-label">完成率</div>
              <div class="overview-trend">
                <span :class="getTrendClass(reportData.completionTrend)">
                  {{ reportData.completionTrend > 0 ? '+' : '' }}{{ reportData.completionTrend || 0 }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-content">
              <div class="overview-number">{{ reportData.efficiency || 0 }}%</div>
              <div class="overview-label">效率指标</div>
              <div class="overview-trend">
                <span :class="getTrendClass(reportData.efficiencyTrend)">
                  {{ reportData.efficiencyTrend > 0 ? '+' : '' }}{{ reportData.efficiencyTrend || 0 }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细报表 -->
      <el-row :gutter="20">
        <!-- 工时分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>工时分布</span>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                工时分布图表
                <br>
                <small>（图表组件需要集成 ECharts）</small>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 成本分析 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>成本分析</span>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                成本分析图表
                <br>
                <small>（图表组件需要集成 ECharts）</small>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 团队工时明细 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>团队工时明细</span>
            </template>
            
            <el-table :data="reportData.teamDetails || []" style="width: 100%">
              <el-table-column prop="userRealName" label="成员" width="120" />
              <el-table-column prop="role" label="角色" width="100" />
              <el-table-column prop="totalHours" label="总工时" width="100">
                <template #default="{ row }">
                  {{ row.totalHours || 0 }}h
                </template>
              </el-table-column>
              <el-table-column prop="billableHours" label="计费工时" width="100">
                <template #default="{ row }">
                  {{ row.billableHours || 0 }}h
                </template>
              </el-table-column>
              <el-table-column prop="cost" label="成本" width="100">
                <template #default="{ row }">
                  ¥{{ row.cost || 0 }}
                </template>
              </el-table-column>
              <el-table-column prop="efficiency" label="效率" width="100">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.efficiency || 0" 
                    :stroke-width="6"
                    :show-text="true"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="completedTasks" label="完成任务" width="100" />
              <el-table-column prop="avgHoursPerTask" label="平均工时/任务" width="120">
                <template #default="{ row }">
                  {{ row.avgHoursPerTask || 0 }}h
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 任务完成情况 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>任务完成情况</span>
            </template>
            
            <el-table :data="reportData.taskSummary || []" style="width: 100%">
              <el-table-column prop="taskType" label="任务类型" width="120" />
              <el-table-column prop="totalTasks" label="总任务数" width="100" />
              <el-table-column prop="completedTasks" label="已完成" width="100" />
              <el-table-column prop="inProgressTasks" label="进行中" width="100" />
              <el-table-column prop="pendingTasks" label="待开始" width="100" />
              <el-table-column prop="completionRate" label="完成率" width="100">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.completionRate || 0" 
                    :stroke-width="6"
                    :show-text="true"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="avgDuration" label="平均耗时" width="100">
                <template #default="{ row }">
                  {{ row.avgDuration || 0 }}天
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else class="empty-state">
      <el-empty description="请选择项目并生成报表" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const managedProjects = ref([])
const selectedProject = ref('')
const dateRange = ref([])
const reportData = ref(null)
const loading = ref(false)

// 获取管理的项目列表
const getManagedProjects = async () => {
  try {
    const response = await request({
      url: '/project-manager/projects',
      method: 'get',
      params: { current: 1, size: 1000 }
    })
    
    if (response.code === 200) {
      managedProjects.value = response.data.records || []
      
      // 如果有项目且没有选中项目，默认选择第一个项目
      if (managedProjects.value.length > 0 && !selectedProject.value) {
        selectedProject.value = managedProjects.value[0].id
        generateReport()
      }
    }
  } catch (error) {
    console.error('获取管理项目失败:', error)
  }
}

// 生成报表
const generateReport = async () => {
  if (!selectedProject.value) {
    ElMessage.warning('请选择项目')
    return
  }
  
  loading.value = true
  try {
    const params = {
      projectId: selectedProject.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    
    const response = await request({
      url: '/project-manager/reports',
      method: 'get',
      params
    })
    
    if (response.code === 200) {
      reportData.value = response.data
      ElMessage.success('报表生成成功')
    } else {
      ElMessage.error(response.message || '生成报表失败')
    }
  } catch (error) {
    ElMessage.error('生成报表失败')
    // 模拟数据用于演示
    reportData.value = {
      totalHours: 1280,
      hoursTrend: 12,
      totalCost: 128000,
      costTrend: 8,
      completionRate: 85,
      completionTrend: 5,
      efficiency: 92,
      efficiencyTrend: 3,
      teamDetails: [
        {
          userRealName: '张三',
          role: '开发工程师',
          totalHours: 160,
          billableHours: 150,
          cost: 24000,
          efficiency: 95,
          completedTasks: 12,
          avgHoursPerTask: 13.3
        },
        {
          userRealName: '李四',
          role: '测试工程师',
          totalHours: 140,
          billableHours: 135,
          cost: 18900,
          efficiency: 88,
          completedTasks: 18,
          avgHoursPerTask: 7.8
        }
      ],
      taskSummary: [
        {
          taskType: '开发任务',
          totalTasks: 25,
          completedTasks: 20,
          inProgressTasks: 3,
          pendingTasks: 2,
          completionRate: 80,
          avgDuration: 5.2
        },
        {
          taskType: '测试任务',
          totalTasks: 30,
          completedTasks: 28,
          inProgressTasks: 2,
          pendingTasks: 0,
          completionRate: 93,
          avgDuration: 2.1
        }
      ]
    }
  } finally {
    loading.value = false
  }
}

// 导出报表
const exportReport = () => {
  if (!reportData.value) {
    ElMessage.warning('请先生成报表')
    return
  }
  
  ElMessage.info('导出功能开发中')
}

// 项目变更处理
const handleProjectChange = () => {
  reportData.value = null
}

// 日期变更处理
const handleDateChange = () => {
  if (selectedProject.value) {
    generateReport()
  }
}

// 获取趋势类名
const getTrendClass = (trend) => {
  if (trend > 0) return 'trend-up'
  if (trend < 0) return 'trend-down'
  return 'trend-stable'
}

onMounted(() => {
  getManagedProjects()
})
</script>

<style scoped>
.project-manager-reports {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  align-items: center;
}

.report-overview {
  margin-bottom: 20px;
}

.overview-card :deep(.el-card__body) {
  padding: 20px;
}

.overview-content {
  text-align: center;
}

.overview-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.overview-trend {
  font-size: 12px;
}

.trend-up {
  color: #67C23A;
}

.trend-down {
  color: #F56C6C;
}

.trend-stable {
  color: #909399;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
  font-size: 16px;
}

.empty-state {
  margin-top: 60px;
}

.el-row {
  margin-bottom: 20px;
}

.el-row:last-child {
  margin-bottom: 0;
}
</style>