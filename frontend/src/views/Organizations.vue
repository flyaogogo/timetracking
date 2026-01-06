<template>
  <div class="organizations">
    <el-card>
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索组织名称"
            style="width: 300px"
            @keyup.enter="loadOrganizations"
          >
            <template #append>
              <el-button @click="loadOrganizations">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建组织
          </el-button>
        </div>
      </div>
      
      <!-- 组织列表 -->
      <el-table
        v-loading="loading"
        :data="flatOrganizations"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="orgName" label="组织名称" min-width="200">
          <template #default="{ row }">
            <div class="org-name-cell">
              <span :style="{ paddingLeft: (row.orgLevel - 1) * 20 + 'px' }">
                <el-icon v-if="row.orgLevel > 1"><Right /></el-icon>
                {{ row.orgName }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="orgCode" label="组织编码" width="120" />
        
        <el-table-column prop="orgType" label="组织类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrgTypeColor(row.orgType)" size="small">
              {{ getOrgTypeText(row.orgType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="managerName" label="负责人" width="100">
          <template #default="{ row }">
            <span v-if="row.managerName">{{ row.managerName }}</span>
            <span v-else class="text-muted">未分配</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="memberCount" label="成员数" width="80">
          <template #default="{ row }">
            {{ row.memberCount || 0 }}人
          </template>
        </el-table-column>
        
        <el-table-column prop="budgetAmount" label="预算金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.budgetAmount) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
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
                  <el-dropdown-item command="statistics">
                    <el-icon><TrendCharts /></el-icon>
                    统计分析
                  </el-dropdown-item>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="addChild">
                    <el-icon><Plus /></el-icon>
                    添加子组织
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
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
          @size-change="loadOrganizations"
          @current-change="loadOrganizations"
        />
      </div>
    </el-card>
    
    <!-- 组织统计对话框 -->
    <el-dialog
      v-model="statisticsVisible"
      :title="`${selectedOrg?.orgName} - 统计信息`"
      width="800px"
    >
      <div v-if="orgStatistics" class="org-statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ orgStatistics.memberCount || 0 }}</div>
                <div class="stat-label">团队成员</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ orgStatistics.projectCount || 0 }}</div>
                <div class="stat-label">进行项目</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ (orgStatistics.monthlyHours || 0).toFixed(1) }}h</div>
                <div class="stat-label">本月工时</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ getBudgetUtilization() }}%</div>
                <div class="stat-label">预算使用率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
    
    <!-- 新建/编辑组织对话框 -->
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
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" />
        </el-form-item>
        
        <el-form-item label="组织编码" prop="orgCode">
          <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
        </el-form-item>
        
        <el-form-item label="上级组织" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="organizationTree"
            :props="treeProps"
            placeholder="请选择上级组织"
            check-strictly
            clearable
          />
        </el-form-item>
        
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" placeholder="请选择组织类型">
            <el-option label="公司" value="COMPANY" />
            <el-option label="部门" value="DEPARTMENT" />
            <el-option label="团队" value="TEAM" />
            <el-option label="项目组" value="PROJECT_GROUP" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="负责人" prop="managerId">
          <el-select v-model="form.managerId" placeholder="请选择负责人" filterable>
            <el-option
              v-for="user in managers"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="成本中心" prop="costCenter">
          <el-input v-model="form.costCenter" placeholder="请输入成本中心" />
        </el-form-item>
        
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number
            v-model="form.budgetAmount"
            :min="0"
            :precision="2"
            placeholder="预算金额"
            style="width: 100%"
          />
        </el-form-item>
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
  Right, 
  ArrowDown, 
  TrendCharts, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'
import { getOrganizationTree, createOrganization, updateOrganization, getOrganizationStatistics, deleteOrganization } from '@/api/organization'
import { getUserList } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const statisticsVisible = ref(false)
const dialogTitle = ref('新建组织')
const searchKeyword = ref('')
const organizationTree = ref([])
const flatOrganizations = ref([])
const selectedOrganizations = ref([])
const managers = ref([])
const selectedOrg = ref(null)
const orgStatistics = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const treeProps = {
  children: 'children',
  label: 'orgName',
  value: 'id'
}

const form = reactive({
  id: null,
  orgName: '',
  orgCode: '',
  parentId: null,
  orgType: 'DEPARTMENT',
  managerId: null,
  costCenter: '',
  budgetAmount: 0
})

