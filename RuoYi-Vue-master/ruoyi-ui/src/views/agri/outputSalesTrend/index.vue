<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
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
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="el-icon-data-analysis" size="mini" @click="refreshDashboard">AI重算预测</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="kpi-row" v-loading="dashboardLoading">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-title">平均产量(吨)</div>
          <div class="kpi-value">{{ kpi.avgOutput }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-title">平均销量(万元)</div>
          <div class="kpi-value">{{ kpi.avgSales }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-title">产量累计增幅</div>
          <div class="kpi-value">{{ kpi.outputGrowth }}%</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-title">风险等级</div>
          <div class="kpi-value">{{ ai.riskLevel || kpi.riskLevel || '中' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">产量与销量趋势（历史 + 预测）</div>
          <div ref="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="chart-header">病虫害高风险地块</div>
          <div ref="riskChart" class="chart-box risk-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="ai-card">
      <div slot="header" class="chart-header">AI预测洞察</div>
      <div class="ai-meta">
        <span>置信度：{{ ai.confidenceRate || kpi.confidenceRate || 0 }}</span>
        <span>模型：{{ ai.modelVersion || 'rule-v1' }}</span>
      </div>
      <div class="ai-summary">{{ ai.summary || '暂无AI洞察' }}</div>
      <div class="ai-suggestion" v-if="ai.suggestion">建议：{{ ai.suggestion }}</div>
    </el-card>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:outputSalesTrend:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:outputSalesTrend:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:outputSalesTrend:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:outputSalesTrend:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="trendList" @selection-change="handleSelectionChange">
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

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

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
      trendChart: null,
      riskChart: null,
      dashboard: {
        history: [],
        forecast: [],
        pestRisk: []
      },
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
  methods: {
    getList() {
      this.loading = true
      listOutputSalesTrend(this.queryParams).then(response => {
        this.trendList = response.rows
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
    refreshDashboard() {
      this.dashboardLoading = true
      getOutputSalesDashboard({
        beginStatDate: this.queryParams.beginStatDate,
        endStatDate: this.queryParams.endStatDate,
        futureMonths: this.futureMonths
      }).then(response => {
        const data = response.data || {}
        this.dashboard = {
          history: data.history || [],
          forecast: data.forecast || [],
          pestRisk: data.pestRisk || []
        }
        this.kpi = data.kpi || this.kpi
        this.ai = data.ai || this.ai
        this.$nextTick(() => {
          this.renderTrendChart()
          this.renderRiskChart()
        })
      }).finally(() => {
        this.dashboardLoading = false
      })
    },
    initTrendChart() {
      if (!this.trendChart) {
        this.trendChart = echarts.init(this.$refs.trendChart, 'macarons')
      }
    },
    initRiskChart() {
      if (!this.riskChart) {
        this.riskChart = echarts.init(this.$refs.riskChart, 'macarons')
      }
    },
    renderTrendChart() {
      this.initTrendChart()
      const history = this.dashboard.history || []
      const forecast = this.dashboard.forecast || []
      const months = history.map(item => item.statMonth).concat(forecast.map(item => item.statMonth))
      const historyOutput = history.map(item => item.outputValue)
      const historySales = history.map(item => item.salesValue)
      const forecastOutput = new Array(history.length).fill(null).concat(forecast.map(item => item.outputValue))
      const forecastSales = new Array(history.length).fill(null).concat(forecast.map(item => item.salesValue))

      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['历史产量', '历史销量', '预测产量', '预测销量'] },
        grid: { left: '4%', right: '5%', bottom: '6%', containLabel: true },
        xAxis: [{ type: 'category', boundaryGap: false, data: months }],
        yAxis: [{ type: 'value', name: '数值' }],
        series: [
          { name: '历史产量', type: 'line', smooth: true, data: historyOutput },
          { name: '历史销量', type: 'line', smooth: true, data: historySales },
          { name: '预测产量', type: 'line', smooth: true, lineStyle: { type: 'dashed' }, data: forecastOutput },
          { name: '预测销量', type: 'line', smooth: true, lineStyle: { type: 'dashed' }, data: forecastSales }
        ]
      })
    },
    renderRiskChart() {
      this.initRiskChart()
      const pestRisk = this.dashboard.pestRisk || []
      this.riskChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '8%', right: '5%', bottom: '6%', containLabel: true },
        xAxis: { type: 'value' },
        yAxis: {
          type: 'category',
          data: pestRisk.map(item => item.plotCode)
        },
        series: [
          {
            type: 'bar',
            data: pestRisk.map(item => item.count),
            itemStyle: { color: '#e67e22' }
          }
        ]
      })
    },
    handleResize() {
      if (this.trendChart) {
        this.trendChart.resize()
      }
      if (this.riskChart) {
        this.riskChart.resize()
      }
    },
    disposeCharts() {
      if (this.trendChart) {
        this.trendChart.dispose()
        this.trendChart = null
      }
      if (this.riskChart) {
        this.riskChart.dispose()
        this.riskChart = null
      }
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.trendId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
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
.kpi-row {
  margin-bottom: 12px;
}

.kpi-card {
  margin-bottom: 12px;
}

.kpi-title {
  color: #666;
  font-size: 13px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2d3d;
}

.chart-row {
  margin-bottom: 12px;
}

.chart-card {
  margin-bottom: 12px;
}

.chart-header {
  font-size: 14px;
  font-weight: 600;
}

.chart-box {
  width: 100%;
  height: 340px;
}

.risk-box {
  height: 340px;
}

.ai-card {
  margin-bottom: 14px;
}

.ai-meta {
  display: flex;
  justify-content: space-between;
  color: #666;
  margin-bottom: 8px;
}

.ai-summary {
  line-height: 1.7;
  color: #2c3e50;
}

.ai-suggestion {
  margin-top: 8px;
  color: #34495e;
}
</style>
