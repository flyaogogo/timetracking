<template>
  <div class="project-team-enhanced">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目权限管理</span>
          <div class="header-actions">
            <el-select
              v-model="selectedProjectId"
              placeholder="选择项目"
              style="width: 200px; margin-right: 10px"
              @change="loadProjectData"
            >
              <el-option
                v-for="project in projects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            <el-button 
              type="info" 
              @click="showPermissionGuide"
              style="margin-right: 10px"
            >
              <el-icon><QuestionFilled /></el-icon>
              权限指南
            </el-button>
            <el-button 
              type="primary" 
              @click="showAddMemberDialog" 
              :disabled="!selectedProjectId || !canManagePermissions"
            >
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 项目信息 -->
      <div class="project-info" v-if="selectedProjectId && currentProject">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="项目名称">{{ currentProject.projectName }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="getProjectStatusColor(currentProject.status)">
              {{ getProjectStatusText(currentProject.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="团队规模">{{ projectMembers.length }}人</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 权限概览 -->
      <div v-if="selectedProjectId && permissionSummary" class="permission-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ permissionSummary.roleDescription || '普通成员' }}</div>
                <div class="stat-label">我的角色</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ (permissionSummary.permissions || []).length }}</div>
                <div class="stat-label">我的权限</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ projectMembers.length }}</div>
                <div class="stat-label">项目成员</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ getActivePermissions() }}</div>
                <div class="stat-label">活跃权限</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 项目成员列表 -->
      <el-table
        v-if="selectedProjectId"
        v-loading="loading"
        :data="projectMembers"
        style="width: 100%; margin-top: 20px"
        empty-text="请选择项目查看成员信息"
      >
        <el-table-column prop="userRealName" label="成员姓名" width="120" />
        
        <el-table-column prop="userRole" label="全局角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getGlobalRoleColor(row.userRole)" size="small">
              {{ getGlobalRoleText(row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="项目角色" width="200">
          <template #default="{ row }">
            <div class="project-roles">
              <el-tag v-if="row.isProjectManager" type="danger" size="small" style="margin: 2px">
                项目经理
              </el-tag>
              <el-tag v-if="row.isTechLeader" type="warning" size="small" style="margin: 2px">
                技术负责人
              </el-tag>
              <el-tag v-if="!row.isProjectManager && !row.isTechLeader" type="info" size="small">
                {{ getProjectRoleText(row.role) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="项目权限" min-width="300">
          <template #default="{ row }">
            <div class="permissions-display">
              <el-tag 
                v-for="permission in getDisplayPermissions(row)" 
                :key="permission.type"
                :type="permission.color"
                size="small"
                style="margin: 2px"
              >
                {{ permission.text }}
              </el-tag>
              <span v-if="getDisplayPermissions(row).length === 0" class="no-permissions">
                无特殊权限
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="权限期限" width="120">
          <template #default="{ row }">
            <span v-if="row.expiryDate" class="expiry-date">
              {{ formatDate(row.expiryDate) }}
            </span>
            <span v-else class="permanent">永久</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(cmd) => handleCommand(cmd, row)" v-if="canManagePermissions">
              <el-button type="primary" size="small">
                权限管理 <el-icon><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    command="setManager"
                    v-if="!row.isProjectManager"
                  >
                    <el-icon><UserFilled /></el-icon>
                    设为项目经理
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="removeManager"
                    v-if="row.isProjectManager"
                  >
                    <el-icon><Remove /></el-icon>
                    撤销项目经理
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="setTechLeader"
                    v-if="!row.isTechLeader"
                  >
                    <el-icon><Tools /></el-icon>
                    设为技术负责人
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="removeTechLeader"
                    v-if="row.isTechLeader"
                  >
                    <el-icon><Remove /></el-icon>
                    撤销技术负责人
                  </el-dropdown-item>
                  <el-dropdown-item command="editPermissions" divided>
                    <el-icon><Setting /></el-icon>
                    编辑权限
                  </el-dropdown-item>
                  <el-dropdown-item command="copyPermissions">
                    <el-icon><CopyDocument /></el-icon>
                    复制权限
                  </el-dropdown-item>
                  <el-dropdown-item command="removeMember" divided>
                    <el-icon><Delete /></el-icon>
                    移除成员
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-else type="info" size="small" disabled>
              无权限操作
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="请选择项目查看权限信息" />
      </div>
    </el-card>
    
    <!-- 权限编辑对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="编辑项目权限" width="700px">
      <el-form :model="permissionForm" label-width="120px">
        <el-form-item label="用户">
          <span class="user-info">
            {{ currentUser.userRealName }} 
            <el-tag size="small" type="info">{{ getGlobalRoleText(currentUser.userRole) }}</el-tag>
          </span>
        </el-form-item>
        
        <el-form-item label="项目角色">
          <el-checkbox 
            v-model="permissionForm.isProjectManager"
            @change="onProjectManagerChange"
          >
            项目经理
          </el-checkbox>
          <el-checkbox 
            v-model="permissionForm.isTechLeader"
            @change="onTechLeaderChange"
          >
            技术负责人
          </el-checkbox>
        </el-form-item>
        
        <el-form-item label="具体权限">
          <el-checkbox-group v-model="permissionForm.permissions">
            <div class="permission-grid">
              <el-checkbox 
                v-for="(desc, type) in PERMISSION_DESCRIPTIONS" 
                :key="type"
                :label="type"
                :disabled="isPermissionDisabled(type)"
              >
                {{ desc }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="权限期限">
          <el-date-picker
            v-model="permissionForm.expiryDate"
            type="date"
            placeholder="选择失效日期（可选）"
            :disabled-date="disabledDate"
          />
          <div class="form-hint">留空表示永久有效</div>
        </el-form-item>
        
        <el-form-item label="授权原因">
          <el-input
            v-model="permissionForm.reason"
            type="textarea"
            :rows="2"
            placeholder="请输入授权原因（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 复制权限对话框 -->
    <el-dialog v-model="copyPermissionDialogVisible" title="复制权限配置" width="500px">
      <el-form label-width="120px">
        <el-form-item label="源用户">
          <span>{{ currentUser.userRealName }}</span>
        </el-form-item>
        
        <el-form-item label="目标用户">
          <el-select v-model="copyTargetUserId" placeholder="请选择目标用户">
            <el-option
              v-for="member in projectMembers"
              :key="member.userId"
              :label="member.userRealName"
              :value="member.userId"
              :disabled="member.userId === currentUser.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="copyPermissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCopyPermissions" :loading="copyLoading">
          复制
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 添加成员对话框 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加项目成员" width="500px">
      <el-form :model="addMemberForm" label-width="120px">
        <el-form-item label="选择用户" required>
          <el-select 
            v-model="addMemberForm.userId" 
            placeholder="请选择用户"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
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
            <el-option label="开发人员" value="DEVELOPER" />
            <el-option label="测试人员" value="TESTER" />
            <el-option label="设计师" value="DESIGNER" />
            <el-option label="项目经理" value="MANAGER" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddMember" :loading="addMemberLoading">
          添加
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 权限使用指南对话框 -->
    <el-dialog
      v-model="permissionGuideVisible"
      title="权限使用指南"
      width="80%"
      :before-close="() => permissionGuideVisible = false"
    >
      <PermissionGuide
        :permissions="permissionSummary || {}"
        :project-id="selectedProjectId"
        @close="permissionGuideVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  Plus, UserFilled, Remove, Tools, Setting, CopyDocument, Delete, ArrowDown, QuestionFilled 
} from '@element-plus/icons-vue'
import { projectPermissionApi, PERMISSION_DESCRIPTIONS } from '@/api/projectPermission'
import { getProjectMembers, addProjectMember, removeProjectMember } from '@/api/projectMember'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { EnhancedPermissionUtil } from '@/utils/enhancedPermissions'
import PermissionGuide from '@/components/PermissionGuide.vue'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const saveLoading = ref(false)
const copyLoading = ref(false)
const addMemberLoading = ref(false)
const userSearchLoading = ref(false)
const permissionDialogVisible = ref(false)
const copyPermissionDialogVisible = ref(false)
const addMemberDialogVisible = ref(false)
const permissionGuideVisible = ref(false)

const selectedProjectId = ref(null)
const projects = ref([])
const currentProject = ref({})
const projectMembers = ref([])
const currentUser = ref({})
const copyTargetUserId = ref(null)
const availableUsers = ref([])
const permissionSummary = ref(null)

const permissionForm = reactive({
  isProjectManager: false,
  isTechLeader: false,
  permissions: [],
  expiryDate: null,
  reason: ''
})

const addMemberForm = reactive({
  userId: null,
  role: 'DEVELOPER'
})

// 计算属性
const canManagePermissions = computed(() => {
  return userStore.user?.role === 'ADMIN' || 
         projectMembers.value.some(m => 
           m.userId === userStore.user?.id && m.isProjectManager
         )
})

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      
      // 如果URL中有projectId参数，自动选择项目
      const urlProjectId = parseInt(route.params.id || route.query.projectId)
      if (urlProjectId) {
        selectedProjectId.value = urlProjectId
        loadProjectData()
      } else if (projects.value.length > 0) {
        // 如果没有URL参数，默认选择第一个项目
        selectedProjectId.value = projects.value[0].id
        loadProjectData()
      }
    } else {
      throw new Error(response.message || '获取项目列表失败')
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  }
}

// 加载项目数据
const loadProjectData = async () => {
  if (!selectedProjectId.value) return
  
  await Promise.all([
    loadProject(),
    loadProjectMembers(),
    loadPermissionSummary()
  ])
}

// 加载项目信息
const loadProject = async () => {
  try {
    const project = projects.value.find(p => p.id === selectedProjectId.value)
    if (project) {
      currentProject.value = project
    }
  } catch (error) {
    console.error('加载项目信息失败:', error)
  }
}

// 加载权限摘要
const loadPermissionSummary = async () => {
  try {
    const response = await projectPermissionApi.getPermissionSummary(selectedProjectId.value)
    if (response.code === 200) {
      permissionSummary.value = response.data
    }
  } catch (error) {
    console.error('加载权限摘要失败:', error)
  }
}

// 获取活跃权限数量
const getActivePermissions = () => {
  if (!permissionSummary.value) return 0
  let count = 0
  if (permissionSummary.value.isProjectManager) count++
  if (permissionSummary.value.isTechLeader) count++
  if (permissionSummary.value.canManageTasks) count++
  if (permissionSummary.value.canApproveTimesheet) count++
  if (permissionSummary.value.canViewReports) count++
  if (permissionSummary.value.canManageCosts) count++
  if (permissionSummary.value.canManageMilestones) count++
  if (permissionSummary.value.canManageTeam) count++
  return count
}

// 加载项目成员
const loadProjectMembers = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    const response = await getProjectMembers(selectedProjectId.value)
    if (response.code === 200) {
      projectMembers.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('加载项目成员失败')
  } finally {
    loading.value = false
  }
}

// 处理操作命令
const handleCommand = (command, row) => {
  currentUser.value = row
  
  switch (command) {
    case 'setManager':
      setAsProjectManager(row)
      break
    case 'removeManager':
      removeProjectManager(row)
      break
    case 'setTechLeader':
      setAsTechLeader(row)
      break
    case 'removeTechLeader':
      removeTechLeader(row)
      break
    case 'editPermissions':
      editPermissions(row)
      break
    case 'copyPermissions':
      showCopyPermissionsDialog(row)
      break
    case 'removeMember':
      removeMember(row)
      break
  }
}

// 设置为项目经理
const setAsProjectManager = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${user.userRealName} 设置为项目经理吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    const response = await projectPermissionApi.setProjectManager(selectedProjectId.value, user.userId)
    if (response.code === 200) {
      ElMessage.success('设置成功')
      loadProjectData()
    } else {
      ElMessage.error(response.message || '设置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置失败')
    }
  }
}

