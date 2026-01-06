<template>
  <div class="organizations-test">
    <el-card>
      <h2>组织管理测试</h2>
      
      <div class="test-buttons">
        <el-button @click="testLoadOrganizations" type="primary">测试加载组织</el-button>
        <el-button @click="testCreateOrganization" type="success">测试创建组织</el-button>
        <el-button @click="testUpdateOrganization" type="warning">测试更新组织</el-button>
        <el-button @click="testGetStatistics" type="info">测试统计分析</el-button>
      </div>
      
      <div class="test-result">
        <h3>测试结果:</h3>
        <pre>{{ testResult }}</pre>
      </div>
      
      <div v-if="organizations.length > 0" class="org-list">
        <h3>组织列表:</h3>
        <el-table :data="organizations" style="width: 100%">
          <el-table-column prop="orgName" label="组织名称" />
          <el-table-column prop="orgCode" label="组织编码" />
          <el-table-column prop="orgType" label="组织类型" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button @click="editOrg(row)" size="small">编辑</el-button>
              <el-button @click="viewStats(row)" size="small" type="info">统计</el-button>
              <el-button @click="addChild(row)" size="small" type="success">添加子组织</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑组织" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="组织名称">
          <el-input v-model="editForm.orgName" />
        </el-form-item>
        <el-form-item label="组织编码">
          <el-input v-model="editForm.orgCode" />
        </el-form-item>
        <el-form-item label="组织类型">
          <el-select v-model="editForm.orgType">
            <el-option label="公司" value="COMPANY" />
            <el-option label="部门" value="DEPARTMENT" />
            <el-option label="团队" value="TEAM" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button @click="saveEdit" type="primary">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 统计对话框 -->
    <el-dialog v-model="statsDialogVisible" title="统计分析" width="600px">
      <div v-if="statsData">
        <p>成员数量: {{ statsData.memberCount }}</p>
        <p>项目数量: {{ statsData.projectCount }}</p>
        <p>本月工时: {{ statsData.monthlyHours }}小时</p>
      </div>
    </el-dialog>
    
    <!-- 添加子组织对话框 -->
    <el-dialog v-model="addChildDialogVisible" title="添加子组织" width="500px">
      <el-form :model="childForm" label-width="100px">
        <el-form-item label="父组织">
          <el-input :value="parentOrgName" disabled />
        </el-form-item>
        <el-form-item label="组织名称">
          <el-input v-model="childForm.orgName" />
        </el-form-item>
        <el-form-item label="组织编码">
          <el-input v-model="childForm.orgCode" />
        </el-form-item>
        <el-form-item label="组织类型">
          <el-select v-model="childForm.orgType">
            <el-option label="部门" value="DEPARTMENT" />
            <el-option label="团队" value="TEAM" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addChildDialogVisible = false">取消</el-button>
        <el-button @click="saveChild" type="primary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getOrganizationTree, 
  createOrganization, 
  updateOrganization, 
  getOrganizationStatistics 
} from '@/api/organization'

const testResult = ref('')
const organizations = ref([])
const editDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const addChildDialogVisible = ref(false)
const statsData = ref(null)
const parentOrgName = ref('')

const editForm = reactive({
  id: null,
  orgName: '',
  orgCode: '',
  orgType: 'DEPARTMENT'
})

const childForm = reactive({
  orgName: '',
  orgCode: '',
  orgType: 'TEAM',
  parentId: null
})

// 测试加载组织
const testLoadOrganizations = async () => {
  try {
    testResult.value = '正在加载组织...'
    const response = await getOrganizationTree()
    console.log('API响应:', response)
    
    if (response.code === 200) {
      organizations.value = flattenTree(response.data || [])
      testResult.value = `成功加载 ${organizations.value.length} 个组织`
    } else {
      testResult.value = `加载失败: ${response.message}`
    }
  } catch (error) {
    console.error('加载组织失败:', error)
    testResult.value = `加载失败: ${error.message}`
  }
}

// 将树形数据转换为扁平数据
const flattenTree = (tree) => {
  const result = []
  const flatten = (nodes, level = 1) => {
    nodes.forEach(node => {
      result.push({ ...node, level })
      if (node.children && node.children.length > 0) {
        flatten(node.children, level + 1)
      }
    })
  }
  flatten(tree)
  return result
}

// 测试创建组织
const testCreateOrganization = async () => {
  try {
    testResult.value = '正在创建测试组织...'
    const orgData = {
      orgName: '测试组织' + Date.now(),
      orgCode: 'TEST_' + Date.now(),
      orgType: 'DEPARTMENT',
      budgetAmount: 100000
    }
    
    const response = await createOrganization(orgData)
    console.log('创建响应:', response)
    
    if (response.code === 200) {
      testResult.value = '创建成功'
      testLoadOrganizations() // 重新加载列表
    } else {
      testResult.value = `创建失败: ${response.message}`
    }
  } catch (error) {
    console.error('创建组织失败:', error)
    testResult.value = `创建失败: ${error.message}`
  }
}

// 测试更新组织
const testUpdateOrganization = async () => {
  if (organizations.value.length === 0) {
    testResult.value = '请先加载组织列表'
    return
  }
  
  try {
    testResult.value = '正在更新组织...'
    const firstOrg = organizations.value[0]
    const updateData = {
      ...firstOrg,
      orgName: firstOrg.orgName + '_更新'
    }
    
    const response = await updateOrganization(firstOrg.id, updateData)
    console.log('更新响应:', response)
    
    if (response.code === 200) {
      testResult.value = '更新成功'
      testLoadOrganizations() // 重新加载列表
    } else {
      testResult.value = `更新失败: ${response.message}`
    }
  } catch (error) {
    console.error('更新组织失败:', error)
    testResult.value = `更新失败: ${error.message}`
  }
}

// 测试统计分析
const testGetStatistics = async () => {
  if (organizations.value.length === 0) {
    testResult.value = '请先加载组织列表'
    return
  }
  
  try {
    testResult.value = '正在获取统计信息...'
    const firstOrg = organizations.value[0]
    const response = await getOrganizationStatistics(firstOrg.id)
    console.log('统计响应:', response)
    
    if (response.code === 200) {
      testResult.value = '获取统计信息成功'
      statsData.value = response.data
      statsDialogVisible.value = true
    } else {
      testResult.value = `获取统计信息失败: ${response.message}`
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    testResult.value = `获取统计信息失败: ${error.message}`
  }
}

// 编辑组织
const editOrg = (org) => {
  Object.assign(editForm, org)
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    const response = await updateOrganization(editForm.id, editForm)
    if (response.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      testLoadOrganizations()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 查看统计
const viewStats = async (org) => {
  try {
    const response = await getOrganizationStatistics(org.id)
    if (response.code === 200) {
      statsData.value = response.data
      statsDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取统计信息失败')
    }
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

// 添加子组织
const addChild = (org) => {
  parentOrgName.value = org.orgName
  childForm.parentId = org.id
  childForm.orgName = ''
  childForm.orgCode = ''
  childForm.orgType = 'TEAM'
  addChildDialogVisible.value = true
}

// 保存子组织
const saveChild = async () => {
  try {
    const response = await createOrganization(childForm)
    if (response.code === 200) {
      ElMessage.success('创建成功')
      addChildDialogVisible.value = false
      testLoadOrganizations()
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}
</script>

<style scoped>
.organizations-test {
  padding: 20px;
}

.test-buttons {
  margin: 20px 0;
}

.test-buttons .el-button {
  margin-right: 10px;
}

.test-result {
  margin: 20px 0;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.test-result pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}

.org-list {
  margin-top: 20px;
}
</style>