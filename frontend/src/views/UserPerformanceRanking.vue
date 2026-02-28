<template>
  <div class="user-performance-ranking">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">员工工作表现排名</h2>
          <div class="header-actions">
            <el-date-picker
              v-if="viewMode === 'monthly'"
              v-model="dateRange"
              type="month"
              placeholder="选择月份"
              @change="handleDateChange"
              style="width: 160px; margin-right: 10px"
              :default-value="new Date()"
            />
            <el-date-picker
              v-else
              v-model="dateRange"
              type="year"
              placeholder="选择年份"
              @change="handleDateChange"
              style="width: 160px; margin-right: 10px"
              :default-value="new Date()"
            />
            <el-select v-model="viewMode" @change="handleViewModeChange" size="default">
              <el-option label="月度统计" value="monthly" />
              <el-option label="年度统计" value="yearly" />
            </el-select>
            <el-button type="primary" size="default" @click="loadUserRanking" style="margin-left: 10px">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计概览 -->
      <div class="stats-overview">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon class="icon-large"><User /></el-icon>
                </div>
                <div class="stat-value">{{ totalUsers }}</div>
                <div class="stat-label">总人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon class="icon-large"><Timer /></el-icon>
                </div>
                <div class="stat-value">{{ avgTotalHours.toFixed(1) }}h</div>
                <div class="stat-label">平均总工时</div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon class="icon-large"><Star /></el-icon>
                </div>
                <div class="stat-value">{{ avgScore.toFixed(1) }}</div>
                <div class="stat-label">平均综合评分</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 统计分析说明 -->
      <div class="analysis-explanation">
        <el-card shadow="hover">
          <template #header>
            <div class="explanation-header">
              <h3>统计分析说明</h3>
              <el-button type="info" size="small" @click="showExplanation = !showExplanation">
                {{ showExplanation ? '收起' : '展开' }}
              </el-button>
            </div>
          </template>
          <div v-if="showExplanation" class="explanation-content">
            <h4>考核维度说明：</h4>
            <ul>
              <li><strong>项目参与度</strong>：基于员工参与的项目数量和在各项目中的工时占比计算</li>
              <li><strong>任务完成率</strong>：按时完成的任务数量占总任务数量的比例</li>
              <li><strong>任务难度系数</strong>：基于任务复杂度、技术要求和完成时间等因素综合评估</li>
              <li><strong>工作饱和度</strong>：实际工作时间与标准工作时间的比例</li>
              <li><strong>加班情况</strong>：超出标准工作时间的加班时长占比</li>
            </ul>
            <h4>综合评分计算：</h4>
            <ul>
              <li>总工时：25%</li>
              <li>任务完成率：20%</li>
              <li>项目参与度：15%</li>
              <li>任务难度：15%</li>
              <li>工作饱和度：15%</li>
              <li>加班情况：10%</li>
            </ul>
            <h4>评分等级：</h4>
            <ul>
              <li><span class="score-badge excellent">优秀</span>：90分及以上</li>
              <li><span class="score-badge good">良好</span>：80-89分</li>
              <li><span class="score-badge average">一般</span>：70-79分</li>
              <li><span class="score-badge poor">待改进</span>：70分以下</li>
            </ul>
          </div>
        </el-card>
      </div>
      
      <!-- 排名表格 -->
      <div class="ranking-table">
        <el-card shadow="hover">
          <template #header>
            <div class="table-header">
              <h3>员工考核维度分析</h3>
              <div class="table-actions">
                <el-select v-model="sortField" @change="handleSortChange" size="small">
                  <el-option label="按排名排序" value="rank" />
                  <el-option label="按总工时排序" value="totalHours" />
                  <el-option label="按综合评分排序" value="score" />
                </el-select>
              </div>
            </div>
          </template>
          <el-table 
            :data="sortedUserRanking" 
            style="width: 100%" 
            border 
            stripe
            :loading="loading"
            @row-click="handleRowClick"
          >
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="realName" label="员工姓名" width="120" />
            <el-table-column prop="totalHours" label="总工时 (h)" width="120" />
            <el-table-column prop="workDays" label="工作天数" width="100" />
            <el-table-column prop="avgDailyHours" label="日均工时 (h)" width="120" />
            <el-table-column prop="projectCount" label="参与项目数" width="120" />
            <el-table-column prop="taskCount" label="任务数量" width="100" />
            <el-table-column prop="taskCompletionRate" label="任务完成率 (%)" width="140" />
            <el-table-column prop="projectParticipation" label="项目参与度 (%)" width="140" />
            <el-table-column prop="taskDifficulty" label="任务难度系数 (%)" width="140" />
            <el-table-column prop="workSaturation" label="工作饱和度 (%)" width="120" />
            <el-table-column prop="overtime" label="加班情况 (%)" width="120" />
            <el-table-column prop="score" label="综合评分" width="100">
              <template #default="scope">
                <div class="score-cell" :class="getScoreClass(scope.row.score)">
                  {{ scope.row.score }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewUserDetails(scope.row.userId)" :icon="View">
                  查看详情
                </el-button>
                <el-button type="info" size="small" @click="viewUserProjects(scope.row.userId)" style="margin-left: 10px">
                  项目详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllUsersMonthlyStats, getAllUsersYearlyStats } from '@/api/dashboard'
