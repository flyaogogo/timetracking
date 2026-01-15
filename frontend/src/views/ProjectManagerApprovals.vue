<template>
  <div class="project-manager-approvals">
    <div class="page-header">
      <h2>工时审批</h2>
      <p>审批您管理项目的工时记录</p>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-select
          v-model="projectFilter"
          placeholder="选择项目"
          style="width: 200px"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="project in managedProjects"
            :key="project.id"
            :label="project.projectName"
            :value="project.id"
          />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
          @change="handleSearch"
        />
        
        <el-button type="primary" @click="batchApprove" :disabled="selectedIds.length === 0">
          批量审批 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table 
        :data="timeEntries" 
        v-loading="loading" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="员工" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <strong>{{ row.userRealName || row.userName || '-' }}</strong>
              <div class="user-role">{{ row.userRole || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectName" label="项目" width="150" />
        
        <el-table-column prop="taskName" label="任务" width="150" />
        
        <el-table-column prop="workDate" label="工作日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.workDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="duration" label="工时" width="120">
          <template #default="{ row }">
            <span>
              {{ row.duration }}h / {{ Math.ceil(row.duration / 8) }}天
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="workType" label="工作类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getWorkTypeText(row.workType) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="工作描述" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button type="success" @click="approveEntry(row.id)">
                <el-icon><Check /></el-icon>
                审批
              </el-button>
              <el-button type="danger" @click="rejectEntry(row.id)">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </el-button-group>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'

const timeEntries = ref([])
const managedProjects = ref([])
const loading = ref(false)
const projectFilter = ref('')
const dateRange = ref([])
const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    }
  } catch (error) {
    console.error('获取管理项目失败:', error)
  }
}

// 获取待审批工时列表
const getPendingApprovals = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    if (projectFilter.value) {
      params.projectId = projectFilter.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    
    const response = await request({
      url: '/project-manager/pending-approvals',
      method: 'get',
      params
    })
    
    if (response.code === 200) {
      timeEntries.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取待审批工时失败')
    }
  } catch (error) {
    ElMessage.error('获取待审批工时失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  getPendingApprovals()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getPendingApprovals()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  getPendingApprovals()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 获取工作类型文本
const getWorkTypeText = (workType) => {
  const typeMap = {
    'DEVELOPMENT': '开发',
    'TESTING': '测试',
    'DESIGN': '设计',
    'MEETING': '会议',
    'RESEARCH': '研究',
    'OTHER': '其他'
  }
  return typeMap[workType] || workType
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 审批工时记录
const approveEntry = async (entryId) => {
  try {
    const response = await request({
      url: `/time-entries/${entryId}/approve`,
      method: 'post',
      data: { comment: '审批通过' }
    })
    
    if (response.code === 200) {
      ElMessage.success('审批成功')
      getPendingApprovals()
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    ElMessage.error('审批失败')
  }
}

// 拒绝工时记录
const rejectEntry = async (entryId) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝工时记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    })
    
    const response = await request({
      url: `/time-entries/${entryId}/reject`,
      method: 'post',
      data: { comment: reason }
    })
    
    if (response.code === 200) {
      ElMessage.success('拒绝成功')
      getPendingApprovals()
    } else {
      ElMessage.error(response.message || '拒绝失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝失败')
    }
  }
}

// 批量审批
const batchApprove = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审批的工时记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量审批选中的 ${selectedIds.value.length} 条工时记录吗？`, '批量审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request({
      url: '/project-manager/batch-approve',
      method: 'post',
      data: {
        ids: selectedIds.value,
        comment: '批量审批通过'
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('批量审批成功')
      selectedIds.value = []
      getPendingApprovals()
    } else {
      ElMessage.error(response.message || '批量审批失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审批失败')
    }
  }
}

onMounted(() => {
  getManagedProjects()
  getPendingApprovals()
})
</script>

<style scoped>
.project-manager-approvals {
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
  align-items: center;
}

.user-info {
  line-height: 1.4;
}

.user-role {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.days-hint {
  font-size: 12px;
  color: #67C23A;
  margin-top: 2px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>