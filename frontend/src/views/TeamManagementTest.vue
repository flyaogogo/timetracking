<template>
  <div class="team-management-test">
    <el-card>
      <template #header>
        <h2>🧪 团队管理功能测试</h2>
      </template>

      <!-- 系统状态 -->
      <div class="status-section">
        <h3>📊 系统状态</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-content">
                <h4>后端连接</h4>
                <p :class="backendStatus.class">{{ backendStatus.text }}</p>
                <el-button @click="checkBackend" size="small">检查</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-content">
                <h4>数据库</h4>
                <p :class="dbStatus.class">{{ dbStatus.text }}</p>
                <el-button @click="checkDatabase" size="small">检查</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-content">
                <h4>成员API</h4>
                <p :class="memberApiStatus.class">{{ memberApiStatus.text }}</p>
                <el-button @click="testMemberAPI" size="small">测试</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-content">
                <h4>认证状态</h4>
                <p :class="authStatus.class">{{ authStatus.text }}</p>
                <el-button @click="checkAuth" size="small">检查</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 快速操作 -->
      <div class="action-section">
        <h3>🚀 快速操作</h3>
        <el-space wrap>
          <el-button @click="runFullTest" type="primary" :loading="testing">完整测试</el-button>
          <el-button @click="fixDatabase" type="success" :loading="fixing">修复数据库</el-button>
          <el-button @click="loadTestData" type="warning">加载测试数据</el-button>
          <el-button @click="clearLogs" type="info">清除日志</el-button>
        </el-space>
      </div>

      <!-- 项目成员管理 -->
      <div class="member-section">
        <h3>👥 项目成员管理</h3>
        
        <!-- 项目选择 -->
        <div class="project-selector">
          <el-select
            v-model="selectedProjectId"
            placeholder="选择项目"
            style="width: 300px; margin-right: 10px"
            @change="loadMembers"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          <el-button @click="loadMembers" :loading="loading">刷新成员</el-button>
          <el-button @click="showAddDialog" type="primary" :disabled="!selectedProjectId">添加成员</el-button>
        </div>

        <!-- 成员列表 -->
        <el-table
          :data="members"
          style="width: 100%; margin-top: 20px"
          v-loading="loading"
          empty-text="暂无成员数据"
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="userRealName" label="用户名" width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="skillLevel" label="技能等级" width="100" />
          <el-table-column prop="allocationPercentage" label="分配比例" width="100">
            <template #default="{ row }">{{ row.allocationPercentage }}%</template>
          </el-table-column>
          <el-table-column prop="performanceRating" label="绩效评分" width="100" />
          <el-table-column prop="productivityIndex" label="生产力指数" width="120" />
          <el-table-column prop="joinDate" label="加入日期" width="110" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button @click="removeMember(row)" type="danger" size="small">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 统计信息 -->
        <div v-if="statistics && selectedProjectId" style="margin-top: 20px">
          <h4>📈 统计信息</h4>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="总成员数">{{ statistics.totalMembers || 0 }}</el-descriptions-item>
            <el-descriptions-item label="平均绩效">{{ (statistics.avgPerformance || 0).toFixed(1) }}</el-descriptions-item>
            <el-descriptions-item label="平均生产力">{{ (statistics.avgProductivity || 1).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="活跃成员">{{ members.filter(m => m.allocationPercentage > 0).length }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 测试日志 -->
      <div class="log-section">
        <h3>📝 测试日志</h3>
        <div class="log-container">
          <div v-for="(log, index) in logs" :key="index" :class="['log-entry', log.type]">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加项目成员" width="500px">
      <el-form :model="newMember" label-width="100px">
        <el-form-item label="用户">
          <el-select v-model="newMember.userId" placeholder="选择用户" style="width: 100%">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newMember.role" placeholder="选择角色">
            <el-option label="项目经理" value="MANAGER" />
            <el-option label="开发人员" value="DEVELOPER" />
            <el-option label="测试人员" value="TESTER" />
            <el-option label="设计师" value="DESIGNER" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能等级">
          <el-select v-model="newMember.skillLevel" placeholder="选择技能等级">
            <el-option label="初级" value="JUNIOR" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="SENIOR" />
            <el-option label="专家" value="EXPERT" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配比例">
          <el-input-number v-model="newMember.allocationPercentage" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button @click="addMember" type="primary" :loading="adding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 响应式数据
const testing = ref(false)
const fixing = ref(false)
const loading = ref(false)
const adding = ref(false)
const selectedProjectId = ref(null)
const addDialogVisible = ref(false)

const projects = ref([])
const members = ref([])
const users = ref([])
const statistics = ref({})
const logs = ref([])

const backendStatus = reactive({ class: 'status-unknown', text: '检查中...' })
const dbStatus = reactive({ class: 'status-unknown', text: '检查中...' })
const memberApiStatus = reactive({ class: 'status-unknown', text: '检查中...' })
const authStatus = reactive({ class: 'status-unknown', text: '检查中...' })

const newMember = reactive({
  userId: null,
  role: 'DEVELOPER',
  skillLevel: 'INTERMEDIATE',
  allocationPercentage: 100
})

// 工具函数
const log = (message, type = 'info') => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift({ time: timestamp, message, type })
  if (logs.value.length > 100) {
    logs.value = logs.value.slice(0, 100)
  }
}

const clearLogs = () => {
  logs.value = []
}

const makeRequest = async (url, options = {}) => {
  const token = localStorage.getItem('token')
  const config = {
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    },
    ...options
  }

  try {
    const response = await axios(url, config)
    return response.data
  } catch (error) {
    throw error
  }
}

// 检查函数
const checkBackend = async () => {
  try {
    await makeRequest('/projects?current=1&size=1')
    backendStatus.class = 'status-success'
    backendStatus.text = '✅ 正常'
    log('后端服务连接正常', 'success')
  } catch (error) {
    backendStatus.class = 'status-error'
    backendStatus.text = '❌ 异常'
    log(`后端服务连接失败: ${error.message}`, 'error')
  }
}

const checkDatabase = async () => {
  try {
    const response = await makeRequest('/projects')
    if (response.code === 200) {
      dbStatus.class = 'status-success'
      dbStatus.text = '✅ 正常'
      log('数据库连接正常', 'success')
    } else {
      dbStatus.class = 'status-warning'
      dbStatus.text = '⚠️ 异常'
      log('数据库响应异常', 'warning')
    }
  } catch (error) {
    dbStatus.class = 'status-error'
    dbStatus.text = '❌ 异常'
    log(`数据库连接失败: ${error.message}`, 'error')
  }
}

const testMemberAPI = async () => {
  try {
    const response = await makeRequest('/project-members/project/1')
    if (response.code === 200) {
      memberApiStatus.class = 'status-success'
      memberApiStatus.text = '✅ 正常'
      log(`成员API测试成功，返回${response.data.length}条数据`, 'success')
    } else {
      memberApiStatus.class = 'status-warning'
      memberApiStatus.text = '⚠️ 异常'
      log('成员API响应异常', 'warning')
    }
  } catch (error) {
    memberApiStatus.class = 'status-error'
    memberApiStatus.text = '❌ 异常'
    log(`成员API测试失败: ${error.message}`, 'error')
  }
}

const checkAuth = () => {
  const token = localStorage.getItem('token')
  if (token) {
    authStatus.class = 'status-success'
    authStatus.text = '✅ 有Token'
    log('认证Token存在', 'success')
  } else {
    authStatus.class = 'status-warning'
    authStatus.text = '⚠️ 无Token'
    log('认证Token不存在', 'warning')
  }
}

// 主要功能
const loadProjects = async () => {
  try {
    const response = await makeRequest('/projects?current=1&size=100')
    if (response.code === 200) {
      projects.value = response.data.records || []
      log(`成功加载${projects.value.length}个项目`, 'success')
    }
  } catch (error) {
    log(`加载项目失败: ${error.message}`, 'error')
  }
}

const loadUsers = async () => {
  try {
    const response = await makeRequest('/users?current=1&size=100')
    if (response.code === 200) {
      users.value = response.data.records || []
      log(`成功加载${users.value.length}个用户`, 'success')
    }
  } catch (error) {
    log(`加载用户失败: ${error.message}`, 'error')
  }
}

const loadMembers = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    const [membersResponse, statsResponse] = await Promise.all([
      makeRequest(`/project-members/project/${selectedProjectId.value}`),
      makeRequest(`/project-members/project/${selectedProjectId.value}/statistics`).catch(() => ({
        code: 200,
        data: { totalMembers: 0, avgPerformance: 0, avgProductivity: 1.0 }
      }))
    ])

    if (membersResponse.code === 200) {
      members.value = membersResponse.data || []
      log(`成功加载${members.value.length}个项目成员`, 'success')
      
      if (members.value.length === 0) {
        ElMessage.info('该项目暂无成员')
      }
    } else {
      throw new Error(membersResponse.message || '获取成员失败')
    }

    if (statsResponse.code === 200) {
      statistics.value = statsResponse.data
    }
  } catch (error) {
    log(`加载项目成员失败: ${error.message}`, 'error')
    ElMessage.error(`加载项目成员失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  addDialogVisible.value = true
}

const addMember = async () => {
  if (!newMember.userId) {
    ElMessage.error('请选择用户')
    return
  }

  adding.value = true
  try {
    const memberData = {
      projectId: selectedProjectId.value,
      userId: newMember.userId,
      role: newMember.role,
      skillLevel: newMember.skillLevel,
      allocationPercentage: newMember.allocationPercentage,
      performanceRating: 3.5,
      productivityIndex: 1.1
    }

    const response = await makeRequest('/project-members', {
      method: 'POST',
      data: memberData
    })

    if (response.code === 200) {
      ElMessage.success('添加成员成功')
      addDialogVisible.value = false
      loadMembers()
      log('成功添加项目成员', 'success')
    } else {
      throw new Error(response.message || '添加成员失败')
    }
  } catch (error) {
    log(`添加成员失败: ${error.message}`, 'error')
    ElMessage.error(`添加成员失败: ${error.message}`)
  } finally {
    adding.value = false
  }
}

const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员"${member.userRealName}"吗？`, '确认', {
      type: 'warning'
    })

    const response = await makeRequest(`/project-members/${member.id}`, {
      method: 'DELETE'
    })

    if (response.code === 200) {
      ElMessage.success('移除成员成功')
      loadMembers()
      log('成功移除项目成员', 'success')
    } else {
      throw new Error(response.message || '移除成员失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      log(`移除成员失败: ${error.message}`, 'error')
      ElMessage.error(`移除成员失败: ${error.message}`)
    }
  }
}

