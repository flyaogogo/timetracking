<template>
  <div class="project-milestones">
    <el-card>
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-select
            v-model="selectedProjectId"
            placeholder="选择项目"
            style="width: 200px; margin-right: 10px"
            @change="loadMilestones"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          
          <el-button @click="loadMilestones">
            <el-icon><Search /></el-icon>
            刷新
          </el-button>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog" :disabled="!selectedProjectId">
            <el-icon><Plus /></el-icon>
            添加里程碑
          </el-button>
        </div>
      </div>
      
      <!-- 里程碑进度概览 -->
      <div v-if="milestoneStats" class="milestone-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ milestoneStats.total }}</div>
                <div class="stat-label">总里程碑</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ milestoneStats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ milestoneStats.inProgress }}</div>
                <div class="stat-label">进行中</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ milestoneStats.delayed }}</div>
                <div class="stat-label">延期</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 里程碑时间轴 -->
      <div class="milestone-timeline">
        <div 
          v-for="(milestone, index) in milestones" 
          :key="milestone.id"
          class="milestone-item"
          :class="{ 
            'completed': milestone.status === 'COMPLETED', 
            'delayed': milestone.status === 'DELAYED',
            'in-progress': milestone.status === 'IN_PROGRESS'
          }"
        >
          <div class="milestone-dot">
            <el-icon v-if="milestone.status === 'COMPLETED'"><Check /></el-icon>
            <el-icon v-else-if="milestone.status === 'DELAYED'"><Warning /></el-icon>
            <el-icon v-else><Clock /></el-icon>
          </div>
          
          <div class="milestone-content">
            <div class="milestone-header">
              <div class="milestone-title">{{ milestone.milestoneName }}</div>
              <div class="milestone-actions">
                <el-button type="primary" size="small" text @click="editMilestone(milestone)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" text @click="deleteMilestone(milestone)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="milestone-info">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="info-item">
                    <span class="info-label">类型：</span>
                    <el-tag :type="getMilestoneTypeColor(milestone.milestoneType)" size="small">
                      {{ getMilestoneTypeText(milestone.milestoneType) }}
                    </el-tag>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="info-item">
                    <span class="info-label">计划日期：</span>
                    <span>{{ formatDate(milestone.plannedDate) }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="info-item">
                    <span class="info-label">实际日期：</span>
                    <span>{{ milestone.actualDate ? formatDate(milestone.actualDate) : '未完成' }}</span>
                  </div>
                </el-col>
              </el-row>
              
              <div class="milestone-progress">
                <div class="progress-header">
                  <span class="progress-label">完成进度</span>
                  <span class="progress-percent">{{ milestone.completionPercentage || 0 }}%</span>
                </div>
                <el-progress 
                  :percentage="milestone.completionPercentage || 0" 
                  :status="getProgressStatus(milestone)"
                  :stroke-width="8"
                />
              </div>
              
              <div v-if="milestone.description" class="milestone-description">
                <span class="info-label">描述：</span>
                <span>{{ milestone.description }}</span>
              </div>
              
              <div v-if="getResponsiblePersonName(milestone.responsiblePersonId)" class="milestone-responsible">
                <span class="info-label">负责人：</span>
                <span>{{ getResponsiblePersonName(milestone.responsiblePersonId) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="milestones.length === 0" class="empty-state">
          <el-empty description="暂无里程碑数据">
            <el-button type="primary" @click="showCreateDialog" :disabled="!selectedProjectId">
              添加第一个里程碑
            </el-button>
          </el-empty>
        </div>
      </div>
    </el-card>
    
    <!-- 新建/编辑里程碑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="里程碑名称" prop="milestoneName">
          <el-input v-model="form.milestoneName" placeholder="请输入里程碑名称" />
        </el-form-item>
        
        <el-form-item label="里程碑类型" prop="milestoneType">
          <el-select v-model="form.milestoneType" placeholder="请选择里程碑类型" style="width: 100%">
            <el-option label="规划阶段 - 需求分析、设计等" value="PLANNING" />
            <el-option label="开发阶段 - 功能开发、编码等" value="DEVELOPMENT" />
            <el-option label="测试阶段 - 功能测试、集成测试" value="TESTING" />
            <el-option label="部署阶段 - 系统部署、上线" value="DEPLOYMENT" />
            <el-option label="交付阶段 - 项目交付、验收" value="DELIVERY" />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划日期" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="date"
                placeholder="选择计划完成日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePersonId">
              <el-select v-model="form.responsiblePersonId" placeholder="请选择负责人" style="width: 100%">
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="user.realName"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入里程碑描述和目标"
          />
        </el-form-item>
        
        <el-form-item label="交付物">
          <el-input
            v-model="form.deliverables"
            type="textarea"
            :rows="2"
            placeholder="请输入该里程碑的交付物（可选）"
          />
        </el-form-item>
        
        <el-form-item label="验收标准">
          <el-input
            v-model="form.acceptanceCriteria"
            type="textarea"
            :rows="2"
            placeholder="请输入验收标准（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  Edit, 
  Delete, 
  Check, 
  Warning, 
  Clock 
} from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { 
  getProjectMilestones, 
  getProjectMilestoneStatistics,
  createProjectMilestone,
  updateProjectMilestone,
  deleteProjectMilestone
} from '@/api/projectMilestone'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加里程碑')
const selectedProjectId = ref(null)
const projects = ref([])
const users = ref([])
const milestones = ref([])
const milestoneStats = ref(null)

const form = reactive({
  id: null,
  projectId: null,
  milestoneName: '',
  milestoneType: 'DEVELOPMENT',
  plannedDate: '',
  responsiblePersonId: null,
  description: '',
  deliverables: '',
  acceptanceCriteria: ''
})

const formRef = ref()
const formRules = {
  milestoneName: [
    { required: true, message: '请输入里程碑名称', trigger: 'blur' }
  ],
  milestoneType: [
    { required: true, message: '请选择里程碑类型', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择计划完成日期', trigger: 'change' }
  ]
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({ 
      current: 1, 
      size: 100,
      userId: userStore.user?.id // 只获取用户参与的项目
    })
    if (response.code === 200) {
      projects.value = response.data.records || []
      
      // 如果有项目且没有选中项目，默认选择第一个项目
      if (projects.value.length > 0 && !selectedProjectId.value) {
        selectedProjectId.value = projects.value[0].id
        loadMilestones()
      }
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await getUserList({ current: 1, size: 100 })
    if (response.code === 200) {
      users.value = response.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

// 加载里程碑
const loadMilestones = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    // 获取里程碑列表
    const response = await getProjectMilestones(selectedProjectId.value)
    if (response.code === 200) {
      milestones.value = response.data || []
      
      // 获取统计数据
      const statsResponse = await getProjectMilestoneStatistics(selectedProjectId.value)
      if (statsResponse.code === 200) {
        const stats = statsResponse.data
        milestoneStats.value = {
          total: stats.totalMilestones || 0,
          completed: stats.completedMilestones || 0,
          inProgress: stats.inProgressMilestones || 0,
          delayed: stats.delayedMilestones || 0
        }
      }
      
      ElMessage.success(`成功加载 ${milestones.value.length} 个里程碑`)
    } else {
      ElMessage.error(response.message || '加载里程碑失败')
    }
  } catch (error) {
    console.error('加载里程碑失败:', error)
    ElMessage.error('加载里程碑失败')
  } finally {
    loading.value = false
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '添加里程碑'
  form.projectId = selectedProjectId.value
  dialogVisible.value = true
}

// 编辑里程碑
const editMilestone = (milestone) => {
  dialogTitle.value = '编辑里程碑'
  Object.assign(form, milestone)
  dialogVisible.value = true
}

// 删除里程碑
const deleteMilestone = async (milestone) => {
  try {
    await ElMessageBox.confirm(`确定要删除里程碑"${milestone.milestoneName}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteProjectMilestone(milestone.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadMilestones()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const formData = {
          ...form,
          projectId: selectedProjectId.value
        }
        
        let response
        if (form.id) {
          response = await updateProjectMilestone(form.id, formData)
        } else {
          response = await createProjectMilestone(formData)
        }
        
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadMilestones()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    projectId: selectedProjectId.value,
    milestoneName: '',
    milestoneType: 'DEVELOPMENT',
    plannedDate: '',
    responsiblePersonId: null,
    description: '',
    deliverables: '',
    acceptanceCriteria: ''
  })
}

// 工具函数
const formatDate = (date) => {
  return date ? new Date(date).toLocaleDateString() : '-'
}

const getMilestoneTypeColor = (type) => {
  const colorMap = {
    'PLANNING': 'primary',
    'DEVELOPMENT': 'success',
    'TESTING': 'warning',
    'DEPLOYMENT': 'danger',
    'DELIVERY': 'info'
  }
  return colorMap[type] || ''
}

const getMilestoneTypeText = (type) => {
  const textMap = {
    'PLANNING': '规划',
    'DEVELOPMENT': '开发',
    'TESTING': '测试',
    'DEPLOYMENT': '部署',
    'DELIVERY': '交付'
  }
  return textMap[type] || type
}

const getProgressStatus = (milestone) => {
  if (milestone.status === 'COMPLETED') return 'success'
  if (milestone.status === 'DELAYED') return 'exception'
  return ''
}

const getResponsiblePersonName = (responsiblePersonId) => {
  if (!responsiblePersonId) return ''
  const user = users.value.find(u => u.id === responsiblePersonId)
  return user ? user.realName : ''
}

onMounted(() => {
  loadProjects()
  loadUsers()
})
</script>

<style scoped>
.project-milestones {
  padding: 20px;
}

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

.milestone-overview {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 里程碑时间轴样式 */
.milestone-timeline {
  position: relative;
  padding-left: 40px;
}

.milestone-timeline::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e4e7ed;
}

.milestone-item {
  position: relative;
  margin-bottom: 40px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #e4e7ed;
}

.milestone-item.completed {
  border-left-color: #67C23A;
  background: #f0f9ff;
}

.milestone-item.in-progress {
  border-left-color: #409EFF;
  background: #f0f9ff;
}

.milestone-item.delayed {
  border-left-color: #F56C6C;
  background: #fef0f0;
}

.milestone-dot {
  position: absolute;
  left: -32px;
  top: 25px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.milestone-item.completed .milestone-dot {
  border-color: #67C23A;
  color: #67C23A;
}

.milestone-item.in-progress .milestone-dot {
  border-color: #409EFF;
  color: #409EFF;
}

.milestone-item.delayed .milestone-dot {
  border-color: #F56C6C;
  color: #F56C6C;
}

.milestone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.milestone-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.milestone-actions {
  display: flex;
  gap: 8px;
}

.milestone-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
}

.milestone-progress {
  margin: 16px 0;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
}

.progress-percent {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

.milestone-description,
.milestone-responsible {
  font-size: 14px;
  color: #606266;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}
</style>