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
                      <el-select v-model="statusChangeForm.newStatus" placeholder="选择新状态">
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
                        placeholder="请输入状态变更原因（可选）"
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
                    </el-form-item>
                  </el-form>
                </div>
                
                <div v-else class="no-transitions">
                  <el-alert
                    title="当前状态不允许变更"
                    type="info"
                    :description="`项目处于"${getStatusText(selectedProject.status)}"状态，无法进行状态变更`"
                    show-icon
                    :closable="false"
                  />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
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
        <div v-for="(definition, status) in statusDefinitions" :key="status" class="status-definition">
          <div class="status-header">
            <el-tag :type="getStatusType(status)" size="large">
              {{ definition.text }}
            </el-tag>
            <span class="status-code">{{ status }}</span>
          </div>
          <div class="status-description">
            {{ definition.description }}
          </div>
          <div v-if="definition.allowedTransitions.length > 0" class="status-transitions">
            <span class="transitions-label">可转换为：</span>
            <el-tag
              v-for="transition in definition.allowedTransitions"
              :key="transition"
              :type="getStatusType(transition)"
              size="small"
              style="margin-right: 8px"
            >
              {{ getStatusText(transition) }}
            </el-tag>
          </div>
          <div v-else class="status-transitions">
            <span class="transitions-label">终态状态，无法转换</span>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import request from '@/utils/request'

const loading = ref(false)
const changeLoading = ref(false)
const statusDefinitionsVisible = ref(false)
const selectedProjectId = ref(null)
const selectedProject = ref(null)
const projects = ref([])
const statusDefinitions = ref({})

const statusChangeForm = reactive({
  newStatus: '',
  reason: ''
})

// 计算允许的状态转换
const allowedTransitions = computed(() => {
  if (!selectedProject.value || !statusDefinitions.value[selectedProject.value.status]) {
    return []
  }
  return statusDefinitions.value[selectedProject.value.status].allowedTransitions || []
})

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  try {
    const response = await getProjectList({
      current: 1,
      size: 1000
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

// 加载状态定义
const loadStatusDefinitions = async () => {
  try {
    const response = await request({
      url: '/project-status/definitions',
      method: 'get'
    })
    
    if (response.code === 200) {
      statusDefinitions.value = response.data.statusDefinitions || {}
    }
  } catch (error) {
    console.error('加载状态定义失败:', error)
  }
}

// 加载项目信息
const loadProjectInfo = () => {
  if (!selectedProjectId.value) {
    selectedProject.value = null
    return
  }
  
  selectedProject.value = projects.value.find(p => p.id === selectedProjectId.value)
  
  // 重置表单
  statusChangeForm.newStatus = ''
  statusChangeForm.reason = ''
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
    
    if (response.code === 200) {
      ElMessage.success('项目状态变更成功')
      
      // 更新本地项目状态
      selectedProject.value.status = statusChangeForm.newStatus
      const projectIndex = projects.value.findIndex(p => p.id === selectedProjectId.value)
      if (projectIndex !== -1) {
        projects.value[projectIndex].status = statusChangeForm.newStatus
      }
      
      // 重置表单
      statusChangeForm.newStatus = ''
      statusChangeForm.reason = ''
    } else {
      ElMessage.error(response.message || '状态变更失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态变更失败')
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

onMounted(() => {
  loadProjects()
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
}

.status-definition {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.status-code {
  font-family: monospace;
  color: #909399;
  font-size: 12px;
}

.status-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.status-transitions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.transitions-label {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
</style>