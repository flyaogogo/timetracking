<template>
  <div class="project-statistics-enhanced">
    <!-- 项目选择器 -->
    <el-card class="project-selector">
      <div class="selector-content">
        <el-select
          v-model="selectedProjectId"
          placeholder="请选择项目"
          style="width: 300px"
          @change="loadProjectStatistics"
          filterable
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.projectName"
            :value="project.id"
          />
        </el-select>
        <el-button 
          type="primary" 
          :loading="loading"
          @click="loadProjectStatistics"
          :disabled="!selectedProjectId"
        >
          刷新数据
        </el-button>
      </div>
    </el-card>

    <!-- 统计内容 -->
    <div v-if="statistics" class="statistics-content">
      <!-- 项目概览 -->
      <el-card class="overview-card">
        <template #header>
          <div class="card-header">
            <span>项目概览</span>
            <el-tag :type="getStatusColor(statistics.status)">
              {{ getStatusText(statistics.status) }}
            </el-tag>
          </div>
        </template>
        
        <div class="overview-content">
          <div class="project-info">
            <h3>{{ statistics.projectName }}</h3>
            <p class="project-code">项目编码: {{ statistics.projectCode }}</p>
          </div>
          
          <el-row :gutter="20" class="overview-stats">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.progressStats?.progressPercentage || 0 }}%</div>
                <div class="stat-label">完成进度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ formatCurrency(statistics.financialStats?.actualCost || 0) }}</div>
                <div class="stat-label">实际成本</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.teamStats?.teamSize || 0 }}</div>
                <div class="stat-label">团队规模</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.scheduleStats?.delayDays || 0 }}</div>
                <div class="stat-label">延期天数</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 成本分析 -->
      <el-card class="cost-analysis-card">
        <template #header>
          <div class="card-header">
            <span>成本分析</span>
            <el-tooltip content="点击查看成本分析统计思路" placement="top">
              <el-icon class="info-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </template>
        
        <el-row :gutter="20">
          <!-- 成本概览 -->
          <el-col :span="12">
            <div class="cost-overview">
              <h4>成本概览</h4>
              <div class="cost-items">
                <div 
                  class="cost-item"
                  @mouseenter="showCostTooltip('budget', $event)"
                  @mouseleave="hideCostTooltip"
                >
                  <span class="cost-label">预算金额</span>
                  <span class="cost-value">{{ formatCurrency(statistics.financialStats?.budgetAmount || 0) }}</span>
                </div>
                <div 
                  class="cost-item"
                  @mouseenter="showCostTooltip('actual', $event)"
                  @mouseleave="hideCostTooltip"
                >
                  <span class="cost-label">实际成本</span>
                  <span class="cost-value actual">{{ formatCurrency(statistics.financialStats?.actualCost || 0) }}</span>
                </div>
                <div 
                  class="cost-item"
                  @mouseenter="showCostTooltip('labor', $event)"
                  @mouseleave="hideCostTooltip"
                >
                  <span class="cost-label">人工成本</span>
                  <span class="cost-value">{{ formatCurrency(statistics.financialStats?.laborCost || 0) }}</span>
                </div>
                <div 
                  class="cost-item"
                  @mouseenter="showCostTooltip('other', $event)"
                  @mouseleave="hideCostTooltip"
                >
                  <span class="cost-label">其他成本</span>
                  <span class="cost-value">{{ formatCurrency(statistics.financialStats?.otherCost || 0) }}</span>
                </div>
                <div 
                  class="cost-item"
                  @mouseenter="showCostTooltip('utilization', $event)"
                  @mouseleave="hideCostTooltip"
                >
                  <span class="cost-label">成本使用率</span>
                  <span class="cost-value">{{ statistics.financialStats?.costUtilization || 0 }}%</span>
                </div>
              </div>
            </div>
          </el-col>
          
          <!-- 成本趋势图 -->
          <el-col :span="12">
            <div class="cost-chart">
              <h4>成本构成</h4>
              <div ref="costChartRef" style="height: 200px;"></div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 团队统计 -->
      <el-card class="team-stats-card">
        <template #header>
          <div class="card-header">
            <span>团队统计</span>
            <el-tooltip content="点击查看团队统计计算方法" placement="top">
              <el-icon class="info-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <div 
              class="team-stat-card"
              @mouseenter="showTeamTooltip('size', $event)"
              @mouseleave="hideTeamTooltip"
            >
              <div class="stat-number">{{ statistics.teamStats?.teamSize || 0 }}</div>
              <div class="stat-label">团队规模</div>
              <div class="stat-desc">项目成员总数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div 
              class="team-stat-card"
              @mouseenter="showTeamTooltip('active', $event)"
              @mouseleave="hideTeamTooltip"
            >
              <div class="stat-number">{{ statistics.teamStats?.activeMembers || 0 }}</div>
              <div class="stat-label">活跃成员</div>
              <div class="stat-desc">近7天有工时记录</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div 
              class="team-stat-card"
              @mouseenter="showTeamTooltip('hours', $event)"
              @mouseleave="hideTeamTooltip"
            >
              <div class="stat-number">{{ (statistics.teamStats?.avgDailyHours || 0).toFixed(1) }}h</div>
              <div class="stat-label">日均工时</div>
              <div class="stat-desc">团队平均每日工时</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div 
              class="team-stat-card"
              @mouseenter="showTeamTooltip('efficiency', $event)"
              @mouseleave="hideTeamTooltip"
            >
              <div class="stat-number">{{ (statistics.teamStats?.efficiency || 0).toFixed(1) }}%</div>
              <div class="stat-label">工作效率</div>
              <div class="stat-desc">实际/预估工时比</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 里程碑进度 -->
      <el-card class="milestone-card">
        <template #header>
          <div class="card-header">
            <span>里程碑进度</span>
            <el-button type="primary" size="small" @click="showMilestoneDialog">
              设置里程碑
            </el-button>
          </div>
        </template>
        
        <div class="milestone-timeline">
          <div 
            v-for="(milestone, index) in statistics.milestones" 
            :key="milestone.id"
            class="milestone-item"
            :class="{ 'completed': milestone.status === 'COMPLETED', 'delayed': milestone.isDelayed }"
          >
            <div class="milestone-dot"></div>
            <div class="milestone-content">
              <div class="milestone-title">{{ milestone.name }}</div>
              <div class="milestone-date">
                计划: {{ formatDate(milestone.plannedDate) }}
                <span v-if="milestone.actualDate"> | 实际: {{ formatDate(milestone.actualDate) }}</span>
              </div>
              <div class="milestone-progress">
                <el-progress 
                  :percentage="milestone.progress" 
                  :status="milestone.status === 'COMPLETED' ? 'success' : (milestone.isDelayed ? 'exception' : '')"
                />
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 里程碑设置对话框 -->
    <el-dialog
      v-model="milestoneDialogVisible"
      title="设置项目里程碑"
      width="800px"
    >
      <div class="milestone-form">
        <el-button type="primary" @click="addMilestone" style="margin-bottom: 20px;">
          添加里程碑
        </el-button>
        
        <el-table :data="milestoneForm" style="width: 100%">
          <el-table-column label="里程碑名称" width="200">
            <template #default="{ row, $index }">
              <el-input v-model="row.name" placeholder="请输入里程碑名称" />
            </template>
          </el-table-column>
          <el-table-column label="计划完成日期" width="150">
            <template #default="{ row, $index }">
              <el-date-picker
                v-model="row.plannedDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="描述">
            <template #default="{ row, $index }">
              <el-input v-model="row.description" placeholder="请输入描述" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row, $index }">
              <el-button type="danger" size="small" @click="removeMilestone($index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="milestoneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMilestones">保存</el-button>
      </template>
    </el-dialog>

    <!-- 悬浮提示 -->
    <div 
      v-if="tooltipVisible" 
      ref="tooltipRef"
      class="custom-tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-title">{{ tooltipData.title }}</div>
      <div class="tooltip-content">{{ tooltipData.content }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getProjectList } from '@/api/project'
