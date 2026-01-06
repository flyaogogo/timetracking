<template>
  <div class="users">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名、姓名、邮箱"
            style="width: 300px"
            @keyup.enter="loadUsers"
          >
            <template #append>
              <el-button @click="loadUsers">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建用户
          </el-button>
        </div>
      </div>
      
      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="users"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="realName" label="真实姓名" width="120" />
        
        <el-table-column prop="email" label="邮箱" min-width="180" />
        
        <el-table-column prop="phone" label="手机号" width="130" />
        
        <el-table-column prop="organizationName" label="部门/团队" width="150">
          <template #default="{ row }">
            <span v-if="row.organizationName">{{ row.organizationName }}</span>
            <span v-else class="text-muted">未分配</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="position" label="职位" width="150" />
        
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleColor(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="monthlySalary" label="月薪" width="100">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.monthlySalary && row.monthlySalary > 0"
              :content="`月薪: ¥${formatCurrency(row.monthlySalary)} | 日薪: ¥${formatCurrency(row.monthlySalary / 22)} | 时薪: ¥${formatCurrency(row.monthlySalary / 176)}`"
              placement="top"
            >
              <span class="salary-display">¥{{ formatSalary(row.monthlySalary) }}</span>
            </el-tooltip>
            <span v-else class="no-salary">未设置</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button type="primary" size="small" text>
                操作
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="toggle">
                    <el-icon><Switch /></el-icon>
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="reset">
                    <el-icon><Key /></el-icon>
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名"
                :disabled="!!form.id"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门/团队" prop="organizationId">
              <el-select v-model="form.organizationId" placeholder="请选择部门/团队" style="width: 100%">
                <el-option
                  v-for="org in organizations"
                  :key="org.id"
                  :label="org.displayName || org.orgName"
                  :value="org.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="form.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色">
                <el-option label="系统管理员" value="ADMIN" />
                <el-option label="项目经理" value="PROJECT_MANAGER" />
                <el-option label="产品经理" value="PRODUCT_MANAGER" />
                <el-option label="部门负责人" value="DEPARTMENT_MANAGER" />
                <el-option label="开发人员" value="DEVELOPER" />
                <el-option label="测试人员" value="TESTER" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="月薪" prop="monthlySalary">
              <el-input-number
                v-model="form.monthlySalary"
                :min="0"
                :precision="2"
                placeholder="月薪（元/月）"
                style="width: 100%"
              />
              <div class="field-hint">
                <el-tooltip content="用于项目成本计算，按月薪/176小时计算时薪，仅管理员和部门负责人可见" placement="top">
                  <span class="hint-text">💡 成本计算基准，管理员可见</span>
                </el-tooltip>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item 
          v-if="!form.id" 
          label="密码" 
          prop="password"
        >
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item 
          v-if="form.id && showPasswordField" 
          label="新密码" 
          prop="password"
        >
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="留空则不修改密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item v-if="form.id">
          <el-checkbox v-model="showPasswordField">修改密码</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  ArrowDown, 
  Edit, 
  Switch, 
  Key, 
  Delete 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser as deleteUserApi 
} from '@/api/user'
import { getAllOrganizationsFlat } from '@/api/organization'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建用户')
const searchKeyword = ref('')
const users = ref([])
const organizations = ref([])
const selectedUsers = ref([])
const showPasswordField = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  organizationId: null,
  position: '',
  role: 'DEVELOPER',
  monthlySalary: 0,
  status: 1
})

const formRef = ref()
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { 
      validator: (rule, value, callback) => {
        if (!form.id && !value) {
          callback(new Error('请输入密码'))
        } else if (value && value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await getUserList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchKeyword.value
    })
    
    if (response.code === 200) {
      users.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载组织列表
const loadOrganizations = async () => {
  try {
    const response = await getAllOrganizationsFlat()
    if (response.code === 200) {
      organizations.value = response.data || []
    }
  } catch (error) {
    console.error('加载组织列表失败:', error)
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '新建用户'
  showPasswordField.value = false
  dialogVisible.value = true
}

// 编辑用户
const editUser = (row) => {
  dialogTitle.value = '编辑用户'
  showPasswordField.value = false
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

// 切换用户状态
const toggleUserStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户"${row.realName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await updateUser(row.id, {
      ...row,
      status: row.status === 1 ? 0 : 1
    })
    
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      loadUsers()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 重置密码
const resetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.realName}"的密码吗？重置后密码为：123456`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await updateUser(row.id, {
      ...row,
      password: '123456'
    })
    
    if (response.code === 200) {
      ElMessage.success('密码重置成功，新密码为：123456')
    } else {
      ElMessage.error(response.message || '密码重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

// 删除用户
const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.realName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteUserApi(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = { ...form }
        
        // 如果是编辑且没有勾选修改密码，则不传递密码字段
        if (form.id && !showPasswordField.value) {
          delete submitData.password
        }
        
        const response = form.id 
          ? await updateUser(form.id, submitData)
          : await createUser(submitData)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadUsers()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    department: '',
    organizationId: null,
    position: '',
    role: 'DEVELOPER',
    status: 1
  })
  showPasswordField.value = false
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editUser(row)
      break
    case 'toggle':
      toggleUserStatus(row)
      break
    case 'reset':
      resetPassword(row)
      break
    case 'delete':
      deleteUser(row)
      break
  }
}

// 获取角色颜色
const getRoleColor = (role) => {
  const colorMap = {
    'ADMIN': 'danger',
    'PROJECT_MANAGER': 'warning',
    'PRODUCT_MANAGER': 'primary',
    'DEPARTMENT_MANAGER': 'success',
    'DEVELOPER': 'info',
    'TESTER': 'info'
  }
  return colorMap[role] || 'info'
}

// 获取角色文本
const getRoleText = (role) => {
  const textMap = {
    'ADMIN': '系统管理员',
    'PROJECT_MANAGER': '项目经理',
    'PRODUCT_MANAGER': '产品经理',
    'DEPARTMENT_MANAGER': '部门负责人',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员'
  }
  return textMap[role] || role
}

// 格式化薪资显示（简化显示）
const formatSalary = (salary) => {
  if (!salary || salary === 0) return '0'
  
  if (salary >= 10000) {
    return (salary / 10000).toFixed(1) + 'w'
  } else if (salary >= 1000) {
    return (salary / 1000).toFixed(1) + 'k'
  } else {
    return salary.toString()
  }
}

// 格式化货币（完整显示）
const formatCurrency = (amount) => {
  if (!amount || amount === 0) return '0.00'
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  loadUsers()
  loadOrganizations()
})
</script>

<style scoped>
.users {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.el-button.success {
  color: #67c23a;
}

.el-button.danger {
  color: #f56c6c;
}

/* 操作列样式优化 */
.el-dropdown {
  vertical-align: middle;
}

.el-dropdown .el-button {
  padding: 5px 8px;
  font-size: 12px;
}

.el-dropdown-menu__item {
  padding: 8px 16px;
  font-size: 13px;
}

.el-dropdown-menu__item .el-icon {
  margin-right: 6px;
  font-size: 14px;
}

/* 薪资显示样式 */
.salary-display {
  color: #409EFF;
  font-weight: 500;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  background: #ecf5ff;
  font-size: 12px;
}

.salary-display:hover {
  background: #d9ecff;
}

.no-salary {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

/* 提示文本样式 */
.hint-text {
  color: #606266;
  font-size: 12px;
  cursor: pointer;
}

.hint-text:hover {
  color: #409EFF;
}
</style>