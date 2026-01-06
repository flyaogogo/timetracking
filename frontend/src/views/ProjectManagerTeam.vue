<template>
  <div class="project-manager-team">
    <div class="page-header">
      <h2>团队管理</h2>
      <p>管理您项目的团队成员和权限</p>
    </div>

    <el-row :gutter="20">
      <!-- 项目选择 -->
      <el-col :span="24">
        <el-card class="project-selector">
          <div class="selector-content">
            <el-select
              v-model="selectedProject"
              placeholder="选择项目"
              style="width: 300px"
              @change="handleProjectChange"
            >
              <el-option
                v-for="project in managedProjects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            
            <el-button 
              type="primary" 
              @click="showAddMemberDialog" 
              style="margin-left: 16px"
              :disabled="!selectedProject"
            >
              添加成员
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="selectedProject" v-loading="loading">
      <!-- 团队成员列表 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>团队成员 ({{ teamMembers.length }})</span>
            </template>
            
            <el-table :data="teamMembers" style="width: 100%">
              <el-table-column prop="userRealName" label="成员" width="120">
                <template #default="{ row }">
                  <div class="member-info">
                    <strong>{{ row.userRealName }}</strong>
                    <div class="member-email">{{ row.email }}</div>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="role" label="项目角色" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ getRoleText(row.role) }}</el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="权限" width="250">
                <template #default="{ row }">
                  <div class="permissions">
                    <el-tag v-if="row.isProjectManager" type="danger" size="small" style="margin: 2px;">项目经理</el-tag>
                    <el-tag v-if="row.isTechLeader" type="warning" size="small" style="margin: 2px;">技术负责人</el-tag>
                    <el-tag v-if="row.canApproveTimesheet" type="success" size="small" style="margin: 2px;">工时审批</el-tag>
                    <el-tag v-if="row.canManageTasks" type="info" size="small" style="margin: 2px;">任务管理</el-tag>
                    <el-tag v-if="row.canViewReports" type="primary" size="small" style="margin: 2px;">报表查看</el-tag>
                    <span v-if="!hasAnyPermission(row)" class="no-permissions">无特殊权限</span>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="joinDate" label="加入日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.joinDate) }}
                </template>
              </el-table-column>
              
              <el-table-column prop="allocationPercentage" label="分配比例" width="100">
                <template #default="{ row }">
                  {{ row.allocationPercentage || 0 }}%
                </template>
              </el-table-column>
              
              <el-table-column prop="hourlyRate" label="时薪" width="100">
                <template #default="{ row }">
                  ¥{{ row.hourlyRate || 0 }}
                </template>
              </el-table-column>
              
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag 
                    :type="isActive(row) ? 'success' : 'info'" 
                    size="small"
                  >
                    {{ isActive(row) ? '活跃' : '非活跃' }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="editMember(row)">
                    编辑
                  </el-button>
                  <el-button type="warning" size="small" @click="setPermissions(row)">
                    权限
                  </el-button>
                  <el-button type="danger" size="small" @click="removeMember(row)">
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else class="empty-state">
      <el-empty description="请选择项目查看团队成员" />
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加团队成员"
      width="600px"
    >
      <el-form :model="addMemberForm" label-width="100px">
        <el-form-item label="选择用户">
          <el-select
            v-model="addMemberForm.userId"
            placeholder="请选择用户"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="`${user.realName} (${user.username})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="项目角色">
          <el-select v-model="addMemberForm.role" placeholder="请选择角色">
            <el-option label="开发工程师" value="DEVELOPER" />
            <el-option label="测试工程师" value="TESTER" />
            <el-option label="设计师" value="DESIGNER" />
            <el-option label="产品经理" value="PRODUCT_MANAGER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="分配比例">
          <el-input-number
            v-model="addMemberForm.allocationPercentage"
            :min="0"
            :max="100"
            :step="5"
          />
          <span style="margin-left: 8px">%</span>
        </el-form-item>
        
        <el-form-item label="时薪">
          <el-input-number
            v-model="addMemberForm.hourlyRate"
            :min="0"
            :step="10"
            :precision="2"
          />
          <span style="margin-left: 8px">元/小时</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addMember">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限设置"
      width="500px"
    >
      <el-form :model="permissionForm" label-width="120px">
        <el-form-item label="项目经理">
          <el-switch v-model="permissionForm.isProjectManager" />
        </el-form-item>
        
        <el-form-item label="技术负责人">
          <el-switch v-model="permissionForm.isTechLeader" />
        </el-form-item>
        
        <el-form-item label="工时审批">
          <el-switch v-model="permissionForm.canApproveTimesheet" />
        </el-form-item>
        
        <el-form-item label="任务管理">
          <el-switch v-model="permissionForm.canManageTasks" />
        </el-form-item>
        
        <el-form-item label="报表查看">
          <el-switch v-model="permissionForm.canViewReports" />
        </el-form-item>
        
        <el-form-item label="生效日期">
          <el-date-picker
            v-model="permissionForm.effectiveDate"
            type="date"
            placeholder="选择生效日期"
          />
        </el-form-item>
        
        <el-form-item label="失效日期">
          <el-date-picker
            v-model="permissionForm.expiryDate"
            type="date"
            placeholder="选择失效日期"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const managedProjects = ref([])
const selectedProject = ref('')
const teamMembers = ref([])
const availableUsers = ref([])
const loading = ref(false)

const addMemberDialogVisible = ref(false)
const addMemberForm = ref({
  userId: '',
  role: '',
  allocationPercentage: 100,
  hourlyRate: 0
})

const permissionDialogVisible = ref(false)
const permissionForm = ref({
  memberId: '',
  isProjectManager: false,
  isTechLeader: false,
  canApproveTimesheet: false,
  canManageTasks: false,
  canViewReports: false,
  effectiveDate: null,
  expiryDate: null
})

// 获取管理的项目列表
const getManagedProjects = async () => {
  try {
    const response = await request({
      url: '/project-manager/projects',
      method: 'get',
      params: { current: 1, size: 1000 }
    })
    
    if (response.code === 200) {
      managedProjects.value = response.data.records || []
      
      // 如果有项目且没有选中项目，默认选择第一个项目
      if (managedProjects.value.length > 0 && !selectedProject.value) {
        selectedProject.value = managedProjects.value[0].id
        getTeamMembers()
      }
    }
  } catch (error) {
    console.error('获取管理项目失败:', error)
  }
}

// 获取团队成员
const getTeamMembers = async () => {
  if (!selectedProject.value) return
  
  loading.value = true
  try {
    const response = await request({
      url: '/project-permissions/members',
      method: 'get',
      params: { projectId: selectedProject.value }
    })
    
    if (response.code === 200) {
      teamMembers.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取团队成员失败')
    }
  } catch (error) {
    ElMessage.error('获取团队成员失败')
  } finally {
    loading.value = false
  }
}

// 获取可用用户列表
const getAvailableUsers = async () => {
  try {
    const response = await request({
      url: '/users',
      method: 'get',
      params: { current: 1, size: 1000 }
    })
    
    if (response.code === 200) {
      availableUsers.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 项目变更处理
const handleProjectChange = () => {
  getTeamMembers()
}

// 显示添加成员对话框
const showAddMemberDialog = () => {
  getAvailableUsers()
  addMemberDialogVisible.value = true
}

// 添加成员
const addMember = async () => {
  if (!addMemberForm.value.userId || !addMemberForm.value.role) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const response = await request({
      url: '/project-permissions/add-member',
      method: 'post',
      data: {
        projectId: selectedProject.value,
        ...addMemberForm.value
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('添加成员成功')
      addMemberDialogVisible.value = false
      addMemberForm.value = {
        userId: '',
        role: '',
        allocationPercentage: 100,
        hourlyRate: 0
      }
      getTeamMembers()
    } else {
      ElMessage.error(response.message || '添加成员失败')
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  }
}

// 编辑成员
const editMember = (member) => {
  ElMessage.info('编辑成员功能开发中')
}

// 设置权限
const setPermissions = (member) => {
  permissionForm.value = {
    memberId: member.id,
    isProjectManager: member.isProjectManager || false,
    isTechLeader: member.isTechLeader || false,
    canApproveTimesheet: member.canApproveTimesheet || false,
    canManageTasks: member.canManageTasks || false,
    canViewReports: member.canViewReports || false,
    effectiveDate: member.effectiveDate ? new Date(member.effectiveDate) : null,
    expiryDate: member.expiryDate ? new Date(member.expiryDate) : null
  }
  permissionDialogVisible.value = true
}

// 保存权限
const savePermissions = async () => {
  try {
    const response = await request({
      url: '/project-permissions/update-permissions',
      method: 'post',
      data: {
        projectId: selectedProject.value,
        ...permissionForm.value
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('权限设置成功')
      permissionDialogVisible.value = false
      getTeamMembers()
    } else {
      ElMessage.error(response.message || '权限设置失败')
    }
  } catch (error) {
    ElMessage.error('权限设置失败')
  }
}

// 移除成员
const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${member.userRealName} 吗？`, '确认移除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request({
      url: '/project-permissions/remove-member',
      method: 'post',
      data: {
        projectId: selectedProject.value,
        memberId: member.id
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('移除成员成功')
      getTeamMembers()
    } else {
      ElMessage.error(response.message || '移除成员失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除成员失败')
    }
  }
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'MANAGER': '项目经理',
    'PROJECT_MANAGER': '项目经理',
    'DEVELOPER': '开发工程师',
    'TESTER': '测试工程师',
    'DESIGNER': '设计师',
    'PRODUCT_MANAGER': '产品经理',
    'TECH_LEADER': '技术负责人',
    'OTHER': '其他'
  }
  return roleMap[role] || role
}

// 检查成员是否有任何权限
const hasAnyPermission = (member) => {
  return member.isProjectManager || member.isTechLeader || 
         member.canApproveTimesheet || member.canManageTasks || 
         member.canViewReports
}

// 检查成员是否活跃
const isActive = (member) => {
  const now = new Date()
  const effectiveDate = member.effectiveDate ? new Date(member.effectiveDate) : null
  const expiryDate = member.expiryDate ? new Date(member.expiryDate) : null
  
  if (effectiveDate && now < effectiveDate) return false
  if (expiryDate && now > expiryDate) return false
  
  return true
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  getManagedProjects()
})
</script>

<style scoped>
.project-manager-team {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.project-selector {
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  align-items: center;
}

.member-info {
  line-height: 1.4;
}

.member-email {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.permissions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.no-permissions {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

.empty-state {
  margin-top: 60px;
}
</style>