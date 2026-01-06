<template>
  <div class="token-test">
    <el-card>
      <h2>Token 测试</h2>
      
      <div class="info">
        <p><strong>当前Token:</strong></p>
        <pre>{{ currentToken || '无Token' }}</pre>
        
        <p><strong>当前用户:</strong></p>
        <pre>{{ JSON.stringify(currentUser, null, 2) || '无用户信息' }}</pre>
      </div>
      
      <div class="actions">
        <el-button @click="testLogin" type="primary">测试登录</el-button>
        <el-button @click="testAPI" type="success">测试API调用</el-button>
        <el-button @click="refreshToken" type="info">刷新Token</el-button>
      </div>
      
      <div class="result">
        <h3>测试结果:</h3>
        <pre>{{ testResult }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { getOrganizationTree } from '@/api/organization'

const userStore = useUserStore()
const testResult = ref('')

const currentToken = computed(() => userStore.token)
const currentUser = computed(() => userStore.user)

const testLogin = async () => {
  try {
    testResult.value = '正在测试登录...'
    const result = await userStore.loginAction({
      username: 'admin',
      password: 'admin123'
    })
    
    if (result.success) {
      testResult.value = '登录成功!\nToken: ' + userStore.token.substring(0, 50) + '...'
    } else {
      testResult.value = '登录失败: ' + result.message
    }
  } catch (error) {
    testResult.value = '登录错误: ' + error.message
  }
}

const testAPI = async () => {
  try {
    testResult.value = '正在测试API调用...'
    
    if (!userStore.token) {
      testResult.value = '错误: 没有Token，请先登录'
      return
    }
    
    const response = await getOrganizationTree()
    testResult.value = 'API调用成功!\n响应: ' + JSON.stringify(response, null, 2)
  } catch (error) {
    testResult.value = 'API调用失败: ' + error.message
    console.error('API错误:', error)
  }
}

const refreshToken = () => {
  userStore.initUser()
  testResult.value = 'Token已刷新'
}
</script>

<style scoped>
.token-test {
  padding: 20px;
}

.info {
  margin: 20px 0;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.info pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.actions {
  margin: 20px 0;
}

.actions .el-button {
  margin-right: 10px;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 4px;
}

.result pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>