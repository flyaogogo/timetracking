<template>
  <div class="projects">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目名称、编号"
            style="width: 300px"
            @keyup.enter="loadProjects"
          >
            <template #append>
              <el-button @click="loadProjects">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建项目
          </el-button>
        </div>
      </div>
      
      <!-- 项目列表 -->
      <el-table
        v-loading="loading"
        :data="projects"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="projectName" label="项目名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewProject(row)">
              {{ row.projectName }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectCode" label="项目编号" width="120" />
        
        <el-table-column prop="projectType" label="项目类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getProjectTypeColor(row.projectType)">
              {{ getProjectTypeText(row.projectType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="managerName" label="项目经理" width="100" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="estimatedHours" label="预估工时" width="100">
          <template #default="{ row }">
            {{ row.estimatedHours }}h
          </template>
        </el-table-column>
        
        <el-table-column prop="actualHours" label="实际工时" width="100">
          <template #default="{ row }">
            {{ row.actualHours || 0 }}h
          </template>
        </el-table-column>
        
        <el-table-column prop="delayDays" label="延期天数" width="100">
          <template #default="{ row }">
            <span v-if="row.delayDays > 0" class="delay-text">
              {{ row.delayDays }}天
            </span>
            <span v-else class="normal-text">正常</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button type="primary" size="small" text>
                操作
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="team">
                    <el-icon><User /></el-icon>
                    团队管理
                  </el-dropdown-item>
                  <el-dropdown-item command="tasks">
                    <el-icon><List /></el-icon>
                    任务
                  </el-dropdown-item>
                  <el-dropdown-item command="statistics">
                    <el-icon><TrendCharts /></el-icon>
                    统计分析
                  </el-dropdown-item>
                  <!-- 只有项目经理才显示状态管理按钮 -->
                  <el-dropdown-item command="status" v-if="isProjectManager(row)">
                    <el-icon><Setting /></el-icon>
                    状态管理
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadProjects"
          @current-change="loadProjects"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑项目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="请输入项目编号" />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-select v-model="form.projectType" placeholder="请选择项目类型">
                <el-option label="开发项目" value="DEVELOPMENT" />
                <el-option label="维护项目" value="MAINTENANCE" />
                <el-option label="研究项目" value="RESEARCH" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择优先级">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估工时" prop="estimatedHours">
              <el-input-number
                v-model="form.estimatedHours"
                :min="0"
                :precision="1"
                placeholder="预估工时"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="项目经理" prop="managerId">
              <el-select v-model="form.managerId" placeholder="请选择项目经理">
                <el-option
                  v-for="user in managers"
                  :key="user.id"
                  :label="user.realName"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 财务信息（可选，用于成本分析） -->
        <el-collapse v-model="activeCollapse" class="form-collapse">
          <el-collapse-item title="财务信息（成本分析必填）" name="financial">
            <template #title>
              <span>财务信息（成本分析必填）</span>
              <el-tooltip content="用于项目成本分析和ROI计算，统计分析功能依赖这些数据" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="合同金额" prop="contractAmount">
                  <el-input-number
                    v-model="form.contractAmount"
                    :min="0"
                    :precision="2"
                    placeholder="与客户签订的合同总金额"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：以元为单位，用于利润率计算</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="预算金额" prop="budgetAmount">
                  <el-input-number
                    v-model="form.budgetAmount"
                    :min="0"
                    :precision="2"
                    placeholder="项目预算总金额"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：通常为合同金额的80-90%，用于成本控制</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="人工预算">
                  <el-input-number
                    v-model="form.laborBudget"
                    :min="0"
                    :precision="2"
                    placeholder="人工成本预算"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：预估人员成本，通常占总预算60-80%</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="其他预算">
                  <el-input-number
                    v-model="form.otherBudget"
                    :min="0"
                    :precision="2"
                    placeholder="设备、差旅等其他成本预算"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：设备费、差旅费、外包费等非人工成本</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户名称">
                  <el-input
                    v-model="form.clientName"
                    placeholder="客户公司名称"
                  />
                  <div class="field-hint">用于客户满意度跟踪和项目分类</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="合同编号">
                  <el-input
                    v-model="form.contractNumber"
                    placeholder="合同编号或订单号"
                  />
                  <div class="field-hint">便于财务对账和项目追溯</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          
          <el-collapse-item title="质量与风险（可选）" name="quality">
            <template #title>
              <span>质量与风险（可选）</span>
              <el-tooltip content="用于项目风险管控和质量目标设定" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="风险级别">
                  <el-select v-model="form.riskLevel" placeholder="评估项目整体风险" style="width: 100%">
                    <el-option label="低风险 - 技术成熟，需求明确" value="LOW" />
                    <el-option label="中等风险 - 有一定技术挑战" value="MEDIUM" />
                    <el-option label="高风险 - 技术难度大或需求不明确" value="HIGH" />
                    <el-option label="极高风险 - 创新项目或高度不确定" value="CRITICAL" />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="质量目标">
                  <el-input-number
                    v-model="form.qualityScore"
                    :min="60"
                    :max="100"
                    :precision="0"
                    placeholder="质量目标分数"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：60-70普通，70-85良好，85-95优秀，95+卓越</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
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
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Search, 
  Plus, 
  Edit, 
  Delete, 
  List, 
  TrendCharts, 
  User, 
  ArrowDown, 
  QuestionFilled,
  Setting
} from '@element-plus/icons-vue'
import { getProjectList, createProject, updateProject, deleteProject as deleteProjectApi, getUserProjects } from '@/api/project'
import { getUserList } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const searchKeyword = ref('')
const projects = ref([])
const selectedProjects = ref([])
const managers = ref([])
const activeCollapse = ref([]) // 折叠面板控制

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  description: '',
  projectType: 'DEVELOPMENT',
  priority: 'MEDIUM',
  estimatedHours: 0,
  managerId: null,
  startDate: '',
  endDate: '',
  // 财务信息
  contractAmount: 0,
  budgetAmount: 0,
  baselineBudget: 0,
  laborBudget: 0,
  otherBudget: 0,
  clientName: '',
  contractNumber: '',
  // 时间规划
  baselineEndDate: '',
  baselineHours: 0,
  // 质量与风险
  riskLevel: 'MEDIUM',
  qualityScore: 85,
  clientSatisfaction: 85,
  resourceUtilization: 80
})

const formRef = ref()
const formRules = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  projectCode: [
    { required: true, message: '请输入项目编号', trigger: 'blur' }
  ],
  projectType: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  try {
    // 检查是否需要只显示用户相关的项目
    const userOnly = route.query.userOnly === 'true'
    
    let response
    if (userOnly && userStore.user?.id) {
      // 调用用户相关项目的API
      response = await getUserProjects(userStore.user.id, {
        current: pagination.current,
        size: pagination.size,
        keyword: searchKeyword.value
      })
    } else {
      // 调用普通项目列表API
      response = await getProjectList({
        current: pagination.current,
        size: pagination.size,
        keyword: searchKeyword.value
      })
    }
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

// 加载项目经理列表
const loadManagers = async () => {
  try {
    const response = await getUserList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      managers.value = response.data.records?.filter(user => 
        user.role === 'PROJECT_MANAGER' || user.role === 'ADMIN'
      ) || []
    }
  } catch (error) {
    console.error('加载项目经理列表失败:', error)
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '新建项目'
  dialogVisible.value = true
}

// 编辑项目
const editProject = (row) => {
  dialogTitle.value = '编辑项目'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看项目详情
const viewProject = (row) => {
  // 可以跳转到项目详情页面
  console.log('查看项目:', row)
}

// 查看项目任务
const viewTasks = (row) => {
  router.push(`/tasks?projectId=${row.id}`)
}

// 删除项目
const deleteProject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除项目"${row.projectName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteProjectApi(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadProjects()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
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
        const response = form.id 
          ? await updateProject(form.id, form)
          : await createProject(form)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadProjects()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
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
    projectName: '',
    projectCode: '',
    description: '',
    projectType: 'DEVELOPMENT',
    priority: 'MEDIUM',
    estimatedHours: 0,
    managerId: null,
    startDate: '',
    endDate: '',
    // 财务信息
    contractAmount: 0,
    budgetAmount: 0,
    baselineBudget: 0,
    laborBudget: 0,
    // 时间规划
    baselineEndDate: '',
    baselineHours: 0,
    // 质量与风险
    riskLevel: 'MEDIUM',
    qualityScore: 85,
    clientSatisfaction: 85,
    resourceUtilization: 80
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedProjects.value = selection
}

// 查看项目统计
const viewStatistics = (row) => {
  router.push(`/project-statistics?projectId=${row.id}`)
}

// 查看项目团队
const viewTeam = (row) => {
  router.push(`/project-team?projectId=${row.id}`)
}

// 查看项目状态管理
const viewStatusManagement = (row) => {
  router.push(`/project-status-management?projectId=${row.id}`)
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editProject(row)
      break
    case 'team':
      viewTeam(row)
      break
    case 'tasks':
      viewTasks(row)
      break
    case 'statistics':
      viewStatistics(row)
      break
    case 'status':
      viewStatusManagement(row)
      break
    case 'delete':
      deleteProject(row)
      break
  }
}

// 获取项目类型颜色
const getProjectTypeColor = (type) => {
  const colorMap = {
    'DEVELOPMENT': 'primary',
    'MAINTENANCE': 'warning',
    'RESEARCH': 'success'
  }
  return colorMap[type] || 'info'
}

// 获取项目类型文本
const getProjectTypeText = (type) => {
  const textMap = {
    'DEVELOPMENT': '开发',
    'MAINTENANCE': '维护',
    'RESEARCH': '研究'
  }
  return textMap[type] || type
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  const colorMap = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'success'
  }
  return colorMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const textMap = {
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return textMap[priority] || priority
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'PAUSED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停'
  }
  return textMap[status] || status
}

// 判断当前用户是否是项目的项目经理
const isProjectManager = (row) => {
  // 检查是否有managerId字段，并且当前用户ID与managerId匹配
  return userStore.user?.id && row.managerId && row.managerId === userStore.user.id
}

onMounted(() => {
  loadProjects()
  loadManagers()
})
</script>

<style scoped>
.projects {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 操作列样式优化 */
.el-dropdown {
  vertical-align: middle;
}

.el-dropdown .el-button {
  padding: 5px 8px;
  font-size: 12px;
}

.el-dropdown-menu__item {
  padding: 8px 16px;
  font-size: 13px;
}

.el-dropdown-menu__item .el-icon {
  margin-right: 6px;
  font-size: 14px;
}

/* 延期状态样式 */
.delay-text {
  color: #F56C6C;
  font-weight: bold;
}

.normal-text {
  color: #67C23A;
}

/* 表单优化样式 */
.form-collapse {
  margin: 20px 0;
}

.form-collapse .el-collapse-item__header {
  font-weight: 500;
  color: #606266;
}

.info-icon {
  margin-left: 8px;
  color: #909399;
  cursor: help;
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>