<template>
  <div class="app-container supply-contract-ops">
    <section class="hero-panel">
      <div>
        <p class="hero-tag">供应链金融合约运营台</p>
        <h2>融资敞口、到期压力、履约分层一体化管理</h2>
      </div>
      <div class="hero-actions">
        <el-button icon="el-icon-refresh" size="mini" @click="refreshAll">刷新面板</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:supplyContract:add']">新增合约</el-button>
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:supplyContract:export']">导出台账</el-button>
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
            <span>合约状态分布</span>
            <el-tag size="mini" type="info">动态漏斗</el-tag>
          </div>
          <div class="status-grid">
            <div class="status-item" v-for="item in statusCards" :key="item.key">
              <span>{{ item.label }}</span>
              <b>{{ item.value }}</b>
              <el-progress :percentage="item.percent" :stroke-width="10" :color="item.color" />
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>到期压力队列</span>
            <el-tag size="mini" type="warning">Top {{ (opsData.pressureQueue || []).length }}</el-tag>
          </div>
          <el-table :data="opsData.pressureQueue" size="mini" v-loading="opsLoading" stripe>
            <el-table-column label="合约编号" prop="contractNo" min-width="140" />
            <el-table-column label="合约名称" prop="contractName" min-width="150" />
            <el-table-column label="融资主体" prop="financeSubject" min-width="130" />
            <el-table-column label="金额" prop="financeAmount" width="110" align="center" />
            <el-table-column label="剩余天数" width="100" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="remainTagType(scope.row.remainDays)">{{ formatRemainDays(scope.row.remainDays) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="handleAssess(scope.row)">智能评估</el-button>
                <el-button size="mini" type="text" @click="handleUpdate(scope.row)" v-hasPermi="['agri:supplyContract:edit']">复核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>处置建议</span>
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
            <span>重点合约预警</span>
          </div>
          <div class="alert-list" v-loading="opsLoading">
            <div class="alert-item" v-for="item in opsData.alerts" :key="item.contractId">
              <div>
                <b>{{ item.contractName }}</b>
                <p>{{ item.contractNo }} · {{ item.financeSubject }} · {{ item.interestRate }}%</p>
              </div>
              <div class="alert-actions">
                <el-tag size="mini" :type="riskTagType(item.riskLevel)">{{ levelText(item.riskLevel) }}</el-tag>
                <el-button type="text" size="mini" @click="handleAssess(item)">评估</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <div slot="header" class="card-header">
        <span>合约台账</span>
        <div>
          <el-button type="text" @click="showSearch = !showSearch">筛选</el-button>
          <el-button type="text" @click="handleExport" v-hasPermi="['agri:supplyContract:export']">导出</el-button>
        </div>
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="合约编号" prop="contractNo">
          <el-input v-model="queryParams.contractNo" placeholder="请输入合约编号" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="合约名称" prop="contractName">
          <el-input v-model="queryParams.contractName" placeholder="请输入合约名称" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="contractStatus">
          <el-select v-model="queryParams.contractStatus" placeholder="请选择合约状态" clearable style="width: 140px">
            <el-option v-for="dict in dict.type.agri_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="风控等级" prop="riskLevel">
          <el-select v-model="queryParams.riskLevel" placeholder="请选择风控等级" clearable style="width: 140px">
            <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="el-icon-edit" size="mini" :disabled="single" @click="handleBatchAssess" v-hasPermi="['agri:supplyContract:edit']">评估选中</el-button>
          <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:supplyContract:remove']">删除选中</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="supplyContractList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="合约编号" align="center" prop="contractNo" min-width="130" show-overflow-tooltip />
        <el-table-column label="合约名称" align="center" prop="contractName" min-width="150" show-overflow-tooltip />
        <el-table-column label="融资主体" align="center" prop="financeSubject" min-width="140" show-overflow-tooltip />
        <el-table-column label="融资金额" align="center" prop="financeAmount" width="110" />
        <el-table-column label="利率(%)" align="center" prop="interestRate" width="90" />
        <el-table-column label="状态" align="center" prop="contractStatus" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.agri_contract_status" :value="scope.row.contractStatus" />
          </template>
        </el-table-column>
        <el-table-column label="风控等级" align="center" prop="riskLevel" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="riskTagType(scope.row.riskLevel)">{{ levelText(scope.row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期日期" align="center" prop="endDate" width="110" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleAssess(scope.row)">智能评估</el-button>
            <el-button size="mini" type="text" @click="handleUpdate(scope.row)" v-hasPermi="['agri:supplyContract:edit']">复核</el-button>
            <el-button size="mini" type="text" @click="handleDelete(scope.row)" v-hasPermi="['agri:supplyContract:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery" />
    </el-card>

    <el-dialog title="合约智能评估" :visible.sync="assessOpen" width="740px" append-to-body>
      <div v-loading="assessLoading" class="analysis-body">
        <el-row :gutter="12">
          <el-col :span="8"><div class="analysis-kpi"><span>压力指数</span><b>{{ assessData.pressureIndex }}</b></div></el-col>
          <el-col :span="8"><div class="analysis-kpi"><span>剩余天数</span><b>{{ assessData.remainDays }}</b></div></el-col>
          <el-col :span="8"><div class="analysis-kpi"><span>风险带</span><b>{{ assessData.riskBand }}</b></div></el-col>
        </el-row>
        <el-alert :title="assessData.summary || '正在生成智能评估结果...'" type="warning" :closable="false" style="margin: 12px 0" />

        <div class="analysis-section">
          <div class="analysis-section-title">AI建议</div>
          <div v-if="assessData.actions && assessData.actions.length" class="analysis-suggestion-list">
            <div class="analysis-suggestion-item" v-for="(item, index) in assessData.actions" :key="index">{{ item }}</div>
          </div>
          <div v-else class="analysis-empty">暂无建议</div>
        </div>

        <div class="analysis-section" v-if="assessData.aiOriginalExcerpt">
          <div class="analysis-section-title">AI原文摘录</div>
          <pre class="analysis-excerpt">{{ assessData.aiOriginalExcerpt }}</pre>
        </div>

        <div class="analysis-footnote" v-if="assessData.requestTime">最近评估时间：{{ assessData.requestTime }}</div>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="合约编号" prop="contractNo"><el-input v-model="form.contractNo" placeholder="请输入合约编号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="合约名称" prop="contractName"><el-input v-model="form.contractName" placeholder="请输入合约名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="融资主体" prop="financeSubject"><el-input v-model="form.financeSubject" placeholder="请输入融资主体" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="融资金额" prop="financeAmount"><el-input-number v-model="form.financeAmount" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="利率(%)" prop="interestRate"><el-input-number v-model="form.interestRate" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="合约状态" prop="contractStatus">
              <el-select v-model="form.contractStatus" placeholder="请选择合约状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="风控等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风控等级" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起始日期" prop="startDate">
              <el-date-picker clearable v-model="form.startDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择起始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="到期日期" prop="endDate">
          <el-date-picker clearable v-model="form.endDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择到期日期" style="width: 100%" />
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
import { listSupplyContract, getSupplyContract, delSupplyContract, addSupplyContract, updateSupplyContract, getSupplyContractOpsDashboard, smartAssessSupplyContract } from '@/api/agri/supplyContract'

export default {
  name: 'SupplyContract',
  dicts: ['agri_contract_status', 'agri_risk_level'],
  data() {
    return {
      loading: false,
      opsLoading: false,
      assessLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      supplyContractList: [],
      title: '',
      open: false,
      assessOpen: false,
      assessData: {
        pressureIndex: '--',
        remainDays: '--',
        riskBand: '--',
        summary: '',
        actions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractNo: null,
        contractName: null,
        contractStatus: null,
        riskLevel: null
      },
      form: {},
      rules: {
        contractNo: [{ required: true, message: '合约编号不能为空', trigger: 'blur' }],
        contractName: [{ required: true, message: '合约名称不能为空', trigger: 'blur' }],
        financeSubject: [{ required: true, message: '融资主体不能为空', trigger: 'blur' }],
        financeAmount: [{ required: true, message: '融资金额不能为空', trigger: 'blur' }],
        contractStatus: [{ required: true, message: '合约状态不能为空', trigger: 'change' }]
      },
      opsData: {
        kpi: {},
        statusBucket: {},
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
        { label: '合约总数', value: kpi.total || 0 },
        { label: '生效中', value: kpi.activeCount || 0 },
        { label: '临期压力', value: kpi.maturityCount || 0 },
        { label: '高风险', value: kpi.highRiskCount || 0 },
        { label: '融资总额', value: this.formatCurrency(kpi.amountSum) }
      ]
    },
    statusCards() {
      const bucket = this.opsData.statusBucket || {}
      const total = Number((this.opsData.kpi && this.opsData.kpi.total) || 0)
      const list = [
        { key: '0', label: '签约中', color: '#f57c00' },
        { key: '1', label: '生效中', color: '#2e7d32' },
        { key: '2', label: '待结清', color: '#1565c0' },
        { key: '3', label: '已关闭', color: '#546e7a' }
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
      listSupplyContract(this.queryParams)
        .then(response => {
          this.supplyContractList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadOpsData() {
      this.opsLoading = true
      getSupplyContractOpsDashboard()
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
      this.ids = selection.map(item => item.contractId)
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
      this.title = '新增供应链金融合约'
    },
    handleUpdate(row) {
      this.reset()
      const contractId = row && row.contractId ? row.contractId : this.ids[0]
      if (!contractId) {
        this.$modal.msgWarning('请先选择要复核的合约')
        return
      }
      getSupplyContract(contractId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改供应链金融合约'
      })
    },
    handleBatchAssess() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请在台账中选择一条记录进行评估')
        return
      }
      this.handleAssess({ contractId: this.ids[0] })
    },
    handleAssess(row) {
      const contractId = row && row.contractId ? row.contractId : this.ids[0]
      if (!contractId) {
        this.$modal.msgWarning('请先选择一条合约进行智能评估')
        return
      }
      this.assessOpen = true
      this.assessLoading = true
      this.assessData = {
        pressureIndex: '--',
        remainDays: '--',
        riskBand: '--',
        summary: '',
        actions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      }
      smartAssessSupplyContract(contractId)
        .then(response => {
          const data = response.data || {}
          const actions = Array.isArray(data.actions)
            ? data.actions.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
            : []
          this.assessData = {
            pressureIndex: data.pressureIndex != null ? data.pressureIndex : '--',
            remainDays: data.remainDays != null ? data.remainDays : '--',
            riskBand: data.riskBand || '--',
            summary: data.summary || '',
            actions,
            aiOriginalExcerpt: data.aiOriginalExcerpt || '',
            requestTime: new Date().toLocaleString()
          }
        })
        .catch(() => {
          this.$modal.msgError('智能评估调用失败，请稍后重试')
        })
        .finally(() => {
          this.assessLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.contractId != null) {
          updateSupplyContract(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.refreshAll()
          })
        } else {
          addSupplyContract(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.refreshAll()
          })
        }
      })
    },
    handleDelete(row) {
      const contractIds = row && row.contractId ? row.contractId : this.ids
      if (!contractIds || contractIds.length === 0) {
        this.$modal.msgWarning('请先选择要删除的记录')
        return
      }
      this.$modal.confirm('是否确认删除供应链金融合约编号为"' + contractIds + '"的数据项？')
        .then(() => delSupplyContract(contractIds))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.refreshAll()
        })
        .catch(() => {})
    },
    handleExport() {
      this.download('agri/supplyContract/export', {
        ...this.queryParams
      }, `supply_contract_${new Date().getTime()}.xlsx`)
    },
    reset() {
      this.form = {
        contractId: null,
        contractNo: null,
        contractName: null,
        financeSubject: null,
        financeAmount: 0,
        interestRate: 0,
        startDate: null,
        endDate: null,
        contractStatus: '0',
        riskLevel: 'L',
        remark: null
      }
      this.resetForm('form')
    },
    formatCurrency(value) {
      const number = Number(value || 0)
      return number.toFixed(2)
    },
    levelText(level) {
      if (level === 'C') {
        return '极高'
      }
      if (level === 'H') {
        return '高'
      }
      if (level === 'M') {
        return '中'
      }
      return '低'
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
    },
    formatRemainDays(days) {
      if (days === null || days === undefined || days === Number.MIN_SAFE_INTEGER) {
        return '未知'
      }
      if (days < 0) {
        return `${Math.abs(days)}天逾期`
      }
      return `${days}天`
    },
    remainTagType(days) {
      if (days === null || days === undefined || days === Number.MIN_SAFE_INTEGER) {
        return 'info'
      }
      if (days < 0) {
        return 'danger'
      }
      if (days <= 30) {
        return 'warning'
      }
      return 'success'
    }
  }
}
</script>

<style scoped>
.supply-contract-ops {
  background: linear-gradient(170deg, #f6f3ff 0%, #fffdf9 58%, #f3fbff 100%);
}

.hero-panel {
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 14px;
  background: linear-gradient(120deg, #2d224f 0%, #4e2f74 56%, #7a4b85 100%);
  color: #fff8ff;
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
  color: #e3c8ff;
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
  box-shadow: 0 6px 14px rgba(68, 47, 97, 0.08);
}

.kpi-label {
  color: #746b82;
  font-size: 13px;
}

.kpi-value {
  margin-top: 8px;
  color: #36245e;
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

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.status-item {
  border: 1px solid #e7e0f0;
  background: #fbf9ff;
  border-radius: 10px;
  padding: 12px;
}

.status-item span {
  display: block;
  color: #756d84;
  font-size: 12px;
}

.status-item b {
  display: block;
  margin: 6px 0 10px;
  color: #37245f;
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
  background: #faf7ff;
  border: 1px solid #ede5f7;
}

.suggestion-item span {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: #5a35a8;
  color: #fff;
  text-align: center;
  line-height: 24px;
  font-size: 12px;
}

.suggestion-item p {
  margin: 0;
  color: #5e556f;
}

.alert-list {
  min-height: 180px;
}

.alert-item {
  padding: 10px 0;
  border-bottom: 1px dashed #e5dbef;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.alert-item p {
  margin: 5px 0 0;
  color: #6e6680;
  font-size: 12px;
}

.alert-actions {
  min-width: 110px;
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
  color: #4e2f74;
  font-weight: 600;
}

.analysis-suggestion-list {
  display: grid;
  gap: 8px;
}

.analysis-suggestion-item {
  border-radius: 8px;
  border: 1px solid #e7dbf3;
  background: #fbf7ff;
  color: #6d3ca8;
  padding: 8px 10px;
  line-height: 1.5;
  word-break: break-word;
}

.analysis-excerpt {
  border-radius: 8px;
  border: 1px solid #e7dbf3;
  background: #faf7ff;
  color: #5f4d7b;
  padding: 10px;
  margin: 0;
  max-height: 180px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
}

.analysis-empty {
  color: #8c839d;
}

.analysis-footnote {
  margin-top: 10px;
  font-size: 12px;
  color: #8c839d;
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
