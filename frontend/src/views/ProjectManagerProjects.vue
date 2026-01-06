<template>
  <div class="project-manager-projects">
    <div class="page-header">
      <h2>管理的项目</h2>
      <p>查看和管理您负责的所有项目</p>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称或编码"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="statusFilter"
          placeholder="项目状态"
          style="width: 150px"
          clearable
          @change="handleSearch"
        >
          <el-option label="规划中" value="PLANNING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="projects" v-loading="loading" style="width: 100%">
        <el-table-column prop="projectName" label="项目名称" min-width="200">
          <template #default="{ row }">
            <div class="project-name-cell">
              <strong>{{ row.projectName }}</strong>
              <div class="project-code">{{ row.projectCode }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress 
              :percentage="calculateProgress(row)" 
              :stroke-width="8"
              :show-text="true"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="endDate" label="结束日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewProject(row.id)">
              查看详情
            </el-button>
            <el-button type="info" size="small" @click="manageTeam(row.id)">
              团队管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const projects = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取管理的项目列表
const getProjects = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/project-manager/projects',
      method: 'get',
      params: {
        current: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        status: statusFilter.value
      }
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取项目列表失败')
    }
  } catch (error) {
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  getProjects()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getProjects()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  getProjects()
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 计算项目进度
const calculateProgress = (project) => {
  if (project.actualHours && project.plannedHours) {
    return Math.min(100, Math.round((project.actualHours / project.plannedHours) * 100))
  }
  return 0
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 查看项目详情
const viewProject = (projectId) => {
  router.push(`/projects/${projectId}`)
}

// 团队管理
const manageTeam = (projectId) => {
  router.push(`/project-permissions?projectId=${projectId}`)
}

onMounted(() => {
  getProjects()
})
</script>

<style scoped>
.project-manager-projects {
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

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.project-name-cell {
  line-height: 1.4;
}

.project-code {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>