<template>
  <div class="app-container warning-page">
    <el-card shadow="never" class="hero-card mb16">
      <div class="hero-wrap">
        <div>
          <div class="hero-title">预警信息汇总中心</div>
          <div class="hero-desc">聚合展示告警总量、严重等级占比与闭环效率，优先处理高风险与积压项。</div>
          <div class="hero-meta">
            <span>汇总记录 {{ total }}</span>
            <span>最近刷新 {{ lastRefreshLabel }}</span>
            <span>自动刷新 {{ refreshInterval / 1000 }} 秒 / 次</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click="refreshDashboard">刷新看板</el-button>
          <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:warningSummary:export']">导出快照</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="handleQuery"></right-toolbar>
        </div>
      </div>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px" class="mb16 warning-search">
      <el-form-item label="开始日期" prop="beginSummaryDate">
        <el-date-picker clearable v-model="queryParams.beginSummaryDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" style="width: 170px" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endSummaryDate">
        <el-date-picker clearable v-model="queryParams.endSummaryDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" style="width: 170px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="mb16">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-foot">{{ item.foot }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>告警态势洞察</span>
            <span class="panel-subtitle">规则引擎实时评估风险</span>
          </div>
          <div class="insight-block" v-loading="dashboardLoading">
            <div class="insight-summary">{{ insightSummary }}</div>
            <div class="insight-meta">
              <el-tag size="mini" :type="riskTagType(insight.riskLevel)">风险{{ insight.riskLevel || '中' }}</el-tag>
              <span>置信度 {{ formatRate(insight.confidenceRate) }}</span>
              <span>模型 {{ insight.modelVersion || 'rule-v1' }}</span>
            </div>
            <div class="insight-suggestion" v-if="insight.suggestion">建议：{{ insight.suggestion }}</div>
            <div class="recommendation-list" v-if="recommendations.length">
              <div v-for="(item, index) in recommendations" :key="index" class="recommendation-item">{{ item }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>高风险汇总日</span>
            <span class="panel-subtitle">优先核销待处理告警</span>
          </div>
          <el-empty v-if="!topAlerts.length" description="暂无高风险数据" />
          <div v-else class="alert-list">
            <div v-for="item in topAlerts" :key="item.summaryId" class="alert-item" :class="{ active: selectedRow && selectedRow.summaryId === item.summaryId }" @click="selectRow(item)">
              <div class="alert-head">
                <div>
                  <div class="alert-title">{{ parseDate(item.summaryDate) }}</div>
                  <div class="alert-sub">总告警 {{ item.totalWarningCount }} · 待处理 {{ item.pendingCount }}</div>
                </div>
                <el-tag size="mini" :type="riskTagType(item.riskLevel)">{{ item.riskLevel }}风险</el-tag>
              </div>
              <div class="alert-suggestion">{{ item.suggestion }}</div>
              <div class="alert-actions">
                <el-button type="text" size="mini" @click.stop="handleQuickHandle(item)">一键核销1条</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card mb16">
      <div slot="header" class="panel-header">
        <span>预警台账</span>
        <span class="panel-subtitle">保留维护入口并支持快速闭环</span>
      </div>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:warningSummary:add']">新增</el-button></el-col>
        <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:warningSummary:edit']">修改</el-button></el-col>
        <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-check" size="mini" :disabled="single" @click="handleQuickHandle(selectedRow)">快速核销</el-button></el-col>
        <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:warningSummary:remove']">删除</el-button></el-col>
        <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:warningSummary:export']">导出</el-button></el-col>
      </el-row>

      <el-table v-loading="loading" :data="summaryList" @selection-change="handleSelectionChange" @row-click="selectRow">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="统计日期" align="center" prop="summaryDate" width="110" />
        <el-table-column label="预警总数" align="center" prop="totalWarningCount" width="90" />
        <el-table-column label="提示级" align="center" prop="level1Count" width="80" />
        <el-table-column label="预警级" align="center" prop="level2Count" width="80" />
        <el-table-column label="严重级" align="center" prop="level3Count" width="80" />
        <el-table-column label="已处理" align="center" prop="handledCount" width="80" />
        <el-table-column label="待处理" align="center" prop="pendingCount" width="80" />
        <el-table-column label="闭环率" align="center" width="90">
          <template slot-scope="scope">{{ formatRate(rate(scope.row.handledCount, add(scope.row.handledCount, scope.row.pendingCount))) }}</template>
        </el-table-column>
        <el-table-column label="平均处理(分)" align="center" prop="avgHandleMinutes" width="110" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-check" @click.stop="handleQuickHandle(scope.row)">核销</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:warningSummary:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:warningSummary:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </el-card>

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
  updateWarningSummary,
  getWarningSummaryDashboard,
  getWarningSummaryAlerts
} from '@/api/agri/warningSummary'

export default {
  name: 'WarningSummary',
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      summaryList: [],
      dashboardSnapshot: null,
      alertSnapshot: null,
      selectedRow: null,
      title: '',
      open: false,
      lastRefreshTime: null,
      refreshInterval: 60000,
      refreshTimer: null,
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
  computed: {
    summaryCards() {
      const kpi = (this.dashboardSnapshot && this.dashboardSnapshot.kpi) || {}
      return [
        { key: 'total', label: '累计预警', value: this.toNumber(kpi.totalWarningCount), foot: '统计窗口内总量' },
        { key: 'pending', label: '待处理', value: this.toNumber(kpi.pendingCount), foot: '待闭环告警数量' },
        { key: 'severe', label: '严重级', value: this.toNumber(kpi.severeCount), foot: '需要优先处置' },
        { key: 'handleRate', label: '闭环率', value: this.formatRate(kpi.handleRate), foot: '已处理 / 已处理+待处理' },
        { key: 'avgHandle', label: '平均处理时长', value: `${this.toNumber(kpi.avgHandleMinutes)} 分钟`, foot: '按告警量加权计算' },
        { key: 'severeRate', label: '严重占比', value: this.formatRate(kpi.severeRate), foot: '严重级告警占比' }
      ]
    },
    topAlerts() {
      if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.topAlerts)) {
        return this.dashboardSnapshot.topAlerts
      }
      return []
    },
    insight() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.insight) || {}
    },
    insightSummary() {
      return this.insight.insightSummary || '暂无预警洞察，刷新看板后查看系统评估结果。'
    },
    recommendations() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.recommendations) || []
    },
    lastRefreshLabel() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '暂无'
    }
  },
  created() {
    this.getList()
    this.refreshDashboard()
  },
  mounted() {
    this.refreshTimer = setInterval(() => {
      this.refreshDashboard()
    }, this.refreshInterval)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  },
  methods: {
    getList() {
      this.loading = true
      listWarningSummary(this.queryParams).then(response => {
        this.summaryList = response.rows || []
        this.total = response.total || 0
        if (!this.selectedRow && this.summaryList.length) {
          this.selectedRow = this.summaryList[0]
        }
        this.lastRefreshTime = new Date()
        this.loading = false
      })
    },
    refreshDashboard() {
      this.dashboardLoading = true
      Promise.all([
        getWarningSummaryDashboard(this.queryParams),
        getWarningSummaryAlerts(this.queryParams)
      ]).then(([dashboardRes, alertRes]) => {
        this.dashboardSnapshot = dashboardRes.data || null
        this.alertSnapshot = alertRes.data || null
        this.lastRefreshTime = new Date()
      }).finally(() => {
        this.dashboardLoading = false
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
      this.refreshDashboard()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.summaryId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (selection.length) {
        this.selectedRow = selection[0]
      }
    },
    selectRow(row) {
      if (row) {
        this.selectedRow = row
      }
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
              this.refreshDashboard()
            })
          } else {
            addWarningSummary(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.refreshDashboard()
            })
          }
        }
      })
    },
    handleQuickHandle(row) {
      const summaryId = row && row.summaryId ? row.summaryId : (this.selectedRow && this.selectedRow.summaryId)
      if (!summaryId) {
        this.$modal.msgWarning('请先选择一条汇总记录')
        return
      }
      getWarningSummary(summaryId).then(response => {
        const data = response.data || {}
        const pending = this.toNumber(data.pendingCount)
        if (pending <= 0) {
          this.$modal.msgWarning('当前记录无待处理告警')
          return
        }
        data.pendingCount = pending - 1
        data.handledCount = this.toNumber(data.handledCount) + 1
        if (this.toNumber(data.totalWarningCount) < data.handledCount + data.pendingCount) {
          data.totalWarningCount = data.handledCount + data.pendingCount
        }
        return updateWarningSummary(data)
      }).then(res => {
        if (res !== undefined) {
          this.$modal.msgSuccess('已核销 1 条待处理告警')
          this.getList()
          this.refreshDashboard()
        }
      })
    },
    handleDelete(row) {
      const summaryIds = row.summaryId || this.ids
      this.$modal.confirm('是否确认删除预警信息汇总编号为"' + summaryIds + '"的数据项？').then(() => {
        return delWarningSummary(summaryIds)
      }).then(() => {
        this.getList()
        this.refreshDashboard()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/warningSummary/export', {
        ...this.queryParams
      }, `warning_summary_${new Date().getTime()}.xlsx`)
    },
    add(a, b) {
      return this.toNumber(a) + this.toNumber(b)
    },
    rate(numerator, denominator) {
      const n = this.toNumber(numerator)
      const d = this.toNumber(denominator)
      if (d <= 0) {
        return 0
      }
      return n / d
    },
    toNumber(value) {
      const num = Number(value)
      return Number.isFinite(num) ? num : 0
    },
    formatRate(value) {
      const num = this.toNumber(value)
      if (num <= 1) {
        return `${Math.round(num * 100)}%`
      }
      return `${Math.round(num)}%`
    },
    parseDate(value) {
      return value ? this.parseTime(value, '{y}-{m}-{d}') : '未设置日期'
    },
    riskTagType(level) {
      const map = { 高: 'danger', 中: 'warning', 低: 'success' }
      return map[level] || 'info'
    }
  }
}
</script>

