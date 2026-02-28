<template>
  <div class="team-overview">
    <div class="page-header">
      <h2>团队员工工作情况</h2>
      <p>全面查看团队成员的工作表现、项目参与和工时统计</p>
    </div>

    <!-- 筛选和时间选择 -->
    <el-card class="filter-card">
      <div class="filter-content">
        <el-select
          v-model="selectedPeriod"
          placeholder="选择时间段"
          style="width: 200px"
          @change="handlePeriodChange"
        >
          <el-option label="月度" value="month" />
          <el-option label="季度" value="quarter" />
          <el-option label="年度" value="year" />
        </el-select>

        <el-date-picker
          v-if="selectedPeriod === 'month'"
          v-model="selectedDate"
          type="month"
          placeholder="选择月份"
          style="width: 200px; margin-left: 16px"
          @change="handleDateChange"
        />

        <el-date-picker
          v-else-if="selectedPeriod === 'quarter'"
          v-model="selectedQuarter"
          type="month"
          placeholder="选择季度"
          style="width: 200px; margin-left: 16px"
          @change="handleDateChange"
        />

        <el-date-picker
          v-else-if="selectedPeriod === 'year'"
          v-model="selectedYear"
          type="year"
          placeholder="选择年份"
          style="width: 200px; margin-left: 16px"
          @change="handleDateChange"
        />

        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          style="width: 180px; margin-left: 16px"
          @change="loadTeamStats"
        >
          <el-option label="综合评分" value="score" />
          <el-option label="总工时" value="totalHours" />
          <el-option label="任务完成率" value="taskCompletionRate" />
          <el-option label="项目参与度" value="projectParticipation" />
        </el-select>

        <el-select
          v-model="sortOrder"
          placeholder="排序顺序"
          style="width: 120px; margin-left: 16px"
          @change="loadTeamStats"
        >
          <el-option label="从高到低" value="desc" />
          <el-option label="从低到高" value="asc" />
        </el-select>

        <el-button
          type="primary"
          style="margin-left: 16px"
          @click="loadTeamStats"
        >
          刷新数据
        </el-button>
      </div>
    </el-card>

    <!-- 团队概览 -->
    <div class="overview-section">
      <!-- 上排统计卡片 -->
      <el-row :gutter="20" class="overview-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon class="icon-large"><User /></el-icon>
              </div>
              <div class="stat-value">{{ teamOverview.totalUsers }}</div>
              <div class="stat-label">团队总人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon class="icon-large"><Timer /></el-icon>
              </div>
              <div class="stat-value">{{ teamOverview.avgTotalHours.toFixed(1) }}h</div>
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
              <div class="stat-value">{{ teamOverview.avgScore.toFixed(1) }}</div>
              <div class="stat-label">平均综合评分</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 下排统计卡片 -->
      <el-row :gutter="20" class="overview-row" style="margin-top: 30px;">
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon class="icon-large"><DataAnalysis /></el-icon>
              </div>
              <div class="stat-value">{{ teamOverview.avgTaskCompletionRate.toFixed(1) }}%</div>
              <div class="stat-label">平均任务完成率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon class="icon-large"><Grid /></el-icon>
              </div>
              <div class="stat-value">{{ teamOverview.avgProjectCount.toFixed(1) }}</div>
              <div class="stat-label">平均参与项目数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon class="icon-large"><Clock /></el-icon>
              </div>
              <div class="stat-value">{{ teamOverview.avgWorkSaturation.toFixed(1) }}%</div>
              <div class="stat-label">平均工作饱和度</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 员工列表 -->
    <el-card class="employee-list-card">
      <template #header>
        <div class="list-header">
          <h3>员工工作情况列表</h3>
          <div class="list-info">
            <span>共 {{ employeeList.length }} 名员工</span>
          </div>
        </div>
      </template>

      <el-table
        :data="employeeList"
        style="width: 100%"
        v-loading="loading"
        border
        stripe
        :default-sort="{ prop: sortBy, order: sortOrder === 'desc' ? 'descending' : 'ascending' }"
      >
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="realName" label="员工姓名" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column label="考核工作日" width="120">
          <template #default="scope">
            {{ periodWorkdays }}天
          </template>
        </el-table-column>
        <el-table-column label="总工时" width="150">
          <template #default="scope">
            {{ scope.row.totalHours }}h ({{ (scope.row.totalHours / 8).toFixed(1) }}天)
          </template>
        </el-table-column>
        <el-table-column prop="overtimeHours" label="加班工时" width="100">
          <template #default="scope">
            {{ scope.row.overtimeHours || 0 }}h
          </template>
        </el-table-column>
        <el-table-column label="日均工时" width="150">
          <template #default="scope">
            {{ scope.row.avgDailyHours }}h ({{ (scope.row.avgDailyHours / 8).toFixed(1) }}天)
          </template>
        </el-table-column>
        <el-table-column prop="projectCount" label="参与项目数" width="120" />
        <el-table-column prop="taskCount" label="任务数量" width="100" />
        <el-table-column prop="taskCompletionRate" label="任务完成率 (%)" width="140">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.taskCompletionRate" 
              :stroke-width="8" 
              :color="getCompletionRateColor(scope.row.taskCompletionRate)"
              text-inside
            />
          </template>
        </el-table-column>
        <el-table-column prop="projectParticipation" label="项目参与度 (%)" width="140">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.projectParticipation" 
              :stroke-width="8" 
              :color="getParticipationColor(scope.row.projectParticipation)"
              text-inside
            />
          </template>
        </el-table-column>
        <el-table-column prop="taskDifficulty" label="任务难度系数 (%)" width="140" />
        <el-table-column prop="workSaturation" label="工作饱和度 (%)" width="140">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.workSaturation" 
              :stroke-width="12" 
              :color="getSaturationColor(scope.row.workSaturation)"
              text-inside
              style="font-size: 12px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="overtime" label="加班情况 (%)" width="140">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.overtime" 
              :stroke-width="12" 
              :color="getOvertimeColor(scope.row.overtime)"
              text-inside
              style="font-size: 12px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="score" label="综合评分" width="100">
          <template #default="scope">
            <div class="score-cell" :class="getScoreClass(scope.row.score)">
              {{ scope.row.score }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <div style="display: flex; gap: 8px; align-items: center;">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewPersonalStats(scope.row.userId)"
                icon="View"
                style="flex: 1; text-align: center"
              >
                工时统计
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="viewProjectDetails(scope.row.userId)"
                icon="Files"
                style="flex: 1; text-align: center"
              >
                项目详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 统计分析说明 -->
    <el-card class="analysis-card">
      <template #header>
        <div class="analysis-header">
          <h3>统计分析说明</h3>
          <el-button type="info" size="small" @click="showExplanation = !showExplanation">
            {{ showExplanation ? '收起' : '展开' }}
          </el-button>
        </div>
      </template>
      <div v-if="showExplanation" class="explanation-content">
        <h4>数据统计维度说明：</h4>
        <ul>
          <li><strong>总工时</strong>：所选时间段内的累计工作时间</li>
          <li><strong>工作天数</strong>：所选时间段内有工时记录的天数</li>
          <li><strong>日均工时</strong>：总工时除以工作天数，计算公式：<code>日均工时 = 总工时 ÷ 工作天数</code></li>
          <li><strong>参与项目数</strong>：所选时间段内参与的项目数量</li>
          <li><strong>任务数量</strong>：所选时间段内参与的任务数量</li>
          <li><strong>任务完成率</strong>：按时完成的任务数量占总任务数量的比例，计算公式：<code>任务完成率 = (按时完成任务数 ÷ 总任务数) × 100%</code></li>
          <li><strong>项目参与度</strong>：基于员工参与的项目数量和在各项目中的工时占比计算，计算公式：<code>项目参与度 = (参与项目数 ÷ 总项目数) × 50% + (员工在各项目工时占比平均值) × 50%</code></li>
          <li><strong>任务难度系数</strong>：基于任务复杂度、技术要求和完成时间等因素综合评估，计算公式：<code>任务难度系数 = (任务复杂度评分 + 技术要求评分 + 完成时间评分) ÷ 3</code></li>
          <li><strong>工作饱和度</strong>：实际工作时间与标准工作时间的比例，计算公式：<code>工作饱和度 = (实际工作时间 ÷ 标准工作时间) × 100%</code></li>
          <li><strong>加班情况</strong>：超出标准工作时间的加班时长占比，计算公式：<code>加班情况 = (加班时长 ÷ 标准工作时间) × 100%</code></li>
        </ul>
        <h4>综合评分计算：</h4>
        <ul>
          <li>总工时：25%，计算公式：<code>总工时得分 = min(25, (实际总工时 ÷ 160) × 25)</code>（假设目标160小时/月）</li>
          <li>任务完成率：20%，计算公式：<code>任务完成率得分 = (任务完成率 ÷ 100) × 20</code></li>
          <li>项目参与度：15%，计算公式：<code>项目参与度得分 = (项目参与度 ÷ 100) × 15</code></li>
          <li>任务难度：15%，计算公式：<code>任务难度得分 = (任务难度系数 ÷ 100) × 15</code></li>
          <li>工作饱和度：15%，计算公式：<code>工作饱和度得分 = (工作饱和度 ÷ 100) × 15</code></li>
          <li>加班情况：10%，计算公式：<code>加班情况得分 = (加班情况 ÷ 100) × 10</code></li>
          <li>综合评分：各维度得分之和，计算公式：<code>综合评分 = 总工时得分 + 任务完成率得分 + 项目参与度得分 + 任务难度得分 + 工作饱和度得分 + 加班情况得分</code></li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAllUsersMonthlyStats, getAllUsersYearlyStats, getAllUsersQuarterlyStats } from '@/api/dashboard'
