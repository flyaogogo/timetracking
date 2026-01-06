<template>
  <div class="project-team-debug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔍 项目团队管理 - 调试模式</span>
          <el-button @click="toggleDebugMode" :type="debugMode ? 'danger' : 'primary'" size="small">
            {{ debugMode ? '关闭调试' : '开启调试' }}
          </el-button>
        </div>
      </template>

      <!-- 调试信息面板 -->
      <el-collapse v-if="debugMode" v-model="activeDebugPanels" style="margin-bottom: 20px">
        <el-collapse-item title="🔧 系统状态检查" name="status">
          <div class="debug-panel">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="status-card">
                  <h4>后端连接</h4>
                  <p :class="backendStatus.class">{{ backendStatus.text }}</p>
                  <el-button @click="checkBackend" size="small">重新检查</el-button>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="status-card">
                  <h4>认证状态</h4>
                  <p :class="authStatus.class">{{ authStatus.text }}</p>
                  <el-button @click="checkAuth" size="small">检查Token</el-button>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="status-card">
                  <h4>API端点</h4>
                  <p :class="apiStatus.class">{{ apiStatus.text }}</p>
                  <el-button @click="testAllAPIs" size="small">测试API</el-button>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <el-collapse-item title="📡 API测试结果" name="api">
          <div class="debug-panel">
            <el-table :data="apiTestResults" style="width: 100%">
              <el-table-column prop="endpoint" label="API端点" width="300" />
              <el-table-column prop="method" label="方法" width="80" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.success ? 'success' : 'danger'">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="response" label="响应" show-overflow-tooltip />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button @click="retestAPI(row)" size="small">重试</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-collapse-item>

        <el-collapse-item title="📝 请求日志" name="logs">
          <div class="debug-panel">
            <div class="log-container">
              <div v-for="(log, index) in requestLogs" :key="index" :class="['log-entry', log.type]">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-message">{{ log.message }}</span>
              </div>
            </div>
            <el-button @click="clearLogs" size="small">清除日志</el-button>
          </div>
        </el-collapse-item>
      </el-collapse>

      <!-- 主要功能区域 -->
      <div class="main-content">
        <!-- 项目选择 -->
        <div class="project-selector">
          <el-select
            v-model="selectedProjectId"
            placeholder="选择项目"
            style="width: 300px; margin-right: 10px"
            @change="onProjectChange"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          <el-button type="primary" @click="loadProjectMembers" :loading="loading">
            <el-icon><Refresh /></el-icon>
            重新加载
          </el-button>
          <el-button @click="createTestData" type="success" :loading="creatingTestData">
            <el-icon><Plus /></el-icon>
            创建测试数据
          </el-button>
        </div>

        <!-- 错误提示 -->
        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          :closable="false"
          style="margin: 20px 0"
        >
          <template #default>
            <div>
              <p><strong>详细错误信息:</strong></p>
              <pre>{{ errorDetails }}</pre>
              <p><strong>建议解决方案:</strong></p>
              <ul>
                <li v-for="suggestion in suggestions" :key="suggestion">{{ suggestion }}</li>
              </ul>
            </div>
          </template>
        </el-alert>

        <!-- 成功状态 -->
        <el-alert
          v-if="!errorMessage && selectedProjectId && members.length > 0"
          title="✅ 项目成员加载成功"
          type="success"
          :closable="false"
          style="margin: 20px 0"
        />

        <!-- 成员列表 -->
        <div v-if="selectedProjectId">
          <h3>项目成员列表 ({{ members.length }})</h3>
          <el-table
            :data="members"
            style="width: 100%"
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
              <template #default="{ row }">
                {{ row.allocationPercentage }}%
              </template>
            </el-table-column>
            <el-table-column prop="performanceRating" label="绩效评分" width="100" />
            <el-table-column prop="productivityIndex" label="生产力指数" width="120" />
            <el-table-column prop="joinDate" label="加入日期" width="110" />
          </el-table>
        </div>

        <!-- 统计信息 -->
        <div v-if="selectedProjectId && statistics" style="margin-top: 20px">
          <h3>统计信息</h3>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="总成员数">{{ statistics.totalMembers || 0 }}</el-descriptions-item>
            <el-descriptions-item label="平均绩效">{{ (statistics.avgPerformance || 0).toFixed(1) }}</el-descriptions-item>
            <el-descriptions-item label="平均生产力">{{ (statistics.avgProductivity || 1).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="活跃成员">{{ getActiveMembers() }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { Refresh, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()

// 响应式数据
const debugMode = ref(true)
const activeDebugPanels = ref(['status', 'api', 'logs'])
const loading = ref(false)
const creatingTestData = ref(false)
const selectedProjectId = ref(null)
const errorMessage = ref('')
const errorDetails = ref('')
const suggestions = ref([])

const projects = ref([])
const members = ref([])
const statistics = ref({})
const requestLogs = ref([])
const apiTestResults = ref([])

const backendStatus = reactive({ class: 'status-unknown', text: '检查中...' })
const authStatus = reactive({ class: 'status-unknown', text: '检查中...' })
const apiStatus = reactive({ class: 'status-unknown', text: '检查中...' })

// 工具函数
const log = (message, type = 'info') => {
  const timestamp = new Date().toLocaleTimeString()
  requestLogs.value.unshift({
    time: timestamp,
    message,
    type
  })
  
  // 保持日志数量在合理范围内
  if (requestLogs.value.length > 50) {
    requestLogs.value = requestLogs.value.slice(0, 50)
  }
}

const clearLogs = () => {
  requestLogs.value = []
}

const toggleDebugMode = () => {
  debugMode.value = !debugMode.value
}

// API请求封装
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

  log(`发起请求: ${config.method || 'GET'} ${url}`, 'info')
  
  try {
    const response = await axios(url, config)
    log(`请求成功: ${url} - 状态码: ${response.status}`, 'success')
    return response.data
  } catch (error) {
    const errorMsg = error.response 
      ? `HTTP ${error.response.status}: ${error.response.statusText}`
      : error.message
    log(`请求失败: ${url} - ${errorMsg}`, 'error')
    throw error
  }
}

// 系统检查函数
const checkBackend = async () => {
  try {
    await makeRequest('/projects?current=1&size=1')
    backendStatus.class = 'status-success'
    backendStatus.text = '✅ 后端服务正常'
  } catch (error) {
    backendStatus.class = 'status-error'
    backendStatus.text = '❌ 后端服务异常'
    log(`后端检查失败: ${error.message}`, 'error')
  }
}

const checkAuth = () => {
  const token = localStorage.getItem('token')
  if (token) {
    authStatus.class = 'status-success'
    authStatus.text = '✅ Token存在'
    log(`Token检查: 存在 (长度: ${token.length})`, 'info')
  } else {
    authStatus.class = 'status-warning'
    authStatus.text = '⚠️ 无Token'
    log('Token检查: 不存在', 'warning')
  }
}

const testAllAPIs = async () => {
  apiTestResults.value = []
  
  const endpoints = [
    { endpoint: '/projects', method: 'GET', description: '项目列表' },
    { endpoint: '/project-members/project/1', method: 'GET', description: '项目成员' },
    { endpoint: '/project-members/project/1/statistics', method: 'GET', description: '成员统计' },
    { endpoint: '/users', method: 'GET', description: '用户列表' }
  ]

  for (const api of endpoints) {
    try {
      const response = await makeRequest(api.endpoint, { method: api.method })
      apiTestResults.value.push({
        ...api,
        status: '成功',
        success: true,
        response: JSON.stringify(response, null, 2).substring(0, 200) + '...'
      })
    } catch (error) {
      apiTestResults.value.push({
        ...api,
        status: '失败',
        success: false,
        response: error.message
      })
    }
  }

  // 更新API状态
  const successCount = apiTestResults.value.filter(r => r.success).length
  if (successCount === endpoints.length) {
    apiStatus.class = 'status-success'
    apiStatus.text = '✅ 所有API正常'
  } else if (successCount > 0) {
    apiStatus.class = 'status-warning'
    apiStatus.text = `⚠️ ${successCount}/${endpoints.length} API正常`
  } else {
    apiStatus.class = 'status-error'
    apiStatus.text = '❌ 所有API异常'
  }
}

const retestAPI = async (apiTest) => {
  try {
    const response = await makeRequest(apiTest.endpoint, { method: apiTest.method })
    apiTest.status = '成功'
    apiTest.success = true
    apiTest.response = JSON.stringify(response, null, 2).substring(0, 200) + '...'
    ElMessage.success(`${apiTest.description} API测试成功`)
  } catch (error) {
    apiTest.status = '失败'
    apiTest.success = false
    apiTest.response = error.message
    ElMessage.error(`${apiTest.description} API测试失败`)
  }
}

// 主要业务函数
const loadProjects = async () => {
  try {
    log('开始加载项目列表', 'info')
    const response = await makeRequest('/projects?current=1&size=100')
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      log(`成功加载 ${projects.value.length} 个项目`, 'success')
      
      // 如果URL中有projectId，自动选择
      if (route.query.projectId) {
        selectedProjectId.value = parseInt(route.query.projectId)
        loadProjectMembers()
      }
    } else {
      throw new Error(response.message || '获取项目列表失败')
    }
  } catch (error) {
    log(`加载项目列表失败: ${error.message}`, 'error')
    setError('加载项目列表失败', error, [
      '检查后端服务是否运行在 http://localhost:8080',
      '检查数据库连接是否正常',
      '确认projects表是否存在数据'
    ])
  }
}

const loadProjectMembers = async () => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }

  loading.value = true
  errorMessage.value = ''
  
  try {
    log(`开始加载项目 ${selectedProjectId.value} 的成员`, 'info')
    
    // 并行请求成员列表和统计信息
    const [membersResponse, statsResponse] = await Promise.all([
      makeRequest(`/project-members/project/${selectedProjectId.value}`),
      makeRequest(`/project-members/project/${selectedProjectId.value}/statistics`).catch(err => {
        log(`统计API失败，使用默认值: ${err.message}`, 'warning')
        return { code: 200, data: { totalMembers: 0, avgPerformance: 0, avgProductivity: 1.0 } }
      })
    ])

    if (membersResponse.code === 200) {
      members.value = membersResponse.data || []
      log(`成功加载 ${members.value.length} 个项目成员`, 'success')
      ElMessage.success(`成功加载 ${members.value.length} 个项目成员`)
    } else {
      throw new Error(membersResponse.message || '获取项目成员失败')
    }

    if (statsResponse.code === 200) {
      statistics.value = statsResponse.data || {}
      log('成功加载统计信息', 'success')
    }

  } catch (error) {
    log(`加载项目成员失败: ${error.message}`, 'error')
    setError('加载项目成员失败', error, [
      '执行数据库修复脚本: database/complete_project_members_fix.sql',
      '检查project_members表是否存在必需字段',
      '确认ProjectMemberController是否正确配置',
      '检查JWT token是否有效'
    ])
  } finally {
    loading.value = false
  }
}

