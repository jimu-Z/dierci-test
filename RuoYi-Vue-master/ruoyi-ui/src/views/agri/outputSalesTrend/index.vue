<template>
  <div class="app-container output-sales-page">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px" class="filter-bar">
      <el-form-item label="开始日期" prop="beginStatDate">
        <el-date-picker clearable v-model="queryParams.beginStatDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endStatDate">
        <el-date-picker clearable v-model="queryParams.endStatDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="预测月份" prop="futureMonths">
        <el-input-number v-model="futureMonths" :min="1" :max="6" :step="1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">刷新看板</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="el-icon-data-analysis" size="mini" @click="handleAiRefresh">AI重算预测</el-button>
      </el-form-item>
    </el-form>

    <div class="overview" v-loading="dashboardLoading">
      <div class="overview-title">产销量趋势驾驶舱</div>
      <div class="overview-desc">覆盖历史走势、目标达成、环比波动、风险地块与预测洞察，支持运维与经营双视角判断。</div>
      <el-row :gutter="12" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">平均产量(吨)</div>
            <div class="kpi-value">{{ formatNumber(kpi.avgOutput) }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">平均销量(万元)</div>
            <div class="kpi-value">{{ formatNumber(kpi.avgSales) }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">产量累计增幅</div>
            <div class="kpi-value">{{ formatPercent(kpi.outputGrowth) }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">销量累计增幅</div>
            <div class="kpi-value">{{ formatPercent(kpi.salesGrowth) }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">病虫害风险等级</div>
            <div class="kpi-value">{{ ai.riskLevel || kpi.riskLevel || '中' }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-title">预测置信度</div>
            <div class="kpi-value">{{ formatRate(ai.confidenceRate || kpi.confidenceRate) }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">历史+预测双轴趋势</div>
          <div ref="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card risk-card">
          <div slot="header" class="chart-header risk-header">
            <div>
              <div class="risk-title">病虫害高风险地块TOP</div>
              <div class="risk-sub">按风险次数降序，动态识别高风险地块</div>
            </div>
            <el-tag size="mini" type="danger" effect="plain">TOP{{ riskPanelMeta.count }}</el-tag>
          </div>
          <div class="risk-kpi-row">
            <div class="risk-kpi">
              <span>最高风险地块</span>
              <strong>{{ riskPanelMeta.topPlot }}</strong>
            </div>
            <div class="risk-kpi">
              <span>最高风险次数</span>
              <strong>{{ riskPanelMeta.maxCount }}</strong>
            </div>
          </div>
          <div ref="riskChart" class="chart-box risk-chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">目标达成率拆解</div>
          <div ref="targetChart" class="chart-box chart-mid"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">产销环比波动</div>
          <div ref="momChart" class="chart-box chart-mid"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">产销结构比（吨/万元）</div>
          <div ref="ratioChart" class="chart-box chart-mid"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card ai-card">
          <div slot="header" class="chart-header">AI预测洞察</div>
          <div class="ai-meta">
            <span>模型：{{ ai.modelVersion || 'rule-v1' }}</span>
            <span>风险：{{ ai.riskLevel || kpi.riskLevel || '中' }}</span>
            <span>置信度：{{ formatRate(ai.confidenceRate || kpi.confidenceRate) }}</span>
          </div>
          <div class="ai-summary">{{ ai.summary || '暂无AI洞察' }}</div>
          <div class="ai-suggestion" v-if="ai.suggestion">建议动作：{{ ai.suggestion }}</div>
          <div class="warnings-box">
            <div class="warning-title">运营预警</div>
            <el-empty v-if="!warnings.length" description="暂无预警" :image-size="58" />
            <div v-for="(item, index) in warnings" :key="index" class="warning-item">
              <el-tag size="mini" :type="warningTagType(item.level)">{{ warningLevelText(item.level) }}</el-tag>
              <span class="warning-name">{{ item.title }}</span>
              <div class="warning-desc">{{ item.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="detail-card">
      <div slot="header" class="chart-header">月度明细指标</div>
      <el-table :data="detailMetrics" size="mini" border>
        <el-table-column prop="statMonth" label="月份" width="90" />
        <el-table-column prop="outputValue" label="产量(吨)" width="100" />
        <el-table-column prop="salesValue" label="销量(万元)" width="110" />
        <el-table-column prop="targetOutput" label="目标产量" width="100" />
        <el-table-column prop="targetSales" label="目标销量" width="100" />
        <el-table-column prop="outputCompletionRate" label="产量达成率(%)" width="120" />
        <el-table-column prop="salesCompletionRate" label="销量达成率(%)" width="120" />
        <el-table-column prop="outputGap" label="产量差额" width="100" />
        <el-table-column prop="salesGap" label="销量差额" width="100" />
        <el-table-column prop="outputMomRate" label="产量环比(%)" width="110" />
        <el-table-column prop="salesMomRate" label="销量环比(%)" width="110" />
        <el-table-column prop="outputSalesRatio" label="产销结构比" width="110" />
      </el-table>
    </el-card>

    <el-divider>台账维护</el-divider>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:outputSalesTrend:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:outputSalesTrend:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:outputSalesTrend:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:outputSalesTrend:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="trendList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="trendId" width="80" />
      <el-table-column label="统计日期" align="center" prop="statDate" width="110" />
      <el-table-column label="产量(吨)" align="center" prop="outputValue" width="100" />
      <el-table-column label="销量(万元)" align="center" prop="salesValue" width="110" />
      <el-table-column label="目标产量" align="center" prop="targetOutput" width="100" />
      <el-table-column label="目标销量" align="center" prop="targetSales" width="100" />
      <el-table-column label="产量环比(%)" align="center" prop="outputMomRate" width="100" />
      <el-table-column label="销量环比(%)" align="center" prop="salesMomRate" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:outputSalesTrend:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:outputSalesTrend:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="116px">
        <el-row>
          <el-col :span="12"><el-form-item label="统计日期" prop="statDate"><el-date-picker clearable v-model="form.statDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择统计日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产量(吨)" prop="outputValue"><el-input-number v-model="form.outputValue" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="销量(万元)" prop="salesValue"><el-input-number v-model="form.salesValue" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目标产量" prop="targetOutput"><el-input-number v-model="form.targetOutput" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="目标销量" prop="targetSales"><el-input-number v-model="form.targetSales" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产量环比(%)" prop="outputMomRate"><el-input-number v-model="form.outputMomRate" :precision="2" :min="-100" :max="1000" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="销量环比(%)" prop="salesMomRate"><el-input-number v-model="form.salesMomRate" :precision="2" :min="-100" :max="1000" style="width: 100%" /></el-form-item></el-col>
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
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import {
  listOutputSalesTrend,
  getOutputSalesTrend,
  delOutputSalesTrend,
  addOutputSalesTrend,
  updateOutputSalesTrend,
  getOutputSalesDashboard
} from '@/api/agri/outputSalesTrend'

export default {
  name: 'OutputSalesTrend',
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      futureMonths: 3,
      trendList: [],
      title: '',
      open: false,
      chartRefs: {
        trendChart: null,
        riskChart: null,
        targetChart: null,
        momChart: null,
        ratioChart: null
      },
      dashboard: {
        history: [],
        forecast: [],
        pestRisk: []
      },
      detailMetrics: [],
      warnings: [],
      kpi: {
        avgOutput: 0,
        avgSales: 0,
        outputGrowth: 0,
        salesGrowth: 0,
        confidenceRate: 0,
        riskLevel: '中'
      },
      ai: {
        summary: '',
        riskLevel: '中',
        confidenceRate: 0,
        modelVersion: 'rule-v1',
        suggestion: ''
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        beginStatDate: null,
        endStatDate: null
      },
      form: {},
      rules: {
        statDate: [{ required: true, message: '统计日期不能为空', trigger: 'change' }],
        outputValue: [{ required: true, message: '产量不能为空', trigger: 'blur' }],
        salesValue: [{ required: true, message: '销量不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.refreshDashboard()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.disposeCharts()
  },
  computed: {
    riskPanelMeta() {
      const list = this.dashboard.pestRisk || []
      if (!list.length) {
        return {
          count: 0,
          topPlot: '-',
          maxCount: 0
        }
      }
      const sorted = [...list].sort((a, b) => Number(b.count || 0) - Number(a.count || 0))
      return {
        count: sorted.length,
        topPlot: sorted[0].plotCode || '-',
        maxCount: Number(sorted[0].count || 0)
      }
    }
  },
  methods: {
    getList() {
      this.loading = true
      listOutputSalesTrend(this.queryParams).then(response => {
        this.trendList = response.rows || []
        this.total = response.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        trendId: null,
        statDate: null,
        outputValue: 0,
        salesValue: 0,
        targetOutput: 0,
        targetSales: 0,
        outputMomRate: 0,
        salesMomRate: 0,
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
      this.futureMonths = 3
      this.handleQuery()
    },
    refreshDashboard(useAi = false) {
      this.dashboardLoading = true
      getOutputSalesDashboard({
        beginStatDate: this.queryParams.beginStatDate,
        endStatDate: this.queryParams.endStatDate,
        futureMonths: this.futureMonths,
        useAi: useAi ? 1 : 0
      }).then(response => {
        const data = response.data || {}
        this.dashboard = {
          history: data.history || [],
          forecast: data.forecast || [],
          pestRisk: data.pestRisk || []
        }
        this.detailMetrics = data.detailMetrics || this.buildDetailMetricsByHistory(this.dashboard.history)
        this.warnings = data.warnings || []
        this.kpi = data.kpi || this.kpi
        this.ai = data.ai || this.ai
        this.$nextTick(() => {
          this.renderAllCharts()
        })
      }).catch(() => {
        this.$message.error('趋势看板接口异常，请稍后重试')
      }).finally(() => {
        this.dashboardLoading = false
      })
    },
    handleAiRefresh() {
      this.refreshDashboard(true)
    },
    buildDetailMetricsByHistory(history) {
      return (history || []).map(item => {
        const output = Number(item.outputValue || 0)
        const sales = Number(item.salesValue || 0)
        const targetOutput = Number(item.targetOutput || 0)
        const targetSales = Number(item.targetSales || 0)
        return {
          statMonth: item.statMonth,
          outputValue: this.fixed(output),
          salesValue: this.fixed(sales),
          targetOutput: this.fixed(targetOutput),
          targetSales: this.fixed(targetSales),
          outputGap: this.fixed(output - targetOutput),
          salesGap: this.fixed(sales - targetSales),
          outputCompletionRate: targetOutput > 0 ? this.fixed((output / targetOutput) * 100) : 0,
          salesCompletionRate: targetSales > 0 ? this.fixed((sales / targetSales) * 100) : 0,
          outputMomRate: this.fixed(item.outputMomRate || 0),
          salesMomRate: this.fixed(item.salesMomRate || 0),
          outputSalesRatio: sales > 0 ? this.fixed(output / sales, 4) : 0
        }
      })
    },
    renderAllCharts() {
      this.renderTrendChart()
      this.renderRiskChart()
      this.renderTargetChart()
      this.renderMomChart()
      this.renderRatioChart()
    },
    ensureChart(refName) {
      if (!this.chartRefs[refName] && this.$refs[refName]) {
        this.chartRefs[refName] = echarts.init(this.$refs[refName], 'macarons')
      }
      return this.chartRefs[refName]
    },
    renderTrendChart() {
      const chart = this.ensureChart('trendChart')
      if (!chart) {
        return
      }
      const history = this.dashboard.history || []
      const forecast = this.dashboard.forecast || []
      const months = history.map(item => item.statMonth).concat(forecast.map(item => item.statMonth))
      const historyOutput = history.map(item => Number(item.outputValue || 0))
      const historySales = history.map(item => Number(item.salesValue || 0))
      const targetOutput = history.map(item => Number(item.targetOutput || 0))
      const targetSales = history.map(item => Number(item.targetSales || 0))
      const forecastOutput = new Array(history.length).fill(null).concat(forecast.map(item => Number(item.outputValue || 0)))
      const forecastSales = new Array(history.length).fill(null).concat(forecast.map(item => Number(item.salesValue || 0)))

      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['历史产量', '历史销量', '预测产量', '预测销量', '目标产量', '目标销量'] },
        grid: { left: '4%', right: '4%', bottom: '6%', containLabel: true },
        xAxis: [{ type: 'category', boundaryGap: false, data: months }],
        yAxis: [
          { type: 'value', name: '产量(吨)', position: 'left' },
          { type: 'value', name: '销量(万元)', position: 'right' }
        ],
        series: [
          { name: '历史产量', type: 'line', smooth: true, yAxisIndex: 0, data: historyOutput },
          { name: '历史销量', type: 'line', smooth: true, yAxisIndex: 1, data: historySales },
          { name: '预测产量', type: 'line', smooth: true, yAxisIndex: 0, lineStyle: { type: 'dashed' }, data: forecastOutput },
          { name: '预测销量', type: 'line', smooth: true, yAxisIndex: 1, lineStyle: { type: 'dashed' }, data: forecastSales },
          { name: '目标产量', type: 'line', smooth: true, yAxisIndex: 0, symbol: 'none', data: targetOutput },
          { name: '目标销量', type: 'line', smooth: true, yAxisIndex: 1, symbol: 'none', data: targetSales }
        ]
      })
    },
    renderRiskChart() {
      const chart = this.ensureChart('riskChart')
      if (!chart) {
        return
      }
      const pestRisk = this.dashboard.pestRisk || []
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '18%', right: '8%', bottom: '18%', top: '8%', containLabel: true },
        xAxis: {
          type: 'value',
          name: '风险次数（次）',
          nameLocation: 'middle',
          nameGap: 34,
          axisLabel: {
            formatter: '{value} 次'
          }
        },
        yAxis: {
          type: 'category',
          name: '地块编码',
          nameLocation: 'middle',
          nameGap: 72,
          data: pestRisk.map(item => item.plotCode)
        },
        series: [{ name: '风险记录', type: 'bar', data: pestRisk.map(item => Number(item.count || 0)), itemStyle: { color: '#d35400' } }]
      })
    },
    renderTargetChart() {
      const chart = this.ensureChart('targetChart')
      if (!chart) {
        return
      }
      const detail = this.detailMetrics || []
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['产量达成率', '销量达成率'] },
        grid: { left: '5%', right: '4%', bottom: '6%', containLabel: true },
        xAxis: { type: 'category', data: detail.map(item => item.statMonth) },
        yAxis: { type: 'value', name: '%' },
        series: [
          { name: '产量达成率', type: 'bar', data: detail.map(item => Number(item.outputCompletionRate || 0)) },
          { name: '销量达成率', type: 'bar', data: detail.map(item => Number(item.salesCompletionRate || 0)) }
        ]
      })
    },
    renderMomChart() {
      const chart = this.ensureChart('momChart')
      if (!chart) {
        return
      }
      const detail = this.detailMetrics || []
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['产量环比', '销量环比'] },
        grid: { left: '5%', right: '4%', bottom: '6%', containLabel: true },
        xAxis: { type: 'category', data: detail.map(item => item.statMonth) },
        yAxis: { type: 'value', name: '%' },
        series: [
          { name: '产量环比', type: 'bar', data: detail.map(item => Number(item.outputMomRate || 0)) },
          { name: '销量环比', type: 'bar', data: detail.map(item => Number(item.salesMomRate || 0)) }
        ]
      })
    },
    renderRatioChart() {
      const chart = this.ensureChart('ratioChart')
      if (!chart) {
        return
      }
      const detail = this.detailMetrics || []
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '5%', right: '4%', bottom: '6%', containLabel: true },
        xAxis: { type: 'category', data: detail.map(item => item.statMonth) },
        yAxis: { type: 'value', name: '吨/万元' },
        series: [
          {
            name: '产销结构比',
            type: 'line',
            smooth: true,
            areaStyle: { opacity: 0.22 },
            data: detail.map(item => Number(item.outputSalesRatio || 0))
          }
        ]
      })
    },
    handleResize() {
      Object.keys(this.chartRefs).forEach(key => {
        if (this.chartRefs[key]) {
          this.chartRefs[key].resize()
        }
      })
    },
    disposeCharts() {
      Object.keys(this.chartRefs).forEach(key => {
        if (this.chartRefs[key]) {
          this.chartRefs[key].dispose()
          this.chartRefs[key] = null
        }
      })
    },
    warningTagType(level) {
      if (level === 'high') {
        return 'danger'
      }
      if (level === 'medium') {
        return 'warning'
      }
      return 'info'
    },
    warningLevelText(level) {
      if (level === 'high') {
        return '高'
      }
      if (level === 'medium') {
        return '中'
      }
      return '低'
    },
    formatNumber(value) {
      return this.fixed(value)
    },
    formatPercent(value) {
      return `${this.fixed(value)}%`
    },
    formatRate(value) {
      const num = Number(value || 0)
      return num <= 1 ? `${this.fixed(num * 100)}%` : `${this.fixed(num)}%`
    },
    fixed(value, scale = 2) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num.toFixed(scale) : '0.00'
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.trendId)
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
      this.title = '新增产量与销量趋势'
    },
    handleUpdate(row) {
      this.reset()
      const trendId = row.trendId || this.ids[0]
      getOutputSalesTrend(trendId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改产量与销量趋势'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.trendId != null) {
            updateOutputSalesTrend(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.refreshDashboard()
            })
          } else {
            addOutputSalesTrend(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.refreshDashboard()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const trendIds = row.trendId || this.ids
      this.$modal.confirm('是否确认删除产量与销量趋势编号为"' + trendIds + '"的数据项？').then(() => {
        return delOutputSalesTrend(trendIds)
      }).then(() => {
        this.getList()
        this.refreshDashboard()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/outputSalesTrend/export', {
        ...this.queryParams
      }, `output_sales_trend_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.output-sales-page {
  background: linear-gradient(180deg, #f4f8ff 0%, #f9fbff 40%, #ffffff 100%);
}

.filter-bar {
  padding: 10px 12px 2px;
  border-radius: 8px;
  background: #fff;
  margin-bottom: 12px;
}

.overview {
  background: #fff;
  border-radius: 8px;
  padding: 14px 14px 2px;
  margin-bottom: 12px;
}

.overview-title {
  font-size: 18px;
  font-weight: 700;
  color: #23395d;
}

.overview-desc {
  margin-top: 6px;
  color: #5a6a85;
  font-size: 13px;
}

.kpi-row {
  margin-top: 10px;
}

.kpi-card {
  margin-bottom: 12px;
  border-radius: 10px;
}

.kpi-title {
  color: #567;
  font-size: 12px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.chart-row {
  margin-bottom: 12px;
}

.chart-card {
  margin-bottom: 12px;
  border-radius: 8px;
}

.chart-header {
  font-size: 14px;
  font-weight: 600;
  color: #2f3f5e;
}

.risk-card {
  position: relative;
  overflow: hidden;
  border: 1px solid #e9edf5;
  background: linear-gradient(180deg, #ffffff 0%, #f8faff 100%);
}

.risk-card::before {
  content: '';
  position: absolute;
  right: -46px;
  top: -46px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(211, 84, 0, 0.08);
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.risk-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3e62;
}

.risk-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8aa3;
}

.risk-kpi-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 8px;
}

.risk-kpi {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #edf1f8;
  background: #fff;
}

.risk-kpi span {
  font-size: 12px;
  color: #76849c;
}

.risk-kpi strong {
  display: block;
  margin-top: 4px;
  font-size: 18px;
  color: #d35400;
  line-height: 1.1;
}

.risk-chart-box {
  height: 272px;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-mid {
  height: 290px;
}

.ai-card {
  min-height: 330px;
}

.ai-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #55657f;
  margin-bottom: 10px;
}

.ai-summary {
  line-height: 1.7;
  color: #2c3e50;
}

.ai-suggestion {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f0f7ff;
  color: #2d517a;
}

.warnings-box {
  margin-top: 10px;
  border-top: 1px dashed #d8e2f0;
  padding-top: 10px;
}

.warning-title {
  font-size: 13px;
  color: #4d5f7f;
  margin-bottom: 8px;
}

.warning-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f3f8;
}

.warning-name {
  margin-left: 8px;
  font-weight: 600;
  color: #324866;
}

.warning-desc {
  margin-top: 5px;
  color: #6b7a95;
  line-height: 1.5;
}

.detail-card {
  margin-bottom: 14px;
}

@media (max-width: 768px) {
  .kpi-value {
    font-size: 18px;
  }

  .chart-box,
  .chart-mid {
    height: 260px;
  }
}
</style>