import { User, Timer, Star, Refresh, View } from '@element-plus/icons-vue'

const router = useRouter()

// 状态管理
const dateRange = ref(new Date())
const viewMode = ref('monthly')
const userRanking = ref([])
const showExplanation = ref(false)
const loading = ref(false)
const sortField = ref('rank')

// 计算统计概览数据
const totalUsers = computed(() => userRanking.value.length)

const avgTotalHours = computed(() => {
  if (userRanking.value.length === 0) return 0
  const total = userRanking.value.reduce((sum, user) => sum + (user.totalHours || 0), 0)
  return total / userRanking.value.length
})

const avgScore = computed(() => {
  if (userRanking.value.length === 0) return 0
  const total = userRanking.value.reduce((sum, user) => sum + (user.score || 0), 0)
  return total / userRanking.value.length
})

// 排序后的用户数据
const sortedUserRanking = computed(() => {
  const sorted = [...userRanking.value]
  if (sortField.value === 'rank') {
    return sorted.sort((a, b) => a.rank - b.rank)
  } else if (sortField.value === 'totalHours') {
    return sorted.sort((a, b) => b.totalHours - a.totalHours)
  } else if (sortField.value === 'score') {
    return sorted.sort((a, b) => b.score - a.score)
  }
  return sorted
})

// 处理日期变更
const handleDateChange = () => {
  loadUserRanking()
}

// 处理视图模式变更
const handleViewModeChange = () => {
  loadUserRanking()
}

// 处理排序变更
const handleSortChange = () => {
  // 排序已在computed属性中处理
}

// 处理行点击
const handleRowClick = (row) => {
  viewUserDetails(row.userId)
}

// 查看用户详情
const viewUserDetails = (userId) => {
  router.push({
    path: '/personal-time-statistics',
    query: {
      userId: userId,
      year: dateRange.value.getFullYear(),
      month: dateRange.value.getMonth() + 1,
      viewMode: viewMode.value
    }
  })
}

// 查看用户项目详情
const viewUserProjects = (userId) => {
  // 这里可以导航到用户项目详情页面
  // 暂时先跳转到个人工时统计页面
  router.push({
    path: '/personal-time-statistics',
    query: {
      userId: userId,
      year: dateRange.value.getFullYear(),
      month: dateRange.value.getMonth() + 1,
      viewMode: viewMode.value,
      tab: 'projects'
    }
  })
}

// 获取评分样式
const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-average'
  return 'score-poor'
}

// 计算考核数据
const calculateAssessment = (user) => {
  // 模拟考核数据计算
  // 实际项目中应该根据真实数据计算
  return {
    projectCount: Math.floor(Math.random() * 5) + 1,
    taskCount: Math.floor(Math.random() * 10) + 5,
    projectParticipation: Math.floor(Math.random() * 50) + 50,
    taskCompletionRate: Math.floor(Math.random() * 50) + 50,
    taskDifficulty: Math.floor(Math.random() * 50) + 50,
    workSaturation: Math.floor(Math.random() * 50) + 50,
    overtime: Math.floor(Math.random() * 50) + 50
  }
}

