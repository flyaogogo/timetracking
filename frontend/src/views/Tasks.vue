<template>
  <div class="tasks">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-select
            v-model="searchProjectId"
            placeholder="选择项目"
            style="width: 200px; margin-right: 10px"
            clearable
            @change="loadTasks"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
          
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务名称"
            style="width: 300px"
            @keyup.enter="loadTasks"
          >
            <template #append>
              <el-button @click="loadTasks">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="actions">
          <el-button 
            type="primary" 
            @click="showCreateDialog"
            :disabled="!canManageCurrentProjectTasks"
          >
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
          <el-button 
            type="success" 
            @click="showImportDialog"
            :disabled="!canManageCurrentProjectTasks"
          >
            <el-icon><Download /></el-icon>
            Excel导入
          </el-button>
        </div>
      </div>
      

      
      <!-- 任务列表 -->
      <el-table
        v-loading="loading"
        :data="processedTasks"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @expand-change="handleExpandChange"
        row-key="id"
        empty-text="暂无任务数据，请创建新任务或检查项目权限"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div v-loading="loadingChildTasks[row.id]" element-loading-text="加载子任务中..." style="min-height: 100px;">
              <div v-if="getChildTasks(row.id).length > 0" class="child-tasks-list">
                <div class="child-tasks-header">
                  <div class="child-tasks-title">子任务列表</div>
                  <div class="child-tasks-count"> - {{ getChildTasks(row.id).length }} 个子任务</div>
                </div>
                <el-table :data="getChildTasks(row.id)" style="width: 100%">
                  <el-table-column prop="taskName" label="任务名称" width="180">
                    <template #default="{ row }">
                      <span class="child-task">{{ row.taskName }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="taskType" label="任务类型" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getTaskTypeColor(row.taskType)" size="small" class="child-task">
                        {{ getTaskTypeText(row.taskType) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="priority" label="优先级" width="80">
                    <template #default="{ row }">
                      <el-rate
                        v-model="row.priority"
                        :max="5"
                        disabled
                        show-score
                        text-color="#ff9900"
                        score-template="{value}"
                        size="small"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="assigneeName" label="执行人" width="100">
                    <template #default="{ row }">
                      <span class="child-task">{{ row.assigneeName }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getStatusColor(row.status)" class="child-task">
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="progress" label="进度" width="120">
                    <template #default="{ row }">
                      <el-progress :percentage="row.progress" stroke-width="6" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="estimatedHours" label="预估工时" width="120">
                    <template #default="{ row }">
                      <span class="child-task">
                        {{ row.estimatedHours }}h / {{ Math.ceil(row.estimatedHours / 8) }}天
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="actualHours" label="实际工时" width="120">
                    <template #default="{ row }">
                      <span class="child-task">
                        {{ row.actualHours || 0 }}h / {{ Math.ceil((row.actualHours || 0) / 8) }}天
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" fixed="right">
                    <template #default="{ row }">
                      <div>
                        <el-dropdown @command="(command) => handleCommand(command, row)" v-if="canManageTask(row)">
                          <el-button type="primary" size="small" text>
                            操作
                            <el-icon class="el-icon--right"><arrow-down /></el-icon>
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item command="edit">
                                <el-icon><Edit /></el-icon>
                                编辑
                              </el-dropdown-item>
                              <el-dropdown-item command="progress">
                                <el-icon><TrendCharts /></el-icon>
                                进度
                              </el-dropdown-item>
                              <el-dropdown-item command="timeEntries">
                                <el-icon><Timer /></el-icon>
                                工时
                              </el-dropdown-item>
                              <el-dropdown-item command="delete" divided v-if="canDeleteTask(row)">
                                <el-icon><Delete /></el-icon>
                                删除
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                        <el-button v-else type="info" size="small" disabled>
                          无权限
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div v-else class="no-child-tasks">
                该任务暂无子任务
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="taskName" label="任务名称" width="180">
          <template #default="{ row }">
            <div class="task-name-wrapper">
              <el-link 
                type="primary" 
                @click="viewTask(row)"
              >
                {{ row.taskName }}
              </el-link>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectName" label="所属项目" width="120">
          <template #default="{ row }">
            <span>{{ row.projectName }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="taskType" label="任务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskTypeColor(row.taskType)" size="small">
              {{ getTaskTypeText(row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <div>
              <el-rate
                v-model="row.priority"
                :max="5"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="assigneeName" label="执行人" width="100">
          <template #default="{ row }">
            <span>{{ row.assigneeName }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <div>
              <el-progress :percentage="row.progress" stroke-width="8" />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="estimatedHours" label="预估工时" width="120">
          <template #default="{ row }">
            <span>
              {{ row.estimatedHours }}h / {{ Math.ceil(row.estimatedHours / 8) }}天
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="actualHours" label="实际工时" width="120">
          <template #default="{ row }">
            <span>
              {{ row.actualHours || 0 }}h / {{ Math.ceil((row.actualHours || 0) / 8) }}天
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="delayDays" label="延期天数" width="100">
          <template #default="{ row }">
            <span v-if="row.delayDays > 0" class="delay-text">
              {{ row.delayDays }}天
            </span>
            <span v-else class="normal-text">正常</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始日期" width="110">
          <template #default="{ row }">
            <span>{{ row.startDate }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="endDate" label="结束日期" width="110">
          <template #default="{ row }">
            <span>{{ row.endDate }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div>
              <el-dropdown @command="(command) => handleCommand(command, row)" v-if="canManageTask(row)">
                <el-button type="primary" size="small" text>
                  操作
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item command="addChild" v-if="row.status !== 'COMPLETED'">
                      <el-icon><Plus /></el-icon>
                      添加子任务
                    </el-dropdown-item>
                    <el-dropdown-item command="progress">
                      <el-icon><TrendCharts /></el-icon>
                      进度
                    </el-dropdown-item>
                    <el-dropdown-item command="timeEntries">
                      <el-icon><Timer /></el-icon>
                      工时
                    </el-dropdown-item>
                    <!-- 只有管理员和项目经理可以删除任务 -->
                    <el-dropdown-item command="delete" divided v-if="canDeleteTask(row)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button v-else type="info" size="small" disabled>
                无权限
              </el-button>
            </div>
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
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>
    
    <!-- 新建/编辑任务对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <!-- 父任务显示在任务名前面 -->
        <el-row :gutter="20" v-if="form.id || form.parentId">
          <el-col :span="12">
            <el-form-item label="父任务" prop="parentId">
              <!-- 添加子任务时，仅显示父任务名称，不允许修改 -->
              <template v-if="form.parentId && !form.id">
                <el-input 
                  :value="getParentTaskName(form.parentId)" 
                  readonly 
                  placeholder="父任务"
                />
              </template>
              <!-- 编辑任务时，可以选择父任务 -->
              <template v-else>
                <el-select v-model="form.parentId" placeholder="选择父任务（可选）" filterable>
                  <el-option label="无（顶级任务）" value="" />
                  <el-option
                    v-for="task in parentTasks"
                    :key="task.id"
                    :label="task.taskName"
                    :value="task.id"
                  />
                </el-select>
                <div class="field-hint">子任务不能再创建子任务</div>
              </template>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目" @change="loadParentTasks">
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.projectName"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="form.taskType" placeholder="请选择任务类型">
                <el-option label="开发" value="DEVELOPMENT" />
                <el-option label="测试" value="TESTING" />
                <el-option label="设计" value="DESIGN" />
                <el-option label="文档" value="DOCUMENT" />
                <el-option label="交付" value="DELIVERY" />
                <el-option label="需求" value="REQUIREMENT" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        

        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-rate v-model="form.priority" :max="5" show-text />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="难度系数" prop="difficulty">
              <el-rate v-model="form.difficulty" :max="5" show-text />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时间范围" prop="dateRange">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="选择开始日期"
                end-placeholder="选择结束日期"
                style="width: 100%"
                @change="handleDateRangeChange"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="执行人" prop="assigneeId">
              <el-select v-model="form.assigneeId" placeholder="请选择执行人" filterable>
                <el-option
                  v-for="member in projectMembers"
                  :key="member.userId"
                  :label="member.userRealName"
                  :value="member.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估工时" prop="estimatedHours">
              <el-input-number
                v-model="form.estimatedHours"
                :min="0"
                :precision="1"
                placeholder="预估工时"
                @change="handleEstimatedHoursChange"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="工时提示">
              <div class="estimate-hint" v-if="form.estimatedHours > 0">
                约{{ calculatedDays }}天（按每天8小时计算）
              </div>
              <div class="field-hint" v-else>
                请输入预估工时
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级设置（可选） -->
        <el-collapse v-model="activeTaskCollapse" class="form-collapse">
          <el-collapse-item title="业务价值与复杂度评估" name="business">
            <template #title>
              <span>业务价值与复杂度评估</span>
              <el-tooltip content="用于任务优先级排序和效率分析，影响统计数据准确性" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="业务价值">
                  <el-rate v-model="form.businessValue" :max="5" show-text />
                  <div class="field-hint">标准：1-2低价值，3中等价值，4-5高价值</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="任务复杂度">
                  <el-select v-model="form.taskComplexity" placeholder="评估任务技术复杂度">
                    <el-option label="简单 - 常规开发，无技术难点" value="LOW" />
                    <el-option label="中等 - 有一定技术挑战" value="MEDIUM" />
                    <el-option label="复杂 - 技术难度较大" value="HIGH" />
                    <el-option label="极复杂 - 创新性技术或高难度" value="VERY_HIGH" />
                  </el-select>
                  <div class="field-hint">用于工时预估准确性分析</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="故事点数">
                  <el-input-number
                    v-model="form.storyPoints"
                    :min="1"
                    :max="21"
                    placeholder="敏捷开发故事点"
                  />
                  <div class="field-hint">标准：1,2,3,5,8,13,21 斐波那契数列</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="基线工时">
                  <el-input-number
                    v-model="form.baselineHours"
                    :min="0"
                    :precision="1"
                    placeholder="基线工时（用于偏差分析）"
                  />
                  <div class="field-hint">初始预估工时，用于计算工时偏差</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          
          <el-collapse-item title="质量与依赖关系" name="quality">
            <template #title>
              <span>质量与依赖关系</span>
              <el-tooltip content="用于质量跟踪和项目风险分析" placement="top">
                <el-icon class="info-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="质量目标">
                  <el-input-number
                    v-model="form.qualityScore"
                    :min="60"
                    :max="100"
                    :precision="0"
                    placeholder="质量目标分数"
                  />
                  <div class="field-hint">标准：60-70普通，70-85良好，85+优秀</div>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="审核人" prop="reviewerId">
                  <el-select v-model="form.reviewerId" placeholder="请选择审核人">
                    <el-option
                      v-for="member in projectMembers"
                      :key="member.userId"
                      :label="member.userRealName"
                      :value="member.userId"
                    />
                  </el-select>
                  <div class="field-hint">负责任务质量审核的人员</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 更新进度对话框 -->
    <el-dialog
      v-model="progressDialogVisible"
      title="更新任务进度"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="当前进度">
          <el-slider
            v-model="currentProgress"
            :min="0"
            :max="100"
            show-input
          />
        </el-form-item>
        
        <el-form-item label="任务状态">
          <el-select v-model="currentStatus" placeholder="请选择状态">
            <el-option label="待开始" value="TODO" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="待审核" value="REVIEW" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="PAUSED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态说明">
          <el-collapse v-model="statusExplanationCollapse" class="status-explanation-collapse">
            <el-collapse-item title="点击查看状态变更说明" name="1">
              <div class="status-explanation-content">
                <div class="status-item">
                  <strong>待开始 (TODO):</strong> 任务尚未开始，进度为0%
                </div>
                <div class="status-item">
                  <strong>进行中 (IN_PROGRESS):</strong> 任务正在执行中，进度0-90%
                </div>
                <div class="status-item">
                  <strong>待审核 (REVIEW):</strong> 任务接近完成，进度90-99%
                </div>
                <div class="status-item">
                  <strong>已完成 (COMPLETED):</strong> 任务已完成，进度100%
                </div>
                <div class="status-item">
                  <strong>已暂停 (PAUSED):</strong> 任务暂时停止，保持当前进度
                </div>
                <div class="status-tip">
                  <el-icon><WarningFilled /></el-icon>
                  <span>状态变更后将保持不变，不会被系统自动覆盖</span>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- Excel导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="Excel导入任务"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".xlsx, .xls"
            drag
            style="margin-bottom: 20px"
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                仅支持.xlsx、.xls格式文件，建议先下载模板再填写数据
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="info" @click="downloadTemplate">
            <el-icon><Document /></el-icon>
            下载导入模板
          </el-button>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeImportDialog">取消</el-button>
        <el-button type="success" @click="importTasks" :loading="importLoading">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTaskList, createTask, updateTask, deleteTask as deleteTaskApi, getUserTasks, importTasks as importTasksApi, downloadTaskImportTemplate, getTaskChildren } from '@/api/task'
import { getProjectList, getProjectMembers } from '@/api/project'
import { getUserList } from '@/api/user'
import { EnhancedPermissionUtil } from '@/utils/enhancedPermissions'
import { InfoFilled, WarningFilled, Upload, Document, Plus, Download, Search, Edit, TrendCharts, Timer, Delete, ArrowDown, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const searchKeyword = ref('')
const searchProjectId = ref(null)
const tasks = ref([])
const projects = ref([])
const users = ref([])
const projectMembers = ref([])
const parentTasks = ref([])
const selectedTasks = ref([])
const currentTaskId = ref(null)
const currentProgress = ref(0)
const currentStatus = ref('')
const statusExplanationCollapse = ref([])
const activeTaskCollapse = ref([]) // 任务表单折叠面板控制
const childTasks = ref({}) // 存储子任务数据，键为父任务ID
const loadingChildTasks = ref({}) // 存储子任务加载状态

// 处理任务数据，只返回父任务
const processedTasks = computed(() => {
  if (!tasks.value || tasks.value.length === 0) return []
  
  // 只返回父任务
  return tasks.value.filter(task => !task.parentId)
})



// Excel导入相关变量
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([]) // 用于组件显示
const selectedFile = ref(null) // 用于保存实际选择的文件
const importLoading = ref(false)

// 权限相关
const canManageCurrentProjectTasks = computed(() => {
  if (!searchProjectId.value) return false
  if (userStore.user?.role === 'ADMIN' || userStore.user?.role === 'PROJECT_MANAGER') return true
  // 这里可以添加更复杂的项目级权限检查
  return true // 临时允许所有用户，实际应该检查项目权限
})

// 检查是否可以管理特定任务（编辑、更新进度等）
const canManageTask = (task) => {
  // 管理员可以管理所有任务
  if (userStore.user?.role === 'ADMIN') return true
  
  // 项目经理可以管理项目内的所有任务
  if (userStore.user?.role === 'PROJECT_MANAGER') return true
  
  // 任务执行人可以更新进度
  if (task.assigneeId === userStore.user?.id) return true
  
  return false
}

// 检查是否可以删除任务
const canDeleteTask = (task) => {
  // 只有管理员和项目经理可以删除任务
  return userStore.user?.role === 'ADMIN' || userStore.user?.role === 'PROJECT_MANAGER'
}

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 日期范围和计算相关
const dateRange = ref([])
const daysBetween = ref(0)
const estimatedHours = ref(0)
const calculatedDays = ref(0)

const form = reactive({
  id: null,
  taskName: '',
  description: '',
  projectId: null,
  parentId: null,
  taskType: 'DEVELOPMENT',
  priority: 3,
  difficulty: 3,
  estimatedHours: 0,
  assigneeId: null,
  reviewerId: null,
  startDate: '',
  endDate: '',
  // 业务价值与复杂度
  businessValue: 3,
  taskComplexity: 'MEDIUM',
  storyPoints: 0,
  technicalRisk: 'MEDIUM',
  // 工时规划
  baselineHours: 0,
  qualityScore: 85,
  acceptanceCriteria: ''
})

const formRef = ref()
const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ]
}

// 加载任务列表
const loadTasks = async () => {
  console.log('================== loadTasks函数被调用 ==================')
  console.log('当前时间:', new Date().toISOString())
  loading.value = true
  try {
    // 检查用户是否已登录
    console.log('当前用户:', userStore.user)
    console.log('当前token:', userStore.token)
    console.log('userStore.user?.id:', userStore.user?.id)
    if (!userStore.token || !userStore.user || !userStore.user.id) {
      console.log('用户未登录或token不存在')
      ElMessage.warning('请先登录')
      tasks.value = []
      pagination.total = 0
      console.log('loadTasks函数提前返回 - 用户未登录')
      return
    }
    console.log('用户已登录，继续加载任务列表')
    
    // 所有用户都能看到自己参与项目中的所有任务
    // 1. 检查URL参数
    const userOnlyFromUrl = route.query.userOnly === 'true'
    console.log('是否只看自己的任务:', userOnlyFromUrl)
    
    let response
    try {
      if (userOnlyFromUrl) {
        // 仅在URL参数指定时，才只看自己的任务
        console.log('调用getUserTasks API...')
        response = await getUserTasks(userStore.user.id, {
          current: pagination.current,
          size: pagination.size,
          projectId: searchProjectId.value,
          keyword: searchKeyword.value
        })
        console.log('getUserTasks API返回:', response)
      } else {
        // 调用普通任务列表API，显示自己参与项目中的所有任务
        console.log('调用getTaskList API...')
        response = await getTaskList({
          current: pagination.current,
          size: pagination.size,
          projectId: searchProjectId.value,
          keyword: searchKeyword.value
        })
        console.log('getTaskList API返回:', response)
      }
    } catch (apiError) {
      console.log('API调用失败:', apiError)
      ElMessage.error('API调用失败，请检查网络连接')
      tasks.value = []
      pagination.total = 0
      loading.value = false
      return
    }
    
    // 检查响应结构
    console.log('检查响应结构:', response)
    if (response) {
      // 处理任务数据
      console.log('响应成功，处理任务数据')
      console.log('loadTastk async:', response)
      
      // 检查response的结构，确保正确获取records和total
      let records = []
      let total = 0
      
      if (response.records) {
        // 直接从response获取records和total
        records = response.records || []
        total = response.total || 0
        console.log('从response直接获取数据:', { records, total })
      } else if (response.data && response.data.records) {
        // 从response.data获取records和total
        records = response.data.records || []
        total = response.data.total || 0
        console.log('从response.data获取数据:', { records, total })
      } else {
        // 其他情况
        console.log('响应数据结构不符合预期:', response)
      }
      
      console.log('处理后的数据:', {
        records: records,
        total: total,
        recordsLength: records.length
      })
      
      // 使用展开运算符创建新数组，确保Vue响应式系统检测到变化
      tasks.value = [...records]
      pagination.total = total
      console.log('tasks.value:', tasks.value)
      console.log('tasks.value.length:', tasks.value.length)
      console.log('pagination.total:', pagination.total)
    } else {
      console.log('响应失败:', response)
      ElMessage.error('加载任务列表失败')
      tasks.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.log('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
    tasks.value = []
    pagination.total = 0
  } finally {
    loading.value = false
    console.log('加载任务列表完成，当前tasks:', tasks.value)
  }
}





// 加载项目列表 - 只加载当前用户参与的项目
const loadProjects = async () => {
  try {
    // 使用getProjectList API，它会根据用户权限返回用户参与的项目
    const response = await getProjectList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      projects.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await getUserList({
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      users.value = response.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 计算两个日期之间的天数差
const calculateDaysBetween = (start, end) => {
  if (!start || !end) return 0
  
  const startDate = new Date(start)
  const endDate = new Date(end)
  
  // 计算毫秒差
  const timeDiff = endDate.getTime() - startDate.getTime()
  
  // 转换为天数（1天 = 86400000毫秒）
  const days = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1 // +1 包含开始和结束日期
  
  return Math.max(0, days)
}

// 处理日期范围变化
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    const [start, end] = value
    form.startDate = start
    form.endDate = end
    
    // 计算天数差
    daysBetween.value = calculateDaysBetween(start, end)
    
    // 计算预估工时（每天8小时）
    estimatedHours.value = daysBetween.value * 8
    
    // 自动填充到表单的预估工时字段
    form.estimatedHours = estimatedHours.value
    
    // 同步更新计算天数
    calculatedDays.value = daysBetween.value
  } else {
    // 重置计算结果
    daysBetween.value = 0
    estimatedHours.value = 0
    calculatedDays.value = 0
    form.startDate = ''
    form.endDate = ''
  }
}

// 处理预估工时变化
const handleEstimatedHoursChange = (value) => {
  if (value > 0) {
    // 计算对应的天数（按每天8小时计算，向上取整）
    calculatedDays.value = Math.ceil(value / 8)
  } else {
    calculatedDays.value = 0
  }
}

// 显示新建对话框
const showCreateDialog = async () => {
  dialogTitle.value = '新建任务'
  // 如果当前有选择的项目，自动填写到表单中
  if (searchProjectId.value) {
    form.projectId = searchProjectId.value
    // 加载项目成员数据
    await loadParentTasks()
  }
  
  // 设置执行人为当前登录用户
  if (userStore.user?.id) {
    form.assigneeId = userStore.user.id
  }
  
  dialogVisible.value = true
}

// 编辑任务
const editTask = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  
  // 设置日期范围
  if (row.startDate && row.endDate) {
    dateRange.value = [new Date(row.startDate), new Date(row.endDate)]
    // 重新计算天数差和预估工时
    daysBetween.value = calculateDaysBetween(row.startDate, row.endDate)
    estimatedHours.value = daysBetween.value * 8
  } else {
    dateRange.value = []
    daysBetween.value = 0
    estimatedHours.value = 0
  }
  
  // 计算天数
  if (row.estimatedHours > 0) {
    calculatedDays.value = Math.ceil(row.estimatedHours / 8)
  } else {
    calculatedDays.value = 0
  }
  
  // 加载父任务列表
  if (row.projectId) {
    loadParentTasks()
  }
  
  dialogVisible.value = true
}

// 查看任务详情
const viewTask = (row) => {
  console.log('查看任务:', row)
}

// 加载子任务
const loadChildTasks = async (parentId) => {
  if (loadingChildTasks.value[parentId]) return
  
  loadingChildTasks.value[parentId] = true
  try {
    const response = await getTaskChildren(parentId)
    if (response.code === 200) {
      childTasks.value[parentId] = response.data
    } else {
      childTasks.value[parentId] = []
    }
  } catch (error) {
    console.error('加载子任务失败:', error)
    childTasks.value[parentId] = []
  } finally {
    loadingChildTasks.value[parentId] = false
  }
}

// 获取子任务列表
const getChildTasks = (parentId) => {
  return childTasks.value[parentId] || []
}

// 更新进度
const updateProgress = (row) => {
  currentTaskId.value = row.id
  currentProgress.value = row.progress
  currentStatus.value = row.status
  progressDialogVisible.value = true
}

// 提交进度更新
const submitProgress = async () => {
  try {
    const response = await updateTask(currentTaskId.value, {
      progress: currentProgress.value,
      status: currentStatus.value
    })
    
    if (response.code === 200) {
      ElMessage.success('更新成功')
      progressDialogVisible.value = false
      loadTasks()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 查看工时记录
const viewTimeEntries = (row) => {
  router.push(`/time-entries?taskId=${row.id}`)
}

// 删除任务
const deleteTask = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务"${row.taskName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteTaskApi(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadTasks()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  // 检查子任务名称是否重复
  if (!form.id && form.parentId) {
    const isDuplicate = await checkTaskNameDuplicate(form.taskName, form.parentId)
    if (isDuplicate) {
      ElMessage.error('子任务名称已存在，请使用其他名称')
      return
    }
  }
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const response = form.id 
          ? await updateTask(form.id, form)
          : await createTask(form)
          
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '创建成功')
          
          // 如果是添加子任务，不关闭对话框，只清空任务名称和其他可编辑字段
          if (!form.id && form.parentId) {
            // 保留父任务信息，清空其他字段
            const parentId = form.parentId
            const projectId = form.projectId
            
            // 清空可编辑字段
            form.taskName = ''
            form.description = ''
            form.taskType = 'DEVELOPMENT'
            form.priority = 3
            form.difficulty = 3
            form.estimatedHours = 0
            form.assigneeId = userStore.user?.id || null
            form.startDate = ''
            form.endDate = ''
            form.businessValue = 3
            form.taskComplexity = 'MEDIUM'
            form.storyPoints = 0
            form.technicalRisk = 'MEDIUM'
            form.baselineHours = 0
            form.qualityScore = 85
            form.acceptanceCriteria = ''
            
            // 重置日期范围相关变量
            dateRange.value = []
            daysBetween.value = 0
            estimatedHours.value = 0
            calculatedDays.value = 0
            
            // 保留父任务和项目信息
            form.parentId = parentId
            form.projectId = projectId
          } else {
            // 其他情况关闭对话框
            dialogVisible.value = false
          }
          
          loadTasks()
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
  
  // 重置日期范围相关变量
  dateRange.value = []
  daysBetween.value = 0
  estimatedHours.value = 0
  calculatedDays.value = 0
  
  Object.assign(form, {
    id: null,
    taskName: '',
    description: '',
    projectId: null,
    parentId: null,
    taskType: 'DEVELOPMENT',
    priority: 3,
    difficulty: 3,
    estimatedHours: 0,
    assigneeId: null,
    startDate: '',
    endDate: '',
    // 业务价值与复杂度
    businessValue: 3,
    taskComplexity: 'MEDIUM',
    storyPoints: 0,
    technicalRisk: 'MEDIUM',
    // 工时规划
    baselineHours: 0,
    qualityTarget: 85,
    acceptanceCriteria: ''
  })
  
  // 重置父任务列表
  parentTasks.value = []
}

// 加载项目成员列表
const loadProjectMembers = async (projectId) => {
  if (!projectId) {
    projectMembers.value = []
    return
  }
  
  try {
    const response = await getProjectMembers(projectId)
    
    if (response.code === 200) {
      // 转换成员数据格式，确保userId和userRealName字段正确
      projectMembers.value = (response.data || []).map(member => ({
        userId: member.userId,
        userRealName: member.userRealName || member.userName,
        userRole: member.userRole,
        username: member.username,
        email: member.email,
        department: member.department,
        position: member.position
      }))
    }
  } catch (error) {
    console.error('加载项目成员失败:', error)
    projectMembers.value = []
  }
}

// 加载父任务列表
const loadParentTasks = async () => {
  if (!form.projectId) {
    parentTasks.value = []
    projectMembers.value = []
    return
  }
  
  try {
    console.log('加载项目成员:', form.projectId)
    // 同时加载父任务和项目成员
    const [taskResponse, memberResponse] = await Promise.all([
      getTaskList({
        projectId: form.projectId,
        current: 1,
        size: 100
      }),
      getProjectMembers(form.projectId)
    ])
    
    console.log('项目成员响应:', memberResponse)
    // 处理父任务数据
    if (taskResponse.code === 200) {
      // 只显示顶级任务（没有父任务的任务）作为父任务选项
      parentTasks.value = (taskResponse.records || []).filter(task => !task.parentId)
    }
    
    // 处理项目成员数据
            if (memberResponse.code === 200) {
              // 转换成员数据格式，确保userId和userRealName字段正确
              projectMembers.value = (memberResponse.data || []).map(member => ({
                userId: member.userId,
                userRealName: member.userRealName || member.userName,
                userRole: member.userRole,
                username: member.username,
                email: member.email,
                department: member.department,
                position: member.position
              }))
              console.log('项目成员数据:', projectMembers.value)
            } else {
              console.error('获取项目成员失败:', memberResponse.message)
            }
  } catch (error) {
    console.error('加载父任务和项目成员失败:', error)
    parentTasks.value = []
    projectMembers.value = []
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 处理展开/收起事件
const handleExpandChange = (row, expandedRows) => {
  if (expandedRows.includes(row)) {
    // 当行被展开时，加载子任务
    loadChildTasks(row.id)
  }
}

// 处理操作命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'edit':
      editTask(row)
      break
    case 'addChild':
      addChildTask(row)
      break
    case 'progress':
      updateProgress(row)
      break
    case 'timeEntries':
      viewTimeEntries(row)
      break
    case 'delete':
      deleteTask(row)
      break
  }
}

// 添加子任务
const addChildTask = (parentTask) => {
  dialogTitle.value = '添加子任务'
  resetForm()
  
  // 设置父任务和项目信息
  form.projectId = parentTask.projectId
  form.parentId = parentTask.id
  
  // 设置审核人为父任务执行人
  if (parentTask.assigneeId) {
    form.reviewerId = parentTask.assigneeId
  }
  
  // 展开质量与依赖关系部分
  activeTaskCollapse.value = ['quality']
  
  // 加载父任务列表（虽然是添加子任务，但需要确保项目信息正确）
  loadParentTasks()
  
  dialogVisible.value = true
}

// 获取任务类型颜色
const getTaskTypeColor = (type) => {
  const colorMap = {
    'DEVELOPMENT': 'primary',
    'TESTING': 'success',
    'DESIGN': 'warning',
    'DOCUMENT': 'info',
    'DELIVERY': 'danger',
    'REQUIREMENT': 'purple',
    'OTHER': 'default'
  }
  return colorMap[type] || 'info'
}

// 获取任务类型文本
const getTaskTypeText = (type) => {
  const textMap = {
    'DEVELOPMENT': '开发',
    'TESTING': '测试',
    'DESIGN': '设计',
    'DOCUMENT': '文档',
    'DELIVERY': '交付',
    'REQUIREMENT': '需求',
    'OTHER': '其他'
  }
  return textMap[type] || type
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'TODO': 'info',
    'IN_PROGRESS': 'warning',
    'REVIEW': 'primary',
    'COMPLETED': 'success',
    'PAUSED': 'danger',
    'CANCELLED': 'danger'
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'TODO': '待开始',
    'IN_PROGRESS': '进行中',
    'REVIEW': '待审核',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 根据父任务ID获取父任务名称
const getParentTaskName = (parentId) => {
  if (!parentId) return ''
  const parentTask = parentTasks.value.find(task => task.id === parentId)
  return parentTask ? parentTask.taskName : ''
}



// 检查子任务名称是否重复
const checkTaskNameDuplicate = async (taskName, parentId) => {
  if (!taskName || !parentId) return false
  
  try {
    // 加载任务列表，检查是否存在相同名称的子任务
    const response = await getTaskList({
      projectId: form.projectId,
      current: 1,
      size: 100
    })
    
    if (response.code === 200) {
      // 检查是否存在同一父任务下名称相同的子任务
      return response.records.some(task => 
        task.parentId === parentId && 
        task.taskName === taskName
      )
    }
    return false
  } catch (error) {
    console.error('检查任务名称重复失败:', error)
    return false
  }
}

// Excel导入相关方法
const showImportDialog = () => {
  importDialogVisible.value = true
}

const closeImportDialog = () => {
  importDialogVisible.value = false
  fileList.value = [] // 清空文件列表
  selectedFile.value = null // 清空选中的文件
}

const handleFileChange = (file, fileList) => {
  console.log('File change event:', file, fileList)
  // 保存当前选择的文件
  selectedFile.value = file.raw
  // 更新fileList用于组件显示
  fileList.value = fileList
  console.log('Selected file:', selectedFile.value)
  console.log('Updated fileList:', fileList.value)
}

const importTasks = async () => {
  console.log('Import tasks called, selectedFile:', selectedFile.value)
  
  // 确保selectedFile不为空
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的Excel文件')
    return
  }
  
  importLoading.value = true
  try {
    const file = selectedFile.value
    console.log('Uploading file:', file.name, file.size)
    const response = await importTasksApi(file)
    
    if (response.code === 200) {
      ElMessage.success(`导入成功，共导入${response.data.successCount || 0}条任务，失败${response.data.failureCount || 0}条`)
      closeImportDialog()
      loadTasks() // 重新加载任务列表
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入任务失败:', error)
    ElMessage.error('导入失败，请检查文件格式是否正确')
  } finally {
    importLoading.value = false
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadTaskImportTemplate()
    
    // 创建下载链接并触发下载
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '任务导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('模板下载失败')
  }
}

onMounted(() => {
  // 确保用户信息已初始化
  userStore.initUser()
  console.log('用户信息初始化后:', userStore.user)
  
  // 如果URL中有projectId参数，设置默认项目
  if (route.query.projectId) {
    searchProjectId.value = parseInt(route.query.projectId)
  }
  
  loadTasks()
  loadProjects()
  loadUsers()
})
</script>

<style scoped>
.tasks {
  padding: 0;
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

.pagination {
  margin-top: 20px;
  text-align: right;
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

/* 延期状态样式 */
.delay-text {
  color: #F56C6C;
  font-weight: bold;
}

.normal-text {
  color: #67C23A;
}

/* 字段提示文本样式 */
.field-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 预估工时提示样式 */
.estimate-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #67C23A;
  line-height: 1.4;
}

/* 状态变更说明折叠面板样式 */
.status-explanation-collapse {
  width: 100%;
}

.status-explanation-content {
  font-size: 13px;
  line-height: 1.6;
  padding: 10px 0;
}

.status-item {
  margin-bottom: 8px;
}

.status-tip {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #E4E7ED;
  display: flex;
  align-items: center;
  color: #E6A23C;
  font-size: 12px;
}

.status-tip .el-icon {
  margin-right: 6px;
}

/* 任务名称样式 */
.task-name-wrapper {
  display: flex;
  align-items: center;
}

.task-name-wrapper .el-link {
  font-size: 14px;
  font-weight: 500;
}

/* 父任务标签样式 */
.parent-task-label {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

/* 子任务样式 */
.child-task {
  font-size: 13px;
  color: #606266;
  margin-left: 20px;
}

/* 子任务标签样式 */
.child-task.el-tag {
  font-size: 11px;
  padding: 2px 6px;
}

/* 子任务进度条样式 */
.child-task .el-progress {
  font-size: 11px;
}

/* 表格行样式 */
.el-table__row {
  transition: all 0.3s ease;
  background-color: #f0f9ff;
}

/* 无内容提示样式 */
.no-child-tasks {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

/* 子任务列表样式 */
.child-tasks-list {
  margin-top: 10px;
  margin-left: 30px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  background-color: #fafafa;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: calc(100% - 30px);
}

/* 子任务名称样式 */
.child-task {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 子任务列表头部 */
.child-tasks-header {
  padding: 10px 15px;
  background-color: #f0f9ff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.child-tasks-title {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.child-tasks-count {
  font-size: 12px;
  color: #909399;
}

/* 确保子表格样式正确 */
.child-tasks-list .el-table {
  margin-bottom: 0;
  border: none;
}

.child-tasks-list .el-table__header-wrapper {
  background-color: #f8f9fa;
}

.child-tasks-list .el-table__row {
  background-color: #ffffff;
}

.child-tasks-list .el-table__row:hover {
  background-color: #f5f7fa;
}

/* 子表格单元格样式 */
.child-tasks-list .el-table__cell {
  padding: 8px 12px;
  font-size: 13px;
}

/* 子表格表头样式 */
.child-tasks-list .el-table__header th {
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 500;
  background-color: #f8f9fa;
  color: #606266;
}

/* 确保子表格边框样式正确 */
.child-tasks-list .el-table--border {
  border: none;
}

.child-tasks-list .el-table--border th,
.child-tasks-list .el-table--border td {
  border-right: 1px solid #ebeef5;
}

.child-tasks-list .el-table--border tr {
  border-bottom: 1px solid #ebeef5;
}

</style>