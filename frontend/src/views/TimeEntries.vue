<template>
  <div class="time-entries">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px; margin-right: 10px"
            @change="loadTimeEntries"
          />
          
          <el-select
            v-model="searchStatus"
            placeholder="审核状态"
            style="width: 120px; margin-right: 10px"
            clearable
            @change="loadTimeEntries"
          >
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            记录工时
          </el-button>
        </div>
      </div>
      
      <!-- 工时记录列表 -->
      <el-table
        v-loading="loading"
        :data="timeEntries"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="workDate" label="工作日期" width="110" />
        
        <el-table-column prop="userName" label="员工" width="100" />
        
        <el-table-column prop="projectName" label="项目" width="120" />
        
        <el-table-column prop="taskName" label="任务" min-width="150">
          <template #default="{ row }">
            <span v-if="row.taskName">{{ row.taskName }}</span>
            <span v-else class="text-muted">无关联任务</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        
        <el-table-column prop="duration" label="工时" width="80">
          <template #default="{ row }">
            <span class="duration">{{ row.duration }}h</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="workType" label="工作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getWorkTypeColor(row.workType)" size="small">
              {{ getWorkTypeText(row.workType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="工作内容" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="approverName" label="审核人" width="100">
          <template #default="{ row }">
            <span v-if="row.approverName">{{ row.approverName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)" v-if="canEdit(row) && row.status === 'PENDING'">
              <el-button type="primary" size="small" text>
                操作
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="view">
                    <el-icon><View /></el-icon>
                    详情
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-else type="primary" size="small" text @click="viewTimeEntry(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
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
          @size-change="loadTimeEntries"
          @current-change="loadTimeEntries"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑工时对话框 -->
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作日期" prop="workDate">
              <el-date-picker
                v-model="form.workDate"
                type="date"
                placeholder="选择工作日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="工作类型" prop="workType">
              <el-select v-model="form.workType" placeholder="请选择工作类型">
                <el-option label="正常工作" value="NORMAL" />
                <el-option label="加班" value="OVERTIME" />
                <el-option label="节假日" value="HOLIDAY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择开始时间"
                style="width: 100%"
                @change="calculateDuration"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="form.endTime"
                placeholder="选择结束时间"
                style="width: 100%"
                @change="calculateDuration"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="工时时长" prop="duration">
          <el-input-number
            v-model="form.duration"
            :min="0"
            :max="24"
            :precision="1"
            placeholder="工时时长"
          />
          <span style="margin-left: 10px; color: #666;">小时</span>
        </el-form-item>
        
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" @change="onProjectChange" style="width: 100%">
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关联任务" prop="taskId">
          <el-select 
            v-model="form.taskId" 
            :placeholder="form.projectId ? '请选择任务（必填）' : '请先选择项目'" 
            :disabled="!form.projectId"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="task in filteredTasks"
              :key="task.id"
              :label="task.taskName"
              :value="task.id"
            />
          </el-select>
          <div v-if="form.projectId && filteredTasks.length === 0" class="field-hint" style="color: #f56c6c;">
            该项目暂无可用任务，请先在任务管理中创建任务
          </div>
        </el-form-item>
        
        <el-form-item label="工作内容" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述工作内容，如：完成用户登录模块开发、修复数据库连接问题等"
          />
        </el-form-item>
        
        <!-- 高级设置（可选） -->
        <el-collapse v-model="activeTimeCollapse" class="form-collapse">
          <el-collapse-item title="效率分析（可选）" name="efficiency">
            <template #title>
              <span>效率分析（可选）</span>
              <el-tooltip content="用于工作效率分析和改进，可后续补充" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="活动类型">
                  <el-select v-model="form.activityType" placeholder="选择主要活动类型" style="width: 100%">
                    <el-option label="编码开发" value="CODING" />
                    <el-option label="测试调试" value="TESTING" />
                    <el-option label="问题调试" value="DEBUGGING" />
                    <el-option label="会议沟通" value="MEETING" />
                    <el-option label="文档编写" value="DOCUMENTATION" />
                    <el-option label="学习研究" value="RESEARCH" />
                    <el-option label="代码审查" value="REVIEW" />
                    <el-option label="需求分析" value="ANALYSIS" />
                    <el-option label="技能学习" value="LEARNING" />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="专注度">
                  <el-slider
                    v-model="form.focusLevel"
                    :min="0"
                    :max="100"
                    show-input
                    :show-input-controls="false"
                    style="width: 100%"
                  />
                  <div class="field-hint">标准：60-70一般，70-85良好，85+优秀</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="是否可计费">
                  <el-switch 
                    v-model="form.isBillable" 
                    active-text="可计费" 
                    inactive-text="不计费"
                  />
                  <div class="field-hint">标准：客户项目通常可计费，内部项目不计费</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="是否返工">
                  <el-switch 
                    v-model="form.isRework" 
                    active-text="返工" 
                    inactive-text="正常"
                  />
                  <div class="field-hint">标准：修复bug、重新实现功能等属于返工</div>
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
    
    <!-- 工时详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工时详情"
      width="500px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工作日期">{{ currentDetail.workDate }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ currentDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ currentDetail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="任务">{{ currentDetail.taskName || '无' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="工时时长">{{ currentDetail.duration }}小时</el-descriptions-item>
        <el-descriptions-item label="工作类型">
          <el-tag :type="getWorkTypeColor(currentDetail.workType)" size="small">
            {{ getWorkTypeText(currentDetail.workType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态" :span="2">
          <el-tag :type="getStatusColor(currentDetail.status)">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核人" :span="2">
          {{ currentDetail.approverName || '未审核' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">
          {{ currentDetail.approveComment || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="工作内容" :span="2">
          {{ currentDetail.description }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { 
  getTimeEntryList, 
  createTimeEntry, 
  updateTimeEntry, 
  deleteTimeEntry as deleteTimeEntryApi,
  getTimeEntryById,
  getUserTimeEntries
} from '@/api/timeEntry'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('记录工时')
const dateRange = ref([])
const searchStatus = ref('')
const timeEntries = ref([])
const projects = ref([])
const tasks = ref([])
const selectedTimeEntries = ref([])
const currentDetail = ref({})
const activeTimeCollapse = ref([]) // 工时表单折叠面板控制

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  workDate: '',
  startTime: '',
  endTime: '',
  duration: 0,
  projectId: null,
  taskId: null,
  workType: 'NORMAL',
  description: '',
  userId: userStore.user?.id,
  // 效率与质量
  activityType: 'CODING',
  focusLevel: 80,
  interruptionCount: 0,
  collaborationHours: 0,
  learningHours: 0,
  isRework: false,
  // 成本信息
  isBillable: true,
  billingRate: 0,
  costCenter: ''
})

const formRef = ref()
const formRules = {
  workDate: [
    { required: true, message: '请选择工作日期', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入工时时长', trigger: 'blur' },
    { type: 'number', min: 0.1, message: '工时时长必须大于0', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  taskId: [
    { 
      required: true,
      validator: (rule, value, callback) => {
        if (!form.projectId) {
          callback(new Error('请先选择项目'))
        } else if (!value) {
          callback(new Error('选择项目后必须选择关联任务'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  description: [
    { required: true, message: '请输入工作内容', trigger: 'blur' }
  ]
}

// 过滤后的任务列表
const filteredTasks = computed(() => {
  return tasks.value.filter(task => task.projectId === form.projectId)
})

// 加载工时记录列表
const loadTimeEntries = async () => {
  loading.value = true
  try {
    // 检查是否需要只显示用户相关的工时记录
    const userOnly = route.query.userOnly === 'true'
    
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: searchStatus.value
    }
    
    // 如果选择了日期范围，添加到参数中
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
      params.endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
    }
    
    let response
    if (userOnly && userStore.user?.id) {
      // 调用用户工时记录的API
      response = await getUserTimeEntries(userStore.user.id, params)
    } else {
      // 调用普通工时记录列表API
      params.userId = userStore.user?.id
      response = await getTimeEntryList(params)
    }
    
    if (response.code === 200) {
      timeEntries.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载工时记录失败')
    }
  } catch (error) {
    console.error('加载工时记录失败:', error)
    ElMessage.error('加载工时记录失败')
  } finally {
    loading.value = false
  }
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 加载任务列表
const loadTasks = async () => {
  try {
    const response = await getTaskList({
      current: 1,
      size: 1000
    })
    
    if (response.code === 200) {
      tasks.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
  }
}

// 项目变化时清空任务选择并触发验证
const onProjectChange = () => {
  form.taskId = null
  // 触发任务字段的验证
  if (formRef.value) {
    formRef.value.validateField('taskId')
  }
}

// 计算工时时长
const calculateDuration = () => {
  if (form.startTime && form.endTime) {
    const start = dayjs(form.startTime)
    const end = dayjs(form.endTime)
    const duration = end.diff(start, 'hour', true)
    if (duration > 0) {
      form.duration = Math.round(duration * 10) / 10 // 保留一位小数
    }
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '记录工时'
  form.workDate = dayjs().format('YYYY-MM-DD')
  dialogVisible.value = true
}

// 编辑工时记录
const editTimeEntry = (row) => {
  dialogTitle.value = '编辑工时'
  Object.assign(form, {
    ...row,
    workDate: row.workDate,
    startTime: row.startTime ? dayjs(row.startTime, 'HH:mm:ss').toDate() : '',
    endTime: row.endTime ? dayjs(row.endTime, 'HH:mm:ss').toDate() : ''
  })
  dialogVisible.value = true
}

// 查看工时详情
const viewTimeEntry = async (row) => {
  try {
    const response = await getTimeEntryById(row.id)
    if (response.code === 200) {
      currentDetail.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取工时详情失败')
  }
}

// 删除工时记录
const deleteTimeEntry = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除这条工时记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteTimeEntryApi(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadTimeEntries()
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
        const submitData = {
          ...form,
          workDate: dayjs(form.workDate).format('YYYY-MM-DD'),
          startTime: form.startTime ? dayjs(form.startTime).format('HH:mm:ss') : null,
          endTime: form.endTime ? dayjs(form.endTime).format('HH:mm:ss') : null,
          userId: userStore.user?.id
        }
        
        const response = form.id 
          ? await updateTimeEntry(form.id, submitData)
          : await createTimeEntry(submitData)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadTimeEntries()
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
    workDate: '',
    startTime: '',
    endTime: '',
    duration: 0,
    projectId: null,
    taskId: null,
    workType: 'NORMAL',
    description: '',
    userId: userStore.user?.id,
    // 效率与质量
    activityType: 'CODING',
    focusLevel: 80,
    interruptionCount: 0,
    collaborationHours: 0,
    learningHours: 0,
    isRework: false,
    // 成本信息
    isBillable: true,
    billingRate: 0,
    costCenter: ''
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTimeEntries.value = selection
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editTimeEntry(row)
      break
    case 'view':
      viewTimeEntry(row)
      break
    case 'delete':
      deleteTimeEntry(row)
      break
  }
}

// 检查是否可以编辑
const canEdit = (row) => {
  return row.userId === userStore.user?.id || userStore.user?.role === 'ADMIN'
}

// 获取工作类型颜色
const getWorkTypeColor = (type) => {
  const colorMap = {
    'NORMAL': 'primary',
    'OVERTIME': 'warning',
    'HOLIDAY': 'danger'
  }
  return colorMap[type] || 'info'
}

// 获取工作类型文本
const getWorkTypeText = (type) => {
  const textMap = {
    'NORMAL': '正常',
    'OVERTIME': '加班',
    'HOLIDAY': '节假日'
  }
  return textMap[type] || type
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

onMounted(() => {
  // 如果URL中有taskId参数，可以预设任务
  if (route.query.taskId) {
    // 这里可以根据taskId预设相关信息
  }
  
  loadTimeEntries()
  loadProjects()
  loadTasks()
})
</script>

<style scoped>
.time-entries {
  padding: 0;
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

.pagination {
  margin-top: 20px;
  text-align: right;
}

.duration {
  font-weight: bold;
  color: #409EFF;
}

.text-muted {
  color: #999;
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

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>