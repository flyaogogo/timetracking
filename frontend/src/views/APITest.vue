<template>
  <div class="api-test">
    <el-card>
      <template #header>
        <h2>🧪 API接口测试工具</h2>
      </template>

      <!-- 基础配置 -->
      <el-form :model="config" label-width="120px" style="margin-bottom: 20px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="API基础URL">
              <el-input v-model="config.baseURL" placeholder="http://localhost:8080/api" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证Token">
              <el-input v-model="config.token" placeholder="Bearer token" type="password" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button @click="loadTokenFromStorage" type="info">从本地存储加载Token</el-button>
          <el-button @click="testConnection" type="primary" :loading="testing">测试连接</el-button>
        </el-form-item>
      </el-form>

      <!-- 快速测试按钮 -->
      <div class="quick-tests" style="margin-bottom: 20px">
        <h3>快速测试</h3>
        <el-space wrap>
          <el-button @click="testAPI('/projects', 'GET')" type="primary">测试项目列表</el-button>
          <el-button @click="testAPI('/users', 'GET')" type="success">测试用户列表</el-button>
          <el-button @click="testAPI('/project-members/project/1', 'GET')" type="warning">测试项目成员</el-button>
          <el-button @click="testAPI('/project-members/project/1/statistics', 'GET')" type="info">测试成员统计</el-button>
          <el-button @click="createTestMember" type="danger">创建测试成员</el-button>
        </el-space>
      </div>

      <!-- 自定义API测试 -->
      <el-form :model="customAPI" label-width="120px" style="margin-bottom: 20px">
        <h3>自定义API测试</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="请求方法">
              <el-select v-model="customAPI.method">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="API路径">
              <el-input v-model="customAPI.path" placeholder="/api/endpoint" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请求体" v-if="customAPI.method !== 'GET'">
          <el-input 
            v-model="customAPI.body" 
            type="textarea" 
            :rows="4" 
            placeholder='{"key": "value"}'
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="testCustomAPI" type="primary" :loading="testing">发送请求</el-button>
        </el-form-item>
      </el-form>

      <!-- 测试结果 -->
      <div class="test-results">
        <h3>测试结果</h3>
        <el-table :data="results" style="width: 100%" max-height="400">
          <el-table-column prop="timestamp" label="时间" width="100" />
          <el-table-column prop="method" label="方法" width="80" />
          <el-table-column prop="url" label="URL" width="300" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.success ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button @click="showDetails(row)" size="small">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div style="margin-top: 10px">
          <el-button @click="clearResults" size="small">清除结果</el-button>
          <el-button @click="exportResults" size="small" type="info">导出结果</el-button>
        </div>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="请求详情" width="80%">
      <div v-if="selectedResult">
        <h4>请求信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ selectedResult.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="方法">{{ selectedResult.method }}</el-descriptions-item>
          <el-descriptions-item label="URL">{{ selectedResult.url }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedResult.status }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px">请求头</h4>
        <pre class="code-block">{{ JSON.stringify(selectedResult.requestHeaders, null, 2) }}</pre>

        <h4 v-if="selectedResult.requestBody">请求体</h4>
        <pre v-if="selectedResult.requestBody" class="code-block">{{ selectedResult.requestBody }}</pre>

        <h4>响应数据</h4>
        <pre class="code-block">{{ selectedResult.response }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 响应式数据
const testing = ref(false)
const detailVisible = ref(false)
const selectedResult = ref(null)
const results = ref([])

const config = reactive({
  baseURL: 'http://localhost:8080/api',
  token: ''
})

const customAPI = reactive({
  method: 'GET',
  path: '/projects',
  body: ''
})

// 工具函数
const addResult = (method, url, success, status, response, requestHeaders = {}, requestBody = '') => {
  results.value.unshift({
    timestamp: new Date().toLocaleTimeString(),
    method,
    url,
    success,
    status,
    response: typeof response === 'string' ? response : JSON.stringify(response, null, 2),
    requestHeaders,
    requestBody
  })
}

const loadTokenFromStorage = () => {
  const token = localStorage.getItem('token')
  if (token) {
    config.token = token
    ElMessage.success('Token已从本地存储加载')
  } else {
    ElMessage.warning('本地存储中未找到Token')
  }
}

const makeRequest = async (method, path, data = null) => {
  const url = `${config.baseURL}${path}`
  const headers = {
    'Content-Type': 'application/json'
  }
  
  if (config.token) {
    headers.Authorization = `Bearer ${config.token}`
  }

  const requestConfig = {
    method,
    url,
    headers,
    timeout: 10000
  }

  if (data && method !== 'GET') {
    requestConfig.data = data
  }

  try {
    const response = await axios(requestConfig)
    addResult(method, url, true, response.status, response.data, headers, data ? JSON.stringify(data, null, 2) : '')
    return response.data
  } catch (error) {
    const errorMsg = error.response 
      ? `HTTP ${error.response.status}: ${error.response.statusText}`
      : error.message
    const errorData = error.response?.data || errorMsg
    addResult(method, url, false, error.response?.status || 'ERROR', errorData, headers, data ? JSON.stringify(data, null, 2) : '')
    throw error
  }
}

const testConnection = async () => {
  testing.value = true
  try {
    await makeRequest('GET', '/projects?current=1&size=1')
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error(`连接测试失败: ${error.message}`)
  } finally {
    testing.value = false
  }
}

const testAPI = async (path, method = 'GET') => {
  testing.value = true
  try {
    const response = await makeRequest(method, path)
    ElMessage.success(`${method} ${path} 测试成功`)
    return response
  } catch (error) {
    ElMessage.error(`${method} ${path} 测试失败: ${error.message}`)
  } finally {
    testing.value = false
  }
}

const createTestMember = async () => {
  const testData = {
    projectId: 1,
    userId: 1,
    role: 'DEVELOPER',
    skillLevel: 'INTERMEDIATE',
    allocationPercentage: 100,
    performanceRating: 4.0,
    productivityIndex: 1.2
  }

  testing.value = true
  try {
    await makeRequest('POST', '/project-members', testData)
    ElMessage.success('测试成员创建成功')
  } catch (error) {
    ElMessage.error(`创建测试成员失败: ${error.message}`)
  } finally {
    testing.value = false
  }
}

const testCustomAPI = async () => {
  let data = null
  if (customAPI.body && customAPI.method !== 'GET') {
    try {
      data = JSON.parse(customAPI.body)
    } catch (error) {
      ElMessage.error('请求体JSON格式错误')
      return
    }
  }

  testing.value = true
  try {
    await makeRequest(customAPI.method, customAPI.path, data)
    ElMessage.success('自定义API测试成功')
  } catch (error) {
    ElMessage.error(`自定义API测试失败: ${error.message}`)
  } finally {
    testing.value = false
  }
}

const showDetails = (result) => {
  selectedResult.value = result
  detailVisible.value = true
}

const clearResults = () => {
  results.value = []
  ElMessage.info('测试结果已清除')
}

const exportResults = () => {
  const data = JSON.stringify(results.value, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `api-test-results-${new Date().toISOString().slice(0, 19)}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('测试结果已导出')
}

// 页面加载时自动加载token
loadTokenFromStorage()
</script>

<style scoped>
.api-test {
  padding: 20px;
}

.quick-tests {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 4px;
  background: #f9f9f9;
}

.test-results {
  margin-top: 20px;
}

.code-block {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}
</style>