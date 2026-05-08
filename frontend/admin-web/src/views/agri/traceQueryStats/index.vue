<template>
  <div class="app-container">
    <el-card shadow="never" class="trace-hero mb16">
      <div class="trace-hero-head">
        <div>
          <div class="trace-title">溯源访问洞察台</div>
          <div class="trace-desc">聚焦查询量、独立用户数、成功率和响应时间，快速识别高负载日期与异常查询趋势。</div>
        </div>
        <div class="trace-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-data-analysis" @click="handleInsight">智能洞察</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="trace-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="trace-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="trace-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="trace-panel">
            <div class="trace-panel-head"><span>高负载日期</span><small>从最近统计中找出峰值与异常</small></div>
            <div class="trace-top-list" v-if="recentRows.length">
              <div v-for="item in recentRows" :key="item.statsId" class="trace-top-item" @click="handleInsight(item)">
                <div>
                  <b>{{ item.statDate }}</b>
                  <p>成功 {{ item.successCount }} / 失败 {{ item.failCount }}</p>
                </div>
                <el-tag size="mini" type="info">{{ item.peakQps }} QPS</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无统计数据" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="trace-panel">
            <div class="trace-panel-head"><span>智能洞察</span><small>根据负载与失败率给出优化建议</small></div>
            <div v-if="smartResult.statsId" class="trace-insight-box">
              <div class="trace-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
              <p>{{ smartResult.stats.statDate }}</p>
              <ul><li v-for="item in smartResult.findings" :key="item">{{ item }}</li></ul>
            </div>
            <el-empty v-else description="请选择一条统计记录进行洞察" />
          </div>
        </el-col>
      </el-row>
    </el-card>

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

    <el-table v-loading="loading" :data="statsList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
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
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handleInsight(scope.row)" v-hasPermi="['agri:traceQueryStats:query']">洞察</el-button>
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
  updateTraceQueryStats,
  getTraceQueryStatsDashboard,
  getTraceQueryStatsInsight
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
      dashboardData: {},
      smartResult: {},
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
    this.refreshDashboard()
    this.getList()
  },
  computed: {
    dashboardCards() {
      const summary = this.dashboardData.summary || {}
      return [
        { key: 'total', label: '查询总量', value: summary.totalQueryCount || 0, foot: '统计期内总查询次数' },
        { key: 'user', label: '独立用户', value: summary.uniqueUserCount || 0, foot: '去重访问用户数' },
        { key: 'success', label: '成功率', value: this.formatPercent(summary.successRate), foot: '查询成功占比' },
        { key: 'avg', label: '平均响应', value: this.formatDuration(summary.avgDurationMs), foot: '响应耗时均值' },
        { key: 'peak', label: '峰值QPS', value: summary.peakQps || 0, foot: '统计期内峰值' },
        { key: 'fail', label: '失败次数', value: summary.failCount || 0, foot: '查询失败累计' }
      ]
    },
    recentRows() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    refreshDashboard() {
      getTraceQueryStatsDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listTraceQueryStats(this.queryParams).then(response => {
        this.statsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatPercent(value) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${number.toFixed(2)}%`
    },
    formatDuration(value) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${number.toFixed(0)}ms`
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
      this.refreshDashboard()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.refreshDashboard()
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.statsId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.handleSelectionChange([row])
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
    handleInsight(row) {
      const target = row || this.statsList[0]
      if (!target || !target.statsId) {
        this.$modal.msgWarning('请先选择一条溯源统计记录')
        return
      }
      getTraceQueryStatsInsight(target.statsId).then(response => {
        this.smartResult = response.data || {}
        this.$modal.msgSuccess('智能洞察已更新')
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

<style scoped>
.trace-hero {
  border: 1px solid #dde5ec;
  background: linear-gradient(135deg, #f6fbfe 0%, #edf5fa 100%);
}

.trace-hero-head,
.trace-panel-head,
.trace-top-item,
.trace-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trace-title {
  font-size: 20px;
  font-weight: 700;
  color: #244a62;
}

.trace-desc {
  margin-top: 6px;
  color: #61798a;
}

.trace-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.trace-kpi-row,
.trace-overview-row {
  margin-top: 14px;
}

.trace-kpi-card,
.trace-panel {
  border: 1px solid #dfe8ef;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.trace-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.trace-kpi-card span,
.trace-panel-head small,
.trace-kpi-card small,
.trace-top-item p,
.trace-insight-box p {
  color: #627787;
}

.trace-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #244a62;
}

.trace-top-item {
  padding: 8px 0;
  border-top: 1px solid #eef4f8;
  cursor: pointer;
}

.trace-top-item p {
  margin: 4px 0 0;
}

.trace-score strong {
  font-size: 26px;
  color: #2d6d88;
}

.trace-insight-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #4f6271;
}
</style>
