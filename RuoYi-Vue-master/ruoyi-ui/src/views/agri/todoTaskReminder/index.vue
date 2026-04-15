<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:todoTaskReminder:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:todoTaskReminder:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:todoTaskReminder:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:todoTaskReminder:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reminderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="reminderId" width="80" />
      <el-table-column label="任务编码" align="center" prop="taskCode" width="140" />
      <el-table-column label="任务标题" align="center" prop="taskTitle" min-width="160" />
      <el-table-column label="优先级" align="center" prop="priorityLevel" width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="priorityType(scope.row.priorityLevel)">{{ priorityLabel(scope.row.priorityLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="assignee" width="100" />
      <el-table-column label="截止时间" align="center" prop="deadlineTime" width="170" />
      <el-table-column label="提醒状态" align="center" prop="reminderStatus" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusType(scope.row.reminderStatus)">{{ statusLabel(scope.row.reminderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:todoTaskReminder:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:todoTaskReminder:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

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
  updateTodoTaskReminder
} from '@/api/agri/todoTaskReminder'

export default {
  name: 'TodoTaskReminder',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      reminderList: [],
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
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTodoTaskReminder(this.queryParams).then(response => {
        this.reminderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    priorityLabel(level) {
      const map = { '1': '低', '2': '中', '3': '高', '4': '紧急' }
      return map[level] || '未知'
    },
    priorityType(level) {
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
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.reminderId)
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
      const reminderId = row.reminderId || this.ids[0]
      getTodoTaskReminder(reminderId).then(response => {
        this.form = response.data
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
            })
          } else {
            addTodoTaskReminder(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const reminderIds = row.reminderId || this.ids
      this.$modal.confirm('是否确认删除任务待办提醒编号为"' + reminderIds + '"的数据项？').then(() => {
        return delTodoTaskReminder(reminderIds)
      }).then(() => {
        this.getList()
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
