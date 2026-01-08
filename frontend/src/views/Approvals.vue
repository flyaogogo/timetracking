<template>
  <div class="approvals">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px; margin-right: 10px"
            @change="loadPendingApprovals"
          />
          
          <el-input
            v-model="searchKeyword"
            placeholder="搜索员工姓名"
            style="width: 200px"
            @keyup.enter="loadPendingApprovals"
          >
            <template #append>
              <el-button @click="loadPendingApprovals">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button 
            type="success" 
            :disabled="selectedApprovals.length === 0"
            @click="batchApprove(true)"
          >
            <el-icon><Check /></el-icon>
            批量通过
          </el-button>
          <el-button 
            type="danger" 
            :disabled="selectedApprovals.length === 0"
            @click="batchApprove(false)"
          >
            <el-icon><Close /></el-icon>
            批量拒绝
          </el-button>
        </div>
      </div>
      
      <!-- 待审核工时列表 -->
      <el-table
        v-loading="loading"
        :data="pendingApprovals"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="workDate" label="工作日期" width="110" />
        
        <el-table-column label="员工" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <strong>{{ row.userRealName || row.userName || '-' }}</strong>
              <div class="user-role" v-if="row.userRole">{{ row.userRole }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectName" label="项目" width="120" />
        
        <el-table-column prop="taskName" label="任务" min-width="150">
          <template #default="{ row }">
            <span v-if="row.taskName">{{ row.taskName }}</span>
            <span v-else class="text-muted">无关联任务</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="managerName" label="项目经理" width="120">
          <template #default="{ row }">
            <span v-if="row.managerName">{{ row.managerName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        
        <el-table-column prop="duration" label="工时" width="80">
          <template #default="{ row }">
            <span class="duration">{{ row.duration }}h</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="workType" label="工作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getWorkTypeColor(row.workType)" size="small">
              {{ getWorkTypeText(row.workType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="工作内容" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button type="primary" size="small" text>
                审核
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">
                    <el-icon><View /></el-icon>
                    详情
                  </el-dropdown-item>
                  <el-dropdown-item command="approve" divided>
                    <el-icon><Check /></el-icon>
                    通过
                  </el-dropdown-item>
                  <el-dropdown-item command="reject">
                    <el-icon><Close /></el-icon>
                    拒绝
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
          @size-change="loadPendingApprovals"
          @current-change="loadPendingApprovals"
        />
      </div>
    </el-card>
    
    <!-- 工时详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工时详情"
      width="500px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工作日期">{{ currentDetail.workDate }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ currentDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ currentDetail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="项目经理">{{ currentDetail.managerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务">{{ currentDetail.taskName || '无' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="工时时长">{{ currentDetail.duration }}小时</el-descriptions-item>
        <el-descriptions-item label="工作类型">
          <el-tag :type="getWorkTypeColor(currentDetail.workType)" size="small">
            {{ getWorkTypeText(currentDetail.workType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">
          {{ formatDateTime(currentDetail.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="工作内容" :span="2">
          {{ currentDetail.description }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="approveFromDetail(true)">
          通过
        </el-button>
        <el-button type="danger" @click="showRejectDialogFromDetail">
          拒绝
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝工时"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定拒绝</el-button>
      </template>
    </el-dialog>
    
    <!-- 批量拒绝对话框 -->
    <el-dialog
      v-model="batchRejectDialogVisible"
      title="批量拒绝工时"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="batchRejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="batchRejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchReject">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { 
  getPendingApprovals, 
  approveTimeEntry as approveTimeEntryApi,
  getTimeEntryById,
  getPendingApprovalsByManager
} from '@/api/timeEntry'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const batchRejectDialogVisible = ref(false)
const dateRange = ref([])
const searchKeyword = ref('')
const pendingApprovals = ref([])
const selectedApprovals = ref([])
const currentDetail = ref({})
const currentApprovalId = ref(null)
const rejectReason = ref('')
const batchRejectReason = ref('')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 加载待审核工时列表
const loadPendingApprovals = async () => {
  loading.value = true
  try {
    // 检查是否需要只显示用户作为项目经理的待审核工时
    const managerOnly = route.query.managerOnly === 'true'
    
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    
    // 如果选择了日期范围，添加到参数中
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
      params.endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
    }
    
    // 如果有搜索关键词
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    let response
    if (managerOnly && userStore.user?.id) {
      // 调用项目经理待审核工时的API
      response = await getPendingApprovalsByManager(userStore.user.id, params)
    } else {
      // 调用普通待审核工时列表API
      response = await getPendingApprovals(params)
    }
    
    if (response.code === 200) {
      pendingApprovals.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载待审核工时失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const response = await getTimeEntryById(row.id)
    if (response.code === 200) {
      currentDetail.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取工时详情失败')
  }
}

// 审核工时
const approveTimeEntry = async (row, approved) => {
  if (!approved) {
    showRejectDialog(row)
    return
  }
  
  try {
    const response = await approveTimeEntryApi(row.id, {
      approverId: userStore.user?.id,
      approved: approved,
      comment: approved ? '审核通过' : rejectReason.value
    })
    
    if (response.code === 200) {
      ElMessage.success(approved ? '审核通过' : '审核拒绝')
      loadPendingApprovals()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

// 从详情页面审核
const approveFromDetail = (approved) => {
  if (approved) {
    approveTimeEntry(currentDetail.value, true)
    detailDialogVisible.value = false
  } else {
    showRejectDialogFromDetail()
  }
}

// 显示拒绝对话框
const showRejectDialog = (row) => {
  currentApprovalId.value = row.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 从详情页面显示拒绝对话框
const showRejectDialogFromDetail = () => {
  currentApprovalId.value = currentDetail.value.id
  rejectReason.value = ''
  detailDialogVisible.value = false
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    const response = await approveTimeEntryApi(currentApprovalId.value, {
      approverId: userStore.user?.id,
      approved: false,
      comment: rejectReason.value
    })
    
    if (response.code === 200) {
      ElMessage.success('审核拒绝')
      rejectDialogVisible.value = false
      loadPendingApprovals()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

// 批量审核
const batchApprove = async (approved) => {
  if (selectedApprovals.value.length === 0) {
    ElMessage.warning('请选择要审核的工时记录')
    return
  }
  
  if (!approved) {
    batchRejectReason.value = ''
    batchRejectDialogVisible.value = true
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量通过 ${selectedApprovals.value.length} 条工时记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量审核通过
    const promises = selectedApprovals.value.map(item => 
      approveTimeEntryApi(item.id, {
        approverId: userStore.user?.id,
        approved: true,
        comment: '批量审核通过'
      })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量审核通过成功')
    loadPendingApprovals()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核失败')
    }
  }
}

// 确认批量拒绝
const confirmBatchReject = async () => {
  if (!batchRejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    // 批量审核拒绝
    const promises = selectedApprovals.value.map(item => 
      approveTimeEntryApi(item.id, {
        approverId: userStore.user?.id,
        approved: false,
        comment: batchRejectReason.value
      })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量审核拒绝成功')
    batchRejectDialogVisible.value = false
    loadPendingApprovals()
    
  } catch (error) {
    ElMessage.error('批量审核失败')
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedApprovals.value = selection
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'view':
      viewDetail(row)
      break
    case 'approve':
      approveTimeEntry(row, true)
      break
    case 'reject':
      showRejectDialog(row)
      break
  }
}

// 获取工作类型颜色
const getWorkTypeColor = (type) => {
  const colorMap = {
    'NORMAL': 'primary',
    'OVERTIME': 'warning',
    'HOLIDAY': 'danger'
  }
  return colorMap[type] || 'info'
}

// 获取工作类型文本
const getWorkTypeText = (type) => {
  const textMap = {
    'NORMAL': '正常',
    'OVERTIME': '加班',
    'HOLIDAY': '节假日'
  }
  return textMap[type] || type
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  loadPendingApprovals()
})
</script>

<style scoped>
.approvals {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.duration {
  font-weight: bold;
  color: #409EFF;
}

.text-muted {
  color: #999;
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
</style>