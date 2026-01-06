<template>
  <div class="team-performance">
    <div class="page-header">
      <h2>团队绩效</h2>
      <p>查看您管理项目的团队绩效统计</p>
    </div>

    <el-row :gutter="20">
      <!-- 项目选择 -->
      <el-col :span="24">
        <el-card class="project-selector">
          <div class="selector-content">
            <el-select
              v-model="selectedProject"
              placeholder="选择项目"
              style="width: 300px"
              @change="handleProjectChange"
            >
              <el-option
                v-for="project in managedProjects"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
            
            <el-select
              v-model="selectedPeriod"
              placeholder="选择时间段"
              style="width: 200px; margin-left: 16px"
              @change="handlePeriodChange"
            >
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
              <el-option label="本季度" value="quarter" />
              <el-option label="本年" value="year" />
            </el-select>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="selectedProject" v-loading="loading">
      <!-- 绩效概览 -->
      <el-row :gutter="20" class="performance-overview">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ performanceData.totalHours || 0 }}</div>
              <div class="stat-label">总工时</div>
            </div>
            <el-icon class="stat-icon"><Timer /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ performanceData.efficiency || 0 }}%</div>
              <div class="stat-label">工作效率</div>
            </div>
            <el-icon class="stat-icon"><TrendCharts /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ performanceData.completedTasks || 0 }}</div>
              <div class="stat-label">完成任务</div>
            </div>
            <el-icon class="stat-icon"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ performanceData.teamSize || 0 }}</div>
              <div class="stat-label">团队人数</div>
            </div>
            <el-icon class="stat-icon"><User /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- 团队成员绩效 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>团队成员绩效</span>
            </template>
            
            <el-table :data="memberPerformance" style="width: 100%">
              <el-table-column prop="userRealName" label="成员" width="120">
                <template #default="{ row }">
                  <div class="member-info">
                    <strong>{{ row.userRealName }}</strong>
                    <div class="member-role">{{ row.role }}</div>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="totalHours" label="总工时" width="100">
                <template #default="{ row }">
                  {{ row.totalHours || 0 }}h
                </template>
              </el-table-column>
              
              <el-table-column prop="completedTasks" label="完成任务" width="100">
                <template #default="{ row }">
                  {{ row.completedTasks || 0 }}
                </template>
              </el-table-column>
              
              <el-table-column prop="efficiency" label="效率" width="100">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.efficiency || 0" 
                    :stroke-width="8"
                    :show-text="true"
                  />
                </template>
              </el-table-column>
              
              <el-table-column prop="quality" label="质量评分" width="120">
                <template #default="{ row }">
                  <el-rate 
                    v-model="row.quality" 
                    disabled 
                    show-score 
                    text-color="#ff9900"
                    score-template="{value}"
                  />
                </template>
              </el-table-column>
              
              <el-table-column prop="attendance" label="出勤率" width="100">
                <template #default="{ row }">
                  {{ row.attendance || 0 }}%
                </template>
              </el-table-column>
              
              <el-table-column label="趋势" width="150">
                <template #default="{ row }">
                  <div class="trend-indicator">
                    <el-icon 
                      :class="getTrendClass(row.trend)"
                      :color="getTrendColor(row.trend)"
                    >
                      <component :is="getTrendIcon(row.trend)" />
                    </el-icon>
                    <span :style="{ color: getTrendColor(row.trend) }">
                      {{ getTrendText(row.trend) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="viewMemberDetail(row.userId)">
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else class="empty-state">
      <el-empty description="请选择项目查看团队绩效" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Timer, TrendCharts, Check, User,
  ArrowUp, ArrowDown, Minus
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const managedProjects = ref([])
const selectedProject = ref('')
const selectedPeriod = ref('month')
const performanceData = ref({})
const memberPerformance = ref([])
const loading = ref(false)

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
      
      // 如果有项目且没有选中项目，默认选择第一个项目
      if (managedProjects.value.length > 0 && !selectedProject.value) {
        selectedProject.value = managedProjects.value[0].id
        getTeamPerformance()
      }
    }
  } catch (error) {
    console.error('获取管理项目失败:', error)
  }
}

// 获取团队绩效数据
const getTeamPerformance = async () => {
  if (!selectedProject.value) return
  
  loading.value = true
  try {
    const response = await request({
      url: '/project-manager/team-performance',
      method: 'get',
      params: {
        projectId: selectedProject.value,
        period: selectedPeriod.value
      }
    })
    
    if (response.code === 200) {
      performanceData.value = response.data.overview || {}
      memberPerformance.value = response.data.members || []
    } else {
      ElMessage.error(response.message || '获取团队绩效失败')
    }
  } catch (error) {
    ElMessage.error('获取团队绩效失败')
    // 模拟数据用于演示
    performanceData.value = {
      totalHours: 320,
      efficiency: 85,
      completedTasks: 24,
      teamSize: 8
    }
    memberPerformance.value = [
      {
        userId: 1,
        userRealName: '张三',
        role: '开发工程师',
        totalHours: 40,
        completedTasks: 5,
        efficiency: 90,
        quality: 4.5,
        attendance: 95,
        trend: 'up'
      },
      {
        userId: 2,
        userRealName: '李四',
        role: '测试工程师',
        totalHours: 38,
        completedTasks: 8,
        efficiency: 85,
        quality: 4.2,
        attendance: 92,
        trend: 'stable'
      },
      {
        userId: 3,
        userRealName: '王五',
        role: '前端工程师',
        totalHours: 42,
        completedTasks: 6,
        efficiency: 88,
        quality: 4.8,
        attendance: 98,
        trend: 'up'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 项目变更处理
const handleProjectChange = () => {
  getTeamPerformance()
}

// 时间段变更处理
const handlePeriodChange = () => {
  getTeamPerformance()
}

// 获取趋势图标
const getTrendIcon = (trend) => {
  const iconMap = {
    'up': ArrowUp,
    'down': ArrowDown,
    'stable': Minus
  }
  return iconMap[trend] || Minus
}

// 获取趋势颜色
const getTrendColor = (trend) => {
  const colorMap = {
    'up': '#67C23A',
    'down': '#F56C6C',
    'stable': '#909399'
  }
  return colorMap[trend] || '#909399'
}

// 获取趋势类名
const getTrendClass = (trend) => {
  return `trend-${trend}`
}

// 获取趋势文本
const getTrendText = (trend) => {
  const textMap = {
    'up': '上升',
    'down': '下降',
    'stable': '稳定'
  }
  return textMap[trend] || '稳定'
}

// 查看成员详情
const viewMemberDetail = (userId) => {
  router.push(`/project-manager/member-detail/${userId}?projectId=${selectedProject.value}`)
}

onMounted(() => {
  getManagedProjects()
})
</script>

<style scoped>
.team-performance {
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

.project-selector {
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  align-items: center;
}

.performance-overview {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  font-size: 48px;
  color: #E4E7ED;
}

.member-info {
  line-height: 1.4;
}

.member-role {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  margin-top: 60px;
}
</style>