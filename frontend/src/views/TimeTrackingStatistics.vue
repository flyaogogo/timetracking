<template>
  <div class="time-tracking-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工时统计管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showBatchUpdateDialog">
              <el-icon><Refresh /></el-icon>
              批量更新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计概览 -->
      <div class="statistics-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总任务数" :value="overview.totalTasks" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="总项目数" :value="overview.totalProjects" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="总工时记录" :value="overview.totalTimeEntries" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已审核工时" :value="overview.approvedHours" suffix="h" />
          </el-col>
        </el-row>
      </div>
      
      <!-- 操作选项卡 -->
      <el-tabs v-model="activeTab" class="statistics-tabs">
        <!-- 任务工时统计 -->
        <el-tab-pane label="任务工时统计" name="tasks">
          <div class="task-statistics">
            <div class="search-bar">
              <el-select
                v-model="selectedProjectId"
                placeholder="选择项目"
                style="width: 200px; margin-right: 10px"
                clearable
                @change="loadTaskStatistics"
              >
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.projectName"
                  :value="project.id"
                />
              </el-select>
              
              <el-button type="primary" @click="loadTaskStatistics">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
            </div>
            
            <el-table
              v-loading="taskLoading"
              :data="taskStatistics"
              style="width: 100%; margin-top: 20px"
            >
              <el-table-column prop="taskName" label="任务名称" min-width="150" />
              <el-table-column prop="projectName" label="所属项目" width="120" />
              <el-table-column prop="assigneeName" label="执行人" width="100" />
              <el-table-column prop="estimatedHours" label="预估工时" width="100">
                <template #default="{ row }">
                  {{ row.estimatedHours || 0 }}h
                </template>
              </el-table-column>
              <el-table-column prop="actualHours" label="实际工时" width="100">
                <template #default="{ row }">
                  <span :class="getHoursVarianceClass(row)">
                    {{ row.actualHours || 0 }}h
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="progress" label="进度" width="120">
                <template #default="{ row }">
                  <el-progress :percentage="row.progress || 0" :stroke-width="8" />
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusColor(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="variance" label="工时偏差" width="100">
                <template #default="{ row }">
                  <span :class="getVarianceClass(row)">
                    {{ calculateVariance(row) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    text
                    @click="updateTaskHours(row.id)"
                  >
                    更新工时
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    text
                    @click="viewTaskDetails(row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 项目工时汇总 -->
        <el-tab-pane label="项目工时汇总" name="projects">
          <div class="project-statistics">
            <el-table
              v-loading="projectLoading"
              :data="projectStatistics"
              style="width: 100%"
            >
              <el-table-column prop="projectName" label="项目名称" min-width="150" />
              <el-table-column prop="totalTasks" label="任务总数" width="100" />
              <el-table-column prop="completedTasks" label="已完成" width="100" />
              <el-table-column prop="estimatedHours" label="预估工时" width="100">
                <template #default="{ row }">
                  {{ row.estimatedHours || 0 }}h
                </template>
              </el-table-column>
              <el-table-column prop="actualHours" label="实际工时" width="100">
                <template #default="{ row }">
                  <span :class="getProjectHoursVarianceClass(row)">
                    {{ row.actualHours || 0 }}h
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="taskCompletionRate" label="任务完成率" width="120">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.taskCompletionRate || 0" 
                    :stroke-width="8"
                    :color="getCompletionRateColor(row.taskCompletionRate)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="approvedEntries" label="已审核工时" width="100" />
              <el-table-column prop="pendingEntries" label="待审核工时" width="100" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    text
                    @click="updateProjectHours(row.projectId)"
                  >
                    更新汇总
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    text
                    @click="viewProjectDetails(row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 批量更新对话框 -->
    <el-dialog
      v-model="batchUpdateDialogVisible"
      title="批量更新工时统计"
      width="500px"
    >
      <div class="batch-update-options">
        <el-alert
          title="批量更新说明"
          description="批量更新将重新计算所有任务的实际工时和进度，以及所有项目的工时汇总。此操作可能需要一些时间。"
          type="info"
          show-icon
          :closable="false"
        />
        
        <div class="update-options" style="margin-top: 20px;">
          <el-checkbox v-model="updateOptions.tasks">更新所有任务工时和进度</el-checkbox>
          <el-checkbox v-model="updateOptions.projects">更新所有项目工时汇总</el-checkbox>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="batchUpdateDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="batchUpdateLoading"
          @click="executeBatchUpdate"
        >
          开始更新
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      title="任务工时统计详情"
      width="800px"
    >
      <div v-if="currentTaskDetail" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTaskDetail.taskName }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusColor(currentTaskDetail.status)">
              {{ getStatusText(currentTaskDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预估工时">{{ currentTaskDetail.estimatedHours || 0 }}h</el-descriptions-item>
          <el-descriptions-item label="实际工时">{{ currentTaskDetail.actualHours || 0 }}h</el-descriptions-item>
          <el-descriptions-item label="进度">{{ currentTaskDetail.progress || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="工时偏差">
            <span :class="getVarianceClass(currentTaskDetail)">
              {{ calculateVariance(currentTaskDetail) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="工时记录总数">{{ currentTaskDetail.totalTimeEntries || 0 }}</el-descriptions-item>
          <el-descriptions-item label="已审核记录">{{ currentTaskDetail.approvedTimeEntries || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待审核记录">{{ currentTaskDetail.pendingTimeEntries || 0 }}</el-descriptions-item>
          <el-descriptions-item label="偏差百分比">
            <span :class="getVariancePercentageClass(currentTaskDetail)">
              {{ calculateVariancePercentage(currentTaskDetail) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 项目详情对话框 -->
    <el-dialog
      v-model="projectDetailDialogVisible"
      title="项目工时统计详情"
      width="800px"
    >
      <div v-if="currentProjectDetail" class="project-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称">{{ currentProjectDetail.projectName }}</el-descriptions-item>
          <el-descriptions-item label="任务总数">{{ currentProjectDetail.totalTasks || 0 }}</el-descriptions-item>
          <el-descriptions-item label="已完成任务">{{ currentProjectDetail.completedTasks || 0 }}</el-descriptions-item>
          <el-descriptions-item label="任务完成率">{{ currentProjectDetail.taskCompletionRate || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="预估工时">{{ currentProjectDetail.estimatedHours || 0 }}h</el-descriptions-item>
          <el-descriptions-item label="实际工时">{{ currentProjectDetail.actualHours || 0 }}h</el-descriptions-item>
          <el-descriptions-item label="工时记录总数">{{ currentProjectDetail.totalTimeEntries || 0 }}</el-descriptions-item>
          <el-descriptions-item label="已审核记录">{{ currentProjectDetail.approvedTimeEntries || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  updateTaskActualHours, 
  updateProjectActualHours,
  updateAllTasksActualHours,
  updateAllProjectsActualHours,
  getTaskTimeStatistics,
  getProjectTimeStatistics
} from '@/api/timeTrackingStatistics'
import { getTaskList } from '@/api/task'
import { getProjectList } from '@/api/project'

const activeTab = ref('tasks')
const taskLoading = ref(false)
const projectLoading = ref(false)
const batchUpdateLoading = ref(false)
const batchUpdateDialogVisible = ref(false)
const taskDetailDialogVisible = ref(false)
const projectDetailDialogVisible = ref(false)

const selectedProjectId = ref(null)
const projects = ref([])
const taskStatistics = ref([])
const projectStatistics = ref([])
const currentTaskDetail = ref(null)
const currentProjectDetail = ref(null)

const overview = reactive({
  totalTasks: 0,
  totalProjects: 0,
  totalTimeEntries: 0,
  approvedHours: 0
})

const updateOptions = reactive({
  tasks: true,
  projects: true
})

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      overview.totalProjects = projects.value.length
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 加载任务统计
const loadTaskStatistics = async () => {
  taskLoading.value = true
  try {
    const params = {
      current: 1,
      size: 100
    }
    
    if (selectedProjectId.value) {
      params.projectId = selectedProjectId.value
    }
    
    const response = await getTaskList(params)
    
    if (response.code === 200) {
      taskStatistics.value = response.data.records || []
      overview.totalTasks = taskStatistics.value.length
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
    ElMessage.error('加载任务统计失败')
  } finally {
    taskLoading.value = false
  }
}

// 加载项目统计
const loadProjectStatistics = async () => {
  projectLoading.value = true
  try {
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      const projects = response.data.records || []
      
      // 为每个项目获取详细统计
      const statisticsPromises = projects.map(async (project) => {
        try {
          const statsResponse = await getProjectTimeStatistics(project.id)
          if (statsResponse.code === 200) {
            return {
              ...project,
              ...statsResponse.data
            }
          }
          return project
        } catch (error) {
          console.error(`获取项目${project.id}统计失败:`, error)
          return project
        }
      })
      
      projectStatistics.value = await Promise.all(statisticsPromises)
    }
  } catch (error) {
    console.error('加载项目统计失败:', error)
    ElMessage.error('加载项目统计失败')
  } finally {
    projectLoading.value = false
  }
}

// 更新任务工时
const updateTaskHours = async (taskId) => {
  try {
    const response = await updateTaskActualHours(taskId)
    if (response.code === 200) {
      ElMessage.success('任务工时更新成功')
      loadTaskStatistics()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 更新项目工时
const updateProjectHours = async (projectId) => {
  try {
    const response = await updateProjectActualHours(projectId)
    if (response.code === 200) {
      ElMessage.success('项目工时汇总更新成功')
      loadProjectStatistics()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 显示批量更新对话框
const showBatchUpdateDialog = () => {
  batchUpdateDialogVisible.value = true
}

// 执行批量更新
const executeBatchUpdate = async () => {
  if (!updateOptions.tasks && !updateOptions.projects) {
    ElMessage.warning('请至少选择一个更新选项')
    return
  }
  
  batchUpdateLoading.value = true
  try {
    const promises = []
    
    if (updateOptions.tasks) {
      promises.push(updateAllTasksActualHours())
    }
    
    if (updateOptions.projects) {
      promises.push(updateAllProjectsActualHours())
    }
    
    const results = await Promise.all(promises)
    
    const allSuccess = results.every(result => result.code === 200)
    
    if (allSuccess) {
      ElMessage.success('批量更新完成')
      batchUpdateDialogVisible.value = false
      
      // 重新加载数据
      if (activeTab.value === 'tasks') {
        loadTaskStatistics()
      } else {
        loadProjectStatistics()
      }
    } else {
      ElMessage.error('部分更新失败，请检查日志')
    }
  } catch (error) {
    ElMessage.error('批量更新失败')
  } finally {
    batchUpdateLoading.value = false
  }
}

// 查看任务详情
const viewTaskDetails = async (task) => {
  try {
    const response = await getTaskTimeStatistics(task.id)
    if (response.code === 200) {
      currentTaskDetail.value = response.data
      taskDetailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取任务详情失败')
  }
}

// 查看项目详情
const viewProjectDetails = async (project) => {
  try {
    const response = await getProjectTimeStatistics(project.projectId)
    if (response.code === 200) {
      currentProjectDetail.value = response.data
      projectDetailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取项目详情失败')
  }
}

// 计算工时偏差
const calculateVariance = (row) => {
  if (!row.estimatedHours || !row.actualHours) {
    return '0h'
  }
  
  const variance = row.actualHours - row.estimatedHours
  return variance >= 0 ? `+${variance}h` : `${variance}h`
}

// 计算偏差百分比
const calculateVariancePercentage = (row) => {
  if (!row.estimatedHours || row.estimatedHours === 0) {
    return '0%'
  }
  
  const variance = ((row.actualHours - row.estimatedHours) / row.estimatedHours * 100).toFixed(1)
  return variance >= 0 ? `+${variance}%` : `${variance}%`
}

// 获取工时偏差样式类
const getHoursVarianceClass = (row) => {
  if (!row.estimatedHours || !row.actualHours) {
    return ''
  }
  
  const variance = row.actualHours - row.estimatedHours
  if (variance > 0) {
    return 'text-warning'
  } else if (variance < 0) {
    return 'text-success'
  }
  return ''
}

// 获取项目工时偏差样式类
const getProjectHoursVarianceClass = (row) => {
  return getHoursVarianceClass(row)
}

// 获取偏差样式类
const getVarianceClass = (row) => {
  if (!row.estimatedHours || !row.actualHours) {
    return ''
  }
  
  const variance = row.actualHours - row.estimatedHours
  if (variance > 0) {
    return 'variance-over'
  } else if (variance < 0) {
    return 'variance-under'
  }
  return 'variance-normal'
}

// 获取偏差百分比样式类
const getVariancePercentageClass = (row) => {
  return getVarianceClass(row)
}

// 获取完成率颜色
const getCompletionRateColor = (rate) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'TODO': 'info',
    'IN_PROGRESS': 'warning',
    'REVIEW': 'primary',
    'COMPLETED': 'success',
    'PAUSED': 'danger',
    'CANCELLED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'TODO': '待开始',
    'IN_PROGRESS': '进行中',
    'REVIEW': '待审核',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

onMounted(() => {
  loadProjects()
  loadTaskStatistics()
  loadProjectStatistics()
})
</script>

<style scoped>
.time-tracking-statistics {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-overview {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.statistics-tabs {
  margin-top: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.batch-update-options {
  padding: 20px 0;
}

.update-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-detail,
.project-detail {
  padding: 20px 0;
}

/* 偏差样式 */
.variance-over {
  color: #f56c6c;
  font-weight: bold;
}

.variance-under {
  color: #67c23a;
  font-weight: bold;
}

.variance-normal {
  color: #409eff;
  font-weight: bold;
}

.text-warning {
  color: #e6a23c;
}

.text-success {
  color: #67c23a;
}
</style>