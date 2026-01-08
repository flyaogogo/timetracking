<template>
  <div class="project-status-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目状态管理</span>
          <el-button type="primary" @click="showStatusDefinitions">
            <el-icon><QuestionFilled /></el-icon>
            状态说明
          </el-button>
        </div>
      </template>
      
      <!-- 项目选择 -->
      <div class="project-selector">
        <el-select
          v-model="selectedProjectId"
          placeholder="选择项目"
          style="width: 300px"
          @change="loadProjectInfo"
          filterable
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="`${project.projectName} (${getStatusText(project.status)})`"
            :value="project.id"
          />
        </el-select>
      </div>
      
      <!-- 项目状态信息 -->
      <div v-if="selectedProject" class="project-info">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="info-card">
              <h3>项目基本信息</h3>
              <div class="info-item">
                <label>项目名称：</label>
                <span>{{ selectedProject.projectName }}</span>
              </div>
              <div class="info-item">
                <label>项目编号：</label>
                <span>{{ selectedProject.projectCode }}</span>
              </div>
              <div class="info-item">
                <label>当前状态：</label>
                <el-tag :type="getStatusType(selectedProject.status)" size="large">
                  {{ getStatusText(selectedProject.status) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>项目经理：</label>
                <span>{{ selectedProject.managerName || '未指定' }}</span>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="info-card">
              <h3>状态变更操作</h3>
              <div class="status-actions">
                <div class="current-status">
                  <span>当前状态：</span>
                  <el-tag :type="getStatusType(selectedProject.status)" size="large">
                    {{ getStatusText(selectedProject.status) }}
                  </el-tag>
                </div>
                
                <div class="status-change" v-if="allowedTransitions.length > 0">
                  <el-form :model="statusChangeForm" label-width="80px">
                    <el-form-item label="变更为：">
                      <el-select v-model="statusChangeForm.newStatus" placeholder="选择新状态" @change="onStatusChange">
                        <el-option
                          v-for="status in allowedTransitions"
                          :key="status"
                          :label="getStatusText(status)"
                          :value="status"
                        />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item label="变更原因：">
                      <el-input
                        v-model="statusChangeForm.reason"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入状态变更原因（建议填写）"
                      />
                    </el-form-item>
                    
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        @click="changeProjectStatus"
                        :loading="changeLoading"
                        :disabled="!statusChangeForm.newStatus"
                      >
                        确认变更
                      </el-button>
                      <el-button @click="resetForm">
                        重置
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
                
                <div v-else class="no-transitions">
                  <el-alert
                    title="当前状态不允许变更"
                    type="info"
                    :description="`项目处于'${getStatusText(selectedProject.status)}'状态，无法进行状态变更`"
                    show-icon
                    :closable="false"
                  />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 状态变更历史 -->
        <el-card class="history-card">
          <template #header>
            <div class="card-header">
              <span>状态变更历史</span>
              <el-button type="info" size="small" @click="refreshHistory">
                  <el-icon><RefreshRight /></el-icon>
                  刷新
                </el-button>
            </div>
          </template>
          
          <div v-if="statusHistory.loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="statusHistory.list.length === 0" class="empty-history">
            <el-empty description="暂无状态变更记录" />
          </div>
          
          <div v-else class="history-list">
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in statusHistory.list"
                :key="item.id || index"
                :timestamp="formatDate(item.changedTime)"
                :type="getStatusChangeType(item.oldStatus, item.newStatus)"
                placement="top"
              >
                <div class="timeline-content">
                  <div class="status-change-info">
                    <span class="old-status">
                      <el-tag :type="getStatusType(item.oldStatus)" size="small">
                        {{ getStatusText(item.oldStatus) }}
                      </el-tag>
                    </span>
                    <span class="arrow">→</span>
                    <span class="new-status">
                      <el-tag :type="getStatusType(item.newStatus)" size="small">
                        {{ getStatusText(item.newStatus) }}
                      </el-tag>
                    </span>
                  </div>
                  <div v-if="item.changeReason" class="change-reason">
                    <strong>变更原因：</strong>{{ item.changeReason }}
                  </div>
                  <div class="changed-by">
                    <strong>变更人：</strong>{{ item.changedBy }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="请选择项目查看状态信息" />
      </div>
    </el-card>
    
    <!-- 状态说明对话框 -->
    <el-dialog
      v-model="statusDefinitionsVisible"
      title="项目状态说明"
      width="800px"
    >
      <div class="status-definitions">
        <div v-if="Object.keys(statusDefinitions).length === 0" class="empty-definitions">
          <el-empty description="暂无状态定义" />
        </div>
        <div v-else class="definitions-list">
          <div v-for="(definition, status) in statusDefinitions" :key="status" class="status-definition">
            <div class="status-header">
              <el-tag :type="getStatusType(status)" size="large">
                {{ definition.text }}
              </el-tag>
              <span class="status-code">{{ status }}</span>
            </div>
            <div class="status-short-description">
              <strong>简短描述：</strong>{{ definition.shortDescription || definition.description }}
            </div>
            <div class="status-long-description" v-if="definition.longDescription">
              <strong>详细说明：</strong>{{ definition.longDescription }}
            </div>
            <div class="status-applicable" v-if="definition.applicableScenarios">
              <strong>适用场景：</strong>{{ definition.applicableScenarios }}
            </div>
            <div class="status-transitions">
              <span class="transitions-label">可转换为：</span>
              <template v-if="definition.allowedTransitions && definition.allowedTransitions.length > 0">
                <el-tag
                  v-for="transition in definition.allowedTransitions"
                  :key="transition"
                  :type="getStatusType(transition)"
                  size="small"
                  style="margin-right: 8px"
                >
                  {{ getStatusText(transition) }}
                </el-tag>
              </template>
              <template v-else>
                <span class="no-transitions-text">终态状态，无法转换</span>
              </template>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="statusDefinitionsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled, RefreshRight } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'

const loading = ref(false)
const changeLoading = ref(false)
const statusDefinitionsVisible = ref(false)
const selectedProjectId = ref(null)
const selectedProject = ref(null)
const projects = ref([])
const statusDefinitions = ref({})

const route = useRoute()

const statusChangeForm = reactive({
  newStatus: '',
  reason: ''
})

// 状态变更历史
const statusHistory = reactive({
  loading: false,
  list: []
})

// 计算允许的状态转换
const allowedTransitions = computed(() => {
  if (!selectedProject.value || !statusDefinitions.value[selectedProject.value.status]) {
    return []
  }
  return statusDefinitions.value[selectedProject.value.status].allowedTransitions || []
})

// 加载项目列表 - 只获取当前用户管理的项目
const loadProjects = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/projects/managed',
      method: 'get',
      params: {
        current: 1,
        size: 1000
      }
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      console.log('加载到管理项目数量:', projects.value.length)
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
  return true // 返回Promise，以便在onMounted中使用.then()
}

// 加载状态定义
const loadStatusDefinitions = async () => {
  try {
    console.log('开始加载状态定义...')
    const response = await request({
      url: '/project-status/definitions',
      method: 'get'
    })
    
    console.log('状态定义响应原始数据:', response)
    
    // 检查响应格式
    if (typeof response === 'object' && response !== null) {
      // 后端直接返回了完整的Result对象，包含code、message和data字段
      if (response.code === 200) {
        const data = response.data
        if (data && data.statusDefinitions) {
          statusDefinitions.value = data.statusDefinitions
          console.log('状态定义加载成功:', Object.keys(statusDefinitions.value).length, '个状态')
          ElMessage.success('状态定义加载成功')
        } else {
          console.error('状态定义数据结构错误:', data)
          statusDefinitions.value = {}
          ElMessage.warning('状态定义数据格式异常')
        }
      } else {
        console.error('加载状态定义失败:', response.message)
        statusDefinitions.value = {}
        ElMessage.error(`加载状态定义失败: ${response.message}`)
      }
    } else {
      console.error('响应格式错误: 不是有效的JSON对象', response)
      statusDefinitions.value = {}
      ElMessage.error('加载状态定义失败: 响应格式错误')
    }
  } catch (error) {
    console.error('加载状态定义网络错误:', error)
    console.error('错误详情:', JSON.stringify(error, null, 2))
    statusDefinitions.value = {}
    if (error.response) {
      console.error('错误响应数据:', error.response)
      console.error('错误响应数据:', JSON.stringify(error.response, null, 2))
      ElMessage.error(`加载状态定义失败: ${error.response.data?.message || '服务器错误'}`)
    } else if (error.request) {
      console.error('错误请求数据:', error.request)
      ElMessage.error('加载状态定义失败: 无响应数据')
    } else {
      console.error('错误信息:', error.message)
      ElMessage.error(`加载状态定义失败: ${error.message}`)
    }
  }
}

// 加载项目信息
const loadProjectInfo = () => {
  if (!selectedProjectId.value) {
    selectedProject.value = null
    statusHistory.list = []
    return
  }
  
  selectedProject.value = projects.value.find(p => p.id === selectedProjectId.value)
  
  // 重置表单
  resetForm()
  
  // 加载状态变更历史
  loadStatusHistory()
}

// 加载状态变更历史
const loadStatusHistory = async () => {
  if (!selectedProjectId.value) return
  
  statusHistory.loading = true
  try {
    const response = await request({
      url: `/project-status/${selectedProjectId.value}/history`,
      method: 'get'
    })
    
    console.log('状态历史响应数据:', response)
    
    // 后端直接返回了完整的Result对象，包含code、message和data字段
    if (typeof response === 'object' && response !== null) {
      if (response.code === 200) {
        const data = response.data
        if (data) {
          statusHistory.list = data.historyList || []
          console.log('状态变更历史加载成功:', statusHistory.list.length, '条记录')
        } else {
          console.error('状态历史数据结构错误:', data)
          statusHistory.list = []
          ElMessage.warning('状态历史数据格式异常')
        }
      } else {
        console.error('加载状态变更历史失败:', response.message)
        ElMessage.error(`加载状态变更历史失败: ${response.message}`)
        statusHistory.list = []
      }
    } else {
      console.error('响应格式错误: 不是有效的JSON对象', response)
      ElMessage.error('加载状态变更历史失败: 响应格式错误')
      statusHistory.list = []
    }
  } catch (error) {
    console.error('加载状态变更历史失败:', error)
    console.error('错误详情:', JSON.stringify(error, null, 2))
    if (error.response) {
      // 处理HTTP错误
      const { status, data } = error.response
      if (status === 401) {
        // 登录已过期，会被拦截器处理
      } else if (status === 403) {
        ElMessage.warning('您没有权限查看状态变更历史')
        statusHistory.list = []
      } else if (status === 404) {
        ElMessage.warning('项目不存在或状态历史未找到')
        statusHistory.list = []
      } else {
        ElMessage.error(`加载失败: ${data?.message || '服务器错误'}`)
        statusHistory.list = []
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
      statusHistory.list = []
    }
  } finally {
    statusHistory.loading = false
  }
}

// 刷新状态变更历史
const refreshHistory = () => {
  loadStatusHistory()
}

// 重置表单
const resetForm = () => {
  statusChangeForm.newStatus = ''
  statusChangeForm.reason = ''
}

// 状态变更时的回调
const onStatusChange = () => {
  // 可以添加一些额外的逻辑，比如根据选择的状态给出建议的变更原因
  // 例如：如果变更为PAUSED，可以建议填写暂停原因
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

// 获取状态变更类型（用于时间线颜色）
const getStatusChangeType = (oldStatus, newStatus) => {
  if (newStatus === 'COMPLETED') return 'success'
  if (newStatus === 'PAUSED') return 'warning'
  if (newStatus === 'CANCELLED') return 'danger'
  if (newStatus === 'IN_PROGRESS') return 'primary'
  if (newStatus === 'PLANNING') return 'info'
  return 'info'
}

// 变更项目状态
const changeProjectStatus = async () => {
  if (!statusChangeForm.newStatus) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要将项目"${selectedProject.value.projectName}"的状态从"${getStatusText(selectedProject.value.status)}"变更为"${getStatusText(statusChangeForm.newStatus)}"吗？`,
      '确认状态变更',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    changeLoading.value = true
    
    const response = await request({
      url: `/project-status/${selectedProjectId.value}/change-status`,
      method: 'post',
      params: {
        newStatus: statusChangeForm.newStatus,
        reason: statusChangeForm.reason
      }
    })
    
    console.log('状态变更响应数据:', response)
    
    // 后端直接返回了完整的Result对象，包含code、message和data字段
    if (typeof response === 'object' && response !== null) {
      if (response.code === 200) {
        ElMessage.success('项目状态变更成功')
        
        // 更新本地项目状态
        selectedProject.value.status = statusChangeForm.newStatus
        const projectIndex = projects.value.findIndex(p => p.id === selectedProjectId.value)
        if (projectIndex !== -1) {
          projects.value[projectIndex].status = statusChangeForm.newStatus
        }
        
        // 重置表单
        resetForm()
        
        // 刷新状态变更历史
        loadStatusHistory()
      } else {
        console.error('状态变更失败:', response.message)
        ElMessage.error(response.message || '状态变更失败')
      }
    } else {
      console.error('响应格式错误: 不是有效的JSON对象', response)
      ElMessage.error('状态变更失败: 响应格式错误')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态变更失败:', error)
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.message || '状态变更失败')
      } else {
        ElMessage.error('状态变更失败')
      }
    }
  } finally {
    changeLoading.value = false
  }
}

// 显示状态说明
const showStatusDefinitions = () => {
  statusDefinitionsVisible.value = true
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status) => {
  const typeMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'PAUSED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

// 监听路由变化，自动选择项目
watch(() => route.query.projectId, (newProjectId) => {
  if (newProjectId && projects.value.length > 0) {
    const projectId = parseInt(newProjectId)
    // 检查项目是否存在于列表中
    const project = projects.value.find(p => p.id === projectId)
    if (project) {
      selectedProjectId.value = projectId
      loadProjectInfo()
    }
  }
})

onMounted(() => {
  loadProjects().then(() => {
    // 加载完项目列表后，检查URL是否包含projectId参数
    const projectId = route.query.projectId
    if (projectId) {
      const parsedProjectId = parseInt(projectId)
      // 检查项目是否存在于列表中
      const project = projects.value.find(p => p.id === parsedProjectId)
      if (project) {
        selectedProjectId.value = parsedProjectId
        loadProjectInfo()
      }
    }
  })
  loadStatusDefinitions()
})
</script>

<style scoped>
.project-status-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-selector {
  margin-bottom: 20px;
}

.project-info {
  margin-top: 20px;
}

.info-card {
  height: 400px;
}

.info-card h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  width: 80px;
  flex-shrink: 0;
}

.status-actions {
  height: 100%;
}

.current-status {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-change {
  margin-top: 20px;
}

.no-transitions {
  margin-top: 20px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 状态说明样式 */
.status-definitions {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px 0;
}

.empty-definitions {
  padding: 40px 0;
  text-align: center;
}

.status-definition {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 6px;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.status-definition:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-color: #dcdfe6;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.status-code {
  font-family: monospace;
  color: #909399;
  font-size: 13px;
  background-color: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-short-description,
.status-long-description,
.status-applicable {
  margin-bottom: 12px;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.status-short-description strong,
.status-long-description strong,
.status-applicable strong {
  color: #303133;
  margin-right: 8px;
}

.status-transitions {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.transitions-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  flex-shrink: 0;
  margin-top: 2px;
}

.no-transitions-text {
  color: #909399;
  font-size: 13px;
  background-color: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
}

.definitions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 状态变更历史样式 */
.history-card {
  margin-top: 20px;
}

.loading-container {
  padding: 20px 0;
}

.empty-history {
  padding: 40px 0;
  text-align: center;
}

.history-list {
  padding: 10px 0;
}

.timeline-content {
  padding: 10px;
  background-color: #fafafa;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.status-change-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.status-change-info .arrow {
  font-weight: bold;
  color: #909399;
}

.change-reason {
  margin: 8px 0;
  padding: 8px;
  background-color: #fff;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.5;
}

.changed-by {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .info-card {
    height: auto;
    margin-bottom: 20px;
  }
  
  .el-col {
    margin-bottom: 20px;
  }
}
</style>