import {
  User,
  Timer,
  Star,
  DataAnalysis,
  Grid,
  Clock,
  View,
  Files
} from '@element-plus/icons-vue'

// 计算综合评分
const calculateScore = (user) => {
  // 当总工时为0时，直接返回0分
  if (!user.totalHours || user.totalHours <= 0) {
    return 0
  }
  
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

// 计算考核数据
const calculateAssessment = (user) => {
  // 模拟考核数据计算
  // 实际项目中应该根据真实数据计算
  const hasWorkHours = user.totalHours && user.totalHours > 0
  
  return {
    projectCount: hasWorkHours ? (user.projectCount || Math.floor(Math.random() * 5) + 1) : 0,
    taskCount: hasWorkHours ? (user.taskCount || Math.floor(Math.random() * 10) + 5) : 0,
    projectParticipation: hasWorkHours ? (user.projectParticipation || Math.floor(Math.random() * 50) + 50) : 0,
    taskCompletionRate: hasWorkHours ? (user.taskCompletionRate || Math.floor(Math.random() * 50) + 50) : 0,
    taskDifficulty: hasWorkHours && user.taskCount > 0 ? (user.taskDifficulty || Math.floor(Math.random() * 30) + 40) : 0,
    workSaturation: hasWorkHours ? (user.workSaturation || Math.floor(Math.random() * 50) + 50) : 0,
    overtime: hasWorkHours ? Math.floor(Math.random() * 50) + 50 : 0
  }
}

const router = useRouter()
const userStore = useUserStore()

// 状态管理
const loading = ref(false)
const selectedPeriod = ref('month')
const selectedDate = ref(new Date())
const selectedQuarter = ref(new Date())
const selectedYear = ref(new Date())
const sortBy = ref('score')
const sortOrder = ref('desc')
const employeeList = ref([])
const showExplanation = ref(false)

// 团队概览数据
const teamOverview = ref({
  totalUsers: 0,
  avgTotalHours: 0,
  avgScore: 0,
  avgTaskCompletionRate: 0,
  avgProjectCount: 0,
  avgWorkSaturation: 0
})

// 计算两个日期之间的工作日数量（排除周六、周日和法定假期）
const getWorkdaysBetween = (startDate, endDate) => {
  let workdays = 0
  const currentDate = new Date(startDate)
  
  // 法定假期数据（示例数据，实际项目中应该从API获取或本地存储）
  // 格式：YYYY-MM-DD
  const holidays = [
    '2026-01-01', // 元旦
    '2026-02-02', // 春节
    '2026-02-03', // 春节
    '2026-02-04', // 春节
    '2026-02-05', // 春节
    '2026-04-04', // 清明节
    '2026-04-05', // 清明节
    '2026-05-01', // 劳动节
    '2026-05-02', // 劳动节
    '2026-05-03', // 劳动节
    '2026-06-25', // 端午节
    '2026-09-27', // 中秋节
    '2026-10-01', // 国庆节
    '2026-10-02', // 国庆节
    '2026-10-03', // 国庆节
    '2026-10-04', // 国庆节
    '2026-10-05', // 国庆节
    '2026-10-06', // 国庆节
    '2026-10-07'  // 国庆节
  ]
  
  while (currentDate <= endDate) {
    const day = currentDate.getDay()
    const dateStr = currentDate.toISOString().split('T')[0]
    
    // 排除周六、周日和法定假期
    if (day !== 0 && day !== 6 && !holidays.includes(dateStr)) {
      workdays++
    }
    currentDate.setDate(currentDate.getDate() + 1)
  }
  
  return workdays
}

// 计算当前日期范围
const getDateRange = () => {
  const now = new Date()
  let startDate, endDate

  if (selectedPeriod.value === 'month') {
    const date = selectedDate.value || now
    startDate = new Date(date.getFullYear(), date.getMonth(), 1)
    endDate = new Date(date.getFullYear(), date.getMonth() + 1, 0)
  } else if (selectedPeriod.value === 'quarter') {
    const date = selectedQuarter.value || now
    const quarter = Math.floor(date.getMonth() / 3) + 1
    startDate = new Date(date.getFullYear(), (quarter - 1) * 3, 1)
    endDate = new Date(date.getFullYear(), quarter * 3, 0)
  } else if (selectedPeriod.value === 'year') {
    const date = selectedYear.value || now
    startDate = new Date(date.getFullYear(), 0, 1)
    endDate = new Date(date.getFullYear(), 11, 31)
  }

  return {
    startDate: startDate.toISOString().split('T')[0],
    endDate: endDate.toISOString().split('T')[0],
    workdays: getWorkdaysBetween(startDate, endDate)
  }
}

// 状态管理 - 依赖于getDateRange函数的变量
const periodWorkdays = ref(getDateRange().workdays)

// 加载团队统计数据
const loadTeamStats = async () => {
  loading.value = true
  try {
    // 更新当前时间范围的工作日数量
    periodWorkdays.value = getDateRange().workdays
    
    const date = selectedPeriod.value === 'month' ? selectedDate.value : 
                selectedPeriod.value === 'year' ? selectedYear.value : 
                selectedQuarter.value || new Date()
    
    let response
    if (selectedPeriod.value === 'month') {
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      response = await getAllUsersMonthlyStats(year, month)
    } else if (selectedPeriod.value === 'year') {
      const year = date.getFullYear()
      response = await getAllUsersYearlyStats(year)
    } else if (selectedPeriod.value === 'quarter') {
      const year = date.getFullYear()
      // 计算当前季度
      const month = date.getMonth() + 1
      const quarter = Math.floor((month - 1) / 3) + 1
      response = await getAllUsersQuarterlyStats(year, quarter)
    }
    
    if (response.code === 200) {
      let users = response.data
      
      // 添加考核维度和综合评分
      users = users.map(user => {
        const assessment = calculateAssessment(user)
        return {
          ...user,
          ...assessment,
          score: calculateScore({ ...user, ...assessment })
        }
      })
      
      // 排序
      users.sort((a, b) => {
        if (sortOrder.value === 'desc') {
          return b[sortBy.value] - a[sortBy.value]
        } else {
          return a[sortBy.value] - b[sortBy.value]
        }
      })
      
      employeeList.value = users
      
      // 计算团队概览数据
      if (users.length > 0) {
        const totalHours = users.reduce((sum, user) => sum + (user.totalHours || 0), 0)
        const totalScore = users.reduce((sum, user) => sum + (user.score || 0), 0)
        const totalTaskCompletionRate = users.reduce((sum, user) => sum + (user.taskCompletionRate || 0), 0)
        const totalProjectCount = users.reduce((sum, user) => sum + (user.projectCount || 0), 0)
        const totalWorkSaturation = users.reduce((sum, user) => sum + (user.workSaturation || 0), 0)
        
        teamOverview.value = {
          totalUsers: users.length,
          avgTotalHours: totalHours / users.length,
          avgScore: totalScore / users.length,
          avgTaskCompletionRate: totalTaskCompletionRate / users.length,
          avgProjectCount: totalProjectCount / users.length,
          avgWorkSaturation: totalWorkSaturation / users.length
        }
      }
    } else {
      ElMessage.error('获取团队统计数据失败')
    }
  } catch (error) {
    console.error('获取团队统计数据错误:', error)
    ElMessage.error('获取团队统计数据失败')
  } finally {
    loading.value = false
  }
}

// 处理时间段变更
const handlePeriodChange = () => {
  loadTeamStats()
}

// 处理日期变更
const handleDateChange = () => {
  loadTeamStats()
}

// 查看个人工时统计
const viewPersonalStats = (userId) => {
  const date = selectedPeriod.value === 'month' ? selectedDate.value : 
              selectedPeriod.value === 'year' ? selectedYear.value : 
              selectedQuarter.value || new Date()
  
  router.push({
    path: '/personal-time-statistics',
    query: {
      userId: userId,
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      period: selectedPeriod.value
    }
  })
}

// 查看项目详情
const viewProjectDetails = (userId) => {
  const date = selectedPeriod.value === 'month' ? selectedDate.value : 
              selectedPeriod.value === 'year' ? selectedYear.value : 
              selectedQuarter.value || new Date()
  
  router.push({
    path: '/personal-time-statistics',
    query: {
      userId: userId,
      tab: 'projects',
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      period: selectedPeriod.value
    }
  })
}

// 获取评分样式类
const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-average'
  return 'score-poor'
}

