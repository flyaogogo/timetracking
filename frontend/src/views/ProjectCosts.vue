<template>
  <div class="project-costs">
    <el-card>
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-select
            v-model="selectedProjectId"
            placeholder="选择项目"
            style="width: 200px; margin-right: 10px"
            @change="loadProjectCosts"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          
          <el-select
            v-model="searchCostType"
            placeholder="成本类型"
            style="width: 150px; margin-right: 10px"
            clearable
            @change="loadProjectCosts"
          >
            <el-option label="设备费用" value="EQUIPMENT" />
            <el-option label="差旅费用" value="TRAVEL" />
            <el-option label="外包费用" value="OUTSOURCING" />
            <el-option label="软件费用" value="SOFTWARE" />
            <el-option label="硬件费用" value="HARDWARE" />
            <el-option label="其他费用" value="OTHER" />
          </el-select>
          
          <el-button @click="loadProjectCosts">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog" :disabled="!selectedProjectId">
            <el-icon><Plus /></el-icon>
            添加成本
          </el-button>
        </div>
      </div>
      
      <!-- 成本统计卡片 -->
      <div v-if="costSummary" class="cost-summary">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="summary-card">
              <div class="summary-content">
                <div class="summary-number">¥{{ formatAmount(costSummary.totalCost) }}</div>
                <div class="summary-label">总成本</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="summary-card">
              <div class="summary-content">
                <div class="summary-number">¥{{ formatAmount(costSummary.approvedCost) }}</div>
                <div class="summary-label">已审批</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="summary-card">
              <div class="summary-content">
                <div class="summary-number">¥{{ formatAmount(costSummary.pendingCost) }}</div>
                <div class="summary-label">待审批</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="summary-card">
              <div class="summary-content">
                <div class="summary-number">{{ costSummary.costCount }}</div>
                <div class="summary-label">成本条目</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 成本列表 -->
      <el-table
        v-loading="loading"
        :data="costs"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="costType" label="成本类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getCostTypeColor(row.costType)">
              {{ getCostTypeText(row.costType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" min-width="200" />
        
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="incurredDate" label="发生日期" width="110" />
        
        <el-table-column prop="vendorName" label="供应商" width="150">
          <template #default="{ row }">
            {{ row.vendorName || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button type="primary" size="small" text>
                操作
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" v-if="row.status === 'PENDING'">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="approve" v-if="row.status === 'PENDING'">
                    <el-icon><Check /></el-icon>
                    审批
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided v-if="row.status === 'PENDING'">
                    <el-icon><Delete /></el-icon>
                    删除
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
          @size-change="loadProjectCosts"
          @current-change="loadProjectCosts"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑成本对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="成本类型" prop="costType">
          <el-select v-model="form.costType" placeholder="请选择成本类型" style="width: 100%">
            <el-option label="设备费用 - 硬件设备采购" value="EQUIPMENT" />
            <el-option label="差旅费用 - 出差交通住宿" value="TRAVEL" />
            <el-option label="外包费用 - 外部服务采购" value="OUTSOURCING" />
            <el-option label="软件费用 - 软件授权费用" value="SOFTWARE" />
            <el-option label="硬件费用 - 服务器等硬件" value="HARDWARE" />
            <el-option label="其他费用 - 其他项目费用" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="费用描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述费用用途和内容"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0"
                :precision="2"
                placeholder="费用金额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="发生日期" prop="incurredDate">
              <el-date-picker
                v-model="form.incurredDate"
                type="date"
                placeholder="选择发生日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商">
              <el-input v-model="form.vendorName" placeholder="供应商名称（可选）" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="发票号码">
              <el-input v-model="form.invoiceNumber" placeholder="发票号码（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  ArrowDown, 
  Edit, 
  Delete, 
  Check 
} from '@element-plus/icons-vue'
import { getProjectList } from '@/api/project'
import { 
  getProjectCosts, 
  getProjectCostStatistics,
  createProjectCost,
  updateProjectCost,
  deleteProjectCost,
  approveProjectCost
} from '@/api/projectCost'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加成本')
const selectedProjectId = ref(null)
const searchCostType = ref('')
const projects = ref([])
const costs = ref([])
const selectedCosts = ref([])
const costSummary = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  projectId: null,
  costType: 'OTHER',
  description: '',
  amount: 0,
  incurredDate: '',
  vendorName: '',
  invoiceNumber: ''
})

const formRef = ref()
const formRules = {
  costType: [
    { required: true, message: '请选择成本类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入费用描述', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  incurredDate: [
    { required: true, message: '请选择发生日期', trigger: 'change' }
  ]
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await getProjectList({ 
      current: 1, 
      size: 100,
      userId: userStore.user?.id // 只获取用户参与的项目
    })
    if (response.code === 200) {
      projects.value = response.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 加载项目成本
const loadProjectCosts = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    // 调用项目成本API
    const response = await getProjectCosts({
      projectId: selectedProjectId.value,
      costType: searchCostType.value,
      current: pagination.current,
      size: pagination.size
    })
    
    if (response.code === 200) {
      costs.value = response.data.records || []
      pagination.total = response.data.total || 0
      
      // 加载成本统计
      const statsResponse = await getProjectCostStatistics(selectedProjectId.value)
      if (statsResponse.code === 200) {
        costSummary.value = statsResponse.data
      }
      
      ElMessage.success(`成功加载 ${costs.value.length} 条成本记录`)
    } else {
      ElMessage.error(response.message || '加载项目成本失败')
    }
  } catch (error) {
    console.error('加载项目成本失败:', error)
    ElMessage.error('加载项目成本失败')
  } finally {
    loading.value = false
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '添加成本'
  form.projectId = selectedProjectId.value
  dialogVisible.value = true
}

// 编辑成本
const editCost = (cost) => {
  dialogTitle.value = '编辑成本'
  Object.assign(form, cost)
  dialogVisible.value = true
}

// 审批成本
const approveCost = async (cost) => {
  try {
    await ElMessageBox.confirm(`确定要审批通过"${cost.description}"吗？`, '审批确认', {
      confirmButtonText: '审批通过',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await approveProjectCost(cost.id, 'APPROVED')
    if (response.code === 200) {
      ElMessage.success('审批成功')
      loadProjectCosts()
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }
}

// 删除成本
const deleteCost = async (cost) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${cost.description}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteProjectCost(cost.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadProjectCosts()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editCost(row)
      break
    case 'approve':
      approveCost(row)
      break
    case 'delete':
      deleteCost(row)
      break
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedCosts.value = selection
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const formData = {
          ...form,
          projectId: selectedProjectId.value
        }
        
        let response
        if (form.id) {
          response = await updateProjectCost(form.id, formData)
        } else {
          response = await createProjectCost(formData)
        }
        
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadProjectCosts()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    projectId: selectedProjectId.value,
    costType: 'OTHER',
    description: '',
    amount: 0,
    incurredDate: '',
    vendorName: '',
    invoiceNumber: ''
  })
}

// 工具函数
const formatAmount = (amount) => {
  return (amount || 0).toLocaleString()
}

const getCostTypeColor = (type) => {
  const colorMap = {
    'EQUIPMENT': 'primary',
    'TRAVEL': 'success',
    'OUTSOURCING': 'warning',
    'SOFTWARE': 'info',
    'HARDWARE': 'danger',
    'OTHER': ''
  }
  return colorMap[type] || ''
}

const getCostTypeText = (type) => {
  const textMap = {
    'EQUIPMENT': '设备费用',
    'TRAVEL': '差旅费用',
    'OUTSOURCING': '外包费用',
    'SOFTWARE': '软件费用',
    'HARDWARE': '硬件费用',
    'OTHER': '其他费用'
  }
  return textMap[type] || type
}

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return colorMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-costs {
  padding: 20px;
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

.cost-summary {
  margin-bottom: 20px;
}

.summary-card {
  text-align: center;
}

.summary-content {
  padding: 20px;
}

.summary-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #666;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.field-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>