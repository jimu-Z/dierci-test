<template>
  <div class="app-container carbon-page">
    <el-card shadow="never" class="hero-card mb16">
      <div class="hero-wrap">
        <div>
          <div class="hero-title">碳足迹核算智能中心</div>
          <div class="hero-desc">基于排放因子、核算边界和历史状态进行智能诊断，优先提示高排放、待计算和久未复核模型。</div>
          <div class="hero-meta">
            <span>模型总数 {{ total }}</span>
            <span>最近刷新 {{ lastRefreshLabel }}</span>
            <span>自动刷新 {{ refreshInterval / 1000 }} 秒 / 次</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click="refreshDashboard">刷新看板</el-button>
          <el-button type="warning" icon="el-icon-data-analysis" size="mini" @click="handleSmartAnalyze(selectedRow)" :disabled="!selectedRow">智能分析</el-button>
          <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:carbonFootprint:export']">导出快照</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="handleQuery"></right-toolbar>
        </div>
      </div>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px" class="mb16 carbon-search">
      <el-form-item label="模型编码" prop="modelCode">
        <el-input v-model="queryParams.modelCode" placeholder="请输入模型编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品类型" prop="productType">
        <el-select v-model="queryParams.productType" placeholder="请选择产品类型" clearable style="width: 150px">
          <el-option v-for="dict in dict.type.agri_product_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="核算状态" prop="calcStatus">
        <el-select v-model="queryParams.calcStatus" placeholder="请选择核算状态" clearable style="width: 150px">
          <el-option v-for="dict in dict.type.agri_calc_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
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
            <span>智能洞察</span>
            <span class="panel-subtitle">基于规则引擎与 AI 结果的双重分析</span>
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
        <el-card shadow="never" class="panel-card mb12">
          <div slot="header" class="panel-header">
            <span>排放排名</span>
            <span class="panel-subtitle">优先处理高排放模型</span>
          </div>
          <el-empty v-if="!topList.length" description="暂无排名数据" />
          <div v-else class="rank-list">
            <div v-for="(item, index) in topList" :key="item.modelId" class="rank-item" :class="{ active: selectedRow && selectedRow.modelId === item.modelId }" @click="selectRow(item)">
              <div class="rank-index">{{ index + 1 }}</div>
              <div class="rank-content">
                <div class="rank-title">{{ item.modelName }}</div>
                <div class="rank-sub">{{ item.modelCode }} · {{ labelOfProduct(item.productType) }}</div>
                <div class="rank-meta">
                  <span>估算 {{ formatNumber(item.effectiveEmission) }} kgCO2e</span>
                  <el-tag size="mini" :type="riskTagType(item.riskLevel)">{{ item.riskLevel }}风险</el-tag>
                </div>
              </div>
              <el-button type="text" size="mini" @click.stop="handleSmartAnalyze(item)">分析</el-button>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>类型分布</span>
            <span class="panel-subtitle">按产品类型统计</span>
          </div>
          <div class="distribution-item" v-for="item in productDistribution" :key="item.key">
            <div class="distribution-top">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
            </div>
            <el-progress :percentage="item.rate" :status="item.type" :show-text="false" :stroke-width="10" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card mb16">
      <div slot="header" class="panel-header">
        <span>模型台账</span>
        <span class="panel-subtitle">保留维护入口，支持单模型智能分析</span>
      </div>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:carbonFootprint:add']">新增</el-button></el-col>
        <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:carbonFootprint:edit']">修改</el-button></el-col>
        <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-data-analysis" size="mini" :disabled="!ids.length" @click="handleSmartAnalyze(selectedRow)">智能分析</el-button></el-col>
        <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:carbonFootprint:remove']">删除</el-button></el-col>
        <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:carbonFootprint:export']">导出</el-button></el-col>
      </el-row>

      <el-table v-loading="loading" :data="carbonFootprintList" @selection-change="handleSelectionChange" @row-click="selectRow">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="模型编码" align="center" prop="modelCode" min-width="120" show-overflow-tooltip />
        <el-table-column label="模型名称" align="center" prop="modelName" min-width="120" show-overflow-tooltip />
        <el-table-column label="产品类型" align="center" prop="productType">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.agri_product_type" :value="scope.row.productType" />
          </template>
        </el-table-column>
        <el-table-column label="排放因子" align="center" prop="emissionFactor" width="110" />
        <el-table-column label="碳排放量" align="center" prop="carbonEmission" width="110">
          <template slot-scope="scope">{{ formatNumber(scope.row.carbonEmission) }}</template>
        </el-table-column>
        <el-table-column label="核算状态" align="center" prop="calcStatus">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.agri_calc_status" :value="scope.row.calcStatus" />
          </template>
        </el-table-column>
        <el-table-column label="核算时间" align="center" prop="calcTime" width="160" />
        <el-table-column label="复核人" align="center" prop="verifier" width="100" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="190">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:carbonFootprint:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:carbonFootprint:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery" />
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="模型编码" prop="modelCode"><el-input v-model="form.modelCode" placeholder="请输入模型编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型名称" prop="modelName"><el-input v-model="form.modelName" placeholder="请输入模型名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="产品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择产品类型" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_product_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="核算状态" prop="calcStatus">
              <el-select v-model="form.calcStatus" placeholder="请选择核算状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_calc_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="排放因子" prop="emissionFactor"><el-input-number v-model="form.emissionFactor" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="碳排放量" prop="carbonEmission"><el-input-number v-model="form.carbonEmission" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="核算边界" prop="boundaryScope"><el-input v-model="form.boundaryScope" placeholder="请输入核算边界" /></el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="核算时间" prop="calcTime">
              <el-date-picker clearable v-model="form.calcTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择核算时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="复核人" prop="verifier"><el-input v-model="form.verifier" placeholder="请输入复核人" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="智能分析结果" :visible.sync="analysisVisible" width="720px" append-to-body>
      <div v-loading="analysisLoading">
        <div class="analysis-title">{{ analysisModelName }}</div>
        <div class="analysis-subtitle">{{ analysisModelCode }}</div>
        <div class="analysis-grid">
          <div><span>风险等级</span><strong>{{ analysisResult.riskLevel || '中' }}</strong></div>
          <div><span>估算排放</span><strong>{{ formatNumber(analysisResult.estimatedEmission) }} kgCO2e</strong></div>
          <div><span>置信度</span><strong>{{ formatRate(analysisResult.confidenceRate) }}</strong></div>
          <div><span>模型版本</span><strong>{{ analysisResult.modelVersion || 'rule-v1' }}</strong></div>
          <div><span>分析引擎</span><strong>{{ analysisResult.analysisEngine || 'rule-local' }}</strong></div>
        </div>
        <div class="analysis-summary">{{ analysisResult.insightSummary || '暂无分析结果' }}</div>
        <div class="analysis-suggestion" v-if="analysisResult.suggestion">建议：{{ analysisResult.suggestion }}</div>
        <div class="analysis-sections">
          <div class="analysis-section" v-if="analysisResult.boundaryCoverage">
            <div class="section-title">边界覆盖复核</div>
            <div class="section-grid">
              <div><span>覆盖等级</span><strong>{{ analysisResult.boundaryCoverage.coverageLevel || '中' }}</strong></div>
              <div><span>边界覆盖率</span><strong>{{ formatRate(analysisResult.boundaryCoverage.completenessRate || 0) }}</strong></div>
              <div><span>已纳入环节</span><strong>{{ analysisResult.boundaryCoverage.coveredStageCount || 0 }}</strong></div>
              <div><span>有业务数据</span><strong>{{ analysisResult.boundaryCoverage.coveredWithData || 0 }}</strong></div>
            </div>
          </div>
          <div class="analysis-section" v-if="analysisResult.emissionFactorReview">
            <div class="section-title">排放因子来源复核</div>
            <div class="section-grid">
              <div><span>当前因子</span><strong>{{ formatNumber(analysisResult.emissionFactorReview.currentFactor) }}</strong></div>
              <div><span>复核状态</span><strong>{{ analysisResult.emissionFactorReview.reviewStatus || '待复核' }}</strong></div>
              <div><span>行业下限</span><strong>{{ formatNumber(analysisResult.emissionFactorReview.benchmarkLower) }}</strong></div>
              <div><span>行业上限</span><strong>{{ formatNumber(analysisResult.emissionFactorReview.benchmarkUpper) }}</strong></div>
            </div>
          </div>
          <div class="analysis-section" v-if="analysisResult.businessCorrelation">
            <div class="section-title">业务数据关联校验（近{{ analysisResult.businessCorrelation.windowDays || 90 }}天）</div>
            <div class="section-grid">
              <div><span>农事记录</span><strong>{{ pickRecordCount(analysisResult.businessCorrelation.production) }}</strong></div>
              <div><span>物流轨迹</span><strong>{{ pickRecordCount(analysisResult.businessCorrelation.transportation) }}</strong></div>
              <div><span>加工批次</span><strong>{{ pickRecordCount(analysisResult.businessCorrelation.processing) }}</strong></div>
              <div><span>合理性校验</span><strong>{{ (analysisResult.rationalityValidation && analysisResult.rationalityValidation.status) || '待验证' }}</strong></div>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="analysisVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listCarbonFootprint,
  getCarbonFootprint,
  delCarbonFootprint,
  addCarbonFootprint,
  updateCarbonFootprint,
  getCarbonFootprintDashboard,
  analyzeCarbonFootprint
} from '@/api/agri/carbonFootprint'

