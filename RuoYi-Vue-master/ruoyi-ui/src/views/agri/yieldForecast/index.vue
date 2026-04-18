<template>
  <div class="app-container yield-hub">
    <div class="hero" v-loading="opsLoading">
      <div class="hero-main">
        <div class="hero-label">产量预测智能中枢</div>
        <h2>融合地块、传感器、病虫害与农事数据，给出智能预测与行动建议</h2>
        <p>本页默认展示趋势折线与地块对比柱图，支持一键调用 DeepSeek，并返回部分 AI 原文摘录用于复核。</p>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增任务</el-button>
          <el-button type="success" icon="el-icon-video-play" :disabled="!focusTask.forecastId" @click="handleInvoke(focusTask)">智能预测</el-button>
          <el-button type="warning" icon="el-icon-cpu" :disabled="!focusTask.forecastId" @click="handleSmartAssess(focusTask)">智能评估</el-button>
          <el-button icon="el-icon-refresh" @click="refreshAll">刷新</el-button>
        </div>
      </div>
      <div class="hero-kpi">
        <div class="kpi-item"><span>任务总数</span><strong>{{ opsKpi.total || 0 }}</strong></div>
        <div class="kpi-item"><span>预测完成率</span><strong>{{ toPercent(opsKpi.predictRate) }}</strong></div>
        <div class="kpi-item"><span>总面积(亩)</span><strong>{{ formatNum(opsKpi.totalArea) }}</strong></div>
        <div class="kpi-item"><span>平均亩产(kg/亩)</span><strong>{{ formatNum(opsKpi.avgYieldPerMu) }}</strong></div>
        <div class="kpi-item danger"><span>高风险地块</span><strong>{{ opsKpi.highRiskCount || 0 }}</strong></div>
      </div>
    </div>

    <el-row :gutter="14" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="card-head">
            <span>近10次预测趋势折线</span>
            <span class="sub">单位：kg</span>
          </div>
          <div ref="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="card-head">
            <span>地块亩产对比柱图</span>
            <span class="sub">单位：kg/亩</span>
          </div>
          <div ref="barChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="ops-row">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="ops-card">
          <div slot="header" class="card-head">
            <span>预测压力地块</span>
            <span class="sub">按风险分排序，优先处理高风险与大面积任务</span>
          </div>
          <el-table :data="pressureQueue" size="mini">
            <el-table-column label="地块" prop="plotCode" width="110" />
            <el-table-column label="作物" prop="cropName" width="110" />
            <el-table-column label="季节" prop="season" width="90" />
            <el-table-column label="面积(亩)" width="90"><template slot-scope="scope">{{ formatNum(scope.row.areaMu) }}</template></el-table-column>
            <el-table-column label="预测产量(kg)" width="120"><template slot-scope="scope">{{ formatNum(scope.row.forecastYieldKg) }}</template></el-table-column>
            <el-table-column label="亩产" width="100"><template slot-scope="scope">{{ formatNum(scope.row.yieldPerMu) }}</template></el-table-column>
            <el-table-column label="状态" width="90"><template slot-scope="scope"><el-tag :type="statusTagType(scope.row.forecastStatus)" size="mini">{{ scope.row.statusLabel }}</el-tag></template></el-table-column>
            <el-table-column label="风险" width="80"><template slot-scope="scope"><el-tag :type="riskTagType(scope.row.riskLevel)" size="mini">{{ scope.row.riskLevel }}</el-tag></template></el-table-column>
            <el-table-column label="处置理由" prop="riskReason" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="190" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="handleInvoke(scope.row)">智能预测</el-button>
                <el-button type="text" size="mini" @click="handleSmartAssess(scope.row)">智能评估</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="ops-card">
          <div slot="header" class="card-head">
            <span>聚焦地块</span>
            <span class="sub">当前最高风险任务</span>
          </div>
          <div v-if="focusTask.forecastId" class="focus-box">
            <div class="focus-title">{{ focusTask.plotCode }} · {{ focusTask.cropName }}</div>
            <div class="focus-line"><span>季节</span><strong>{{ focusTask.season || '-' }}</strong></div>
            <div class="focus-line"><span>面积</span><strong>{{ formatNum(focusTask.areaMu) }} 亩</strong></div>
            <div class="focus-line"><span>预测产量</span><strong>{{ formatNum(focusTask.forecastYieldKg) }} kg</strong></div>
            <div class="focus-line"><span>亩产</span><strong>{{ formatNum(focusTask.yieldPerMu) }} kg/亩</strong></div>
            <div class="focus-line"><span>风险分</span><strong>{{ focusTask.riskScore || 0 }}</strong></div>
            <div class="focus-actions">
              <el-button type="primary" size="mini" @click="handleInvoke(focusTask)">智能预测</el-button>
              <el-button size="mini" @click="handleSmartAssess(focusTask)">智能评估</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无聚焦地块" :image-size="80" />
        </el-card>

        <el-card shadow="never" class="ops-card">
          <div slot="header" class="card-head">
            <span>运营建议</span>
            <span class="sub">自动生成</span>
          </div>
          <div v-for="item in suggestions" :key="item.title" class="sg-item">
            <div class="sg-head">
              <strong>{{ item.title }}</strong>
              <el-tag size="mini" :type="priorityTagType(item.priority)">{{ item.priority }}</el-tag>
            </div>
            <div class="sg-content">{{ item.content }}</div>
          </div>
          <el-empty v-if="!suggestions.length" description="暂无建议" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px" class="query-form">
      <el-form-item label="地块编码" prop="plotCode"><el-input v-model="queryParams.plotCode" placeholder="请输入地块编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="作物名称" prop="cropName"><el-input v-model="queryParams.cropName" placeholder="请输入作物名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" /></el-form-item>
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
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:yieldForecast:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate()" v-hasPermi="['agri:yieldForecast:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-cpu" size="mini" :disabled="single" @click="handleSmartAssess()" v-hasPermi="['agri:yieldForecast:query']">评估</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['agri:yieldForecast:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:yieldForecast:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="forecastId" align="center" width="70" />
      <el-table-column label="地块" prop="plotCode" align="center" width="110" />
      <el-table-column label="作物" prop="cropName" align="center" width="110" />
      <el-table-column label="季节" prop="season" align="center" width="90" />
      <el-table-column label="面积(亩)" prop="areaMu" align="center" width="90" />
      <el-table-column label="预测产量(kg)" prop="forecastYieldKg" align="center" width="120" />
      <el-table-column label="状态" align="center" width="100"><template slot-scope="scope"><el-tag :type="statusTagType(scope.row.forecastStatus)" size="mini">{{ formatForecastStatus(scope.row.forecastStatus) }}</el-tag></template></el-table-column>
      <el-table-column label="模型版本" prop="modelVersion" align="center" width="120" />
      <el-table-column label="预测说明" prop="remark" align="left" min-width="240" show-overflow-tooltip />
      <el-table-column label="预测时间" prop="forecastTime" align="center" width="170"><template slot-scope="scope"><span>{{ parseTime(scope.row.forecastTime) }}</span></template></el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:yieldForecast:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-video-play" @click="handleInvoke(scope.row)" v-hasPermi="['agri:yieldForecast:edit']">智能预测</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handlePredict(scope.row)" v-hasPermi="['agri:yieldForecast:edit']">回写</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:yieldForecast:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12"><el-form-item label="地块编码" prop="plotCode"><el-input v-model="form.plotCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作物名称" prop="cropName"><el-input v-model="form.cropName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="季节" prop="season"><el-input v-model="form.season" placeholder="春季/夏季" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="播种日期" prop="sowDate"><el-date-picker v-model="form.sowDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="面积(亩)" prop="areaMu"><el-input-number v-model="form.areaMu" :precision="2" :step="0.1" :min="0.1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测状态" prop="forecastStatus"><el-select v-model="form.forecastStatus" style="width: 100%"><el-option v-for="item in forecastStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预测产量" prop="forecastYieldKg"><el-input-number v-model="form.forecastYieldKg" :precision="2" :step="1" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="预测说明"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="建议保留关键参数和判断理由" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="执行回写" :visible.sync="predictOpen" width="520px" append-to-body>
      <el-form ref="predictForm" :model="predictForm" :rules="predictRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="predictForm.forecastId" disabled /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="predictForm.modelVersion" /></el-form-item>
        <el-form-item label="预测产量(kg)" prop="forecastYieldKg"><el-input-number v-model="predictForm.forecastYieldKg" :precision="2" :step="1" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPredict">确 定</el-button>
        <el-button @click="predictOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="智能评估" :visible.sync="assessOpen" width="760px" append-to-body>
      <el-skeleton v-if="assessLoading" :rows="6" animated />
      <div v-else class="assess-wrap">
        <el-row :gutter="12" class="assess-row">
          <el-col :span="8"><el-card shadow="never" class="assess-card"><div class="lb">可靠性分</div><div class="val">{{ assessResult.reliabilityScore || 0 }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="never" class="assess-card"><div class="lb">风险等级</div><div class="val">{{ assessResult.riskLevel || '-' }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="never" class="assess-card"><div class="lb">引擎</div><div class="val small">{{ assessResult.analysisEngine || '-' }}</div></el-card></el-col>
        </el-row>
        <el-alert :title="assessResult.summary || '暂无结果'" :type="riskAlertType(assessResult.riskLevel)" :closable="false" />
        <div class="metric">预测亩产：{{ formatNum(assessResult.yieldPerMu) }} kg/亩，季节基线：{{ formatNum(assessResult.benchmarkPerMu) }} kg/亩</div>
        <div class="metric">DeepSeek状态：{{ assessResult.deepseekMessage || '-' }}</div>
        <div class="ttl">建议动作</div>
        <el-tag v-for="item in assessResult.suggestionList || []" :key="item" class="tag" size="mini" type="success">{{ item }}</el-tag>
        <div class="ttl">异常因子</div>
        <el-tag v-for="item in assessResult.factors || []" :key="item" class="tag" size="mini" type="warning">{{ item }}</el-tag>
        <div class="ttl">AI原文摘录</div>
        <div class="excerpt">{{ assessResult.aiOriginalExcerpt || '本次未返回原文摘录' }}</div>
      </div>
      <div slot="footer" class="dialog-footer"><el-button @click="assessOpen = false">关 闭</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import {
  listYieldForecast,
  getYieldForecast,
  addYieldForecast,
  updateYieldForecast,
  invokeYieldForecast,
  predictYieldForecast,
  delYieldForecast,
  getYieldForecastDashboardOps,
  smartAssessYieldForecast
} from '@/api/agri/yieldForecast'

export default {
  name: 'YieldForecast',
  data() {
    return {
      loading: true,
      opsLoading: false,
      assessOpen: false,
      assessLoading: false,
      assessResult: {},
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      taskList: [],
      pressureQueue: [],
      focusTask: {},
      suggestions: [],
      opsKpi: {},
      trendSeries: [],
      plotBarSeries: [],
      chartRefs: {
        trendChart: null,
        barChart: null
      },
      title: '',
      open: false,
      predictOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plotCode: undefined,
        cropName: undefined,
        forecastStatus: undefined
      },
      form: {},
      predictForm: {
        forecastId: undefined,
        modelVersion: 'yield-model-v1',
        forecastYieldKg: undefined
      },
      forecastStatusOptions: [
        { label: '待预测', value: '0' },
        { label: '已预测', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        cropName: [{ required: true, message: '作物名称不能为空', trigger: 'blur' }],
        season: [{ required: true, message: '季节不能为空', trigger: 'blur' }],
        sowDate: [{ required: true, message: '播种日期不能为空', trigger: 'change' }],
        areaMu: [{ required: true, message: '种植面积不能为空', trigger: 'change' }]
      },
      predictRules: {
        modelVersion: [{ required: true, message: '模型版本不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadOps()
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
      listYieldForecast(this.queryParams)
        .then(response => {
          this.taskList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadOps() {
      this.opsLoading = true
      getYieldForecastDashboardOps()
        .then(response => {
          const data = response.data || {}
          this.opsKpi = data.kpi || {}
          this.pressureQueue = data.pressureQueue || []
          this.focusTask = data.focus || {}
          this.suggestions = data.suggestions || []
          this.trendSeries = data.trendSeries || []
          this.plotBarSeries = data.plotBarSeries || []
          this.$nextTick(() => {
            this.renderCharts()
          })
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    refreshAll() {
      this.getList()
      this.loadOps()
    },
    ensureChart(refName) {
      if (!this.chartRefs[refName] && this.$refs[refName]) {
        this.chartRefs[refName] = echarts.init(this.$refs[refName], 'macarons')
      }
      return this.chartRefs[refName]
    },
    renderCharts() {
      this.renderTrendChart()
      this.renderBarChart()
    },
    renderTrendChart() {
      const chart = this.ensureChart('trendChart')
      if (!chart) return
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '4%', right: '4%', bottom: '8%', containLabel: true },
        xAxis: { type: 'category', data: this.trendSeries.map(item => item.name) },
        yAxis: { type: 'value', name: 'kg' },
        series: [
          {
            name: '预测产量',
            type: 'line',
            smooth: true,
            symbolSize: 8,
            areaStyle: { opacity: 0.15 },
            data: this.trendSeries.map(item => Number(item.value || 0))
          }
        ]
      })
    },
    renderBarChart() {
      const chart = this.ensureChart('barChart')
      if (!chart) return
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '8%', right: '4%', bottom: '8%', containLabel: true },
        xAxis: { type: 'value', name: 'kg/亩' },
        yAxis: { type: 'category', data: this.plotBarSeries.map(item => item.name) },
        series: [
          {
            name: '亩产',
            type: 'bar',
            itemStyle: { color: '#0f766e' },
            data: this.plotBarSeries.map(item => Number(item.value || 0))
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
    formatForecastStatus(value) {
      const option = this.forecastStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    statusTagType(status) {
      if (status === '1') return 'success'
      if (status === '2') return 'danger'
      return 'warning'
    },
    riskTagType(level) {
      if (level === '高') return 'danger'
      if (level === '中') return 'warning'
      return 'success'
    },
    priorityTagType(priority) {
      if (priority === '高') return 'danger'
      if (priority === '中') return 'warning'
      return 'success'
    },
    riskAlertType(level) {
      if (level === '高') return 'error'
      if (level === '中') return 'warning'
      return 'success'
    },
    toPercent(value) {
      if (value === undefined || value === null) return '0%'
      return `${Number(value).toFixed(1)}%`
    },
    formatNum(value) {
      if (value === undefined || value === null || value === '') return '-'
      return Number(value).toFixed(2)
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        forecastId: undefined,
        plotCode: undefined,
        cropName: undefined,
        season: undefined,
        sowDate: undefined,
        areaMu: undefined,
        forecastYieldKg: undefined,
        modelVersion: 'yield-model-v1',
        forecastStatus: '0',
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      this.loadOps()
    },
    resetQuery() {
      this.resetForm('queryForm')
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
      this.title = '新增产量预测任务'
    },
    handleUpdate(row) {
      this.reset()
      const forecastId = row && row.forecastId ? row.forecastId : this.ids[0]
      if (!forecastId) {
        this.$modal.msgWarning('请选择一条任务')
        return
      }
      getYieldForecast(forecastId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改产量预测任务'
      })
    },
    handlePredict(row) {
      const current = row && row.forecastId ? row : this.taskList.find(item => item.forecastId === this.ids[0])
      if (!current) {
        this.$modal.msgWarning('请选择一条任务执行回写')
        return
      }
      this.predictForm = {
        forecastId: current.forecastId,
        modelVersion: current.modelVersion || 'yield-model-v1',
        forecastYieldKg: current.forecastYieldKg
      }
      this.predictOpen = true
    },
    handleInvoke(row) {
      const forecastId = row && row.forecastId ? row.forecastId : this.ids[0]
      if (!forecastId) {
        this.$modal.msgWarning('请选择一条任务调用智能预测')
        return
      }
      this.$modal
        .confirm(`是否确认调用智能预测任务编号为"${forecastId}"的数据项？`)
        .then(() => invokeYieldForecast(forecastId))
        .then(response => {
          const data = response.data || {}
          this.$modal.msgSuccess(`预测完成，产量 ${this.formatNum(data.forecastYieldKg)} kg`)
          if (data.aiOriginalExcerpt) {
            this.$message({
              type: 'info',
              message: `AI原文摘录：${data.aiOriginalExcerpt}`,
              duration: 5000
            })
          }
          this.getList()
          this.loadOps()
        })
        .catch(() => {})
    },
    handleSmartAssess(row) {
      const forecastId = row && row.forecastId ? row.forecastId : this.ids[0]
      if (!forecastId) {
        this.$modal.msgWarning('请选择一条任务执行智能评估')
        return
      }
      this.assessOpen = true
      this.assessLoading = true
      this.assessResult = {}
      smartAssessYieldForecast(forecastId)
        .then(response => {
          this.assessResult = response.data || {}
        })
        .finally(() => {
          this.assessLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.forecastId !== undefined) {
          updateYieldForecast(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.loadOps()
          })
          return
        }
        addYieldForecast(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
          this.loadOps()
        })
      })
    },
    submitPredict() {
      this.$refs.predictForm.validate(valid => {
        if (!valid) return
        predictYieldForecast(this.predictForm).then(() => {
          this.$modal.msgSuccess('回写成功')
          this.predictOpen = false
          this.getList()
          this.loadOps()
        })
      })
    },
    handleDelete(row) {
      const forecastIds = row && row.forecastId ? row.forecastId : this.ids
      this.$modal
        .confirm(`是否确认删除产量预测任务编号为"${forecastIds}"的数据项？`)
        .then(() => delYieldForecast(forecastIds))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.getList()
          this.loadOps()
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/yieldForecast/export',
        {
          ...this.queryParams
        },
        `yield_forecast_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.yield-hub {
  background: linear-gradient(180deg, #edf7f5 0%, #ffffff 36%);
}

.hero {
  background: linear-gradient(120deg, #0f766e, #0e7490 52%, #164e63);
  color: #ecfeff;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.hero-main {
  flex: 1;
  min-width: 320px;
}

.hero-label {
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.9;
}

.hero-main h2 {
  margin: 8px 0;
}

.hero-main p {
  margin: 0;
  opacity: 0.9;
}

.hero-actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-kpi {
  width: 360px;
  display: grid;
  grid-template-columns: repeat(2, minmax(150px, 1fr));
  gap: 8px;
}

.kpi-item {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 10px;
  padding: 10px;
}

.kpi-item span {
  font-size: 12px;
}

.kpi-item strong {
  display: block;
  margin-top: 4px;
  font-size: 22px;
}

.kpi-item.danger {
  background: rgba(254, 202, 202, 0.2);
}

.chart-row,
.ops-row {
  margin-bottom: 12px;
}

.chart-card,
.ops-card {
  border-radius: 12px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub {
  font-size: 12px;
  color: #64748b;
}

.chart-box {
  height: 290px;
}

.focus-box {
  background: #f0fdfa;
  border: 1px solid #99f6e4;
  border-radius: 10px;
  padding: 10px;
}

.focus-title {
  color: #0f766e;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.focus-line {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px dashed #bfdbfe;
  padding: 6px 0;
}

.focus-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.sg-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 8px;
}

.sg-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sg-content {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #475569;
}

.assess-row {
  margin-bottom: 12px;
}

.assess-card {
  text-align: center;
}

.lb {
  color: #64748b;
  font-size: 12px;
}

.val {
  margin-top: 4px;
  font-size: 22px;
  font-weight: 700;
}

.val.small {
  font-size: 14px;
  word-break: break-all;
}

.metric {
  margin-top: 10px;
  background: #f0fdfa;
  border: 1px solid #99f6e4;
  border-radius: 8px;
  padding: 8px;
  font-size: 13px;
  color: #115e59;
}

.ttl {
  margin-top: 12px;
  margin-bottom: 6px;
  font-weight: 600;
}

.tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.excerpt {
  border: 1px solid #fde68a;
  background: #fffbeb;
  color: #7c2d12;
  border-radius: 8px;
  padding: 10px;
  line-height: 1.6;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .hero-kpi {
    width: 100%;
  }
}
</style>
