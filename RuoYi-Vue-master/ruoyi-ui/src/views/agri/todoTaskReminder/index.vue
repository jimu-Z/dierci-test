<template>
  <div class="app-container todo-dispatch">
    <el-card shadow="never" class="hero-card mb16">
      <div class="hero-wrap" v-loading="dashboardLoading">
        <div>
          <div class="hero-title">待办任务调度台</div>
          <div class="hero-desc">围绕待提醒、已提醒、超期与紧急任务进行分级处置，减少遗漏和超时。</div>
          <div class="hero-meta">
            <span>任务总量 {{ total }}</span>
            <span>最近刷新 {{ lastRefreshLabel }}</span>
            <span>模型 {{ insight.modelVersion || 'todo-dispatch-v1' }}</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="loadDashboard">刷新看板</el-button>
          <el-button type="warning" plain size="mini" icon="el-icon-bell" :disabled="!selectedReminderId" :loading="dispatchLoading" @click="handleSmartDispatch()">智能处置</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12" class="mb16">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="12" :sm="12" :md="4">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-foot">{{ item.foot }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb16">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>调度建议</span>
            <span class="panel-subtitle">依据截止时间与优先级自动生成</span>
          </div>
          <div class="insight-box">
            <div class="insight-summary">{{ insight.summary || '暂无调度建议，刷新看板后查看系统评估。' }}</div>
            <div class="insight-meta">
              <el-tag size="mini" :type="dispatchTagType(insight.dispatchLevel)">{{ insight.dispatchLevel || '稳定' }}</el-tag>
              <span>置信度 {{ insight.confidenceRate || '58%' }}</span>
              <span>模型 {{ insight.modelVersion || 'todo-dispatch-v1' }}</span>
            </div>
            <div class="recommendation-list" v-if="recommendations.length">
              <div v-for="(item, index) in recommendations" :key="index" class="recommendation-item">{{ item }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>优先处置队列</span>
            <span class="panel-subtitle">点击后可执行单条智能处置</span>
          </div>
          <el-empty v-if="!topList.length" description="暂无待办记录" />
          <div v-else class="dispatch-list">
            <div v-for="item in topList" :key="item.reminderId" class="dispatch-item" :class="{ active: selectedReminderId === item.reminderId }" @click="selectRow(item)">
              <div class="dispatch-head">
                <div>
                  <div class="dispatch-title">{{ item.taskTitle }}</div>
                  <div class="dispatch-sub">{{ item.taskCode }} · {{ item.assignee || '未分配' }}</div>
                </div>
                <el-tag size="mini" :type="priorityTagType(item.priorityLevel)">{{ priorityLabel(item.priorityLevel) }}</el-tag>
              </div>
              <div class="dispatch-desc">截止 {{ item.deadlineTime || '未填写' }} · 状态 {{ statusLabel(item.reminderStatus) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:todoTaskReminder:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:todoTaskReminder:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:todoTaskReminder:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:todoTaskReminder:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px" class="mb16">
      <el-form-item label="任务编码" prop="taskCode">
        <el-input v-model="queryParams.taskCode" placeholder="请输入任务编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="任务标题" prop="taskTitle">
        <el-input v-model="queryParams.taskTitle" placeholder="请输入任务标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="优先级" prop="priorityLevel">
        <el-select v-model="queryParams.priorityLevel" placeholder="优先级" clearable>
          <el-option label="低" value="1" />
          <el-option label="中" value="2" />
          <el-option label="高" value="3" />
          <el-option label="紧急" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="提醒状态" prop="reminderStatus">
        <el-select v-model="queryParams.reminderStatus" placeholder="提醒状态" clearable>
          <el-option label="待提醒" value="0" />
          <el-option label="已提醒" value="1" />
          <el-option label="已完成" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="reminderList" @selection-change="handleSelectionChange" @row-click="selectRow">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编码" align="center" prop="taskCode" width="140" />
      <el-table-column label="任务标题" align="center" prop="taskTitle" min-width="160" />
      <el-table-column label="任务类型" align="center" prop="taskType" width="120" />
      <el-table-column label="优先级" align="center" prop="priorityLevel" width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="priorityTagType(scope.row.priorityLevel)">{{ priorityLabel(scope.row.priorityLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="assignee" width="100" />
      <el-table-column label="截止时间" align="center" prop="deadlineTime" width="170" />
      <el-table-column label="提醒状态" align="center" prop="reminderStatus" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusType(scope.row.reminderStatus)">{{ statusLabel(scope.row.reminderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-bell" @click.stop="handleSmartDispatch(scope.row)">处置</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:todoTaskReminder:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:todoTaskReminder:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="任务编码" prop="taskCode"><el-input v-model="form.taskCode" placeholder="请输入任务编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="任务标题" prop="taskTitle"><el-input v-model="form.taskTitle" placeholder="请输入任务标题" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="任务类型" prop="taskType"><el-input v-model="form.taskType" placeholder="请输入任务类型" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级" prop="priorityLevel"><el-select v-model="form.priorityLevel" placeholder="请选择优先级" style="width: 100%"><el-option label="低" value="1" /><el-option label="中" value="2" /><el-option label="高" value="3" /><el-option label="紧急" value="4" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="处理人" prop="assignee"><el-input v-model="form.assignee" placeholder="请输入处理人" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="截止时间" prop="deadlineTime"><el-date-picker clearable v-model="form.deadlineTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择截止时间" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="提醒状态" prop="reminderStatus"><el-select v-model="form.reminderStatus" placeholder="请选择提醒状态" style="width: 100%"><el-option label="待提醒" value="0" /><el-option label="已提醒" value="1" /><el-option label="已完成" value="2" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listTodoTaskReminder,
  getTodoTaskReminder,
  delTodoTaskReminder,
  addTodoTaskReminder,
  updateTodoTaskReminder,
  getTodoTaskReminderDashboard,
  smartDispatchTodoTaskReminder
} from '@/api/agri/todoTaskReminder'

export default {
  name: 'TodoTaskReminder',
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      dispatchLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      reminderList: [],
      dashboardSnapshot: null,
      selectedReminder: null,
      lastRefreshTime: null,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskCode: null,
        taskTitle: null,
        priorityLevel: null,
        reminderStatus: null
      },
      form: {},
      rules: {
        taskCode: [{ required: true, message: '任务编码不能为空', trigger: 'blur' }],
        taskTitle: [{ required: true, message: '任务标题不能为空', trigger: 'blur' }],
        deadlineTime: [{ required: true, message: '截止时间不能为空', trigger: 'change' }]
      }
    }
  },
  computed: {
    summaryCards() {
      const kpi = (this.dashboardSnapshot && this.dashboardSnapshot.kpi) || {}
      return [
        { key: 'total', label: '任务总量', value: kpi.total || 0, foot: '全部待办记录' },
        { key: 'pending', label: '待提醒', value: kpi.pendingCount || 0, foot: '待发送提醒任务' },
        { key: 'overdue', label: '超期', value: kpi.overdueCount || 0, foot: '已超出截止时间' },
        { key: 'urgent', label: '紧急', value: kpi.urgentCount || 0, foot: '高优先级任务' },
        { key: 'reminded', label: '已提醒', value: kpi.remindedCount || 0, foot: '已发出提醒' },
        { key: 'completed', label: '已完成', value: kpi.completedCount || 0, foot: '已闭环任务' }
      ]
    },
    insight() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.insight) || {}
    },
    recommendations() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.recommendations) || []
    },
    topList() {
      return (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.topList)) ? this.dashboardSnapshot.topList : []
    },
    lastRefreshLabel() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '暂无'
    },
    selectedReminderId() {
      return this.selectedReminder ? this.selectedReminder.reminderId : null
    }
  },
  created() {
    this.getList()
    this.loadDashboard()
  },
  methods: {
    getList() {
      this.loading = true
      listTodoTaskReminder(this.queryParams).then(response => {
        this.reminderList = response.rows || []
        this.total = response.total || 0
        this.lastRefreshTime = new Date()
        if (!this.selectedReminder && this.reminderList.length) {
          this.selectedReminder = this.reminderList[0]
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadDashboard() {
      this.dashboardLoading = true
      getTodoTaskReminderDashboard().then(response => {
        this.dashboardSnapshot = response.data || null
        this.lastRefreshTime = new Date()
        if (!this.selectedReminder && this.topList.length) {
          this.selectedReminder = this.topList[0]
        }
      }).finally(() => {
        this.dashboardLoading = false
      })
    },
    selectRow(row) {
      if (row) {
        this.selectedReminder = row
      }
    },
    handleSmartDispatch(row) {
      const reminderId = row && row.reminderId ? row.reminderId : this.selectedReminderId
      if (!reminderId) {
        this.$modal.msgWarning('请先选择一条待办记录')
        return
      }
      this.dispatchLoading = true
      smartDispatchTodoTaskReminder(reminderId).then(response => {
        const data = response.data || {}
        const nextStatus = data.reminderStatus || '1'
        this.dashboardSnapshot = this.dashboardSnapshot || {}
        this.dashboardSnapshot.insight = data
        this.reminderList = this.reminderList.map(item => {
          if (item.reminderId === reminderId) {
            return Object.assign({}, item, { reminderStatus: nextStatus })
          }
          return item
        })
        this.topList.forEach(item => {
          if (item.reminderId === reminderId) {
            item.reminderStatus = nextStatus
          }
        })
        if (this.selectedReminder && this.selectedReminder.reminderId === reminderId) {
          this.selectedReminder = Object.assign({}, this.selectedReminder, { reminderStatus: nextStatus })
        }
        this.getList()
        this.loadDashboard()
        this.$modal.msgSuccess(nextStatus === '2' ? '智能处置完成，提醒已完成' : '智能处置完成，提醒状态已更新为已提醒')
      }).catch(error => {
        const message = (error && error.msg) || '智能处置失败，请稍后重试'
        this.$modal.msgError(message)
      }).finally(() => {
        this.dispatchLoading = false
      })
    },
    priorityLabel(level) {
      const map = { '1': '低', '2': '中', '3': '高', '4': '紧急' }
      return map[level] || '未知'
    },
    priorityTagType(level) {
      const map = { '1': 'info', '2': '', '3': 'warning', '4': 'danger' }
      return map[level] || 'info'
    },
    statusLabel(status) {
      const map = { '0': '待提醒', '1': '已提醒', '2': '已完成' }
      return map[status] || '未知'
    },
    statusType(status) {
      const map = { '0': 'info', '1': 'warning', '2': 'success' }
      return map[status] || 'info'
    },
    dispatchTagType(level) {
      if (level === '紧急') {
        return 'danger'
      }
      if (level === '关注') {
        return 'warning'
      }
      return 'success'
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        reminderId: null,
        taskCode: null,
        taskTitle: null,
        taskType: null,
        priorityLevel: '2',
        assignee: null,
        deadlineTime: null,
        reminderStatus: '0',
        remark: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      this.loadDashboard()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.reminderId)
      if (selection.length === 1) {
        this.selectedReminder = selection[0]
      }
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增任务待办提醒'
    },
    handleUpdate(row) {
      this.reset()
      const reminderId = row && row.reminderId ? row.reminderId : this.ids[0]
      if (!reminderId) {
        return
      }
      getTodoTaskReminder(reminderId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '修改任务待办提醒'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.reminderId != null) {
            updateTodoTaskReminder(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.loadDashboard()
            })
          } else {
            addTodoTaskReminder(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.loadDashboard()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const reminderIds = row && row.reminderId ? row.reminderId : this.ids
      this.$modal.confirm('是否确认删除任务待办提醒编号为"' + reminderIds + '"的数据项？').then(() => {
        return delTodoTaskReminder(reminderIds)
      }).then(() => {
        this.getList()
        this.loadDashboard()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/todoTaskReminder/export', {
        ...this.queryParams
      }, `todo_task_reminder_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.todo-dispatch {
  background: linear-gradient(180deg, rgba(241, 247, 252, 0.7), rgba(255, 255, 255, 1));
}

.hero-card,
.panel-card,
.kpi-card {
  border-radius: 14px;
}

.hero-wrap {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
}

.hero-desc {
  margin-top: 8px;
  color: #5a6a7a;
}

.hero-meta,
.insight-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #6b7785;
  font-size: 12px;
}

.kpi-card {
  min-height: 92px;
}

.kpi-label {
  color: #768191;
  font-size: 13px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #18222d;
}

.kpi-foot {
  margin-top: 6px;
  color: #97a2ad;
  font-size: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
}

.panel-subtitle {
  color: #8a97a4;
  font-size: 12px;
  font-weight: 400;
}

.insight-summary,
.dispatch-desc {
  color: #41505f;
  line-height: 1.8;
}

.dispatch-list {
  display: grid;
  gap: 10px;
}

.dispatch-item {
  padding: 12px;
  border: 1px solid #e8edf3;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dispatch-item.active,
.dispatch-item:hover {
  border-color: #5a88ff;
  box-shadow: 0 8px 18px rgba(90, 136, 255, 0.12);
}

.dispatch-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.dispatch-title {
  font-weight: 600;
  color: #1f2d3d;
}

.dispatch-sub {
  margin-top: 4px;
  color: #7d8894;
  font-size: 12px;
}

.recommendation-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.recommendation-item {
  padding: 10px 12px;
  background: #f6f8fb;
  border-radius: 10px;
  color: #334155;
}
</style>