const runFullTest = async () => {
  testing.value = true
  log('开始完整测试...', 'info')
  
  try {
    await checkBackend()
    await checkDatabase()
    await testMemberAPI()
    checkAuth()
    
    await loadProjects()
    await loadUsers()
    
    if (projects.value.length > 0) {
      selectedProjectId.value = projects.value[0].id
      await loadMembers()
    }
    
    log('完整测试完成', 'success')
    ElMessage.success('测试完成，请查看日志')
  } catch (error) {
    log(`测试失败: ${error.message}`, 'error')
    ElMessage.error('测试失败')
  } finally {
    testing.value = false
  }
}

const fixDatabase = async () => {
  fixing.value = true
  log('开始修复数据库...', 'info')
  
  try {
    // 这里可以调用修复API或提示用户执行修复脚本
    ElMessage.info('请执行 run_ultimate_fix.bat 脚本来修复数据库')
    log('请手动执行数据库修复脚本', 'warning')
  } finally {
    fixing.value = false
  }
}

const loadTestData = async () => {
  log('加载测试数据...', 'info')
  
  // 模拟测试数据
  const testProjects = [
    { id: 1, projectName: '示例项目1' },
    { id: 2, projectName: '示例项目2' },
    { id: 3, projectName: '示例项目3' }
  ]
  
  const testUsers = [
    { id: 1, realName: '系统管理员' },
    { id: 2, realName: '项目经理张三' },
    { id: 3, realName: '开发工程师李四' },
    { id: 4, realName: '测试工程师王五' },
    { id: 5, realName: '设计师赵六' }
  ]
  
  projects.value = testProjects
  users.value = testUsers
  
  log('测试数据加载完成', 'success')
  ElMessage.success('测试数据已加载')
}

onMounted(() => {
  log('团队管理测试页面已加载', 'info')
  checkAuth()
  
  // 自动执行初始检查
  setTimeout(() => {
    checkBackend()
    checkDatabase()
    testMemberAPI()
    loadProjects()
    loadUsers()
  }, 1000)
})
</script>

<style scoped>
.team-management-test {
  padding: 20px;
}

.status-section,
.action-section,
.member-section,
.log-section {
  margin-bottom: 30px;
}

.status-card {
  text-align: center;
}

.status-content {
  padding: 15px;
}

.status-success {
  color: #67C23A;
  font-weight: bold;
}

.status-error {
  color: #F56C6C;
  font-weight: bold;
}

.status-warning {
  color: #E6A23C;
  font-weight: bold;
}

.status-unknown {
  color: #909399;
}

.project-selector {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  font-family: monospace;
  font-size: 12px;
}

.log-entry {
  margin-bottom: 5px;
  padding: 2px 0;
}

.log-entry.success {
  color: #67C23A;
}

.log-entry.error {
  color: #F56C6C;
}

.log-entry.warning {
  color: #E6A23C;
}

.log-entry.info {
  color: #409EFF;
}

.log-time {
  color: #666;
  margin-right: 10px;
}
</style>