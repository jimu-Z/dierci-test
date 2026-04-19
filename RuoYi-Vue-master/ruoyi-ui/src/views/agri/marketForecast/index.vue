<template>
  <div class="app-container">
    <el-card shadow="never" class="forecast-hero mb16">
      <div class="forecast-head">
        <div>
          <div class="forecast-title">市场预测决策台</div>
          <div class="forecast-desc">围绕市场区域、品类走势和预测置信度做决策复核，优先处理低置信度或异常预测任务。</div>
        </div>
        <div class="forecast-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-view" @click="handleReview">智能复核</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="forecast-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="forecast-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="forecast-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="forecast-panel">
            <div class="panel-head"><span>预测热点</span><small>优先处理低置信度或待预测任务</small></div>
            <div class="forecast-hot-list" v-if="hotRows.length">
              <div v-for="item in hotRows" :key="item.forecastId" class="forecast-hot-item" @click="handleReview(item)">
                <div>
                  <b>{{ item.productName }}</b>
                  <p>{{ item.marketArea }} · {{ item.productCode }}</p>
                </div>
                <el-tag size="mini" type="warning">{{ formatConfidence(item.confidenceRate) }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无热点任务" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="forecast-panel">
            <div class="panel-head"><span>智能复核</span><small>根据预测结果给出业务建议</small></div>
            <div v-if="smartResult.forecastId" class="forecast-box" v-loading="reviewLoading">
                <div class="forecast-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
                <p>{{ smartResult.forecast && smartResult.forecast.productName }}</p>
                <div class="forecast-section">
                  <div class="forecast-section-title">AI建议</div>
                  <ul v-if="smartResult.suggestions && smartResult.suggestions.length" class="forecast-suggestion-list">
                    <li v-for="item in smartResult.suggestions" :key="item">{{ item }}</li>
                  </ul>
                  <div v-else class="forecast-empty">暂无建议</div>
                </div>
                <div class="forecast-section" v-if="smartResult.aiOriginalExcerpt">
                  <div class="forecast-section-title">AI原文摘录</div>
                  <pre class="forecast-excerpt">{{ smartResult.aiOriginalExcerpt }}</pre>
                </div>
                <div class="forecast-footnote" v-if="smartResult.requestTime">最近复核时间：{{ smartResult.requestTime }}</div>
            </div>
            <el-empty v-else description="请选择一条预测任务进行复核" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="市场区域" prop="marketArea">
        <el-input v-model="queryParams.marketArea" placeholder="请输入市场区域" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品编码" prop="productCode">
        <el-input v-model="queryParams.productCode" placeholder="请输入产品编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="预测状态" prop="forecastStatus">
        <el-select v-model="queryParams.forecastStatus" clearable style="width: 140px">
          <el-option v-for="item in forecastStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:marketForecast:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-video-play" size="mini" :disabled="single" @click="handleInvoke" v-hasPermi="['agri:marketForecast:edit']">调用AI</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-data-analysis" size="mini" :disabled="single" @click="handlePredict" v-hasPermi="['agri:marketForecast:edit']">执行预测</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:marketForecast:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:marketForecast:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="forecastList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="forecastId" align="center" width="70" />
      <el-table-column label="市场区域" prop="marketArea" align="center" width="110" />
      <el-table-column label="产品编码" prop="productCode" align="center" width="120" />
      <el-table-column label="产品名称" prop="productName" align="center" width="120" />
      <el-table-column label="历史销量(kg)" prop="historicalSalesKg" align="center" width="110" />
      <el-table-column label="预测销量(kg)" prop="forecastSalesKg" align="center" width="110" />
      <el-table-column label="预测价格" prop="forecastPrice" align="center" width="100" />
      <el-table-column label="置信度" prop="confidenceRate" align="center" width="90" />
      <el-table-column label="预测摘要" prop="remark" align="left" min-width="280">
        <template slot-scope="scope"><div class="agri-text-cell">{{ scope.row.remark }}</div></template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">{{ formatStatus(scope.row.forecastStatus) }}</template>
      </el-table-column>
      <el-table-column label="预测时间" prop="forecastTime" align="center" width="165">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.forecastTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleReview(scope.row)" v-hasPermi="['agri:marketForecast:query']">复核</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:marketForecast:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:marketForecast:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="市场区域" prop="marketArea"><el-input v-model="form.marketArea" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="周期开始" prop="periodStart"><el-date-picker v-model="form.periodStart" type="date" value-format="yyyy-MM-dd" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="周期结束" prop="periodEnd"><el-date-picker v-model="form.periodEnd" type="date" value-format="yyyy-MM-dd" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="历史销量" prop="historicalSalesKg"><el-input-number v-model="form.historicalSalesKg" :precision="2" :step="10" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测状态" prop="forecastStatus"><el-select v-model="form.forecastStatus" style="width: 100%"><el-option v-for="item in forecastStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="执行市场预测" :visible.sync="predictOpen" width="540px" append-to-body>
      <el-form ref="predictForm" :model="predictForm" :rules="predictRules" label-width="110px">
        <el-form-item label="任务ID"><el-input v-model="predictForm.forecastId" disabled /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="predictForm.modelVersion" /></el-form-item>
        <el-form-item label="预测销量(kg)" prop="forecastSalesKg"><el-input-number v-model="predictForm.forecastSalesKg" :precision="2" :step="10" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="预测价格(元/kg)" prop="forecastPrice"><el-input-number v-model="predictForm.forecastPrice" :precision="2" :step="0.1" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="置信度" prop="confidenceRate"><el-input-number v-model="predictForm.confidenceRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPredict">确 定</el-button>
        <el-button @click="predictOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listMarketForecast,
  getMarketForecast,
  addMarketForecast,
  updateMarketForecast,
  invokeMarketForecast,
  predictMarketForecast,
  delMarketForecast,
  getMarketForecastDashboard,
  getMarketForecastReview
} from '@/api/agri/marketForecast'

