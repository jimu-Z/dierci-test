<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="开始日期" prop="beginStatDate">
        <el-date-picker clearable v-model="queryParams.beginStatDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endStatDate">
        <el-date-picker clearable v-model="queryParams.endStatDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:traceQueryStats:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:traceQueryStats:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:traceQueryStats:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:traceQueryStats:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="statsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="statsId" width="80" />
      <el-table-column label="统计日期" align="center" prop="statDate" width="110" />
      <el-table-column label="查询总次数" align="center" prop="totalQueryCount" width="100" />
      <el-table-column label="独立用户数" align="center" prop="uniqueUserCount" width="100" />
      <el-table-column label="平均响应(ms)" align="center" prop="avgDurationMs" width="110" />
      <el-table-column label="成功次数" align="center" prop="successCount" width="90" />
      <el-table-column label="失败次数" align="center" prop="failCount" width="90" />
      <el-table-column label="峰值QPS" align="center" prop="peakQps" width="90" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:traceQueryStats:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:traceQueryStats:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="116px">
        <el-row>
          <el-col :span="12"><el-form-item label="统计日期" prop="statDate"><el-date-picker clearable v-model="form.statDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择统计日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="查询总次数" prop="totalQueryCount"><el-input-number v-model="form.totalQueryCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="独立用户数" prop="uniqueUserCount"><el-input-number v-model="form.uniqueUserCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="平均响应(ms)" prop="avgDurationMs"><el-input-number v-model="form.avgDurationMs" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="成功次数" prop="successCount"><el-input-number v-model="form.successCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="失败次数" prop="failCount"><el-input-number v-model="form.failCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="峰值QPS" prop="peakQps"><el-input-number v-model="form.peakQps" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
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
  listTraceQueryStats,
  getTraceQueryStats,
  delTraceQueryStats,
  addTraceQueryStats,
  updateTraceQueryStats
} from '@/api/agri/traceQueryStats'

export default {
  name: 'TraceQueryStats',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      statsList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        beginStatDate: null,
        endStatDate: null
      },
      form: {},
      rules: {
        statDate: [{ required: true, message: '统计日期不能为空', trigger: 'change' }],
        totalQueryCount: [{ required: true, message: '查询总次数不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTraceQueryStats(this.queryParams).then(response => {
        this.statsList = response.rows
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
        statsId: null,
        statDate: null,
        totalQueryCount: 0,
        uniqueUserCount: 0,
        avgDurationMs: 0,
        successCount: 0,
        failCount: 0,
        peakQps: 0,
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
      this.ids = selection.map(item => item.statsId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增溯源查询统计'
    },
    handleUpdate(row) {
      this.reset()
      const statsId = row.statsId || this.ids[0]
      getTraceQueryStats(statsId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改溯源查询统计'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.statsId != null) {
            updateTraceQueryStats(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addTraceQueryStats(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const statsIds = row.statsId || this.ids
      this.$modal.confirm('是否确认删除溯源查询统计编号为"' + statsIds + '"的数据项？').then(() => {
        return delTraceQueryStats(statsIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/traceQueryStats/export', {
        ...this.queryParams
      }, `trace_query_stats_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