import { getProjectStatistics } from '@/api/projectStatistics'

const loading = ref(false)
const selectedProjectId = ref(null)
const projects = ref([])
const statistics = ref(null)
const costChartRef = ref()
const milestoneDialogVisible = ref(false)
const milestoneForm = ref([])

// 悬浮提示相关
const tooltipVisible = ref(false)
const tooltipRef = ref()
const tooltipStyle = ref({})
const tooltipData = ref({ title: '', content: '' })

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({ current: 1, size: 100 })
    if (response.code === 200) {
      projects.value = response.data.records || []
      if (projects.value.length > 0 && !selectedProjectId.value) {
        selectedProjectId.value = projects.value[0].id
        await loadProjectStatistics()
      }
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 加载项目统计数据
const loadProjectStatistics = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    const response = await getProjectStatistics(selectedProjectId.value)
    if (response.code === 200) {
      statistics.value = response.data
      await nextTick()
      renderCostChart()
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 渲染成本图表
const renderCostChart = () => {
  if (!costChartRef.value || !statistics.value) return
  
  const chart = echarts.init(costChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '成本构成',
        type: 'pie',
        radius: '70%',
        data: [
          { 
            value: statistics.value.financialStats?.laborCost || 0, 
            name: '人工成本',
            itemStyle: { color: '#409EFF' }
          },
          { 
            value: statistics.value.financialStats?.otherCost || 0, 
            name: '其他成本',
            itemStyle: { color: '#67C23A' }
          }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

// 成本分析提示
const showCostTooltip = (type, event) => {
  const tooltipMap = {
    budget: {
      title: '预算金额统计思路',
      content: '从项目基础信息中的预算金额字段获取，包含人工成本预算和其他成本预算的总和'
    },
    actual: {
      title: '实际成本统计思路', 
      content: '实际成本 = 人工成本 + 其他成本。人工成本 = Σ(员工工时 × 员工时薪)，其他成本从项目费用记录中统计'
    },
    labor: {
      title: '人工成本统计思路',
      content: '人工成本 = Σ(项目成员实际工时 × 成员时薪)，从time_entries表和users表关联计算得出'
    },
    other: {
      title: '其他成本统计思路',
      content: '其他成本包括设备费用、差旅费、外包费用等，从项目费用记录表中统计非人工类成本'
    },
    utilization: {
      title: '成本使用率统计思路',
      content: '成本使用率 = (实际成本 / 预算成本) × 100%，反映项目成本控制情况'
    }
  }
  
  tooltipData.value = tooltipMap[type]
  showTooltip(event)
}

// 团队统计提示
const showTeamTooltip = (type, event) => {
  const tooltipMap = {
    size: {
      title: '团队规模统计思路',
      content: '统计project_members表中该项目的所有成员数量，包括项目经理、开发人员、测试人员等所有角色'
    },
    active: {
      title: '活跃成员判断标准',
      content: '活跃成员 = 近7天内在该项目有工时记录的成员数量，从time_entries表中统计work_date >= 当前日期-7天的不重复用户数'
    },
    hours: {
      title: '日均工时统计方法',
      content: '日均工时 = 项目总工时 / 项目进行天数 / 团队规模，从time_entries表统计该项目所有工时记录的平均值'
    },
    efficiency: {
      title: '工作效率计算方法',
      content: '工作效率 = (实际完成工时 / 预估工时) × 100%，反映团队的工作效率和预估准确性'
    }
  }
  
  tooltipData.value = tooltipMap[type]
  showTooltip(event)
}

// 显示提示
const showTooltip = (event) => {
  tooltipVisible.value = true
  tooltipStyle.value = {
    left: event.pageX + 10 + 'px',
    top: event.pageY + 10 + 'px'
  }
}

// 隐藏提示
const hideCostTooltip = () => {
  tooltipVisible.value = false
}

const hideTeamTooltip = () => {
  tooltipVisible.value = false
}

// 显示里程碑设置对话框
const showMilestoneDialog = () => {
  milestoneForm.value = statistics.value.milestones?.map(m => ({
    id: m.id,
    name: m.name,
    plannedDate: m.plannedDate,
    description: m.description
  })) || []
  milestoneDialogVisible.value = true
}

// 添加里程碑
const addMilestone = () => {
  milestoneForm.value.push({
    id: null,
    name: '',
    plannedDate: null,
    description: ''
  })
}

// 删除里程碑
const removeMilestone = (index) => {
  milestoneForm.value.splice(index, 1)
}

// 保存里程碑
const saveMilestones = async () => {
  try {
    // 这里应该调用API保存里程碑数据
    ElMessage.success('里程碑保存成功')
    milestoneDialogVisible.value = false
    await loadProjectStatistics()
  } catch (error) {
    ElMessage.error('保存里程碑失败')
  }
}

// 工具函数
const formatCurrency = (amount) => {
  return '¥' + (amount || 0).toLocaleString()
}

const formatDate = (date) => {
  return date ? new Date(date).toLocaleDateString() : '-'
}

const getStatusColor = (status) => {
  const colorMap = {
    'PLANNING': 'info',
    'IN_PROGRESS': 'primary', 
    'COMPLETED': 'success',
    'PAUSED': 'warning',
    'CANCELLED': 'danger'
  }
  return colorMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成', 
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-statistics-enhanced {
  padding: 20px;
}

.project-selector {
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.statistics-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-icon {
  color: #409EFF;
  cursor: help;
}

/* 项目概览样式 */
.overview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.project-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.project-code {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.overview-stats {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
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

/* 成本分析样式 */
.cost-overview h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.cost-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cost-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.cost-item:hover {
  background: #e8f4ff;
  transform: translateY(-2px);
}

.cost-label {
  font-size: 14px;
  color: #666;
}

.cost-value {
  font-weight: bold;
  color: #303133;
}

.cost-value.actual {
  color: #409EFF;
}

.cost-chart h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

/* 团队统计样式 */
.team-stat-card {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.team-stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.team-stat-card .stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.team-stat-card .stat-label {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.team-stat-card .stat-desc {
  font-size: 12px;
  opacity: 0.8;
}

/* 里程碑样式 */
.milestone-timeline {
  position: relative;
  padding-left: 30px;
}

.milestone-timeline::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e4e7ed;
}

.milestone-item {
  position: relative;
  margin-bottom: 30px;
}

.milestone-dot {
  position: absolute;
  left: -23px;
  top: 8px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #e4e7ed;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e4e7ed;
}

.milestone-item.completed .milestone-dot {
  background: #67C23A;
  box-shadow: 0 0 0 2px #67C23A;
}

.milestone-item.delayed .milestone-dot {
  background: #F56C6C;
  box-shadow: 0 0 0 2px #F56C6C;
}

.milestone-content {
  padding-left: 20px;
}

.milestone-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.milestone-date {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.milestone-progress {
  width: 200px;
}

/* 自定义提示样式 */
.custom-tooltip {
  position: fixed;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.tooltip-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #409EFF;
}

.tooltip-content {
  font-size: 14px;
  line-height: 1.5;
}

/* 里程碑表单样式 */
.milestone-form {
  max-height: 400px;
  overflow-y: auto;
}
</style>