export default {
  name: 'MarketForecast',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      reviewLoading: false,
      total: 0,
      forecastList: [],
      dashboardData: {},
      smartResult: {
        forecastId: null,
        riskScore: '--',
        riskLevel: '--',
        forecast: null,
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      },
      title: '',
      open: false,
      predictOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        marketArea: undefined,
        productCode: undefined,
        forecastStatus: undefined
      },
      form: {},
      predictForm: {
        forecastId: undefined,
        modelVersion: 'market-model-v1',
        forecastSalesKg: undefined,
        forecastPrice: undefined,
        confidenceRate: 0.82
      },
      forecastStatusOptions: [
        { label: '待预测', value: '0' },
        { label: '已预测', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        marketArea: [{ required: true, message: '市场区域不能为空', trigger: 'blur' }],
        productCode: [{ required: true, message: '产品编码不能为空', trigger: 'blur' }],
        productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
        periodStart: [{ required: true, message: '预测周期开始不能为空', trigger: 'change' }],
        periodEnd: [{ required: true, message: '预测周期结束不能为空', trigger: 'change' }]
      },
      predictRules: {
        modelVersion: [{ required: true, message: '模型版本不能为空', trigger: 'blur' }]
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
        { key: 'total', label: '任务总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录' },
        { key: 'predicted', label: '已预测', value: summary.predictedCount || 0, foot: '已执行预测' },
        { key: 'pending', label: '待预测', value: summary.pendingCount || 0, foot: '待执行任务' },
        { key: 'avgSales', label: '平均销量', value: this.formatMetric(summary.avgSales, 'kg'), foot: '预测销量均值' },
        { key: 'avgPrice', label: '平均价格', value: this.formatMetric(summary.avgPrice, '元/kg'), foot: '预测价格均值' }
      ]
    },
    hotRows() {
      return this.dashboardData.hotRows || []
    }
  },
  methods: {
    refreshDashboard() {
      getMarketForecastDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listMarketForecast(this.queryParams).then(response => {
        this.forecastList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatMetric(value, unit) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${number.toFixed(2)}${unit}`
    },
    formatConfidence(value) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${(number * 100).toFixed(1)}%`
    },
    formatStatus(value) {
      const option = this.forecastStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        forecastId: undefined,
        marketArea: undefined,
        productCode: undefined,
        productName: undefined,
        periodStart: undefined,
        periodEnd: undefined,
        historicalSalesKg: undefined,
        forecastSalesKg: undefined,
        forecastPrice: undefined,
        confidenceRate: undefined,
        modelVersion: 'market-model-v1',
        forecastStatus: '0',
        status: '0',
        remark: undefined
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
      this.ids = selection.map(item => item.forecastId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增市场预测分析'
    },
    handleUpdate(row) {
      this.reset()
      const forecastId = row.forecastId || this.ids
      getMarketForecast(forecastId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改市场预测分析'
      })
    },
    handleInvoke(row) {
      const forecastId = row.forecastId || this.ids[0]
      if (!forecastId) {
        this.$modal.msgWarning('请选择一条预测任务调用AI')
        return
      }
      this.$modal
        .confirm('是否确认调用市场预测AI任务编号为"' + forecastId + '"的数据项？')
        .then(() => invokeMarketForecast(forecastId))
        .then(() => {
          this.$modal.msgSuccess('AI调用成功')
          this.getList()
        })
        .catch(() => {})
    },
    handleReview(row) {
      const target = row || this.forecastList[0]
      if (!target || !target.forecastId) {
        this.$modal.msgWarning('请先选择一条预测任务')
        return
      }
      this.reviewLoading = true
      this.smartResult = {
        forecastId: target.forecastId,
        riskScore: '--',
        riskLevel: '--',
        forecast: target,
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      }
      getMarketForecastReview(target.forecastId).then(response => {
        const data = response.data || {}
        const suggestions = Array.isArray(data.suggestions)
          ? data.suggestions.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
          : []
        this.smartResult = {
          forecastId: data.forecastId || target.forecastId,
          riskScore: data.riskScore != null ? data.riskScore : '--',
          riskLevel: data.riskLevel || '--',
          forecast: data.forecast || target,
          suggestions,
          aiOriginalExcerpt: data.aiOriginalExcerpt || '',
          requestTime: new Date().toLocaleString()
        }
        this.$modal.msgSuccess('智能复核已更新')
      }).finally(() => {
        this.reviewLoading = false
      })
    },
    handlePredict() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条预测任务执行预测')
        return
      }
      const selected = this.forecastList.find(item => item.forecastId === this.ids[0])
      this.predictForm = {
        forecastId: selected.forecastId,
        modelVersion: selected.modelVersion || 'market-model-v1',
        forecastSalesKg: selected.forecastSalesKg,
        forecastPrice: selected.forecastPrice,
        confidenceRate: selected.confidenceRate || 0.82
      }
      this.predictOpen = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.forecastId !== undefined) {
          updateMarketForecast(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addMarketForecast(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    submitPredict() {
      this.$refs.predictForm.validate(valid => {
        if (!valid) {
          return
        }
        predictMarketForecast(this.predictForm).then(() => {
          this.$modal.msgSuccess('预测回写成功')
          this.predictOpen = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const forecastIds = row.forecastId || this.ids
      this.$modal
        .confirm('是否确认删除市场预测分析编号为"' + forecastIds + '"的数据项？')
        .then(function () {
          return delMarketForecast(forecastIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/marketForecast/export',
        {
          ...this.queryParams
        },
        `market_forecast_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.agri-text-cell {
  white-space: normal;
  word-break: break-word;
  line-height: 20px;
  text-align: left;
}

.forecast-hero {
  border: 1px solid #e6e0d7;
  background: linear-gradient(135deg, #fffaf4 0%, #f6efe2 100%);
}

.forecast-head,
.panel-head,
.forecast-hot-item,
.forecast-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forecast-title {
  font-size: 20px;
  font-weight: 700;
  color: #7a4e12;
}

.forecast-desc {
  margin-top: 6px;
  color: #8a6e42;
}

.forecast-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.forecast-kpi-row,
.forecast-overview-row {
  margin-top: 14px;
}

.forecast-kpi-card,
.forecast-panel {
  border: 1px solid #eadfcf;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.forecast-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.forecast-kpi-card span,
.panel-head small,
.forecast-kpi-card small,
.forecast-hot-item p,
.forecast-box p {
  color: #8c7558;
}

.forecast-kpi-card strong {
  max-height: 340px;
  overflow-y: auto;
  display: block;

.forecast-section {
  margin-top: 12px;
}

.forecast-section-title {
  margin-bottom: 8px;
  color: #7a2d18;
  font-weight: 600;
}

.forecast-suggestion-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 6px;
}

.forecast-suggestion-list li {
  line-height: 1.5;
  word-break: break-word;
}

.forecast-excerpt {
  border-radius: 8px;
  border: 1px solid #f0d9d9;
  background: #fff7f7;
  color: #b44d4b;
  padding: 10px;
  margin: 0;
  max-height: 140px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
}

.forecast-empty {
  color: #9a8a80;
}

.forecast-footnote {
  margin-top: 10px;
  font-size: 12px;
  color: #9a8a80;
}
  margin: 8px 0 4px;
  font-size: 22px;
  color: #7a4e12;
}

.forecast-hot-item {
  padding: 8px 0;
  border-top: 1px solid #f2eadf;
  cursor: pointer;
}

.forecast-hot-item p {
  margin: 4px 0 0;
}

.forecast-score strong {
  font-size: 26px;
  color: #b17318;
}

.forecast-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #6f5a3f;
}
</style>
