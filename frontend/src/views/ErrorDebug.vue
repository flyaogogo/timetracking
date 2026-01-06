<template>
  <div class="error-debug">
    <el-card>
      <h2>错误调试工具</h2>
      
      <div class="test-section">
        <h3>API连接测试</h3>
        <div class="test-buttons">
          <el-button @click="testUserAPI" type="primary">测试用户API</el-button>
          <el-button @click="testOrgAPI" type="success">测试组织API</el-button>
          <el-button @click="testAuth" type="warning">测试认证</el-button>
        </div>
      </div>
      
      <div class="error-section">
        <h3>错误日志</h3>
        <div class="error-log">
          <div v-for="(error, index) in errorLog" :key="index" class="error-item">
            <span class="error-time">{{ error.time }}</span>
            <span class="error-type" :class="error.type">{{ error.type }}</span>
            <span class="error-message">{{ error.message }}</span>
          </div>
        </div>
      </div>
      
      <div class="result-section">
        <h3>测试结果</h3>
        <pre class="result-content">{{ testResult }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getUserList } from '@/api/user'
import { getOrganizationTree } from '@/api/organization'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const errorLog = ref([])
const testResult = ref('')

// 添加错误日志
const addError = (type, message) => {
  errorLog.value.unshift({
    time: new Date().toLocaleTimeString(),
    type,
    message
  })
  
  // 只保留最近20条错误
  if (errorLog.value.length > 20) {
    errorLog.value = errorLog.value.slice(0, 20)
  }
}

// 捕获全局错误
const handleError = (event) => {
  addError('ERROR', `JavaScript错误: ${event.error?.message || event.message}`)
}

const handleUnhandledRejection = (event) => {
  addError('PROMISE', `Promise拒绝: ${event.reason?.message || event.reason}`)
}

// 测试用户API
const testUserAPI = async () => {
  try {
    testResult.value = '正在测试用户API...'
    addError('INFO', '开始测试用户API')
    
    const response = await getUserList({ current: 1, size: 5 })
    
    if (response.code === 200) {
      testResult.value = `✅ 用户API测试成功\n数据: ${JSON.stringify(response.data, null, 2)}`
      addError('SUCCESS', '用户API测试成功')
    } else {
      testResult.value = `❌ 用户API测试失败\n错误: ${response.message}`
      addError('ERROR', `用户API失败: ${response.message}`)
    }
  } catch (error) {
    testResult.value = `❌ 用户API测试异常\n错误: ${error.message}`
    addError('ERROR', `用户API异常: ${error.message}`)
  }
}

// 测试组织API
const testOrgAPI = async () => {
  try {
    testResult.value = '正在测试组织API...'
    addError('INFO', '开始测试组织API')
    
    const response = await getOrganizationTree()
    
    if (response.code === 200) {
      testResult.value = `✅ 组织API测试成功\n数据: ${JSON.stringify(response.data, null, 2)}`
      addError('SUCCESS', '组织API测试成功')
    } else {
      testResult.value = `❌ 组织API测试失败\n错误: ${response.message}`
      addError('ERROR', `组织API失败: ${response.message}`)
    }
  } catch (error) {
    testResult.value = `❌ 组织API测试异常\n错误: ${error.message}`
    addError('ERROR', `组织API异常: ${error.message}`)
  }
}

// 测试认证
const testAuth = () => {
  try {
    testResult.value = '正在检查认证状态...'
    
    const token = userStore.token
    const user = userStore.user
    
    const authInfo = {
      hasToken: !!token,
      tokenLength: token ? token.length : 0,
      hasUser: !!user,
      userRole: user?.role,
      userName: user?.username
    }
    
    testResult.value = `认证状态:\n${JSON.stringify(authInfo, null, 2)}`
    
    if (token && user) {
      addError('SUCCESS', '认证状态正常')
    } else {
      addError('WARNING', '认证状态异常')
    }
  } catch (error) {
    testResult.value = `❌ 认证检查异常\n错误: ${error.message}`
    addError('ERROR', `认证检查异常: ${error.message}`)
  }
}

onMounted(() => {
  // 监听全局错误
  window.addEventListener('error', handleError)
  window.addEventListener('unhandledrejection', handleUnhandledRejection)
  
  addError('INFO', '错误调试工具已启动')
  
  // 自动检查认证状态
  testAuth()
})

onUnmounted(() => {
  window.removeEventListener('error', handleError)
  window.removeEventListener('unhandledrejection', handleUnhandledRejection)
})
</script>

<style scoped>
.error-debug {
  padding: 20px;
}

.test-section, .error-section, .result-section {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
}

.test-buttons {
  display: flex;
  gap: 10px;
}

.error-log {
  max-height: 300px;
  overflow-y: auto;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.error-item {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
  font-family: monospace;
}

.error-time {
  color: #666;
  min-width: 80px;
}

.error-type {
  min-width: 60px;
  font-weight: bold;
}

.error-type.ERROR {
  color: #f56c6c;
}

.error-type.WARNING {
  color: #e6a23c;
}

.error-type.SUCCESS {
  color: #67c23a;
}

.error-type.INFO {
  color: #409eff;
}

.error-type.PROMISE {
  color: #f56c6c;
}

.error-message {
  flex: 1;
  word-break: break-all;
}

.result-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 12px;
  max-height: 400px;
  overflow-y: auto;
}
</style>