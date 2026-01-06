<template>
  <div class="cost-report">
    <div class="report-header">
      <h3>成本分析报表</h3>
      <div class="report-actions">
        <el-button type="primary" size="small" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 成本统计概览 -->
    <el-row :gutter="20" class="cost-stats">
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">¥{{ reportData.totalCost || 0 }}</div>
          <div class="stat-label">总成本</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">¥{{ reportData.laborCost || 0 }}</div>
          <div class="stat-label">人力成本</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">¥{{ reportData.materialCost || 0 }}</div>
          <div class="stat-label">材料成本</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-item">
          <div class="stat-value">{{ reportData.budgetUsage || 0 }}%</div>
          <div class="stat-label">预算使用率</div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 成本明细 -->
    <el-table :data="costList" style="width: 100%; margin-top: 20px">
      <el-table-column prop="costType" label="成本类型" width="120" />
      <el-table-column prop="description" label="描述" min-width="150" />
      <el-table-column prop="budgetAmount" label="预算金额" width="120">
        <template #default="{ row }">
          ¥{{ row.budgetAmount || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="actualAmount" label="实际金额" width="120">
        <template #default="{ row }">
          ¥{{ row.actualAmount || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="variance" label="差异" width="100">
        <template #default="{ row }">
          <span :class="getVarianceClass(row.variance)">
            {{ row.variance > 0 ? '+' : '' }}¥{{ row.variance || 0 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="usageRate" label="使用率" width="120">
        <template #default="{ row }">
          <el-progress 
            :percentage="row.usageRate || 0" 
            :stroke-width="6"
            :color="getUsageColor(row.usageRate)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
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

const costList = ref([])

// 模拟成本数据
const loadCostData = () => {
  costList.value = [
    {
      costType: '人力成本',
      description: '开发团队工资',
      budgetAmount: 100000,
      actualAmount: 95000,
      variance: -5000,
      usageRate: 95,
      status: 'NORMAL'
    },
    {
      costType: '设备成本',
      description: '开发设备采购',
      budgetAmount: 20000,
      actualAmount: 22000,
      variance: 2000,
      usageRate: 110,
      status: 'OVER_BUDGET'
    },
    {
      costType: '软件许可',
      description: '开发工具许可费',
      budgetAmount: 15000,
      actualAmount: 12000,
      variance: -3000,
      usageRate: 80,
      status: 'UNDER_BUDGET'
    }
  ]
}

// 获取差异类名
const getVarianceClass = (variance) => {
  if (variance > 0) return 'variance-over'
  if (variance < 0) return 'variance-under'
  return 'variance-normal'
}

// 获取使用率颜色
const getUsageColor = (rate) => {
  if (rate > 100) return '#F56C6C'
  if (rate > 80) return '#E6A23C'
  return '#67C23A'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'NORMAL': 'success',
    'OVER_BUDGET': 'danger',
    'UNDER_BUDGET': 'warning'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'OVER_BUDGET': '超预算',
    'UNDER_BUDGET': '低于预算'
  }
  return textMap[status] || status
}

// 导出报表
const exportReport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadCostData()
})
</script>

<style scoped>
.cost-report {
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

.cost-stats {
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
  color: #E6A23C;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.variance-over {
  color: #F56C6C;
  font-weight: bold;
}

.variance-under {
  color: #67C23A;
  font-weight: bold;
}

.variance-normal {
  color: #909399;
}
</style>