export default {
  name: 'CarbonFootprint',
  dicts: ['agri_product_type', 'agri_calc_status'],
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      analysisLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      carbonFootprintList: [],
      dashboardSnapshot: null,
      selectedRow: null,
      title: '',
      open: false,
      analysisVisible: false,
      analysisResult: {},
      lastRefreshTime: null,
      refreshInterval: 60000,
      refreshTimer: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelCode: null,
        modelName: null,
        productType: null,
        calcStatus: null
      },
      form: {},
      rules: {
        modelCode: [{ required: true, message: '模型编码不能为空', trigger: 'blur' }],
        modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
        productType: [{ required: true, message: '产品类型不能为空', trigger: 'change' }],
        emissionFactor: [{ required: true, message: '排放因子不能为空', trigger: 'blur' }],
        calcStatus: [{ required: true, message: '核算状态不能为空', trigger: 'change' }]
      }
    }
  },
  computed: {
    summaryCards() {
      const kpi = (this.dashboardSnapshot && this.dashboardSnapshot.kpi) || {}
      const boundary = this.insight.boundaryCoverage || {}
      return [
        { key: 'total', label: '模型总数', value: this.totalValue(kpi.totalCount, this.carbonFootprintList.length), foot: '覆盖全部碳足迹模型' },
        { key: 'calc', label: '已计算', value: this.totalValue(kpi.calculatedCount, this.countByStatus(['1', '2'])), foot: '已完成核算或复核' },
        { key: 'review', label: '已复核', value: this.totalValue(kpi.reviewedCount, this.countByStatus(['2'])), foot: '复核通过的模型' },
        { key: 'pending', label: '待计算', value: this.totalValue(kpi.pendingCount, this.countByStatus(['0'])), foot: '需补齐核算的模型' },
        { key: 'emission', label: '平均排放', value: `${this.formatNumber(kpi.avgEmission)} kgCO2e`, foot: '基于当前可用数据估算' },
        { key: 'boundary', label: '边界覆盖率', value: this.formatRate(boundary.completenessRate || 0), foot: '生产/运输/加工三环节覆盖度' }
      ]
    },
    topList() {
      if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.topList)) {
        return this.dashboardSnapshot.topList
      }
      return [...this.carbonFootprintList]
        .map(item => ({
          ...item,
          effectiveEmission: this.effectiveEmission(item),
          riskLevel: this.localRiskLevel(item)
        }))
        .sort((a, b) => this.toNumber(b.effectiveEmission) - this.toNumber(a.effectiveEmission))
        .slice(0, 6)
    },
    productDistribution() {
      if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.productDistribution)) {
        return this.dashboardSnapshot.productDistribution
      }
      const total = this.carbonFootprintList.length || 1
      const counts = {}
      this.carbonFootprintList.forEach(item => {
        const key = item.productType || 'unknown'
        counts[key] = (counts[key] || 0) + 1
      })
      return Object.keys(counts).map(key => ({
        key,
        label: this.labelOfProduct(key),
        count: counts[key],
        rate: Math.round((counts[key] / total) * 100),
        type: 'warning'
      }))
    },
    insight() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.insight) || {}
    },
    insightSummary() {
      return this.insight.insightSummary || '暂无智能洞察，刷新看板后可查看系统评估结果。'
    },
    recommendations() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.recommendations) || []
    },
    lastRefreshLabel() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '暂无'
    },
    analysisModelName() {
      return this.analysisResult.modelName ||
        (this.analysisResult.model && this.analysisResult.model.modelName) ||
        (this.selectedRow && this.selectedRow.modelName) ||
        '未选择模型'
    },
    analysisModelCode() {
      return this.analysisResult.modelCode ||
        (this.analysisResult.model && this.analysisResult.model.modelCode) ||
        (this.selectedRow && this.selectedRow.modelCode) ||
        ''
    },
    selectedRowId() {
      return this.selectedRow ? this.selectedRow.modelId : null
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
      listCarbonFootprint(this.queryParams).then(response => {
        this.carbonFootprintList = response.rows || []
        this.total = response.total || 0
        this.lastRefreshTime = new Date()
        this.loading = false
        if (!this.selectedRow && this.carbonFootprintList.length) {
          this.selectedRow = this.carbonFootprintList[0]
        }
      }).catch(() => {
        this.loading = false
      })
    },
    refreshDashboard() {
      this.dashboardLoading = true
      getCarbonFootprintDashboard(this.queryParams).then(response => {
        this.dashboardSnapshot = response.data || null
        this.lastRefreshTime = new Date()
      }).catch(() => {}).finally(() => {
        this.dashboardLoading = false
      })
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
      this.ids = selection.map(item => item.modelId)
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
      this.title = '新增碳足迹核算模型'
    },
    handleUpdate(row) {
      this.reset()
      const modelId = row && row.modelId ? row.modelId : this.ids[0]
      if (!modelId) {
        return
      }
      getCarbonFootprint(modelId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '修改碳足迹核算模型'
      })
    },
    handleSmartAnalyze(row) {
      const modelId = row && row.modelId ? row.modelId : this.selectedRowId
      if (!modelId) {
        this.$modal.msgWarning('请先选择一个模型')
        return
      }
      this.analysisLoading = true
      analyzeCarbonFootprint(modelId).then(response => {
        this.analysisResult = response.data || {}
        this.analysisVisible = true
        if (this.analysisResult.analysisEngine !== 'deepseek') {
          this.$modal.msgWarning(this.analysisResult.deepseekMessage || 'DeepSeek未生效，当前为本地规则结果')
        }
        this.getList()
        this.refreshDashboard()
      }).finally(() => {
        this.analysisLoading = false
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.modelId != null) {
            updateCarbonFootprint(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.refreshDashboard()
            })
          } else {
            addCarbonFootprint(this.form).then(() => {
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
      const modelIds = row && row.modelId ? row.modelId : this.ids
      this.$modal.confirm('是否确认删除碳足迹核算模型编号为"' + modelIds + '"的数据项？').then(() => {
        return delCarbonFootprint(modelIds)
      }).then(() => {
        this.getList()
        this.refreshDashboard()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/carbonFootprint/export', {
        ...this.queryParams
      }, `carbon_footprint_${new Date().getTime()}.xlsx`)
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        modelId: null,
        modelCode: null,
        modelName: null,
        productType: null,
        boundaryScope: null,
        emissionFactor: 0,
        carbonEmission: 0,
        calcStatus: '0',
        calcTime: null,
        verifier: null,
        remark: null
      }
      this.resetForm('form')
    },
    totalValue(primary, fallback) {
      return primary != null ? primary : fallback
    },
    countByStatus(statusList) {
      return this.carbonFootprintList.filter(item => statusList.includes(String(item.calcStatus))).length
    },
    highEmissionCount() {
      const emissions = this.carbonFootprintList.map(item => this.effectiveEmission(item))
      if (!emissions.length) {
        return 0
      }
      const avg = emissions.reduce((sum, item) => sum + this.toNumber(item), 0) / emissions.length
      return this.carbonFootprintList.filter(item => this.toNumber(this.effectiveEmission(item)) > avg * 1.2).length
    },
    effectiveEmission(item) {
      const value = this.toNumber(item && item.carbonEmission)
      if (value > 0) {
        return value
      }
      return this.estimateEmission(item)
    },
    estimateEmission(item) {
      const factor = this.toNumber(item && item.emissionFactor)
      return Math.round(factor * this.productMultiplier(item && item.productType) * this.boundaryMultiplier(item && item.boundaryScope) * this.statusMultiplier(item && item.calcStatus) * 100 * 10000) / 10000
    },
    productMultiplier(productType) {
      const map = { vegetable: 0.9, fruit: 1.0, grain: 1.12 }
      return map[productType] || 1.0
    },
    boundaryMultiplier(boundaryScope) {
      const scope = String(boundaryScope || '')
      if (scope.includes('全链路') || scope.includes('全周期')) return 1.25
      if (scope.includes('加工') || scope.includes('物流') || scope.includes('仓储')) return 1.12
      if (scope.includes('种植') || scope.includes('田间')) return 0.96
      return 1.0
    },
    statusMultiplier(calcStatus) {
      if (String(calcStatus) === '0') return 1.1
      if (String(calcStatus) === '2') return 0.98
      return 1.0
    },
    localRiskLevel(item) {
      const emission = this.toNumber(this.effectiveEmission(item))
      if (String(item.calcStatus) === '0' || emission > 1200) return '高'
      if (emission > 600) return '中'
      return '低'
    },
    labelOfProduct(productType) {
      const map = { vegetable: '蔬菜', fruit: '水果', grain: '粮食' }
      return map[productType] || productType || '未知'
    },
    riskTagType(level) {
      const map = { 高: 'danger', 中: 'warning', 低: 'success' }
      return map[level] || 'info'
    },
    formatNumber(value) {
      const num = this.toNumber(value)
      return Number.isInteger(num) ? num : Math.round(num * 100) / 100
    },
    formatRate(value) {
      const num = this.toNumber(value)
      if (num <= 1) {
        return `${Math.round(num * 100)}%`
      }
      return `${Math.round(num)}%`
    },
    toNumber(value) {
      const num = Number(value)
      return Number.isFinite(num) ? num : 0
    },
    pickRecordCount(section) {
      if (!section || typeof section !== 'object') {
        return 0
      }
      return this.toNumber(section.recordCount)
    }
  }
}
</script>

