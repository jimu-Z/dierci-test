<template>
  <div class="app-container">
    <el-card shadow="never" class="farm-hero mb16">
      <div class="farm-hero-head">
        <div>
          <div class="farm-title">农事作业调度台</div>
          <div class="farm-desc">围绕播种、施肥、灌溉、用药和采收做作业节奏管理，优先呈现待复核事项。</div>
        </div>
        <div class="farm-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-magic-stick" :loading="smartAdviceLoading" @click="handleAdvice">智能建议</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="farm-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="farm-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="farm-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="farm-panel">
            <div class="farm-panel-head"><span>作业结构</span><small>统计当前作业类型分布</small></div>
            <div class="farm-mix-list" v-if="operationMix.length">
              <div v-for="item in operationMix" :key="item.type" class="farm-mix-item">
                <span>{{ formatOperationType(item.type) }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <el-empty v-else description="暂无作业统计" />
            <div class="farm-recent" v-if="recentRows.length">
              <div class="farm-panel-subhead">最近作业</div>
              <div v-for="item in recentRows" :key="item.operationId" class="farm-recent-item">
                <div>
                  <b>{{ formatOperationType(item.operationType) }}</b>
                  <p>{{ item.plotCode || '-' }} · {{ item.operatorName || '-' }}</p>
                </div>
                <span>{{ parseTime(item.operationTime) }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="farm-panel">
            <div class="farm-panel-head"><span>智能建议</span><small>根据当前作业记录给出复核提示</small></div>
            <div v-if="smartAdvice.operationId" class="farm-advice-box">
              <div class="farm-score"><strong>{{ smartAdvice.riskScore }}</strong><span>{{ smartAdvice.riskLevel }}风险</span></div>
              <p>{{ smartAdvice.advice }}</p>
              <ul><li v-for="item in smartAdvice.suggestions" :key="item">{{ item }}</li></ul>
              <div v-if="smartAdvice.aiOriginalExcerpt" class="farm-ai-excerpt">
                <div class="farm-panel-subhead">AI原文摘录</div>
                <pre>{{ smartAdvice.aiOriginalExcerpt }}</pre>
              </div>
              <div class="farm-advice-foot" v-if="smartAdviceRequestTime">更新时间：{{ smartAdviceRequestTime }}</div>
            </div>
            <el-empty v-else description="点击智能建议或表格操作获取分析" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="地块编码" prop="plotCode">
        <el-input
          v-model="queryParams.plotCode"
          placeholder="请输入地块编码"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作业类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="请选择作业类型" clearable style="width: 180px">
          <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="作业人" prop="operatorName">
        <el-input
          v-model="queryParams.operatorName"
          placeholder="请输入作业人"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 130px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:farmOp:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:farmOp:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:farmOp:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:farmOp:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="farmOpList" highlight-current-row @selection-change="handleSelectionChange" @row-click="handleRowClick">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="operationId" width="70" />
      <el-table-column label="地块编码" align="center" prop="plotCode" width="120" />
      <el-table-column label="作业类型" align="center" prop="operationType" width="120">
        <template slot-scope="scope">{{ formatOperationType(scope.row.operationType) }}</template>
      </el-table-column>
      <el-table-column label="作业内容" align="center" prop="operationContent" :show-overflow-tooltip="true" />
      <el-table-column label="作业人" align="center" prop="operatorName" width="100" />
      <el-table-column label="投入品" align="center" min-width="160">
        <template slot-scope="scope">
          {{ scope.row.inputName || '-' }} {{ scope.row.inputAmount || '' }} {{ scope.row.inputUnit || '' }}
        </template>
      </el-table-column>
      <el-table-column label="作业时间" align="center" prop="operationTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.operationTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-magic-stick" :loading="smartAdviceLoading" @click="handleAdvice(scope.row)" v-hasPermi="['agri:farmOp:query']">建议</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:farmOp:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:farmOp:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="地块编码" prop="plotCode">
              <el-select
                v-model="form.plotCode"
                filterable
                remote
                reserve-keyword
                clearable
                :remote-method="fetchPlotOptions"
                :loading="plotOptionsLoading"
                placeholder="请选择地块编码"
                style="width: 100%"
                @visible-change="handlePlotOptionsVisible"
              >
                <el-option v-for="item in plotOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业类型" prop="operationType">
              <el-select v-model="form.operationType" placeholder="请选择作业类型" style="width: 100%">
                <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作业内容" prop="operationContent">
          <el-input v-model="form.operationContent" type="textarea" :rows="3" placeholder="请输入作业内容" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="作业人" prop="operatorName">
              <el-input v-model="form.operatorName" placeholder="请输入作业人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业时间" prop="operationTime">
              <el-date-picker
                v-model="form.operationTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择作业时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="10">
            <el-form-item label="投入品名称" prop="inputName">
              <el-input v-model="form.inputName" placeholder="如：有机肥" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="用量" prop="inputAmount">
              <el-input-number v-model="form.inputAmount" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="单位" prop="inputUnit">
              <el-input v-model="form.inputUnit" placeholder="kg / L" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFarmOp, getFarmOp, delFarmOp, addFarmOp, updateFarmOp } from '@/api/agri/farmOp'
import { getFarmOpDashboard, getFarmOpAdvice, getFarmOpPlotOptions } from '@/api/agri/farmOp'

export default {
  name: 'FarmOperation',
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      farmOpList: [],
      selectedRows: [],
      currentRow: null,
      dashboardData: {},
      smartAdvice: {},
      smartAdviceLoading: false,
      smartAdviceRequestTime: '',
      plotOptions: [],
      plotOptionsLoading: false,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plotCode: undefined,
        operationType: undefined,
        operatorName: undefined,
        status: undefined
      },
      form: {},
      operationTypeOptions: [
        { label: '播种', value: 'SOWING' },
        { label: '施肥', value: 'FERTILIZATION' },
        { label: '灌溉', value: 'IRRIGATION' },
        { label: '用药', value: 'PEST_CONTROL' },
        { label: '采收', value: 'HARVEST' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'change' }],
        operationType: [{ required: true, message: '作业类型不能为空', trigger: 'change' }],
        operationContent: [{ required: true, message: '作业内容不能为空', trigger: 'blur' }],
        operatorName: [{ required: true, message: '作业人不能为空', trigger: 'blur' }],
        operationTime: [{ required: true, message: '作业时间不能为空', trigger: 'change' }]
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
        { key: 'total', label: '作业总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录数' },
        { key: 'plot', label: '地块数', value: summary.plotCount || 0, foot: '参与作业的地块' },
        { key: 'operator', label: '作业人', value: summary.operatorCount || 0, foot: '去重执行人数' },
        { key: 'active', label: '正常记录', value: summary.activeCount || 0, foot: '状态正常的作业记录' }
      ]
    },
    operationMix() {
      return this.dashboardData.typeRows || []
    },
    recentRows() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    refreshDashboard() {
      getFarmOpDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      }).catch(() => {
        this.dashboardData = this.dashboardData || {}
      })
    },
    getList() {
      this.loading = true
      listFarmOp(this.queryParams)
        .then(response => {
          const rows = Array.isArray(response.rows) ? response.rows : []
          this.farmOpList = rows.map(row => this.normalizeOperationRow(row))
          this.total = response.total
        })
        .catch(() => {
          this.farmOpList = []
          this.total = 0
          this.$modal.msgError('农事记录加载失败，请稍后重试')
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatOperationType(value) {
      const option = this.operationTypeOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        operationId: undefined,
        plotCode: undefined,
        operationType: undefined,
        operationContent: undefined,
        operatorName: undefined,
        inputName: undefined,
        inputAmount: undefined,
        inputUnit: undefined,
        operationTime: undefined,
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
      this.selectedRows = selection
      this.ids = selection.map(item => this.getOperationId(item)).filter(item => item !== undefined && item !== null)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      this.currentRow = this.normalizeOperationRow(row)
    },
    normalizeOperationRow(row) {
      if (!row) {
        return row
      }
      const operationId = row.operationId || row.operation_id || row.operationID || row.id
      if (!operationId) {
        return row
      }
      return {
        ...row,
        operationId
      }
    },
    normalizeAdviceResult(payload) {
      if (!payload) {
        return {}
      }
      return {
        operationId: payload.operationId || payload.operation_id || payload.id,
        operationType: payload.operationType || payload.operation_type,
        riskScore: payload.riskScore !== undefined ? payload.riskScore : payload.risk_score,
        riskLevel: payload.riskLevel || payload.risk_level,
        advice: payload.advice || payload.summary || payload.message,
        suggestions: Array.isArray(payload.suggestions) ? payload.suggestions : [],
        aiOriginalExcerpt: payload.aiOriginalExcerpt || payload.ai_original_excerpt || ''
      }
    },
    getOperationId(target) {
      if (!target) {
        return undefined
      }
      return target.operationId || target.operation_id || target.operationID || target.id
    },
    resolveAdviceTarget(row) {
      if (row) {
        return row
      }
      if (this.currentRow && this.getOperationId(this.currentRow)) {
        return this.currentRow
      }
      if (this.selectedRows[0] && this.getOperationId(this.selectedRows[0])) {
        return this.selectedRows[0]
      }
      if (this.ids[0]) {
        const rowBySelectionId = this.farmOpList.find(item => String(item.operationId) === String(this.ids[0]))
        if (rowBySelectionId) {
          return rowBySelectionId
        }
      }
      if (this.farmOpList[0] && this.getOperationId(this.farmOpList[0])) {
        return this.farmOpList[0]
      }
      if (this.recentRows[0] && this.getOperationId(this.recentRows[0])) {
        return this.recentRows[0]
      }
      return null
    },
    handleAdd() {
      this.reset()
      this.fetchPlotOptions('')
      this.open = true
      this.title = '新增农事记录'
    },
    handleUpdate(row) {
      this.reset()
      const operationId = row.operationId || this.ids
      getFarmOp(operationId).then(response => {
        this.form = response.data
        this.fetchPlotOptions(this.form.plotCode || '')
        this.open = true
        this.title = '修改农事记录'
      })
    },
    handlePlotOptionsVisible(visible) {
      if (visible) {
        this.fetchPlotOptions('')
      }
    },
    fetchPlotOptions(keyword) {
      this.plotOptionsLoading = true
      getFarmOpPlotOptions(keyword || '')
        .then(response => {
          const options = Array.isArray(response.data) ? response.data : []
          if (this.form.plotCode && options.indexOf(this.form.plotCode) === -1) {
            options.unshift(this.form.plotCode)
          }
          this.plotOptions = options
        })
        .catch(() => {
          this.plotOptions = []
          this.$modal.msgError('地块选项加载失败，请稍后重试')
        })
        .finally(() => {
          this.plotOptionsLoading = false
        })
    },
    handleAdvice(row) {
      const target = this.resolveAdviceTarget(row)
      const operationId = this.getOperationId(target)
      if (!target || !operationId) {
        this.$modal.msgWarning('请先选择一条农事记录')
        return
      }
      this.smartAdviceLoading = true
      getFarmOpAdvice(operationId)
        .then(response => {
          this.smartAdvice = this.normalizeAdviceResult(response.data)
          this.smartAdviceRequestTime = this.parseTime(new Date())
          this.$modal.msgSuccess('智能建议已更新')
        })
        .catch(() => {
          this.$modal.msgError('智能建议获取失败，请稍后重试')
        })
        .finally(() => {
          this.smartAdviceLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.operationId !== undefined) {
          updateFarmOp(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addFarmOp(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const operationIds = row.operationId || this.ids
      this.$modal
        .confirm('是否确认删除农事记录编号为"' + operationIds + '"的数据项？')
        .then(function () {
          return delFarmOp(operationIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/farmOp/export',
        {
          ...this.queryParams
        },
        `farm_op_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.farm-hero {
  border: 1px solid #e6e1d5;
  background: linear-gradient(135deg, #fffdf6 0%, #f9f5ea 100%);
}

.farm-hero-head,
.farm-panel-head,
.farm-recent-item,
.farm-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.farm-title {
  font-size: 20px;
  font-weight: 700;
  color: #5a3a0a;
}

.farm-desc {
  margin-top: 6px;
  color: #7a684a;
}

.farm-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.farm-kpi-row,
.farm-overview-row {
  margin-top: 14px;
}

.farm-kpi-card,
.farm-panel {
  border: 1px solid #eadfc9;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.farm-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.farm-kpi-card span,
.farm-panel-head small,
.farm-kpi-card small,
.farm-recent-item p,
.farm-advice-box p {
  color: #81745f;
}

.farm-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #5a3a0a;
}

.farm-panel-head,
.farm-panel-subhead {
  margin-bottom: 12px;
}

.farm-mix-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.farm-mix-item {
  padding: 10px 12px;
  border-radius: 8px;
  background: #fffaf0;
  display: flex;
  justify-content: space-between;
}

.farm-recent {
  margin-top: 14px;
}

.farm-recent-item {
  padding: 8px 0;
  border-top: 1px solid #f1eadc;
}

.farm-recent-item p {
  margin: 4px 0 0;
}

.farm-score strong {
  font-size: 26px;
  color: #a16912;
}

.farm-advice-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #5f513f;
}

.farm-ai-excerpt {
  margin-top: 12px;
}

.farm-ai-excerpt pre {
  max-height: 140px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  padding: 8px 10px;
  border-radius: 6px;
  background: #faf6ef;
  color: #6a5b48;
}

.farm-advice-foot {
  margin-top: 8px;
  font-size: 12px;
  color: #9a8d78;
}
</style>