<style scoped>
.warning-page {
  background: linear-gradient(180deg, #f6f8f9 0%, #ffffff 30%, #ffffff 100%);
}

.hero-card,
.panel-card {
  border-radius: 14px;
}

.hero-wrap {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #13322a;
  margin-bottom: 8px;
}

.hero-desc {
  color: #536b64;
  line-height: 1.7;
  margin-bottom: 10px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #6b7d76;
  font-size: 12px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.kpi-card {
  min-height: 116px;
}

.kpi-label {
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #13322a;
  line-height: 1.1;
}

.kpi-foot {
  margin-top: 10px;
  color: #9ca3af;
  font-size: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #13322a;
}

.panel-subtitle {
  color: #9ca3af;
  font-size: 12px;
  font-weight: 400;
}

.insight-block {
  min-height: 180px;
}

.insight-summary {
  color: #1f2937;
  line-height: 1.8;
  margin-bottom: 12px;
}

.insight-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 12px;
}

.insight-suggestion {
  padding: 12px;
  border-radius: 10px;
  background: #eff8f3;
  color: #166534;
  line-height: 1.7;
}

.recommendation-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.recommendation-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: #f8fafc;
  color: #334155;
}

.alert-list {
  display: grid;
  gap: 10px;
}

.alert-item {
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
}

.alert-item.active {
  border-color: #f59e0b;
  box-shadow: 0 8px 20px rgba(245, 158, 11, 0.12);
}

.alert-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.alert-title {
  font-weight: 600;
  color: #1f2937;
}

.alert-sub {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
}

.alert-suggestion {
  color: #4b5563;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 8px;
}

.alert-actions {
  text-align: right;
  margin-top: 6px;
}

@media (max-width: 992px) {
  .hero-wrap {
    flex-direction: column;
  }
}
</style>
