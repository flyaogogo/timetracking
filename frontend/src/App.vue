<template>
  <div id="app" ref="appRef">
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const appRef = ref(null)
const router = useRouter()
const userStore = useUserStore()

// 会话超时配置
const SESSION_TIMEOUT = 3600000 // 1小时，与后端JWT过期时间一致
let timeoutTimer = null

// 重置超时计时器
const resetTimeoutTimer = () => {
  if (timeoutTimer) {
    clearTimeout(timeoutTimer)
  }
  
  timeoutTimer = setTimeout(() => {
    // 会话超时，执行退出登录
    handleSessionTimeout()
  }, SESSION_TIMEOUT)
}

// 处理会话超时
const handleSessionTimeout = () => {
  if (userStore.token) {
    ElMessage.warning('登录已过期，请重新登录')
    userStore.logout()
    router.push('/login')
  }
}

// 监听用户活动
const handleUserActivity = () => {
  // 只有在用户已登录的情况下才重置计时器
  if (userStore.token) {
    resetTimeoutTimer()
  }
}

// 注册事件监听器
const registerActivityListeners = () => {
  const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart']
  const appElement = appRef.value || document
  
  events.forEach(event => {
    appElement.addEventListener(event, handleUserActivity)
  })
  
  return () => {
    events.forEach(event => {
      appElement.removeEventListener(event, handleUserActivity)
    })
  }
}

onMounted(() => {
  // 初始化超时计时器
  resetTimeoutTimer()
  
  // 注册用户活动监听器
  registerActivityListeners()
})

onUnmounted(() => {
  // 清除超时计时器
  if (timeoutTimer) {
    clearTimeout(timeoutTimer)
  }
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

/* 全局响应式表格样式 */
.el-table {
  font-size: 14px;
}

@media (max-width: 768px) {
  .el-table {
    font-size: 12px;
  }
  
  .el-table .el-table__cell {
    padding: 8px 0;
  }
  
  .el-button--small {
    padding: 5px 8px;
    font-size: 11px;
  }
}

/* 全局卡片样式 */
.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 全局分页样式 */
.pagination {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .pagination {
    text-align: center;
  }
  
  .el-pagination {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
  }
}

/* 全局工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .actions {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }
}

/* 全局对话框响应式 */
@media (max-width: 768px) {
  .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .el-dialog__body {
    padding: 20px 15px;
  }
}

/* 全局表单响应式 */
@media (max-width: 480px) {
  .el-form-item__label {
    text-align: left !important;
    padding-right: 0 !important;
  }
  
  .el-col {
    margin-bottom: 16px;
  }
}
</style>