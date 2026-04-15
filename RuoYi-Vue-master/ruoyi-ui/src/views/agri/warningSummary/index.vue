<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="开始日期" prop="beginSummaryDate">
        <el-date-picker clearable v-model="queryParams.beginSummaryDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endSummaryDate">
        <el-date-picker clearable v-model="queryParams.endSummaryDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:warningSummary:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:warningSummary:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:warningSummary:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:warningSummary:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="summaryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="summaryId" width="80" />
      <el-table-column label="统计日期" align="center" prop="summaryDate" width="110" />
      <el-table-column label="预警总数" align="center" prop="totalWarningCount" width="90" />
      <el-table-column label="提示级" align="center" prop="level1Count" width="80" />
      <el-table-column label="预警级" align="center" prop="level2Count" width="80" />
      <el-table-column label="严重级" align="center" prop="level3Count" width="80" />
      <el-table-column label="已处理" align="center" prop="handledCount" width="80" />
      <el-table-column label="待处理" align="center" prop="pendingCount" width="80" />
      <el-table-column label="平均处理(分)" align="center" prop="avgHandleMinutes" width="110" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:warningSummary:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:warningSummary:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="116px">
        <el-row>
          <el-col :span="12"><el-form-item label="统计日期" prop="summaryDate"><el-date-picker clearable v-model="form.summaryDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择统计日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预警总数" prop="totalWarningCount"><el-input-number v-model="form.totalWarningCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="提示级" prop="level1Count"><el-input-number v-model="form.level1Count" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="预警级" prop="level2Count"><el-input-number v-model="form.level2Count" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="严重级" prop="level3Count"><el-input-number v-model="form.level3Count" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="已处理" prop="handledCount"><el-input-number v-model="form.handledCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="待处理" prop="pendingCount"><el-input-number v-model="form.pendingCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="平均处理时长" prop="avgHandleMinutes"><el-input-number v-model="form.avgHandleMinutes" :min="0" style="width: 100%" /></el-form-item></el-col>
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
  listWarningSummary,
  getWarningSummary,
  delWarningSummary,
  addWarningSummary,
  updateWarningSummary
} from '@/api/agri/warningSummary'

export default {
  name: 'WarningSummary',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      summaryList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        beginSummaryDate: null,
        endSummaryDate: null
      },
      form: {},
      rules: {
        summaryDate: [{ required: true, message: '统计日期不能为空', trigger: 'change' }],
        totalWarningCount: [{ required: true, message: '预警总数不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listWarningSummary(this.queryParams).then(response => {
        this.summaryList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        summaryId: null,
        summaryDate: null,
        totalWarningCount: 0,
        level1Count: 0,
        level2Count: 0,
        level3Count: 0,
        handledCount: 0,
        pendingCount: 0,
        avgHandleMinutes: 0,
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
      this.ids = selection.map(item => item.summaryId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增预警信息汇总'
    },
    handleUpdate(row) {
      this.reset()
      const summaryId = row.summaryId || this.ids[0]
      getWarningSummary(summaryId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改预警信息汇总'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.summaryId != null) {
            updateWarningSummary(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addWarningSummary(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const summaryIds = row.summaryId || this.ids
      this.$modal.confirm('是否确认删除预警信息汇总编号为"' + summaryIds + '"的数据项？').then(() => {
        return delWarningSummary(summaryIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/warningSummary/export', {
        ...this.queryParams
      }, `warning_summary_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
