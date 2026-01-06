<template>
  <div class="test-page">
    <el-card>
      <template #header>
        <h3>权限测试页面</h3>
      </template>
      
      <div class="user-info">
        <h4>当前用户信息：</h4>
        <p><strong>用户名：</strong>{{ userStore.user?.username || '未登录' }}</p>
        <p><strong>真实姓名：</strong>{{ userStore.user?.realName || '未设置' }}</p>
        <p><strong>角色：</strong>{{ userStore.user?.role || '未设置' }}</p>
        <p><strong>Token：</strong>{{ userStore.token ? '已设置' : '未设置' }}</p>
      </div>
      
      <div class="menu-permissions">
        <h4>菜单权限检查：</h4>
        <ul>
          <li>工作台：{{ hasMenuPermissionLocal('dashboard') ? '✅' : '❌' }}</li>
          <li>项目管理：{{ hasMenuPermissionLocal('projects') ? '✅' : '❌' }}</li>
          <li>任务管理：{{ hasMenuPermissionLocal('tasks') ? '✅' : '❌' }}</li>
          <li>工时记录：{{ hasMenuPermissionLocal('timeEntries') ? '✅' : '❌' }}</li>
          <li>工时审核：{{ hasMenuPermissionLocal('approvals') ? '✅' : '❌' }}</li>
          <li>用户管理：{{ hasMenuPermissionLocal('users') ? '✅' : '❌' }}</li>
        </ul>
      </div>
      
      <div class="actions">
        <el-button @click="refreshUserInfo">刷新用户信息</el-button>
        <el-button @click="testApi">测试API</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { hasMenuPermission } from '@/config/permissions'

const userStore = useUserStore()

const hasMenuPermissionLocal = (menuKey) => {
  const userRole = userStore.user?.role
  return hasMenuPermission(menuKey, userRole)
}

const refreshUserInfo = () => {
  userStore.initUser()
  ElMessage.success('用户信息已刷新')
}

const testApi = async () => {
  try {
    const response = await fetch('/api/test/hello')
    const data = await response.json()
    ElMessage.success('API测试成功: ' + data.message)
  } catch (error) {
    ElMessage.error('API测试失败: ' + error.message)
  }
}
</script>

<style scoped>
.test-page {
  padding: 20px;
}

.user-info, .menu-permissions {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.user-info p, .menu-permissions li {
  margin: 8px 0;
}

.actions {
  margin-top: 20px;
}
</style>