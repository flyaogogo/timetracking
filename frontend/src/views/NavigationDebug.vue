<template>
  <div class="navigation-debug">
    <el-card>
      <h2>导航调试信息</h2>
      
      <div class="debug-section">
        <h3>用户信息</h3>
        <pre>{{ JSON.stringify(userInfo, null, 2) }}</pre>
      </div>
      
      <div class="debug-section">
        <h3>当前路由</h3>
        <pre>{{ JSON.stringify(routeInfo, null, 2) }}</pre>
      </div>
      
      <div class="debug-section">
        <h3>菜单权限检查</h3>
        <div class="permission-checks">
          <div v-for="menu in menuList" :key="menu.key" class="permission-item">
            <span class="menu-name">{{ menu.name }}</span>
            <span :class="['permission-status', menu.hasPermission ? 'allowed' : 'denied']">
              {{ menu.hasPermission ? '✅ 允许' : '❌ 拒绝' }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="debug-section">
        <h3>测试导航</h3>
        <div class="test-buttons">
          <el-button @click="testNavigation('/dashboard')" type="primary">测试工作台</el-button>
          <el-button @click="testNavigation('/users')" type="success">测试用户管理</el-button>
          <el-button @click="testNavigation('/organizations')" type="warning">测试组织管理</el-button>
          <el-button @click="testNavigation('/projects')" type="info">测试项目管理</el-button>
        </div>
      </div>
      
      <div class="debug-section">
        <h3>测试结果</h3>
        <pre>{{ testResult }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { hasMenuPermission } from '@/config/permissions'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const testResult = ref('')

const userInfo = computed(() => ({
  token: userStore.token ? userStore.token.substring(0, 50) + '...' : null,
  user: userStore.user,
  role: userStore.user?.role
}))

const routeInfo = computed(() => ({
  path: route.path,
  name: route.name,
  meta: route.meta,
  params: route.params,
  query: route.query
}))

const menuList = ref([
  { key: 'dashboard', name: '工作台' },
  { key: 'projects', name: '项目管理' },
  { key: 'tasks', name: '任务管理' },
  { key: 'timeEntries', name: '工时记录' },
  { key: 'approvals', name: '工时审核' },
  { key: 'users', name: '用户管理' },
  { key: 'organizations', name: '组织管理' },
  { key: 'test', name: '权限测试' }
])

// 检查菜单权限
const checkMenuPermissions = () => {
  const userRole = userStore.user?.role
  menuList.value.forEach(menu => {
    menu.hasPermission = hasMenuPermission(menu.key, userRole)
  })
}

// 测试导航
const testNavigation = async (path) => {
  try {
    testResult.value = `正在导航到 ${path}...`
    
    // 检查权限
    const userRole = userStore.user?.role
    console.log('用户角色:', userRole)
    console.log('目标路径:', path)
    
    // 尝试导航
    await router.push(path)
    testResult.value = `✅ 成功导航到 ${path}`
  } catch (error) {
    testResult.value = `❌ 导航失败: ${error.message}`
    console.error('导航错误:', error)
  }
}

onMounted(() => {
  checkMenuPermissions()
  
  // 输出调试信息到控制台
  console.log('=== 导航调试信息 ===')
  console.log('用户信息:', userStore.user)
  console.log('Token:', userStore.token ? '存在' : '不存在')
  console.log('当前路由:', route.path)
  console.log('菜单权限:', menuList.value)
})
</script>

<style scoped>
.navigation-debug {
  padding: 20px;
}

.debug-section {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  background-color: #fafafa;
}

.debug-section h3 {
  margin-top: 0;
  color: #333;
}

.debug-section pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 12px;
}

.permission-checks {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.permission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.menu-name {
  font-weight: 500;
}

.permission-status.allowed {
  color: #67c23a;
}

.permission-status.denied {
  color: #f56c6c;
}

.test-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.test-buttons .el-button {
  margin: 0;
}
</style>