const createTestData = async () => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }

  try {
    await ElMessageBox.confirm('确定要创建测试数据吗？这将添加示例项目成员。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    creatingTestData.value = true
    log('开始创建测试数据', 'info')

    // 创建测试成员数据
    const testMembers = [
      {
        projectId: selectedProjectId.value,
        userId: 1,
        role: 'MANAGER',
        skillLevel: 'SENIOR',
        allocationPercentage: 80,
        performanceRating: 4.5,
        productivityIndex: 1.3
      },
      {
        projectId: selectedProjectId.value,
        userId: 2,
        role: 'DEVELOPER',
        skillLevel: 'INTERMEDIATE',
        allocationPercentage: 100,
        performanceRating: 4.0,
        productivityIndex: 1.2
      }
    ]

    for (const member of testMembers) {
      try {
        await makeRequest('/project-members', {
          method: 'POST',
          data: member
        })
        log(`成功创建测试成员: 用户${member.userId}`, 'success')
      } catch (error) {
        log(`创建测试成员失败: ${error.message}`, 'warning')
      }
    }

    ElMessage.success('测试数据创建完成')
    loadProjectMembers()

  } catch (error) {
    if (error !== 'cancel') {
      log(`创建测试数据失败: ${error.message}`, 'error')
      ElMessage.error('创建测试数据失败')
    }
  } finally {
    creatingTestData.value = false
  }
}

const onProjectChange = () => {
  if (selectedProjectId.value) {
    loadProjectMembers()
  }
}

const setError = (message, error, suggestionList = []) => {
  errorMessage.value = message
  errorDetails.value = error.response 
    ? JSON.stringify(error.response.data, null, 2)
    : error.message
  suggestions.value = suggestionList
}

const getActiveMembers = () => {
  return members.value.filter(m => (m.allocationPercentage || 0) > 0).length
}

// 生命周期
onMounted(async () => {
  log('项目团队调试页面已加载', 'info')
  
  // 初始化检查
  checkAuth()
  await checkBackend()
  await loadProjects()
  
  // 自动测试API
  setTimeout(() => {
    testAllAPIs()
  }, 1000)
})
</script>

<style scoped>
.project-team-debug {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.debug-panel {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
}

.status-card {
  text-align: center;
  padding: 10px;
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
  background: white;
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

.main-content {
  margin-top: 20px;
}
</style>