<template>
  <div class="project-member-debug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目成员显示调试</span>
          <el-button type="primary" @click="debugLoad">重新加载并调试</el-button>
        </div>
      </template>

      <!-- 调试信息 -->
      <div class="debug-info">
        <h3>调试信息</h3>
        <p>选择的项目ID: {{ selectedProjectId }}</p>
        <p>加载状态: {{ loading ? '加载中' : '已完成' }}</p>
        <p>成员数量: {{ projectMembers.length }}</p>
        <p>API响应状态: {{ apiResponse.code }}</p>
        <p>API响应消息: {{ apiResponse.message }}</p>
      </div>

      <!-- 项目选择 -->
      <div class="project-selector">
        <el-select
          v-model="selectedProjectId"
          placeholder="选择项目"
          style="width: 300px"
          @change="loadProjectMembers"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.projectName"
            :value="project.id"
          />
        </el-select>
      </div>

      <!-- 原始API响应数据 -->
      <div class="raw-data" v-if="rawApiData">
        <h3>原始API响应数据</h3>
        <pre>{{ JSON.stringify(rawApiData, null, 2) }}</pre>
      </div>

      <!-- 成员列表调试表格 -->
      <el-table
        v-if="selectedProjectId"
        v-loading="loading"
        :data="projectMembers"
        style="width: 100%; margin-top: 20px"
        empty-text="没有成员数据"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        
        <!-- 调试：显示原始字段值 -->
        <el-table-column label="userRealName (原始)" width="150">
          <template #default="{ row }">
            <span style="color: red">{{ row.userRealName || '空值' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="userRole (原始)" width="150">
          <template #default="{ row }">
            <span style="color: red">{{ row.userRole || '空值' }}</span>
          </template>
        </el-table-column>
        
        <!-- 正常显示 -->
        <el-table-column prop="userRealName" label="成员姓名" width="120" />
        
        <el-table-column prop="userRole" label="全局角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getGlobalRoleColor(row.userRole)" size="small">
              {{ getGlobalRoleText(row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="role" label="项目角色" width="100" />
        <el-table-column prop="joinDate" label="加入日期" width="120" />
        
        <!-- 调试：显示完整对象 -->
        <el-table-column label="完整对象" min-width="300">
          <template #default="{ row }">
            <pre style="font-size: 10px">{{ JSON.stringify(row, null, 1) }}</pre>
          </template>
        </el-table-column>
      </el-table>

      <!-- 字段检查结果 -->
      <div class="field-check" v-if="projectMembers.length > 0">
        <h3>字段检查结果</h3>
        <div v-for="(member, index) in projectMembers" :key="member.id" class="member-check">
          <h4>成员 {{ index + 1 }} (ID: {{ member.id }})</h4>
          <ul>
            <li>userRealName: {{ member.userRealName ? '✅ ' + member.userRealName : '❌ 空值' }}</li>
            <li>userRole: {{ member.userRole ? '✅ ' + member.userRole : '❌ 空值' }}</li>
            <li>userId: {{ member.userId ? '✅ ' + member.userId : '❌ 空值' }}</li>
            <li>role: {{ member.role ? '✅ ' + member.role : '❌ 空值' }}</li>
            <li>所有字段: {{ Object.keys(member).join(', ') }}</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProjectMembers } from '@/api/projectMember'
import { getProjectList } from '@/api/project'

const loading = ref(false)
const selectedProjectId = ref(null)
const projects = ref([])
const projectMembers = ref([])
const rawApiData = ref(null)
const apiResponse = reactive({
  code: null,
  message: ''
})

// 角色映射函数
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

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList()
    if (response.code === 200) {
      projects.value = response.data || []
      console.log('项目列表加载成功:', projects.value)
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 加载项目成员
const loadProjectMembers = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  console.log('开始加载项目成员，项目ID:', selectedProjectId.value)
  
  try {
    const response = await getProjectMembers(selectedProjectId.value)
    console.log('API响应:', response)
    
    // 保存原始响应数据
    rawApiData.value = response
    apiResponse.code = response.code
    apiResponse.message = response.message
    
    if (response.code === 200) {
      projectMembers.value = response.data || []
      console.log('项目成员加载成功:', projectMembers.value)
      
      // 详细检查每个成员的字段
      projectMembers.value.forEach((member, index) => {
        console.log(`成员 ${index + 1}:`, {
          id: member.id,
          userId: member.userId,
          userRealName: member.userRealName,
          userRole: member.userRole,
          role: member.role,
          allFields: Object.keys(member)
        })
      })
    } else {
      console.error('API返回错误:', response.message)
    }
  } catch (error) {
    console.error('加载项目成员失败:', error)
    apiResponse.code = 'ERROR'
    apiResponse.message = error.message
  } finally {
    loading.value = false
  }
}

// 调试重新加载
const debugLoad = () => {
  console.log('=== 开始调试加载 ===')
  console.log('当前选择的项目ID:', selectedProjectId.value)
  loadProjectMembers()
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-member-debug {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.debug-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.debug-info h3 {
  margin-top: 0;
  color: #409eff;
}

.project-selector {
  margin-bottom: 20px;
}

.raw-data {
  background: #f8f8f8;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.raw-data h3 {
  margin-top: 0;
  color: #e6a23c;
}

.raw-data pre {
  font-size: 12px;
  line-height: 1.4;
}

.field-check {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 5px;
  margin-top: 20px;
}

.field-check h3 {
  margin-top: 0;
  color: #67c23a;
}

.member-check {
  border: 1px solid #ddd;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 3px;
}

.member-check h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.member-check ul {
  margin: 0;
  padding-left: 20px;
}

.member-check li {
  margin-bottom: 5px;
}
</style>