<style scoped>
.carbon-page {
  background: linear-gradient(180deg, #f4f7f8 0%, #ffffff 34%, #ffffff 100%);
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
  color: #102a24;
  margin-bottom: 8px;
}

.hero-desc {
  color: #52615d;
  line-height: 1.7;
  margin-bottom: 10px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #6b7b75;
  font-size: 12px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.kpi-card {
  min-height: 120px;
}

.kpi-label {
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #102a24;
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
  color: #102a24;
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
  background: #f0f9f4;
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

.rank-list {
  display: grid;
  gap: 10px;
}

.rank-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #eef2f7;
  cursor: pointer;
}

.rank-item.active {
  border-color: #409eff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
}

.rank-index {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #0f766e;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.rank-content {
  flex: 1;
}

.rank-title {
  font-weight: 600;
  color: #102a24;
}

.rank-sub {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
}

.rank-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 8px;
  font-size: 12px;
  color: #475569;
}

.distribution-item + .distribution-item {
  margin-top: 14px;
}

.distribution-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #4b5563;
}

.analysis-title {
  font-size: 18px;
  font-weight: 600;
  color: #102a24;
}

.analysis-subtitle {
  color: #9ca3af;
  margin: 6px 0 12px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.analysis-grid div {
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
}

.analysis-grid span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 6px;
}

.analysis-grid strong {
  color: #102a24;
}

.analysis-summary {
  margin-top: 12px;
  color: #1f2937;
  line-height: 1.8;
}

.analysis-suggestion {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #fff7ed;
  color: #9a3412;
  line-height: 1.7;
}

.analysis-sections {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.analysis-section {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  background: #ffffff;
}

.section-title {
  font-size: 13px;
  color: #0f766e;
  font-weight: 600;
  margin-bottom: 8px;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.section-grid div {
  background: #f8fafc;
  padding: 8px;
  border-radius: 8px;
}

.section-grid span {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.section-grid strong {
  color: #102a24;
}

@media (max-width: 992px) {
  .hero-wrap {
    flex-direction: column;
  }
}
</style>
