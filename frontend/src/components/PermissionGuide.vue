<template>
  <div class="permission-guide">
    <el-card>
      <template #header>
        <div class="guide-header">
          <span>权限使用指南</span>
          <el-button type="text" @click="$emit('close')">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>
      
      <div class="guide-content">
        <el-alert
          title="权限说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>以下是您在当前项目中拥有的权限及其使用方法：</p>
          </template>
        </el-alert>
        
        <div class="permission-sections">
          <!-- 项目经理权限 -->
          <div v-if="permissions.isProjectManager" class="permission-section">
            <h3>
              <el-icon><UserFilled /></el-icon>
              项目经理权限
            </h3>
            <div class="permission-items">
              <div class="permission-item">
                <strong>项目管理：</strong>
                <span>可以在"项目管理 > 项目列表"中管理项目基本信息</span>
                <el-button type="primary" size="small" @click="navigateTo('/projects')">
                  前往使用
                </el-button>
              </div>
              <div class="permission-item">
                <strong>团队管理：</strong>
                <span>可以在"项目管理 > 权限管理"中管理团队成员和权限</span>
                <el-button type="primary" size="small" @click="navigateTo('/project-permissions')">
                  前往使用
                </el-button>
              </div>
              <div class="permission-item">
                <strong>经理工作台：</strong>
                <span>可以访问专门的项目经理工作台查看项目概览</span>
                <el-button type="primary" size="small" @click="navigateTo('/project-manager')">
                  前往使用
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 技术负责人权限 -->
          <div v-if="permissions.isTechLeader" class="permission-section">
            <h3>
              <el-icon><Tools /></el-icon>
              技术负责人权限
            </h3>
            <div class="permission-items">
              <div class="permission-item">
                <strong>里程碑管理：</strong>
                <span>可以在"项目管理 > 里程碑"中管理项目里程碑</span>
                <el-button type="warning" size="small" @click="navigateTo('/project-milestones')">
                  前往使用
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 具体权限 -->
          <div v-if="hasSpecificPermissions" class="permission-section">
            <h3>
              <el-icon><Key /></el-icon>
              具体权限
            </h3>
            <div class="permission-items">
              <div v-if="permissions.canApproveTimesheet" class="permission-item">
                <strong>工时审核：</strong>
                <span>可以在"工时审核"页面审核团队成员的工时记录</span>
                <el-button type="success" size="small" @click="navigateTo('/approvals')">
                  前往使用
                </el-button>
              </div>
              <div v-if="permissions.canManageTasks" class="permission-item">
                <strong>任务管理：</strong>
                <span>可以在"任务管理"页面创建、编辑和分配任务</span>
                <el-button type="primary" size="small" @click="navigateTo('/tasks')">
                  前往使用
                </el-button>
              </div>
              <div v-if="permissions.canViewReports" class="permission-item">
                <strong>报表查看：</strong>
                <span>可以在"项目管理 > 报表查看"中查看项目统计报表</span>
                <el-button type="info" size="small" @click="navigateTo('/project-reports')">
                  前往使用
                </el-button>
              </div>
              <div v-if="permissions.canManageCosts" class="permission-item">
                <strong>成本管理：</strong>
                <span>可以在"项目管理 > 成本管理"中管理项目成本</span>
                <el-button type="warning" size="small" @click="navigateTo('/project-costs')">
                  前往使用
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 无权限提示 -->
          <div v-if="!hasAnyPermissions" class="no-permissions-section">
            <el-empty description="您在当前项目中暂无特殊权限">
              <template #description>
                <p>您在当前项目中暂无特殊权限</p>
                <p>如需获取权限，请联系项目管理员</p>
              </template>
            </el-empty>
          </div>
        </div>
        
        <!-- 权限申请 -->
        <div class="permission-request">
          <el-divider>需要更多权限？</el-divider>
          <p>如果您需要其他权限来完成工作，可以：</p>
          <ul>
            <li>联系项目经理或系统管理员</li>
            <li>在"项目管理 > 权限管理"中查看当前权限设置</li>
            <li>通过内部流程申请相应权限</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Close, UserFilled, Tools, Key } from '@element-plus/icons-vue'

const props = defineProps({
  permissions: {
    type: Object,
    default: () => ({})
  },
  projectId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close'])
const router = useRouter()

// 检查是否有具体权限
const hasSpecificPermissions = computed(() => {
  return props.permissions.canApproveTimesheet || 
         props.permissions.canManageTasks || 
         props.permissions.canViewReports ||
         props.permissions.canManageCosts
})

// 检查是否有任何权限
const hasAnyPermissions = computed(() => {
  return props.permissions.isProjectManager || 
         props.permissions.isTechLeader || 
         hasSpecificPermissions.value
})

// 导航到指定页面
const navigateTo = (path) => {
  const query = props.projectId ? { projectId: props.projectId } : {}
  router.push({ path, query })
  emit('close')
}
</script>

<style scoped>
.permission-guide {
  max-width: 800px;
  margin: 0 auto;
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.guide-content {
  max-height: 600px;
  overflow-y: auto;
}

.permission-sections {
  margin-top: 20px;
}

.permission-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.permission-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.permission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.permission-item strong {
  color: #303133;
  margin-right: 8px;
  min-width: 80px;
}

.permission-item span {
  flex: 1;
  color: #606266;
  margin-right: 12px;
}

.no-permissions-section {
  text-align: center;
  padding: 40px 20px;
}

.permission-request {
  margin-top: 30px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #b3d8ff;
}

.permission-request p {
  margin: 8px 0;
  color: #303133;
}

.permission-request ul {
  margin: 12px 0;
  padding-left: 20px;
}

.permission-request li {
  margin: 6px 0;
  color: #606266;
}
</style>