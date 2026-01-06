<template>
  <div class="task-report">
    <div class="report-header">
      <h3>任务完成情况报表</h3>
      <div class="report-actions">
        <el-button type="primary" size="small" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 任务统计概览 -->
    <el-row :gutter="20" class="task-stats">
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.totalTasks || 0 }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.completedTasks || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.inProgressTasks || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.completionRate || 0 }}%</div>
          <div class="stat-label">完成率</div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 任务详细列表 -->
    <el-table :data="taskList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="taskName" label="任务名称" min-width="150" />
      <el-table-column prop="assigneeName" label="执行人" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="120">
        <template #default="{ row }">
          <el-progress :percentage="row.progress || 0" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column prop="estimatedHours" label="预估工时" width="100">
        <template #default="{ row }">
          {{ row.estimatedHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="actualHours" label="实际工时" width="100">
        <template #default="{ row }">
          {{ row.actualHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="110" />
      <el-table-column prop="endDate" label="结束日期" width="110" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const props = defineProps({
  projectId: {
    type: Number,
    required: true
  },
  reportData: {
    type: Object,
    default: () => ({})
  }
})

const taskList = ref([])

// 模拟任务数据
const loadTaskData = () => {
  taskList.value = [
    {
      taskName: '用户登录功能开发',
      assigneeName: '张三',
      status: 'COMPLETED',
      progress: 100,
      estimatedHours: 16,
      actualHours: 18,
      startDate: '2024-01-01',
      endDate: '2024-01-03'
    },
    {
      taskName: '数据库设计',
      assigneeName: '李四',
      status: 'IN_PROGRESS',
      progress: 75,
      estimatedHours: 24,
      actualHours: 20,
      startDate: '2024-01-02',
      endDate: '2024-01-05'
    }
  ]
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'TODO': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'PAUSED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'TODO': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停'
  }
  return textMap[status] || status
}

// 导出报表
const exportReport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadTaskData()
})
</script>

<style scoped>
.task-report {
  padding: 20px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.report-header h3 {
  margin: 0;
  color: #303133;
}

.task-stats {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>