// 计算综合评分
const calculateScore = (user) => {
  // 基于多项指标计算综合评分
  // 1. 总工时 (25%)
  // 2. 任务完成率 (20%)
  // 3. 项目参与度 (15%)
  // 4. 任务难度 (15%)
  // 5. 工作饱和度 (15%)
  // 6. 加班情况 (10%)
  
  const totalHoursScore = Math.min(25, (user.totalHours / 160) * 25) // 假设目标160小时/月
  const taskCompletionScore = user.taskCompletionRate ? (user.taskCompletionRate / 100) * 20 : 0
  const projectScore = user.projectParticipation ? (user.projectParticipation / 100) * 15 : 0
  const difficultyScore = user.taskDifficulty ? (user.taskDifficulty / 100) * 15 : 0
  const saturationScore = user.workSaturation ? (user.workSaturation / 100) * 15 : 0
  const overtimeScore = user.overtime ? (user.overtime / 100) * 10 : 0
  
  return Math.round(totalHoursScore + taskCompletionScore + projectScore + difficultyScore + saturationScore + overtimeScore)
}

// 加载用户排名
const loadUserRanking = async () => {
  loading.value = true
  const year = dateRange.value.getFullYear()
  const month = dateRange.value.getMonth() + 1
  
  try {
    let response
    if (viewMode.value === 'monthly') {
      response = await getAllUsersMonthlyStats(year, month)
    } else {
      response = await getAllUsersYearlyStats(year)
    }
    
    if (response.code === 200) {
      // 添加排名、考核维度和综合评分
      userRanking.value = response.data.map((user, index) => {
        const assessment = calculateAssessment(user)
        return {
          ...user,
          rank: index + 1,
          ...assessment,
          score: calculateScore({ ...user, ...assessment })
        }
      })
    }
  } catch (error) {
    console.error('加载用户排名失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserRanking()
})
</script>

<style scoped>
.user-performance-ranking {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stats-overview {
  margin-bottom: 30px;
}

.stat-card {
  height: 120px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.stat-icon {
  margin-bottom: 10px;
  font-size: 24px;
  color: #409EFF;
}

.icon-large {
  font-size: 32px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.analysis-explanation {
  margin: 30px 0;
}

.explanation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.explanation-header h3 {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.explanation-content {
  padding: 20px 0;
}

.explanation-content h4 {
  margin: 20px 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.explanation-content ul {
  margin: 0 0 20px 0;
  padding-left: 20px;
}

.explanation-content li {
  margin-bottom: 8px;
  color: #606266;
  line-height: 1.5;
}

.score-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: bold;
  font-size: 12px;
  margin-right: 8px;
}

.score-badge.excellent {
  color: #67C23A;
  background-color: #F0F9EB;
}

.score-badge.good {
  color: #E6A23C;
  background-color: #FEFCE8;
}

.score-badge.average {
  color: #909399;
  background-color: #F5F7FA;
}

.score-badge.poor {
  color: #F56C6C;
  background-color: #FEF0F0;
}

.ranking-table {
  margin-top: 30px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.table-header h3 {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.table-actions {
  display: flex;
  align-items: center;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table .el-table__cell {
  padding: 15px 0;
}

.el-table .el-table__header-wrapper th {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #303133;
  padding: 12px 0;
}

.el-table .el-table__body-wrapper {
  background-color: #fff;
}

.el-table--striped .el-table__row:nth-child(2n) {
  background-color: #fafafa;
}

.el-table__row:hover {
  background-color: #ecf5ff !important;
  cursor: pointer;
}

.score-cell {
  text-align: center;
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.score-excellent {
  color: #67C23A;
  background-color: #F0F9EB;
}

.score-good {
  color: #E6A23C;
  background-color: #FEFCE8;
}

.score-average {
  color: #909399;
  background-color: #F5F7FA;
}

.score-poor {
  color: #F56C6C;
  background-color: #FEF0F0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .user-performance-ranking {
    padding: 10px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .icon-large {
    font-size: 28px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table .el-table__cell {
    padding: 10px 0;
  }
  
  .explanation-content {
    font-size: 14px;
  }
  
  .explanation-content h4 {
    font-size: 16px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .el-date-picker {
    width: 100% !important;
  }
  
  .stat-card {
    margin-bottom: 10px;
  }
}
</style>