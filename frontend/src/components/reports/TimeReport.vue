<template>
  <div class="time-report">
    <div class="report-header">
      <h3>工时统计报表</h3>
      <div class="report-actions">
        <el-button type="primary" size="small" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 工时统计概览 -->
    <el-row :gutter="20" class="time-stats">
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.totalHours || 0 }}h</div>
          <div class="stat-label">总工时</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.billableHours || 0 }}h</div>
          <div class="stat-label">计费工时</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.avgHoursPerDay || 0 }}h</div>
          <div class="stat-label">日均工时</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.efficiency || 0 }}%</div>
          <div class="stat-label">工时效率</div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 团队成员工时统计 -->
    <el-table :data="timeList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="userRealName" label="成员姓名" width="120" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="totalHours" label="总工时" width="100">
        <template #default="{ row }">
          {{ row.totalHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="billableHours" label="计费工时" width="100">
        <template #default="{ row }">
          {{ row.billableHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="approvedHours" label="已审核工时" width="120">
        <template #default="{ row }">
          {{ row.approvedHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="pendingHours" label="待审核工时" width="120">
        <template #default="{ row }">
          {{ row.pendingHours || 0 }}h
        </template>
      </el-table-column>
      <el-table-column prop="efficiency" label="效率" width="120">
        <template #default="{ row }">
          <el-progress :percentage="row.efficiency || 0" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column prop="avgHoursPerTask" label="平均工时/任务" width="130">
        <template #default="{ row }">
          {{ row.avgHoursPerTask || 0 }}h
        </template>
      </el-table-column>
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

const timeList = ref([])

// 模拟工时数据
const loadTimeData = () => {
  timeList.value = [
    {
      userRealName: '张三',
      role: '开发工程师',
      totalHours: 160,
      billableHours: 150,
      approvedHours: 140,
      pendingHours: 10,
      efficiency: 92,
      avgHoursPerTask: 13.3
    },
    {
      userRealName: '李四',
      role: '测试工程师',
      totalHours: 140,
      billableHours: 135,
      approvedHours: 130,
      pendingHours: 5,
      efficiency: 88,
      avgHoursPerTask: 7.8
    }
  ]
}

// 导出报表
const exportReport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadTimeData()
})
</script>

<style scoped>
.time-report {
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

.time-stats {
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
  color: #67C23A;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>