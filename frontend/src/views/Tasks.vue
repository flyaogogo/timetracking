<template>
  <div class="tasks">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-select
            v-model="searchProjectId"
            placeholder="选择项目"
            style="width: 200px; margin-right: 10px"
            clearable
            @change="loadTasks"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务名称"
            style="width: 300px"
            @keyup.enter="loadTasks"
          >
            <template #append>
              <el-button @click="loadTasks">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button 
            type="primary" 
            @click="showCreateDialog"
            :disabled="!canManageCurrentProjectTasks"
          >
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
          <el-button 
            type="success" 
            @click="showImportDialog"
            :disabled="!canManageCurrentProjectTasks"
          >
            <el-icon><Download /></el-icon>
            Excel导入
          </el-button>
        </div>
      </div>
      
      <!-- 任务列表 -->
      <el-table
        v-loading="loading"
        :data="tasks"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="taskName" label="任务名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewTask(row)">
              {{ row.taskName }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectName" label="所属项目" width="120" />
        
        <el-table-column prop="taskType" label="任务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskTypeColor(row.taskType)" size="small">
              {{ getTaskTypeText(row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-rate
              v-model="row.priority"
              :max="5"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="assigneeName" label="执行人" width="100" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="8" />
          </template>
        </el-table-column>
        
        <el-table-column prop="estimatedHours" label="预估工时" width="120">
          <template #default="{ row }">
            {{ row.estimatedHours }}h / {{ Math.ceil(row.estimatedHours / 8) }}天
          </template>
        </el-table-column>
        
        <el-table-column prop="actualHours" label="实际工时" width="120">
          <template #default="{ row }">
            {{ row.actualHours || 0 }}h / {{ Math.ceil((row.actualHours || 0) / 8) }}天
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
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)" v-if="canManageTask(row)">
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
                  <el-dropdown-item command="progress">
                    <el-icon><TrendCharts /></el-icon>
                    进度
                  </el-dropdown-item>
                  <el-dropdown-item command="timeEntries">
                    <el-icon><Timer /></el-icon>
                    工时
                  </el-dropdown-item>
                  <!-- 只有管理员和项目经理可以删除任务 -->
                  <el-dropdown-item command="delete" divided v-if="canDeleteTask(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-else type="info" size="small" disabled>
              无权限
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
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑任务对话框 -->
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
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目">
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.projectName"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="form.taskType" placeholder="请选择任务类型">
                <el-option label="开发" value="DEVELOPMENT" />
                <el-option label="测试" value="TESTING" />
                <el-option label="设计" value="DESIGN" />
                <el-option label="文档" value="DOCUMENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-rate v-model="form.priority" :max="5" show-text />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="难度系数" prop="difficulty">
              <el-rate v-model="form.difficulty" :max="5" show-text />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时间范围" prop="dateRange">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="选择开始日期"
                end-placeholder="选择结束日期"
                style="width: 100%"
                @change="handleDateRangeChange"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="执行人" prop="assigneeId">
              <el-select v-model="form.assigneeId" placeholder="请选择执行人">
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
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估工时" prop="estimatedHours">
              <el-input-number
                v-model="form.estimatedHours"
                :min="0"
                :precision="1"
                placeholder="预估工时"
                @change="handleEstimatedHoursChange"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="工时提示">
              <div class="estimate-hint" v-if="form.estimatedHours > 0">
                约{{ calculatedDays }}天（按每天8小时计算）
              </div>
              <div class="field-hint" v-else>
                请输入预估工时
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级设置（可选） -->
        <el-collapse v-model="activeTaskCollapse" class="form-collapse">
          <el-collapse-item title="业务价值与复杂度评估" name="business">
            <template #title>
              <span>业务价值与复杂度评估</span>
              <el-tooltip content="用于任务优先级排序和效率分析，影响统计数据准确性" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="业务价值">
                  <el-rate v-model="form.businessValue" :max="5" show-text />
                  <div class="field-hint">标准：1-2低价值，3中等价值，4-5高价值</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="任务复杂度">
                  <el-select v-model="form.taskComplexity" placeholder="评估任务技术复杂度">
                    <el-option label="简单 - 常规开发，无技术难点" value="LOW" />
                    <el-option label="中等 - 有一定技术挑战" value="MEDIUM" />
                    <el-option label="复杂 - 技术难度较大" value="HIGH" />
                    <el-option label="极复杂 - 创新性技术或高难度" value="VERY_HIGH" />
                  </el-select>
                  <div class="field-hint">用于工时预估准确性分析</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="故事点数">
                  <el-input-number
                    v-model="form.storyPoints"
                    :min="1"
                    :max="21"
                    placeholder="敏捷开发故事点"
                  />
                  <div class="field-hint">标准：1,2,3,5,8,13,21 斐波那契数列</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="基线工时">
                  <el-input-number
                    v-model="form.baselineHours"
                    :min="0"
                    :precision="1"
                    placeholder="基线工时（用于偏差分析）"
                  />
                  <div class="field-hint">初始预估工时，用于计算工时偏差</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          
          <el-collapse-item title="质量与依赖关系" name="quality">
            <template #title>
              <span>质量与依赖关系</span>
              <el-tooltip content="用于质量跟踪和项目风险分析" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="质量目标">
                  <el-input-number
                    v-model="form.qualityScore"
                    :min="60"
                    :max="100"
                    :precision="0"
                    placeholder="质量目标分数"
                  />
                  <div class="field-hint">标准：60-70普通，70-85良好，85+优秀</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="审核人" prop="reviewerId">
                  <el-select v-model="form.reviewerId" placeholder="请选择审核人">
                    <el-option
                      v-for="user in users"
                      :key="user.id"
                      :label="user.realName"
                      :value="user.id"
                    />
                  </el-select>
                  <div class="field-hint">负责任务质量审核的人员</div>
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
    
    <!-- 更新进度对话框 -->
    <el-dialog
      v-model="progressDialogVisible"
      title="更新任务进度"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="当前进度">
          <el-slider
            v-model="currentProgress"
            :min="0"
            :max="100"
            show-input
          />
        </el-form-item>
        
        <el-form-item label="任务状态">
          <el-select v-model="currentStatus" placeholder="请选择状态">
            <el-option label="待开始" value="TODO" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="待审核" value="REVIEW" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="PAUSED" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- Excel导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="Excel导入任务"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".xlsx, .xls"
            drag
            style="margin-bottom: 20px"
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                仅支持.xlsx、.xls格式文件，建议先下载模板再填写数据
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="info" @click="downloadTemplate">
            <el-icon><Document /></el-icon>
            下载导入模板
          </el-button>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeImportDialog">取消</el-button>
        <el-button type="success" @click="importTasks" :loading="importLoading">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTaskList, createTask, updateTask, deleteTask as deleteTaskApi, getUserTasks, importTasks as importTasksApi, downloadTaskImportTemplate } from '@/api/task'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { EnhancedPermissionUtil } from '@/utils/enhancedPermissions'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const searchKeyword = ref('')
const searchProjectId = ref(null)
const tasks = ref([])
const projects = ref([])
const users = ref([])
const selectedTasks = ref([])
const currentTaskId = ref(null)
const currentProgress = ref(0)
const currentStatus = ref('')
const activeTaskCollapse = ref([]) // 任务表单折叠面板控制

// Excel导入相关变量
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([]) // 用于组件显示
const selectedFile = ref(null) // 用于保存实际选择的文件
const importLoading = ref(false)

// 权限相关
const canManageCurrentProjectTasks = computed(() => {
  if (!searchProjectId.value) return false
  if (userStore.user?.role === 'ADMIN' || userStore.user?.role === 'PROJECT_MANAGER') return true
  // 这里可以添加更复杂的项目级权限检查
  return true // 临时允许所有用户，实际应该检查项目权限
})

// 检查是否可以管理特定任务（编辑、更新进度等）
const canManageTask = (task) => {
  // 管理员可以管理所有任务
  if (userStore.user?.role === 'ADMIN') return true
  
  // 项目经理可以管理项目内的所有任务
  if (userStore.user?.role === 'PROJECT_MANAGER') return true
  
  // 任务执行人可以更新进度
  if (task.assigneeId === userStore.user?.id) return true
  
  return false
}

// 检查是否可以删除任务
const canDeleteTask = (task) => {
  // 只有管理员和项目经理可以删除任务
  return userStore.user?.role === 'ADMIN' || userStore.user?.role === 'PROJECT_MANAGER'
}

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 日期范围和计算相关
const dateRange = ref([])
const daysBetween = ref(0)
const estimatedHours = ref(0)
const calculatedDays = ref(0)

const form = reactive({
  id: null,
  taskName: '',
  description: '',
  projectId: null,
  taskType: 'DEVELOPMENT',
  priority: 3,
  difficulty: 3,
  estimatedHours: 0,
  assigneeId: null,
  reviewerId: null,
  startDate: '',
  endDate: '',
  // 业务价值与复杂度
  businessValue: 3,
  taskComplexity: 'MEDIUM',
  storyPoints: 0,
  technicalRisk: 'MEDIUM',
  // 工时规划
  baselineHours: 0,
  qualityScore: 85,
  acceptanceCriteria: ''
})

const formRef = ref()
const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ]
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    // 所有用户都能看到自己参与项目中的所有任务
    // 1. 检查URL参数
    const userOnlyFromUrl = route.query.userOnly === 'true'
    
    let response
    if (userOnlyFromUrl && userStore.user?.id) {
      // 仅在URL参数指定时，才只看自己的任务
      response = await getUserTasks(userStore.user.id, {
        current: pagination.current,
        size: pagination.size,
        projectId: searchProjectId.value,
        keyword: searchKeyword.value
      })
    } else {
      // 调用普通任务列表API，显示自己参与项目中的所有任务
      // 由于getProjectList已经只返回当前用户参与的项目，所以这里不需要额外的权限检查
      response = await getTaskList({
        current: pagination.current,
        size: pagination.size,
        projectId: searchProjectId.value,
        keyword: searchKeyword.value
      })
    }
    
    if (response.code === 200) {
      tasks.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载任务列表失败')
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 加载项目列表 - 只加载当前用户参与的项目
const loadProjects = async () => {
  try {
    // 使用getProjectList API，它会根据用户权限返回用户参与的项目
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

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await getUserList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      users.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 计算两个日期之间的天数差
const calculateDaysBetween = (start, end) => {
  if (!start || !end) return 0
  
  const startDate = new Date(start)
  const endDate = new Date(end)
  
  // 计算毫秒差
  const timeDiff = endDate.getTime() - startDate.getTime()
  
  // 转换为天数（1天 = 86400000毫秒）
  const days = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1 // +1 包含开始和结束日期
  
  return Math.max(0, days)
}

// 处理日期范围变化
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    const [start, end] = value
    form.startDate = start
    form.endDate = end
    
    // 计算天数差
    daysBetween.value = calculateDaysBetween(start, end)
    
    // 计算预估工时（每天8小时）
    estimatedHours.value = daysBetween.value * 8
    
    // 自动填充到表单的预估工时字段
    form.estimatedHours = estimatedHours.value
    
    // 同步更新计算天数
    calculatedDays.value = daysBetween.value
  } else {
    // 重置计算结果
    daysBetween.value = 0
    estimatedHours.value = 0
    calculatedDays.value = 0
    form.startDate = ''
    form.endDate = ''
  }
}

// 处理预估工时变化
const handleEstimatedHoursChange = (value) => {
  if (value > 0) {
    // 计算对应的天数（按每天8小时计算，向上取整）
    calculatedDays.value = Math.ceil(value / 8)
  } else {
    calculatedDays.value = 0
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '新建任务'
  // 如果当前有选择的项目，自动填写到表单中
  if (searchProjectId.value) {
    form.projectId = searchProjectId.value
  }
  
  // 设置执行人为当前登录用户
  if (userStore.user?.id) {
    form.assigneeId = userStore.user.id
  }
  
  dialogVisible.value = true
}

// 编辑任务
const editTask = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  
  // 设置日期范围
  if (row.startDate && row.endDate) {
    dateRange.value = [new Date(row.startDate), new Date(row.endDate)]
    // 重新计算天数差和预估工时
    daysBetween.value = calculateDaysBetween(row.startDate, row.endDate)
    estimatedHours.value = daysBetween.value * 8
  } else {
    dateRange.value = []
    daysBetween.value = 0
    estimatedHours.value = 0
  }
  
  // 计算天数
  if (row.estimatedHours > 0) {
    calculatedDays.value = Math.ceil(row.estimatedHours / 8)
  } else {
    calculatedDays.value = 0
  }
  
  dialogVisible.value = true
}

// 查看任务详情
const viewTask = (row) => {
  console.log('查看任务:', row)
}

// 更新进度
const updateProgress = (row) => {
  currentTaskId.value = row.id
  currentProgress.value = row.progress
  currentStatus.value = row.status
  progressDialogVisible.value = true
}

// 提交进度更新
const submitProgress = async () => {
  try {
    const response = await updateTask(currentTaskId.value, {
      progress: currentProgress.value,
      status: currentStatus.value
    })
    
    if (response.code === 200) {
      ElMessage.success('更新成功')
      progressDialogVisible.value = false
      loadTasks()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 查看工时记录
const viewTimeEntries = (row) => {
  router.push(`/time-entries?taskId=${row.id}`)
}

// 删除任务
const deleteTask = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务"${row.taskName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteTaskApi(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadTasks()
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
          ? await updateTask(form.id, form)
          : await createTask(form)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadTasks()
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
  
  // 重置日期范围相关变量
  dateRange.value = []
  daysBetween.value = 0
  estimatedHours.value = 0
  calculatedDays.value = 0
  
  Object.assign(form, {
    id: null,
    taskName: '',
    description: '',
    projectId: null,
    taskType: 'DEVELOPMENT',
    priority: 3,
    difficulty: 3,
    estimatedHours: 0,
    assigneeId: null,
    startDate: '',
    endDate: '',
    // 业务价值与复杂度
    businessValue: 3,
    taskComplexity: 'MEDIUM',
    storyPoints: 0,
    technicalRisk: 'MEDIUM',
    // 工时规划
    baselineHours: 0,
    qualityTarget: 85,
    acceptanceCriteria: ''
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editTask(row)
      break
    case 'progress':
      updateProgress(row)
      break
    case 'timeEntries':
      viewTimeEntries(row)
      break
    case 'delete':
      deleteTask(row)
      break
  }
}

// 获取任务类型颜色
const getTaskTypeColor = (type) => {
  const colorMap = {
    'DEVELOPMENT': 'primary',
    'TESTING': 'success',
    'DESIGN': 'warning',
    'DOCUMENT': 'info'
  }
  return colorMap[type] || 'info'
}

// 获取任务类型文本
const getTaskTypeText = (type) => {
  const textMap = {
    'DEVELOPMENT': '开发',
    'TESTING': '测试',
    'DESIGN': '设计',
    'DOCUMENT': '文档'
  }
  return textMap[type] || type
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

// Excel导入相关方法
const showImportDialog = () => {
  importDialogVisible.value = true
}

const closeImportDialog = () => {
  importDialogVisible.value = false
  fileList.value = [] // 清空文件列表
  selectedFile.value = null // 清空选中的文件
}

const handleFileChange = (file, fileList) => {
  console.log('File change event:', file, fileList)
  // 保存当前选择的文件
  selectedFile.value = file.raw
  // 更新fileList用于组件显示
  fileList.value = fileList
  console.log('Selected file:', selectedFile.value)
  console.log('Updated fileList:', fileList.value)
}

const importTasks = async () => {
  console.log('Import tasks called, selectedFile:', selectedFile.value)
  
  // 确保selectedFile不为空
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的Excel文件')
    return
  }
  
  importLoading.value = true
  try {
    const file = selectedFile.value
    console.log('Uploading file:', file.name, file.size)
    const response = await importTasksApi(file)
    
    if (response.code === 200) {
      ElMessage.success(`导入成功，共导入${response.data.successCount || 0}条任务，失败${response.data.failureCount || 0}条`)
      closeImportDialog()
      loadTasks() // 重新加载任务列表
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入任务失败:', error)
    ElMessage.error('导入失败，请检查文件格式是否正确')
  } finally {
    importLoading.value = false
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadTaskImportTemplate()
    
    // 创建下载链接并触发下载
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '任务导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('模板下载失败')
  }
}

onMounted(() => {
  // 如果URL中有projectId参数，设置默认项目
  if (route.query.projectId) {
    searchProjectId.value = parseInt(route.query.projectId)
  }
  
  loadTasks()
  loadProjects()
  loadUsers()
})
</script>

<style scoped>
.tasks {
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

/* 字段提示文本样式 */
.field-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 预估工时提示样式 */
.estimate-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #67C23A;
  line-height: 1.4;
}
</style>