const formRef = ref()
const formRules = {
  orgName: [
    { required: true, message: '请输入组织名称', trigger: 'blur' }
  ],
  orgCode: [
    { required: true, message: '请输入组织编码', trigger: 'blur' }
  ],
  orgType: [
    { required: true, message: '请选择组织类型', trigger: 'change' }
  ]
}

// 加载组织树
const loadOrganizations = async () => {
  loading.value = true
  try {
    const response = await getOrganizationTree()
    if (response.code === 200) {
      organizationTree.value = response.data || []
      flatOrganizations.value = flattenTree(organizationTree.value)
      pagination.total = flatOrganizations.value.length
    }
  } catch (error) {
    ElMessage.error('加载组织架构失败')
  } finally {
    loading.value = false
  }
}

// 将树形数据转换为扁平数据
const flattenTree = (tree, level = 1) => {
  const result = []
  tree.forEach(node => {
    result.push({
      ...node,
      orgLevel: level
    })
    if (node.children && node.children.length > 0) {
      result.push(...flattenTree(node.children, level + 1))
    }
  })
  return result
}

// 加载管理员列表
const loadManagers = async () => {
  try {
    const response = await getUserList({ current: 1, size: 100 })
    if (response.code === 200) {
      managers.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载管理员列表失败:', error)
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogTitle.value = '新建组织'
  dialogVisible.value = true
}

// 编辑组织
const editOrganization = (org) => {
  dialogTitle.value = '编辑组织'
  Object.assign(form, org)
  dialogVisible.value = true
}

// 添加子组织
const addChild = (parentOrg) => {
  dialogTitle.value = '新建子组织'
  resetForm()
  form.parentId = parentOrg.id
  dialogVisible.value = true
}

// 查看统计信息
const viewStatistics = async (org) => {
  selectedOrg.value = org
  try {
    const response = await getOrganizationStatistics(org.id)
    if (response.code === 200) {
      orgStatistics.value = response.data
      statisticsVisible.value = true
    } else {
      ElMessage.error(response.message || '获取统计信息失败')
    }
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

// 删除组织
const deleteOrg = async (org) => {
  try {
    await ElMessageBox.confirm(`确定要删除组织"${org.orgName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteOrganization(org.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadOrganizations()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'statistics':
      viewStatistics(row)
      break
    case 'edit':
      editOrganization(row)
      break
    case 'addChild':
      addChild(row)
      break
    case 'delete':
      deleteOrg(row)
      break
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedOrganizations.value = selection
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const response = form.id 
          ? await updateOrganization(form.id, form)
          : await createOrganization(form)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadOrganizations()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
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
    orgName: '',
    orgCode: '',
    parentId: null,
    orgType: 'DEPARTMENT',
    managerId: null,
    costCenter: '',
    budgetAmount: 0
  })
}

// 获取组织类型颜色
const getOrgTypeColor = (type) => {
  const colorMap = {
    'COMPANY': 'danger',
    'DEPARTMENT': 'primary',
    'TEAM': 'success',
    'PROJECT_GROUP': 'warning'
  }
  return colorMap[type] || 'info'
}

// 获取组织类型文本
const getOrgTypeText = (type) => {
  const textMap = {
    'COMPANY': '公司',
    'DEPARTMENT': '部门',
    'TEAM': '团队',
    'PROJECT_GROUP': '项目组'
  }
  return textMap[type] || type
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0'
  return (amount / 10000).toFixed(1) + '万'
}

// 获取预算使用率
const getBudgetUtilization = () => {
  if (!orgStatistics.value?.budgetInfo) return 0
  const { budgetAmount, actualCost } = orgStatistics.value.budgetInfo
  if (!budgetAmount || budgetAmount === 0) return 0
  return ((actualCost || 0) / budgetAmount * 100).toFixed(1)
}

onMounted(() => {
  loadOrganizations()
  loadManagers()
})
</script>

<style scoped>
.organizations {
  padding: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 组织名称层级显示 */
.org-name-cell {
  display: flex;
  align-items: center;
}

.org-name-cell .el-icon {
  margin-right: 5px;
  color: #999;
  font-size: 12px;
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

/* 统计对话框样式 */
.org-statistics {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 16px;
}

.stat-content {
  padding: 16px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 文本样式 */
.text-muted {
  color: #999;
  font-style: italic;
}
</style>