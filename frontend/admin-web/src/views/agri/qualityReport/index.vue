<template>
  <div class="app-container">
    <el-card shadow="never" class="report-hero mb16">
      <div class="report-hero-head">
        <div>
          <div class="report-title">质检报告审阅台</div>
          <div class="report-desc">围绕报告生成、缺陷率、等级与归档状态做人工复核，优先处理高风险报告。</div>
        </div>
        <div class="report-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-view" @click="handleReview">智能审阅</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="report-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="report-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="report-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="report-panel">
            <div class="report-panel-head"><span>高风险报告</span><small>缺陷率偏高或待生成项优先处理</small></div>
            <div class="report-risk-list" v-if="highRiskRows.length">
              <div v-for="item in highRiskRows" :key="item.reportId" class="report-risk-item" @click="handleReview(item)">
                <div>
                  <b>{{ item.reportNo }}</b>
                  <p>{{ item.processBatchNo || '-' }} · {{ item.qualityGrade || '未分级' }}</p>
                </div>
                <el-tag size="mini" type="danger">{{ item.defectRateText || formatRate(item.defectRate) }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无高风险报告" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="report-panel" v-loading="reviewLoading">
            <div class="report-panel-head"><span>智能审阅</span><small>根据当前报告生成复核意见</small></div>
            <div v-if="smartResult.reportId" class="report-review-box">
              <div class="report-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
              <p>{{ (smartResult.reportView && smartResult.reportView.reportNo) || (smartResult.report && smartResult.report.reportNo) }}</p>
              <div v-if="smartResult.reportView" class="report-review-meta">
                <span>批次：{{ smartResult.reportView.processBatchNo || '-' }}</span>
                <span>等级：{{ smartResult.reportView.qualityGrade || '未分级' }}</span>
                <span>状态：{{ smartResult.reportView.reportStatusLabel || formatReportStatus(smartResult.reportView.reportStatus) }}</span>
                <span>缺陷率：{{ smartResult.reportView.defectRateText || formatRate(smartResult.reportView.defectRate) }}</span>
              </div>
              <ul><li v-for="item in smartResult.findings" :key="item">{{ item }}</li></ul>
              <div v-if="smartResult.aiOriginalExcerpt" class="report-excerpt">
                <span>AI原文摘录</span>
                <p>{{ smartResult.aiOriginalExcerpt }}</p>
              </div>
            </div>
            <el-empty v-else description="请选择一条报告进行审阅" />
          </div>
          <div class="report-panel report-recent-panel">
            <div class="report-panel-head"><span>最新报告</span><small>按报告时间倒序展示最近记录</small></div>
            <div class="report-risk-list" v-if="recentRows.length">
              <div v-for="item in recentRows" :key="item.reportId" class="report-risk-item" @click="handleReview(item)">
                <div>
                  <b>{{ item.reportNo }}</b>
                  <p>{{ item.processBatchNo || '-' }} · {{ item.qualityGrade || '未分级' }}</p>
                </div>
                <div class="report-risk-tags">
                  <el-tag size="mini" :type="item.reportStatus === '1' ? 'success' : 'info'">{{ item.reportStatusLabel || formatReportStatus(item.reportStatus) }}</el-tag>
                  <el-tag size="mini" type="warning">{{ item.defectRateText || formatRate(item.defectRate) }}</el-tag>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无最新报告" />
          </div>
        </el-col>
      </el-row>
    </el-card>

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

    <el-table
      ref="reportTable"
      v-loading="loading"
      :data="reportList"
      highlight-current-row
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="reportId" align="center" width="70" />
      <el-table-column label="报告编号" prop="reportNo" align="center" width="180" />
      <el-table-column label="检测任务ID" prop="inspectId" align="center" width="100" />
      <el-table-column label="加工批次号" prop="processBatchNo" align="center" width="140" />
      <el-table-column label="品质等级" prop="qualityGrade" align="center" width="90" />
      <el-table-column label="缺陷率" align="center" width="90">
        <template slot-scope="scope">{{ formatRate(scope.row.defectRate) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">{{ formatReportStatus(scope.row.reportStatus) }}</template>
      </el-table-column>
      <el-table-column label="报告时间" prop="reportTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
      </el-table-column>
      <el-table-column label="摘要" prop="reportSummary" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleReview(scope.row)" v-hasPermi="['agri:qualityReport:query']">审阅</el-button>
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
  delQualityReport,
  getQualityReportDashboard,
  getQualityReportReview
} from '@/api/agri/qualityReport'

export default {
  name: 'QualityReport',
  data() {
    return {
      loading: true,
      reviewLoading: false,
      selectionSyncing: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      reportList: [],
      dashboardData: {},
      smartResult: {},
      selectedReport: null,
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
    this.refreshDashboard()
    this.getList()
  },
  computed: {
    dashboardCards() {
      const summary = this.dashboardData.summary || {}
      return [
        { key: 'total', label: '报告总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录' },
        { key: 'batch', label: '批次数', value: summary.batchCount || 0, foot: '去重加工批次' },
        { key: 'generated', label: '已生成', value: summary.generatedCount || 0, foot: '状态为已生成的报告' },
        { key: 'draft', label: '草稿数', value: summary.draftCount || 0, foot: '待确认报告' },
        { key: 'defect', label: '平均缺陷率', value: this.formatRate(summary.avgDefectRate), foot: '均值指标' },
        { key: 'risk', label: '高风险数', value: summary.highRiskCount || 0, foot: '缺陷率偏高项' }
      ]
    },
    highRiskRows() {
      return this.dashboardData.highRiskRows || []
    },
    recentRows() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    async refreshDashboard() {
      try {
        const response = await getQualityReportDashboard(this.queryParams)
        this.dashboardData = response.data || {}
      } catch (error) {
        this.$modal.msgError('加载质检看板失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const response = await listQualityReport(this.queryParams)
        this.reportList = response.rows || []
        this.total = response.total || 0
      } catch (error) {
        this.reportList = []
        this.total = 0
        this.$modal.msgError('加载质检报告列表失败')
      } finally {
        this.loading = false
      }
    },
    formatRate(value) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${(number * 100).toFixed(2)}%`
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
      this.smartResult = {}
      this.refreshDashboard()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.smartResult = {}
      this.selectedReport = null
      if (this.$refs.reportTable) {
        this.$refs.reportTable.clearSelection()
      }
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.reportId)
      this.selectedReport = selection.length === 1 ? selection[0] : null
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (!this.selectionSyncing && selection.length === 1) {
        this.handleReview(selection[0], { silentSuccess: true, syncSelection: false })
      }
    },
    handleRowClick(row) {
      if (!this.$refs.reportTable || !row) {
        return
      }
      this.$refs.reportTable.clearSelection()
      this.$refs.reportTable.toggleRowSelection(row, true)
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
    syncTableSelection(reportId) {
      if (!this.$refs.reportTable || !reportId) {
        return
      }
      const targetRow = this.reportList.find(item => item.reportId === reportId)
      if (!targetRow) {
        return
      }
      this.selectionSyncing = true
      this.$refs.reportTable.clearSelection()
      this.$refs.reportTable.toggleRowSelection(targetRow, true)
      this.$nextTick(() => {
        this.selectionSyncing = false
      })
    },
    getPreferredReport(row) {
      if (row && row.reportId) {
        return row
      }
      if (this.selectedReport && this.selectedReport.reportId) {
        return this.selectedReport
      }
      return this.reportList.length ? this.reportList[0] : null
    },
    async handleReview(row, options = {}) {
      const { silentSuccess = false, syncSelection = true } = options
      const target = this.getPreferredReport(row)
      if (!target) {
        this.$modal.msgWarning('请先选择一条质检报告')
        return
      }
      if (syncSelection) {
        this.syncTableSelection(target.reportId)
      }
      this.reviewLoading = true
      try {
        const response = await getQualityReportReview(target.reportId)
        this.smartResult = response.data || {}
        if (!silentSuccess) {
          this.$modal.msgSuccess('智能审阅已更新')
        }
      } catch (error) {
        this.$modal.msgError((error && error.msg) || '智能审阅失败')
      } finally {
        this.reviewLoading = false
      }
    },
    async handleGenerate() {
      const selected = this.getPreferredReport(null)
      if (!selected || !selected.inspectId) {
        this.$modal.msgWarning('所选记录缺少检测任务ID')
        return
      }
      try {
        await generateQualityReport(selected.inspectId)
        this.$modal.msgSuccess('报告生成成功')
        await this.getList()
        await this.refreshDashboard()
        if (selected.reportId) {
          this.syncTableSelection(selected.reportId)
        }
      } catch (error) {
        this.$modal.msgError((error && error.msg) || '报告生成失败')
      }
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
            this.refreshDashboard()
          })
          return
        }
        addQualityReport(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
          this.refreshDashboard()
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

<style scoped>
.report-hero {
  border: 1px solid #e9e1d7;
  background: linear-gradient(135deg, #fffaf6 0%, #f7efea 100%);
}

.report-hero-head,
.report-panel-head,
.report-risk-item,
.report-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-title {
  font-size: 20px;
  font-weight: 700;
  color: #7b3d2a;
}

.report-desc {
  margin-top: 6px;
  color: #8b6c5d;
}

.report-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.report-kpi-row,
.report-overview-row {
  margin-top: 14px;
}

.report-kpi-card,
.report-panel {
  border: 1px solid #ecdcd3;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.report-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.report-kpi-card span,
.report-panel-head small,
.report-kpi-card small,
.report-risk-item p,
.report-review-box p {
  color: #8f776b;
}

.report-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #7b3d2a;
}

.report-risk-item {
  padding: 8px 0;
  border-top: 1px solid #f3eae6;
  cursor: pointer;
}

.report-risk-item p {
  margin: 4px 0 0;
}

.report-score strong {
  font-size: 26px;
  color: #b04f34;
}

.report-review-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #6f5649;
}
</style>
