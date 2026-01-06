<template>
  <div class="project-team">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目团队管理</span>
          <div class="header-actions">
            <el-select
              v-model="selectedProjectId"
              placeholder="选择项目"
              style="width: 200px; margin-right: 10px"
              @change="loadProjectMembers"
            >
              <el-option
                v-for="project in projects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            <el-button type="primary" @click="showAddMemberDialog" :disabled="!selectedProjectId">
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
            <el-button @click="debugInfo" type="info" size="small">
              <el-icon><Tools /></el-icon>
              调试信息
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 错误提示 -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>{{ errorMessage }}</p>
          <p><strong>可能的解决方案:</strong></p>
          <ul>
            <li>检查后端服务是否运行 (http://localhost:8080)</li>
            <li>执行数据库修复脚本: database/complete_project_members_fix.sql</li>
            <li>重新登录获取有效的认证token</li>
            <li>检查project_members表是否存在必需字段</li>
          </ul>
          <el-button @click="retryLoad" type="primary" size="small">重试加载</el-button>
          <el-button @click="openDebugTool" type="info" size="small">打开调试工具</el-button>
        </template>
      </el-alert>
      
      <!-- 调试信息面板 -->
      <el-collapse v-if="showDebug" v-model="debugActiveNames" style="margin-bottom: 20px">
        <el-collapse-item title="🔍 调试信息" name="debug">
          <div class="debug-info">
            <h4>API请求信息:</h4>
            <p><strong>请求URL:</strong> {{ debugInfo.lastRequestUrl }}</p>
            <p><strong>请求状态:</strong> {{ debugInfo.lastRequestStatus }}</p>
            <p><strong>响应数据:</strong></p>
            <pre>{{ debugInfo.lastResponse }}</pre>
            
            <h4>系统状态:</h4>
            <p><strong>JWT Token:</strong> {{ debugInfo.hasToken ? '✓ 存在' : '✗ 缺失' }}</p>
            <p><strong>选中项目ID:</strong> {{ selectedProjectId || '未选择' }}</p>
            <p><strong>项目列表:</strong> {{ projects.length }} 个项目</p>
            <p><strong>成员列表:</strong> {{ members.length }} 个成员</p>
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <!-- 团队统计概览 -->
      <div v-if="selectedProjectId && !errorMessage" class="team-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ statistics.totalMembers || 0 }}</div>
                <div class="stat-label">团队成员</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ (statistics.avgPerformance || 0).toFixed(1) }}</div>
                <div class="stat-label">平均绩效</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ (statistics.avgProductivity || 1).toFixed(2) }}</div>
                <div class="stat-label">生产力指数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ getActiveMembers() }}</div>
                <div class="stat-label">活跃成员</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 成员列表 -->
      <div v-if="selectedProjectId && !errorMessage" class="member-list">
        <el-table
          v-loading="loading"
          :data="members"
          style="width: 100%"
          empty-text="暂无项目成员数据"
        >
          <el-table-column prop="userRealName" label="姓名" width="120" />
          
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleColor(row.role)">
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="skillLevel" label="技能等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getSkillColor(row.skillLevel)" size="small">
                {{ getSkillText(row.skillLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="allocationPercentage" label="分配比例" width="120">
            <template #default="{ row }">
              <el-progress 
                :percentage="row.allocationPercentage || 100" 
                :stroke-width="8"
                :show-text="false"
              />
              <span class="allocation-text">{{ row.allocationPercentage || 100 }}%</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="performanceRating" label="绩效评分" width="120">
            <template #default="{ row }">
              <el-rate
                :model-value="row.performanceRating || 3"
                :max="5"
                disabled
                show-score
                text-color="#ff9900"
                :score-template="`${row.performanceRating || 3}`"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="productivityIndex" label="生产力指数" width="120">
            <template #default="{ row }">
              <el-tooltip :content="`生产力指数: ${row.productivityIndex || 1.0}`" placement="top">
                <el-tag 
                  :type="getProductivityColor(row.productivityIndex)"
                  size="small"
                >
                  {{ (row.productivityIndex || 1.0).toFixed(2) }}
                </el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="joinDate" label="加入日期" width="110" />
          
          <el-table-column label="操作" width="120" fixed="right">
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
                    <el-dropdown-item command="remove" divided>
                      <el-icon><Delete /></el-icon>
                      移除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!errorMessage" class="empty-state">
        <el-empty description="请选择项目查看团队成员" />
      </div>
    </el-card>
    
    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加项目成员"
      width="600px"
      @close="resetMemberForm"
    >
      <el-form
        ref="memberFormRef"
        :model="memberForm"
        :rules="memberFormRules"
        label-width="100px"
      >
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="memberForm.userId"
            placeholder="请选择用户"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="`${user.realName} (${user.position || user.role})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目角色" prop="role">
              <el-select v-model="memberForm.role" placeholder="请选择角色">
                <el-option label="项目经理" value="MANAGER" />
                <el-option label="开发人员" value="DEVELOPER" />
                <el-option label="测试人员" value="TESTER" />
                <el-option label="设计师" value="DESIGNER" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="技能等级" prop="skillLevel">
              <el-select v-model="memberForm.skillLevel" placeholder="请选择技能等级">
                <el-option label="初级" value="JUNIOR" />
                <el-option label="中级" value="INTERMEDIATE" />
                <el-option label="高级" value="SENIOR" />
                <el-option label="专家" value="EXPERT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分配比例">
              <el-input-number
                v-model="memberForm.allocationPercentage"
                :min="1"
                :max="100"
                :precision="0"
                placeholder="分配比例(%)"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="绩效评分">
              <el-rate
                v-model="memberForm.performanceRating"
                :max="5"
                show-text
                text-color="#ff9900"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitMemberForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { 
  Plus, 
  Edit, 
  Delete, 
  ArrowDown,
  Tools
} from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { 
  getProjectMembers, 
  getProjectMemberStatistics,
  addProjectMember,
  removeProjectMember
} from '@/api/projectMember'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const selectedProjectId = ref(null)
const addMemberDialogVisible = ref(false)
const errorMessage = ref('')
const showDebug = ref(false)
const debugActiveNames = ref(['debug'])

const projects = ref([])
const members = ref([])
const availableUsers = ref([])
const statistics = reactive({
  totalMembers: 0,
  avgPerformance: 0,
  avgProductivity: 1.0
})

const debugInfo = reactive({
  lastRequestUrl: '',
  lastRequestStatus: '',
  lastResponse: '',
  hasToken: false
})

const memberForm = reactive({
  userId: null,
  role: 'DEVELOPER',
  skillLevel: 'INTERMEDIATE',
  allocationPercentage: 100,
  performanceRating: 3
})

const memberFormRef = ref()

const memberFormRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 方法
const loadProjects = async () => {
  try {
    debugInfo.lastRequestUrl = '/api/projects'
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    debugInfo.lastRequestStatus = response.code
    debugInfo.lastResponse = JSON.stringify(response, null, 2)
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      
      // 如果URL中有projectId参数，自动选择项目
      if (route.query.projectId) {
        selectedProjectId.value = parseInt(route.query.projectId)
        loadProjectMembers()
      }
    } else {
      throw new Error(response.message || '获取项目列表失败')
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    errorMessage.value = `加载项目列表失败: ${error.message}`
  }
}

const loadUsers = async () => {
  try {
    const response = await getUserList({
      current: 1,
      size: 100
    })
    if (response.code === 200) {
      availableUsers.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const loadProjectMembers = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    debugInfo.lastRequestUrl = `/api/project-members/project/${selectedProjectId.value}`
    
    const [membersResponse, statsResponse] = await Promise.all([
      getProjectMembers(selectedProjectId.value),
      getProjectMemberStatistics(selectedProjectId.value).catch(err => {
        console.warn('统计API失败，使用默认值:', err)
        return { code: 200, data: { totalMembers: 0, avgPerformance: 0, avgProductivity: 1.0 } }
      })
    ])
    
    debugInfo.lastRequestStatus = membersResponse.code
    debugInfo.lastResponse = JSON.stringify(membersResponse, null, 2)
    
    if (membersResponse.code === 200) {
      members.value = membersResponse.data || []
      ElMessage.success(`成功加载 ${members.value.length} 个项目成员`)
    } else {
      throw new Error(membersResponse.message || '获取项目成员失败')
    }
    
    if (statsResponse.code === 200) {
      Object.assign(statistics, statsResponse.data)
    }
  } catch (error) {
    console.error('加载项目成员失败:', error)
    errorMessage.value = `加载项目成员失败: ${error.message}`
    
    // 显示详细的错误信息
    if (error.response) {
      errorMessage.value += ` (HTTP ${error.response.status})`
    }
  } finally {
    loading.value = false
  }
}

const showAddMemberDialog = () => {
  addMemberDialogVisible.value = true
}

const resetMemberForm = () => {
  Object.assign(memberForm, {
    userId: null,
    role: 'DEVELOPER',
    skillLevel: 'INTERMEDIATE',
    allocationPercentage: 100,
    performanceRating: 3
  })
}

const submitMemberForm = async () => {
  if (!memberFormRef.value) return
  
  await memberFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const memberData = {
          ...memberForm,
          projectId: selectedProjectId.value
        }
        
        const response = await addProjectMember(memberData)
        if (response.code === 200) {
          ElMessage.success('添加成员成功')
          addMemberDialogVisible.value = false
          loadProjectMembers()
        } else {
          ElMessage.error(response.message || '添加成员失败')
        }
      } catch (error) {
        ElMessage.error('添加成员失败: ' + error.message)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      ElMessage.info('编辑功能开发中')
      break
    case 'remove':
      removeMember(row)
      break
  }
}

const removeMember = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员"${row.userRealName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await removeProjectMember(row.id)
    if (response.code === 200) {
      ElMessage.success('移除成员成功')
      loadProjectMembers()
    } else {
      ElMessage.error(response.message || '移除成员失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除成员失败: ' + error.message)
    }
  }
}

const retryLoad = () => {
  errorMessage.value = ''
  loadProjectMembers()
}

const openDebugTool = () => {
  window.open('/debug_project_members_issue.html', '_blank')
}

const debugInfo = () => {
  showDebug.value = !showDebug.value
  debugInfo.hasToken = !!localStorage.getItem('token')
}

// 工具函数
const getRoleColor = (role) => {
  const colorMap = {
    'MANAGER': 'danger',
    'DEVELOPER': 'primary',
    'TESTER': 'success',
    'DESIGNER': 'warning'
  }
  return colorMap[role] || 'info'
}

const getRoleText = (role) => {
  const textMap = {
    'MANAGER': '项目经理',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员',
    'DESIGNER': '设计师'
  }
  return textMap[role] || role
}

const getSkillColor = (skill) => {
  const colorMap = {
    'JUNIOR': 'info',
    'INTERMEDIATE': 'primary',
    'SENIOR': 'success',
    'EXPERT': 'danger'
  }
  return colorMap[skill] || 'info'
}

const getSkillText = (skill) => {
  const textMap = {
    'JUNIOR': '初级',
    'INTERMEDIATE': '中级',
    'SENIOR': '高级',
    'EXPERT': '专家'
  }
  return textMap[skill] || skill
}

const getProductivityColor = (productivity) => {
  if (productivity >= 1.5) return 'success'
  if (productivity >= 1.2) return 'primary'
  if (productivity >= 0.8) return 'warning'
  return 'danger'
}

const getActiveMembers = () => {
  return members.value.filter(m => (m.allocationPercentage || 0) > 0).length
}

onMounted(() => {
  debugInfo.hasToken = !!localStorage.getItem('token')
  loadProjects()
  loadUsers()
})
</script>

<style scoped>
.project-team {
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

.team-overview {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
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

.member-list {
  margin-top: 20px;
}

.allocation-text {
  margin-left: 8px;
  font-size: 12px;
  color: #666;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.debug-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}

.debug-info pre {
  background: white;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  max-height: 200px;
}

/* 操作列样式 */
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
</style>