// 撤销项目经理
const removeProjectManager = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销 ${user.userRealName} 的项目经理权限吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    const response = await projectPermissionApi.revokeProjectManager(selectedProjectId.value, user.userId)
    if (response.code === 200) {
      ElMessage.success('撤销成功')
      loadProjectData()
    } else {
      ElMessage.error(response.message || '撤销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

// 设置为技术负责人
const setAsTechLeader = async (user) => {
  try {
    const response = await projectPermissionApi.setTechLeader(selectedProjectId.value, user.userId)
    if (response.code === 200) {
      ElMessage.success('设置成功')
      loadProjectData()
    } else {
      ElMessage.error(response.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

// 撤销技术负责人
const removeTechLeader = async (user) => {
  try {
    const response = await projectPermissionApi.revokeTechLeader(selectedProjectId.value, user.userId)
    if (response.code === 200) {
      ElMessage.success('撤销成功')
      loadProjectData()
    } else {
      ElMessage.error(response.message || '撤销失败')
    }
  } catch (error) {
    ElMessage.error('撤销失败')
  }
}

// 编辑权限
const editPermissions = async (user) => {
  currentUser.value = user
  
  // 获取用户当前权限
  try {
    const response = await projectPermissionApi.getPermissionSummary(selectedProjectId.value, user.userId)
    if (response.code === 200) {
      const summary = response.data
      permissionForm.isProjectManager = summary.isProjectManager || false
      permissionForm.isTechLeader = summary.isTechLeader || false
      permissionForm.permissions = summary.permissions?.map(p => p.name) || []
      permissionForm.expiryDate = user.expiryDate || null
      permissionForm.reason = ''
    }
  } catch (error) {
    console.error('获取权限信息失败:', error)
  }
  
  permissionDialogVisible.value = true
}

// 保存权限
const savePermissions = async () => {
  saveLoading.value = true
  try {
    // 根据角色设置自动授予相应权限
    let permissions = [...permissionForm.permissions]
    
    if (permissionForm.isProjectManager) {
      const managerPermissions = [
        'PROJECT_MANAGEMENT', 'TASK_MANAGEMENT', 'TIMESHEET_APPROVAL', 
        'REPORT_VIEW', 'TEAM_MANAGEMENT'
      ]
      permissions = [...new Set([...permissions, ...managerPermissions])]
    }
    
    if (permissionForm.isTechLeader) {
      const techLeaderPermissions = [
        'TASK_MANAGEMENT', 'REPORT_VIEW', 'MILESTONE_MANAGEMENT'
      ]
      permissions = [...new Set([...permissions, ...techLeaderPermissions])]
    }
    
    // 批量授予权限
    const response = await projectPermissionApi.grantMultiplePermissions(
      selectedProjectId.value,
      currentUser.value.userId,
      permissions,
      permissionForm.expiryDate
    )
    
    if (response.code === 200) {
      ElMessage.success('权限更新成功')
      permissionDialogVisible.value = false
      loadProjectData()
    } else {
      ElMessage.error(response.message || '权限更新失败')
    }
  } catch (error) {
    ElMessage.error('权限更新失败')
  } finally {
    saveLoading.value = false
  }
}

// 显示复制权限对话框
const showCopyPermissionsDialog = (user) => {
  currentUser.value = user
  copyTargetUserId.value = null
  copyPermissionDialogVisible.value = true
}

// 确认复制权限
const confirmCopyPermissions = async () => {
  if (!copyTargetUserId.value) {
    ElMessage.warning('请选择目标用户')
    return
  }
  
  copyLoading.value = true
  try {
    const response = await projectPermissionApi.copyPermissions(
      currentUser.value.userId,
      copyTargetUserId.value,
      selectedProjectId.value
    )
    
    if (response.code === 200) {
      ElMessage.success('权限复制成功')
      copyPermissionDialogVisible.value = false
      loadProjectData()
    } else {
      ElMessage.error(response.message || '权限复制失败')
    }
  } catch (error) {
    ElMessage.error('权限复制失败')
  } finally {
    copyLoading.value = false
  }
}

// 显示添加成员对话框
const showAddMemberDialog = () => {
  addMemberForm.userId = null
  addMemberForm.role = 'DEVELOPER'
  searchUsers('')
  addMemberDialogVisible.value = true
}

// 搜索用户
const searchUsers = async (query) => {
  userSearchLoading.value = true
  try {
    const response = await getUserList({
      current: 1,
      size: 20,
      keyword: query
    })
    
    if (response.code === 200) {
      // 过滤掉已经是项目成员的用户
      const memberUserIds = projectMembers.value.map(m => m.userId)
      availableUsers.value = response.data.records.filter(
        user => !memberUserIds.includes(user.id)
      )
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 确认添加成员
const confirmAddMember = async () => {
  if (!addMemberForm.userId) {
    ElMessage.warning('请选择用户')
    return
  }
  
  addMemberLoading.value = true
  try {
    const response = await addProjectMember({
      projectId: selectedProjectId.value,
      userId: addMemberForm.userId,
      role: addMemberForm.role
    })
    
    if (response.code === 200) {
      ElMessage.success('添加成员成功')
      addMemberDialogVisible.value = false
      loadProjectData()
    } else {
      ElMessage.error(response.message || '添加成员失败')
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  } finally {
    addMemberLoading.value = false
  }
}

// 移除成员
const removeMember = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${user.userRealName} 从项目中移除吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    const response = await removeProjectMember(user.id)
    if (response.code === 200) {
      ElMessage.success('移除成功')
      loadProjectData()
    } else {
      ElMessage.error(response.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 项目经理变化处理
const onProjectManagerChange = (value) => {
  if (value) {
    // 自动勾选项目经理相关权限
    const managerPermissions = [
      'PROJECT_MANAGEMENT', 'TASK_MANAGEMENT', 'TIMESHEET_APPROVAL', 
      'REPORT_VIEW', 'TEAM_MANAGEMENT'
    ]
    permissionForm.permissions = [...new Set([...permissionForm.permissions, ...managerPermissions])]
  }
}

// 技术负责人变化处理
const onTechLeaderChange = (value) => {
  if (value) {
    // 自动勾选技术负责人相关权限
    const techLeaderPermissions = [
      'TASK_MANAGEMENT', 'REPORT_VIEW', 'MILESTONE_MANAGEMENT'
    ]
    permissionForm.permissions = [...new Set([...permissionForm.permissions, ...techLeaderPermissions])]
  }
}

// 判断权限是否禁用
const isPermissionDisabled = (permissionType) => {
  // 项目经理的核心权限不能取消
  if (permissionForm.isProjectManager) {
    const managerCorePermissions = ['PROJECT_MANAGEMENT', 'TEAM_MANAGEMENT']
    return managerCorePermissions.includes(permissionType)
  }
  return false
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 昨天之前的日期
}

// 获取显示的权限
const getDisplayPermissions = (member) => {
  const permissions = []
  
  // 项目经理权限
  if (member.isProjectManager) {
    permissions.push({ type: 'PROJECT_MANAGEMENT', text: '项目管理', color: 'danger' })
    permissions.push({ type: 'TEAM_MANAGEMENT', text: '团队管理', color: 'danger' })
  }
  
  // 技术负责人权限
  if (member.isTechLeader) {
    permissions.push({ type: 'MILESTONE_MANAGEMENT', text: '里程碑管理', color: 'warning' })
  }
  
  // 具体权限（只有在有权限时才显示）
  if (member.canApproveTimesheet) {
    permissions.push({ type: 'TIMESHEET_APPROVAL', text: '工时审核', color: 'success' })
  }
  if (member.canManageTasks) {
    permissions.push({ type: 'TASK_MANAGEMENT', text: '任务管理', color: 'primary' })
  }
  if (member.canViewReports) {
    permissions.push({ type: 'REPORT_VIEW', text: '报表查看', color: 'info' })
  }
  
  return permissions
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取全局角色颜色
const getGlobalRoleColor = (role) => {
  const colorMap = {
    'ADMIN': 'danger',
    'PROJECT_MANAGER': 'warning',
    'DEVELOPER': 'primary',
    'TESTER': 'success',
    'DESIGNER': 'info'
  }
  return colorMap[role] || 'info'
}

// 获取全局角色文本
const getGlobalRoleText = (role) => {
  const textMap = {
    'ADMIN': '系统管理员',
    'PROJECT_MANAGER': '项目经理',
    'DEVELOPER': '开发人员',
    'TESTER': '测试人员',
    'DESIGNER': '设计师'
  }
  return textMap[role] || role
}

// 获取项目角色文本
const getProjectRoleText = (role) => {
  const textMap = {
    'MANAGER': '经理',
    'DEVELOPER': '开发',
    'TESTER': '测试',
    'DESIGNER': '设计'
  }
  return textMap[role] || role
}

// 获取项目状态颜色
const getProjectStatusColor = (status) => {
  const colorMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'PAUSED': 'warning',
    'CANCELLED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取项目状态文本
const getProjectStatusText = (status) => {
  const textMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 显示权限使用指南
const showPermissionGuide = () => {
  permissionGuideVisible.value = true
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-team-enhanced {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.project-info {
  margin-bottom: 20px;
}

.permission-overview {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.project-roles {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.permissions-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.no-permissions {
  color: #999;
  font-style: italic;
}

.expiry-date {
  color: #f56c6c;
  font-size: 12px;
}

.permanent {
  color: #67c23a;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>