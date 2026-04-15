<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="报告编号" prop="reportNo">
        <el-input v-model="queryParams.reportNo" placeholder="请输入报告编号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="加工批次号" prop="processBatchNo">
        <el-input v-model="queryParams.processBatchNo" placeholder="请输入加工批次号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="报告状态" prop="reportStatus">
        <el-select v-model="queryParams.reportStatus" clearable style="width: 140px">
          <el-option v-for="item in reportStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:qualityReport:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-document-add" size="mini" :disabled="single" @click="handleGenerate" v-hasPermi="['agri:qualityReport:add']">生成报告</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:qualityReport:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:qualityReport:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="reportId" align="center" width="70" />
      <el-table-column label="报告编号" prop="reportNo" align="center" width="180" />
      <el-table-column label="检测任务ID" prop="inspectId" align="center" width="100" />
      <el-table-column label="加工批次号" prop="processBatchNo" align="center" width="140" />
      <el-table-column label="品质等级" prop="qualityGrade" align="center" width="90" />
      <el-table-column label="缺陷率" prop="defectRate" align="center" width="90" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">{{ formatReportStatus(scope.row.reportStatus) }}</template>
      </el-table-column>
      <el-table-column label="报告时间" prop="reportTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
      </el-table-column>
      <el-table-column label="摘要" prop="reportSummary" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:qualityReport:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:qualityReport:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="报告编号" prop="reportNo"><el-input v-model="form.reportNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检测任务ID" prop="inspectId"><el-input-number v-model="form.inspectId" :min="1" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="加工批次号" prop="processBatchNo"><el-input v-model="form.processBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="reportStatus"><el-select v-model="form.reportStatus" style="width: 100%"><el-option v-for="item in reportStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="品质等级" prop="qualityGrade"><el-input v-model="form.qualityGrade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="缺陷率" prop="defectRate"><el-input-number v-model="form.defectRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="报告时间" prop="reportTime"><el-date-picker v-model="form.reportTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="摘要" prop="reportSummary"><el-input v-model="form.reportSummary" type="textarea" :rows="3" /></el-form-item>
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
  listQualityReport,
  getQualityReport,
  addQualityReport,
  updateQualityReport,
  generateQualityReport,
  delQualityReport
} from '@/api/agri/qualityReport'

export default {
  name: 'QualityReport',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      reportList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        reportNo: undefined,
        processBatchNo: undefined,
        reportStatus: undefined
      },
      form: {},
      reportStatusOptions: [
        { label: '草稿', value: '0' },
        { label: '已生成', value: '1' }
      ],
      rules: {
        reportNo: [{ required: true, message: '报告编号不能为空', trigger: 'blur' }],
        inspectId: [{ required: true, message: '检测任务ID不能为空', trigger: 'change' }],
        processBatchNo: [{ required: true, message: '加工批次号不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQualityReport(this.queryParams).then(response => {
        this.reportList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatReportStatus(value) {
      const option = this.reportStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        reportId: undefined,
        reportNo: undefined,
        inspectId: undefined,
        processBatchNo: undefined,
        qualityGrade: undefined,
        defectRate: undefined,
        reportSummary: undefined,
        reportStatus: '0',
        reportTime: undefined,
        status: '0',
        remark: undefined
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
      this.ids = selection.map(item => item.reportId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增质检报告'
    },
    handleUpdate(row) {
      this.reset()
      const reportId = row.reportId || this.ids
      getQualityReport(reportId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改质检报告'
      })
    },
    handleGenerate() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条报告记录用于读取检测任务ID')
        return
      }
      const selected = this.reportList.find(item => item.reportId === this.ids[0])
      if (!selected || !selected.inspectId) {
        this.$modal.msgWarning('所选记录缺少检测任务ID')
        return
      }
      generateQualityReport(selected.inspectId).then(() => {
        this.$modal.msgSuccess('报告生成成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.reportId !== undefined) {
          updateQualityReport(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addQualityReport(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const reportIds = row.reportId || this.ids
      this.$modal
        .confirm('是否确认删除质检报告编号为"' + reportIds + '"的数据项？')
        .then(function () {
          return delQualityReport(reportIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/qualityReport/export',
        {
          ...this.queryParams
        },
        `quality_report_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
