<template>
  <div class="navigation-test">
    <el-card>
      <h2>导航测试页面</h2>
      
      <div class="user-info">
        <h3>当前用户信息</h3>
        <pre>{{ JSON.stringify(userInfo, null, 2) }}</pre>
      </div>
      
      <div class="navigation-buttons">
        <h3>测试导航</h3>
        <el-button @click="testNavigation('/dashboard')" type="primary">
          测试工作台
        </el-button>
        <el-button @click="testNavigation('/users')" type="success">
          测试用户管理
        </el-button>
        <el-button @click="testNavigation('/organizations')" type="warning">
          测试组织管理
        </el-button>
        <el-button @click="testNavigation('/projects')" type="info">
          测试项目管理
        </el-button>
      </div>
      
      <div class="test-results">
        <h3>测试结果</h3>
        <div class="result-log">
          <div v-for="(log, index) in testLogs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-level" :class="log.level">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
      
      <div class="manual-test">
        <h3>手动测试</h3>
        <el-input 
          v-model="manualPath" 
          placeholder="输入路径，如 /users"
          style="width: 200px; margin-right: 10px;"
        />
        <el-button @click="testNavigation(manualPath)" type="primary">
          手动导航
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const testLogs = ref([])
const manualPath = ref('/users')

const userInfo = computed(() => ({
  token: userStore.token ? userStore.token.substring(0, 50) + '...' : null,
  user: userStore.user,
  role: userStore.user?.role
}))

// 添加日志
const addLog = (level, message) => {
  testLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    level,
    message
  })
  
  // 只保留最近20条日志
  if (testLogs.value.length > 20) {
    testLogs.value = testLogs.value.slice(0, 20)
  }
}

// 测试导航
const testNavigation = async (path) => {
  addLog('INFO', `开始测试导航到: ${path}`)
  
  try {
    // 检查用户信息
    if (!userStore.token || !userStore.user) {
      addLog('ERROR', '用户未登录或用户信息缺失')
      return
    }
    
    addLog('INFO', `用户角色: ${userStore.user.role}`)
    
    // 执行导航
    await router.push(path)
    addLog('SUCCESS', `导航成功: ${path}`)
    
  } catch (error) {
    addLog('ERROR', `导航失败: ${error.message}`)
    console.error('导航错误详情:', error)
  }
}

// 监听路由变化
router.afterEach((to, from) => {
  addLog('INFO', `路由变化: ${from.path} -> ${to.path}`)
})

// 初始化日志
addLog('INFO', '导航测试页面已加载')
</script>

<style scoped>
.navigation-test {
  padding: 20px;
}

.user-info, .navigation-buttons, .test-results, .manual-test {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
}

.user-info pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
}

.navigation-buttons .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

.result-log {
  max-height: 300px;
  overflow-y: auto;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.log-item {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
  font-family: monospace;
}

.log-time {
  color: #666;
  min-width: 80px;
}

.log-level {
  min-width: 60px;
  font-weight: bold;
}

.log-level.INFO {
  color: #409eff;
}

.log-level.SUCCESS {
  color: #67c23a;
}

.log-level.ERROR {
  color: #f56c6c;
}

.log-message {
  flex: 1;
}

.manual-test {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>