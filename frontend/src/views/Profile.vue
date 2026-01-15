<template>
  <div class="profile-container">
    <el-card shadow="hover" class="profile-card">
      <template #header>
        <div class="card-header">
          <h3 class="title">个人信息</h3>
          <el-loading v-if="loading" text="加载中..." background="rgba(255, 255, 255, 0.8)"></el-loading>
        </div>
      </template>
      
      <!-- 个人信息 -->
      <div class="profile-info">
        <div class="info-content">
          <div class="info-row">
            <div class="info-label">用户ID</div>
            <div class="info-value">{{ userProfile.id }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">用户名</div>
            <div class="info-value">{{ userProfile.username }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">真实姓名</div>
            <div class="info-value">{{ userProfile.realName }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">角色</div>
            <div class="info-value">{{ getRoleText(userProfile.role) }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">邮箱</div>
            <div class="info-value">{{ userProfile.email }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">手机号</div>
            <div class="info-value">{{ userProfile.phone || '未设置' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">部门</div>
            <div class="info-value">{{ userProfile.department || '未设置' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">职位</div>
            <div class="info-value">{{ userProfile.position || '未设置' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">状态</div>
            <div class="info-value">
              <el-tag :type="userProfile.status === 1 ? 'success' : 'danger'">
                {{ userProfile.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" @click="showEditDialog" :icon="Edit">
          编辑信息
        </el-button>
        <el-button @click="showPasswordDialog" :icon="Key">
          修改密码
        </el-button>
      </div>
    </el-card>
    
    <!-- 编辑信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑个人信息"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="editForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="editForm.position" placeholder="请输入职位" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordFormRules"
        label-width="120px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserById, updateUser } from '@/api/user'

const userStore = useUserStore()

// 用户信息
const userProfile = ref({})
const loading = ref(true)

// 编辑信息对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  realName: '',
  email: '',
  phone: '',
  department: '',
  position: ''
})

const editFormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: ['blur', 'change'] }
  ]
}

// 修改密码对话框
const passwordDialogVisible = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 角色文本转换
const getRoleText = (role) => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'PROJECT_MANAGER': '项目经理',
    'PRODUCT_MANAGER': '产品经理',
    'DEPARTMENT_MANAGER': '部门经理',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员'
  }
  return roleMap[role] || role
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

// 加载用户信息
const loadUserProfile = async () => {
  if (!userStore.user) return
  
  loading.value = true
  try {
    const result = await getUserById(userStore.user.id)
    if (result.code === 200) {
      // 确保不显示薪资
      const user = result.data
      delete user.monthlySalary
      userProfile.value = user
      
      // 初始化编辑表单
      Object.assign(editForm, {
        realName: user.realName,
        email: user.email,
        phone: user.phone || '',
        department: user.department || '',
        position: user.position || ''
      })
    } else {
      ElMessage.error('获取个人信息失败')
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败')
  } finally {
    loading.value = false
  }
}

// 显示编辑信息对话框
const showEditDialog = () => {
  editDialogVisible.value = true
}

// 提交编辑信息
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const result = await updateUser(userProfile.value.id, editForm)
        if (result.code === 200) {
          ElMessage.success('信息更新成功')
          editDialogVisible.value = false
          // 重新加载用户信息
          loadUserProfile()
        } else {
          ElMessage.error(result.message || '信息更新失败')
        }
      } catch (error) {
        console.error('信息更新失败:', error)
        ElMessage.error('信息更新失败')
      }
    }
  })
}

// 显示修改密码对话框
const showPasswordDialog = () => {
  passwordDialogVisible.value = true
}

// 提交修改密码
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 这里需要后端提供修改密码的接口
        ElMessage.warning('修改密码功能开发中')
        passwordDialogVisible.value = false
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error('修改密码失败')
      }
    }
  })
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
/* 重置body滚动条 */
:deep(html), :deep(body) {
  overflow: hidden;
}

.profile-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
  overflow: hidden;
}

.profile-card {
  max-width: 100%;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 个人信息样式 */
.profile-info {
  padding: 16px 20px;
}

.info-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #fff;
  padding: 10px 14px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
}

.info-row:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  min-width: 90px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: flex-start;
  padding: 0 20px 16px;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
  padding: 8px 16px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 12px;
    min-height: calc(100vh - 48px);
  }
  
  .profile-card {
    margin: 0;
    box-shadow: none;
    border-radius: 0;
  }
  
  .card-header {
    padding: 12px 16px;
  }
  
  .profile-info {
    padding: 16px;
  }
  
  .info-content {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .info-row {
    padding: 10px 14px;
  }
  
  .info-row.full-width {
    grid-column: span 1;
  }
  
  .info-label {
    min-width: 80px;
    font-size: 13px;
  }
  
  .info-value {
    font-size: 13px;
  }
  
  .actions {
    flex-direction: column;
    align-items: stretch;
    padding: 0 16px 16px;
  }
}
</style>