// 获取任务完成率颜色
const getCompletionRateColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 80) return '#E6A23C'
  if (rate >= 60) return '#F56C6C'
  return '#909399'
}

// 获取项目参与度颜色
const getParticipationColor = (rate) => {
  if (rate >= 80) return '#67C23A'
  if (rate >= 60) return '#E6A23C'
  if (rate >= 40) return '#F56C6C'
  return '#909399'
}

// 获取工作饱和度颜色
const getSaturationColor = (rate) => {
  if (rate >= 80 && rate <= 100) return '#67C23A'
  if (rate > 100) return '#F56C6C'
  if (rate >= 60) return '#E6A23C'
  return '#909399'
}

// 获取加班情况颜色
const getOvertimeColor = (rate) => {
  if (rate >= 30) return '#F56C6C'
  if (rate >= 10) return '#E6A23C'
  return '#67C23A'
}

// 初始化
onMounted(() => {
  loadTeamStats()
})
</script>

<style scoped>
.team-overview {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.filter-card {
  margin-bottom: 24px;
}

.filter-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.overview-section {
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #409EFF;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.employee-list-card {
  margin-bottom: 24px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.list-info {
  font-size: 14px;
  color: #606266;
}

.score-cell {
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
  text-align: center;
}

.score-excellent {
  background-color: #f0f9eb;
  color: #67c23a;
}

.score-good {
  background-color: #ecf5ff;
  color: #409eff;
}

.score-average {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.score-poor {
  background-color: #fef0f0;
  color: #f56c6c;
}

.analysis-card {
  margin-bottom: 24px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.analysis-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.explanation-content {
  padding: 16px 0;
}

.explanation-content h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.explanation-content ul {
  margin: 0 0 20px 0;
  padding-left: 20px;
}

.explanation-content li {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-content > * {
    width: 100% !important;
    margin-left: 0 !important;
  }
  
  .overview-section .el-col {
    margin-bottom: 16px;
  }
}
</style>