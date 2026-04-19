<template>
  <div class="app-container finance-risk-ops">
    <section class="hero-panel">
      <div>
        <p class="hero-tag">农业金融风控运营台</p>
        <h2>压力监测、授信预警、评估复核一体化中枢</h2>
      </div>
      <div class="hero-actions">
        <el-button icon="el-icon-refresh" size="mini" @click="refreshAll">刷新面板</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:financeRisk:add']">新增指标</el-button>
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:financeRisk:export']">导出台账</el-button>
      </div>
    </section>

    <el-row :gutter="14" class="kpi-row">
      <el-col :xs="12" :sm="8" :md="4.8" v-for="card in kpiCards" :key="card.label">
        <div class="kpi-card">
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="board-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>风险维度分布</span>
            <el-tag size="mini" type="info">四象限聚合</el-tag>
          </div>
          <div class="dimension-grid">
            <div class="dimension-item" v-for="item in dimensionCards" :key="item.key">
              <span>{{ item.label }}</span>
              <b>{{ item.value }}</b>
              <el-progress :percentage="item.percent" :stroke-width="10" :color="item.color" />
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>压力指标队列</span>
            <el-tag size="mini" type="warning">Top {{ (opsData.pressureQueue || []).length }}</el-tag>
          </div>
          <el-table :data="opsData.pressureQueue" size="mini" v-loading="opsLoading" stripe>
            <el-table-column label="指标编码" prop="indicatorCode" min-width="120" />
            <el-table-column label="指标名称" prop="indicatorName" min-width="140" />
            <el-table-column label="风险分值" prop="riskScore" width="100" align="center" />
            <el-table-column label="阈值" prop="thresholdValue" width="100" align="center" />
            <el-table-column label="等级" width="90" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="riskTagType(scope.row.riskLevel)">{{ levelText(scope.row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="handleAnalyze(scope.row)">智能分析</el-button>
                <el-button size="mini" type="text" @click="handleUpdate(scope.row)" v-hasPermi="['agri:financeRisk:edit']">复核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>风险处置建议</span>
          </div>
          <div class="suggestion-list">
            <div class="suggestion-item" v-for="(item, index) in opsData.suggestions" :key="index">
              <span>{{ index + 1 }}</span>
              <p>{{ item }}</p>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>高压预警清单</span>
          </div>
          <div class="alert-list" v-loading="opsLoading">
            <div class="alert-item" v-for="item in opsData.alerts" :key="item.riskId">
              <div>
                <b>{{ item.indicatorName }}</b>
                <p>{{ item.indicatorCode }} · 评分 {{ item.riskScore }} / 阈值 {{ item.thresholdValue }}</p>
              </div>
              <div class="alert-actions">
                <el-tag size="mini" :type="riskTagType(item.riskLevel)">{{ levelText(item.riskLevel) }}</el-tag>
                <el-button type="text" size="mini" @click="handleAnalyze(item)">分析</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <div slot="header" class="card-header">
        <span>指标台账</span>
        <div>
          <el-button type="text" @click="showSearch = !showSearch">筛选</el-button>
          <el-button type="text" @click="handleExport" v-hasPermi="['agri:financeRisk:export']">导出</el-button>
        </div>
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="指标编码" prop="indicatorCode">
          <el-input v-model="queryParams.indicatorCode" placeholder="请输入指标编码" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="指标名称" prop="indicatorName">
          <el-input v-model="queryParams.indicatorName" placeholder="请输入指标名称" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="风险维度" prop="riskDimension">
          <el-select v-model="queryParams.riskDimension" placeholder="请选择风险维度" clearable style="width: 150px">
            <el-option v-for="dict in dict.type.agri_risk_dimension" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="queryParams.riskLevel" placeholder="请选择风险等级" clearable style="width: 150px">
            <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="el-icon-edit" size="mini" :disabled="single" @click="handleBatchReview" v-hasPermi="['agri:financeRisk:edit']">复核选中</el-button>
          <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:financeRisk:remove']">删除选中</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="financeRiskList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="指标编码" align="center" prop="indicatorCode" min-width="120" show-overflow-tooltip />
        <el-table-column label="指标名称" align="center" prop="indicatorName" min-width="150" show-overflow-tooltip />
        <el-table-column label="维度" align="center" prop="riskDimension" width="110">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.agri_risk_dimension" :value="scope.row.riskDimension" />
          </template>
        </el-table-column>
        <el-table-column label="分值" align="center" prop="riskScore" width="90" />
        <el-table-column label="阈值" align="center" prop="thresholdValue" width="90" />
        <el-table-column label="等级" align="center" prop="riskLevel" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="riskTagType(scope.row.riskLevel)">{{ levelText(scope.row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="evaluateStatus" width="90">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.agri_evaluate_status" :value="scope.row.evaluateStatus" />
          </template>
        </el-table-column>
        <el-table-column label="评估时间" align="center" prop="evaluateTime" width="160" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleAnalyze(scope.row)">智能分析</el-button>
            <el-button size="mini" type="text" @click="handleUpdate(scope.row)" v-hasPermi="['agri:financeRisk:edit']">复核</el-button>
            <el-button size="mini" type="text" @click="handleDelete(scope.row)" v-hasPermi="['agri:financeRisk:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery" />
    </el-card>

    <el-dialog title="风控评估分析" :visible.sync="analysisOpen" width="700px" append-to-body>
      <div v-loading="analysisLoading" class="analysis-body">
        <el-row :gutter="12">
          <el-col :span="8"><div class="analysis-kpi"><span>压力指数</span><b>{{ analysisData.stressIndex }}</b></div></el-col>
          <el-col :span="8"><div class="analysis-kpi"><span>偏差</span><b>{{ analysisData.scoreGap }}</b></div></el-col>
          <el-col :span="8"><div class="analysis-kpi"><span>等级</span><b>{{ analysisData.riskLevel }}</b></div></el-col>
        </el-row>
        <el-alert :title="analysisData.summary || '正在生成智能分析结果...'
          " type="warning" :closable="false" style="margin: 12px 0" />

        <div class="analysis-section">
          <div class="analysis-section-title">AI建议</div>
          <div v-if="analysisData.suggestions && analysisData.suggestions.length" class="analysis-suggestion-list">
            <div class="analysis-suggestion-item" v-for="(item, index) in analysisData.suggestions" :key="index">{{ item }}</div>
          </div>
          <div v-else class="analysis-empty">暂无建议</div>
        </div>

        <div class="analysis-section" v-if="analysisData.aiOriginalExcerpt">
          <div class="analysis-section-title">AI原文摘录</div>
          <pre class="analysis-excerpt">{{ analysisData.aiOriginalExcerpt }}</pre>
        </div>

        <div class="analysis-footnote" v-if="analysisData.requestTime">最近分析时间：{{ analysisData.requestTime }}</div>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="指标编码" prop="indicatorCode"><el-input v-model="form.indicatorCode" placeholder="请输入指标编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="指标名称" prop="indicatorName"><el-input v-model="form.indicatorName" placeholder="请输入指标名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="风险维度" prop="riskDimension">
              <el-select v-model="form.riskDimension" placeholder="请选择风险维度" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_dimension" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风险等级" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="风险分值" prop="riskScore"><el-input-number v-model="form.riskScore" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="阈值" prop="thresholdValue"><el-input-number v-model="form.thresholdValue" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="评估状态" prop="evaluateStatus">
              <el-select v-model="form.evaluateStatus" placeholder="请选择评估状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_evaluate_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="评估人" prop="evaluator"><el-input v-model="form.evaluator" placeholder="请输入评估人" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="评估时间" prop="evaluateTime">
          <el-date-picker clearable v-model="form.evaluateTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择评估时间" style="width: 100%" />
        </el-form-item>
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
import { listFinanceRisk, getFinanceRisk, delFinanceRisk, addFinanceRisk, updateFinanceRisk, getFinanceRiskOpsDashboard, smartAnalyzeFinanceRisk } from '@/api/agri/financeRisk'

export default {
  name: 'FinanceRisk',
  dicts: ['agri_risk_dimension', 'agri_risk_level', 'agri_evaluate_status'],
  data() {
    return {
      loading: false,
      opsLoading: false,
      analysisLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      financeRiskList: [],
      selectedRisk: null,
      title: '',
      open: false,
      analysisOpen: false,
      analysisData: {
        stressIndex: '--',
        scoreGap: '--',
        riskLevel: '--',
        summary: '',
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        indicatorCode: null,
        indicatorName: null,
        riskDimension: null,
        riskLevel: null
      },
      form: {},
      rules: {
        indicatorCode: [{ required: true, message: '指标编码不能为空', trigger: 'blur' }],
        indicatorName: [{ required: true, message: '指标名称不能为空', trigger: 'blur' }],
        riskDimension: [{ required: true, message: '风险维度不能为空', trigger: 'change' }],
        riskScore: [{ required: true, message: '风险分值不能为空', trigger: 'blur' }],
        riskLevel: [{ required: true, message: '风险等级不能为空', trigger: 'change' }],
        evaluateStatus: [{ required: true, message: '评估状态不能为空', trigger: 'change' }]
      },
      opsData: {
        kpi: {},
        dimensionBucket: {},
        pressureQueue: [],
        alerts: [],
        suggestions: []
      }
    }
  },
  computed: {
    kpiCards() {
      const kpi = this.opsData.kpi || {}
      return [
        { label: '指标总数', value: kpi.total || 0 },
        { label: '高风险指标', value: kpi.highRiskCount || 0 },
        { label: '待复核', value: kpi.overdueCount || 0 },
        { label: '已监控', value: kpi.monitoredCount || 0 },
        { label: '平均分', value: this.formatNumber(kpi.avgScore) }
      ]
    },
    dimensionCards() {
      const bucket = this.opsData.dimensionBucket || {}
      const total = Number((this.opsData.kpi && this.opsData.kpi.total) || 0)
      const list = [
        { key: 'CREDIT', label: '授信维度', color: '#c62828' },
        { key: 'REPAY', label: '回款维度', color: '#f57c00' },
        { key: 'OPERATION', label: '经营维度', color: '#00897b' },
        { key: 'OTHER', label: '其他维度', color: '#546e7a' }
      ]
      return list.map(item => {
        const value = Number(bucket[item.key] || 0)
        return {
          ...item,
          value,
          percent: total ? Number(((value * 100) / total).toFixed(1)) : 0
        }
      })
    }
  },
  created() {
    this.refreshAll()
  },
  methods: {
    refreshAll() {
      this.loadList()
      this.loadOpsData()
    },
    loadList() {
      this.loading = true
      listFinanceRisk(this.queryParams)
        .then(response => {
          this.financeRiskList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadOpsData() {
      this.opsLoading = true
      getFinanceRiskOpsDashboard()
        .then(response => {
          this.opsData = response.data || this.opsData
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.loadList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.riskId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (selection.length === 1) {
        this.selectedRisk = selection[0]
      }
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.selectedRisk = row
      this.handleSelectionChange([row])
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增农业金融风控指标'
    },
    handleUpdate(row) {
      this.reset()
      const target = this.resolveRiskTarget(row)
      const riskId = target && target.riskId
      if (!riskId) {
        this.$modal.msgWarning('请先选择要复核的指标')
        return
      }
      getFinanceRisk(riskId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改农业金融风控指标'
      })
    },
    handleBatchReview() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请在台账中选择一条记录进行复核')
        return
      }
      this.handleUpdate({ riskId: this.ids[0] })
    },
    handleAnalyze(row) {
      const target = this.resolveRiskTarget(row)
      const riskId = target && target.riskId
      if (!riskId) {
        this.$modal.msgWarning('请先选择一条指标进行智能分析')
        return
      }
      this.analysisOpen = true
      this.analysisLoading = true
      this.analysisData = {
        stressIndex: '--',
        scoreGap: '--',
        riskLevel: '--',
        summary: '',
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      }
      smartAnalyzeFinanceRisk(riskId)
        .then(response => {
          const data = response.data || {}
          const suggestions = Array.isArray(data.suggestions)
            ? data.suggestions.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
            : []
          this.analysisData = {
            stressIndex: data.stressIndex != null ? data.stressIndex : '--',
            scoreGap: data.scoreGap != null ? data.scoreGap : '--',
            riskLevel: data.riskLevel || '--',
            summary: data.summary || '',
            suggestions,
            aiOriginalExcerpt: data.aiOriginalExcerpt || '',
            requestTime: new Date().toLocaleString()
          }
        })
        .catch(() => {
          this.$modal.msgError('智能分析调用失败，请稍后重试')
        })
        .finally(() => {
          this.analysisLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.riskId != null) {
          updateFinanceRisk(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.refreshAll()
          })
        } else {
          addFinanceRisk(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.refreshAll()
          })
        }
      })
    },
    handleDelete(row) {
      const target = this.resolveRiskTarget(row)
      const riskIds = target && target.riskId ? target.riskId : this.ids
      if (!riskIds || riskIds.length === 0) {
        this.$modal.msgWarning('请先选择要删除的记录')
        return
      }
      this.$modal.confirm('是否确认删除农业金融风控指标编号为"' + riskIds + '"的数据项？')
        .then(() => delFinanceRisk(riskIds))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.refreshAll()
        })
        .catch(() => {})
    },
    handleExport() {
      this.download('agri/financeRisk/export', {
        ...this.queryParams
      }, `finance_risk_${new Date().getTime()}.xlsx`)
    },
    reset() {
      this.form = {
        riskId: null,
        indicatorCode: null,
        indicatorName: null,
        riskDimension: null,
        riskScore: 0,
        thresholdValue: 0,
        riskLevel: 'L',
        evaluateStatus: '0',
        evaluateTime: null,
        evaluator: null,
        remark: null
      }
      this.resetForm('form')
    },
    formatNumber(value) {
      const number = Number(value || 0)
      return number.toFixed(2)
    },
    levelText(level) {
      if (level === 'H') {
        return '高'
      }
      if (level === 'C') {
        return '极高'
      }
      if (level === 'M') {
        return '中'
      }
      return '低'
    },
    resolveRiskTarget(row) {
      if (row && row.riskId) {
        return row
      }
      if (this.selectedRisk && this.selectedRisk.riskId) {
        return this.selectedRisk
      }
      if (this.ids.length) {
        const selected = this.financeRiskList.find(item => item.riskId === this.ids[0])
        if (selected) {
          return selected
        }
      }
      if (this.financeRiskList.length) {
        return this.financeRiskList[0]
      }
      return (this.opsData.pressureQueue || [])[0]
    },
    riskTagType(level) {
      if (level === 'C') {
        return 'danger'
      }
      if (level === 'H') {
        return 'warning'
      }
      if (level === 'M') {
        return 'info'
      }
      return 'success'
    }
  }
}
</script>

<style scoped>
.finance-risk-ops {
  background: linear-gradient(165deg, #fff8f4 0%, #fffdf9 56%, #f6fbff 100%);
}

.hero-panel {
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 14px;
  background: linear-gradient(120deg, #541a16 0%, #7a2d18 52%, #9d5518 100%);
  color: #fff7f4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hero-panel h2 {
  margin: 6px 0 0;
  font-size: 22px;
  font-weight: 700;
}

.hero-tag {
  margin: 0;
  color: #ffd1c0;
  letter-spacing: 1px;
}

.hero-actions .el-button + .el-button {
  margin-left: 8px;
}

.kpi-row {
  margin-bottom: 14px;
}

.kpi-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 14px;
  min-height: 88px;
  box-shadow: 0 6px 14px rgba(125, 50, 21, 0.08);
}

.kpi-label {
  color: #7b6860;
  font-size: 13px;
}

.kpi-value {
  margin-top: 8px;
  color: #5a2614;
  font-size: 26px;
  font-weight: 700;
}

.panel-card {
  border-radius: 12px;
  margin-bottom: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.dimension-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.dimension-item {
  border: 1px solid #f0e4db;
  background: #fffaf6;
  border-radius: 10px;
  padding: 12px;
}

.dimension-item span {
  display: block;
  color: #7d6d62;
  font-size: 12px;
}

.dimension-item b {
  display: block;
  margin: 6px 0 10px;
  color: #5a2614;
  font-size: 20px;
}

.suggestion-list {
  display: grid;
  gap: 10px;
}

.suggestion-item {
  display: grid;
  grid-template-columns: 24px 1fr;
  gap: 10px;
  align-items: start;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff8f3;
  border: 1px solid #f2e2d8;
}

.suggestion-item span {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: #9d5518;
  color: #fff;
  text-align: center;
  line-height: 24px;
  font-size: 12px;
}

.suggestion-item p {
  margin: 0;
  color: #5f4f46;
}

.alert-list {
  min-height: 180px;
}

.alert-item {
  padding: 10px 0;
  border-bottom: 1px dashed #ead8cc;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.alert-item p {
  margin: 5px 0 0;
  color: #7c6a5e;
  font-size: 12px;
}

.alert-actions {
  min-width: 100px;
  text-align: right;
}

.analysis-body {
  min-height: 120px;
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 2px;
}

.analysis-kpi {
  border-radius: 8px;
  background: #f5f0ff;
  padding: 10px;
  text-align: center;
}

.analysis-kpi span {
  color: #766b89;
  display: block;
  font-size: 12px;
}

.analysis-kpi b {
  display: block;
  margin-top: 6px;
  color: #4e2f74;
  font-size: 20px;
}

.analysis-section {
  margin-top: 12px;
}

.analysis-section-title {
  margin-bottom: 8px;
  color: #7a2d18;
  font-weight: 600;
}

.analysis-suggestion-list {
  display: grid;
  gap: 8px;
}

.analysis-suggestion-item {
  border-radius: 8px;
  border: 1px solid #f1d6d6;
  background: #fff2f2;
  color: #d9534f;
  padding: 8px 10px;
  line-height: 1.5;
  word-break: break-word;
}

.analysis-excerpt {
  border-radius: 8px;
  border: 1px solid #f0d9d9;
  background: #fff7f7;
  color: #b44d4b;
  padding: 10px;
  margin: 0;
  max-height: 180px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
}

.analysis-empty {
  color: #9a8a80;
}

.analysis-footnote {
  margin-top: 10px;
  font-size: 12px;
  color: #9a8a80;
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-panel h2 {
    font-size: 18px;
  }
}
</style>
