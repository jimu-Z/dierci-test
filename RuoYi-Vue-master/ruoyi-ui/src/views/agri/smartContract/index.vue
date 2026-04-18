<template>
  <div class="app-container">
    <div class="ops-shell contract-shell" v-loading="opsLoading">
      <div class="hero-banner contract-hero">
        <div class="hero-copy">
          <div class="hero-label">智能合约发布控制台</div>
          <h2>从“部署记录”进化为“合约版本治理中枢”</h2>
          <p>首屏聚焦发布压力、ABI完备度、部署失败风险，支持一键部署和安全体检。</p>
          <div class="hero-actions">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建部署任务</el-button>
            <el-button type="success" icon="el-icon-cpu" :disabled="!focusContract.deployId" @click="handleDeploy(focusContract)">执行部署</el-button>
            <el-button type="warning" icon="el-icon-data-analysis" :disabled="!focusContract.deployId" @click="handleSecurityCheck(focusContract)">安全体检</el-button>
            <el-button icon="el-icon-refresh" @click="refreshDashboard">刷新控制台</el-button>
          </div>
        </div>
        <div class="hero-stats">
          <div class="hero-stat-card">
            <div class="hero-stat-label">部署总量</div>
            <div class="hero-stat-value">{{ opsKpi.total || 0 }}</div>
            <div class="hero-stat-desc">累计合约发布记录</div>
          </div>
          <div class="hero-stat-card">
            <div class="hero-stat-label">部署成功率</div>
            <div class="hero-stat-value">{{ toPercent(opsKpi.deployRate) }}</div>
            <div class="hero-stat-desc">已部署 {{ opsKpi.deployed || 0 }} 条</div>
          </div>
          <div class="hero-stat-card danger-card">
            <div class="hero-stat-label">失败 / 待部署</div>
            <div class="hero-stat-value">{{ opsKpi.failed || 0 }} / {{ opsKpi.pending || 0 }}</div>
            <div class="hero-stat-desc">建议优先修复失败记录</div>
          </div>
          <div class="hero-stat-card warning-card">
            <div class="hero-stat-label">元数据缺失</div>
            <div class="hero-stat-value">{{ opsKpi.missingAbi || 0 }} / {{ opsKpi.missingContractAddress || 0 }}</div>
            <div class="hero-stat-desc">ABI / 合约地址</div>
          </div>
        </div>
      </div>

      <el-row :gutter="16" class="ops-grid">
        <el-col :span="15">
          <el-card shadow="never" class="ops-card">
            <div slot="header" class="ops-card-header">
              <span>发布压力队列</span>
              <span class="ops-card-subtitle">按部署风险自动排序</span>
            </div>
            <el-table :data="pressureQueue" size="mini">
              <el-table-column label="合约" prop="contractName" width="150" />
              <el-table-column label="版本" prop="contractVersion" width="90" />
              <el-table-column label="链平台" prop="chainPlatform" width="120" />
              <el-table-column label="状态" width="90">
                <template slot-scope="scope">
                  <el-tag :type="statusTagType(scope.row.deployStatus)" size="mini">{{ scope.row.statusLabel || formatDeployStatus(scope.row.deployStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="风险" width="90">
                <template slot-scope="scope">
                  <el-tag :type="riskTagType(scope.row.riskLevel)" size="mini">{{ scope.row.riskLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="处置理由" prop="riskReason" min-width="180" show-overflow-tooltip />
              <el-table-column label="操作" width="170" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" icon="el-icon-cpu" @click="handleDeploy(scope.row)">部署</el-button>
                  <el-button type="text" size="mini" icon="el-icon-data-analysis" @click="handleSecurityCheck(scope.row)">体检</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="9">
          <el-card shadow="never" class="ops-card focus-card">
            <div slot="header" class="ops-card-header">
              <span>聚焦合约</span>
              <span class="ops-card-subtitle">当前最高优先级发布任务</span>
            </div>
            <div v-if="focusContract.deployId" class="focus-panel">
              <div class="focus-title">{{ focusContract.contractName }}</div>
              <div class="focus-row"><span>版本</span><span>{{ focusContract.contractVersion || '-' }}</span></div>
              <div class="focus-row"><span>链平台</span><span>{{ focusContract.chainPlatform || '-' }}</span></div>
              <div class="focus-row"><span>状态</span><el-tag :type="statusTagType(focusContract.deployStatus)" size="mini">{{ focusContract.statusLabel || formatDeployStatus(focusContract.deployStatus) }}</el-tag></div>
              <div class="focus-row"><span>风险分</span><span>{{ focusContract.riskScore || 0 }}</span></div>
              <div class="focus-row"><span>合约地址</span><span class="mono">{{ shortText(focusContract.contractAddress) }}</span></div>
              <div class="focus-row"><span>部署交易</span><span class="mono">{{ shortText(focusContract.deployTxHash) }}</span></div>
              <div class="focus-summary">{{ focusContract.summary || focusContract.riskReason }}</div>
              <div class="focus-actions">
                <el-button type="primary" size="mini" @click="handleDeploy(focusContract)">执行部署</el-button>
                <el-button size="mini" @click="handleSecurityCheck(focusContract)">安全体检</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无聚焦合约" />
          </el-card>

          <el-card shadow="never" class="ops-card suggestions-card">
            <div slot="header" class="ops-card-header">
              <span>治理建议</span>
              <span class="ops-card-subtitle">自动化策略提示</span>
            </div>
            <div v-for="item in suggestions" :key="item.title" class="suggestion-item">
              <div class="suggestion-top">
                <strong>{{ item.title }}</strong>
                <el-tag :type="priorityTagType(item.priority)" size="mini">{{ item.priority }}</el-tag>
              </div>
              <div class="suggestion-content">{{ item.content }}</div>
            </div>
            <el-empty v-if="!suggestions.length" description="暂无建议" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="合约名称" prop="contractName">
        <el-input v-model="queryParams.contractName" placeholder="请输入合约名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="版本" prop="contractVersion">
        <el-input v-model="queryParams.contractVersion" placeholder="请输入版本" clearable style="width: 120px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="链平台" prop="chainPlatform">
        <el-select v-model="queryParams.chainPlatform" clearable style="width: 140px">
          <el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="部署状态" prop="deployStatus">
        <el-select v-model="queryParams.deployStatus" clearable style="width: 140px">
          <el-option v-for="item in deployStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:smartContract:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-cpu" size="mini" :disabled="single" @click="handleDeploy()" v-hasPermi="['agri:smartContract:edit']">执行部署</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-data-analysis" size="mini" :disabled="single" @click="handleSecurityCheck()" v-hasPermi="['agri:smartContract:edit']">安全体检</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:smartContract:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:smartContract:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="deployId" align="center" width="70" />
      <el-table-column label="合约名称" prop="contractName" align="center" width="140" />
      <el-table-column label="版本" prop="contractVersion" align="center" width="90" />
      <el-table-column label="链平台" prop="chainPlatform" align="center" width="120" />
      <el-table-column label="源码哈希" prop="sourceHash" align="center" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="合约地址" prop="contractAddress" align="center" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="部署交易" prop="deployTxHash" align="center" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="部署状态" align="center" width="100">
        <template slot-scope="scope"><el-tag :type="statusTagType(scope.row.deployStatus)" size="mini">{{ formatDeployStatus(scope.row.deployStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="部署时间" prop="deployTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.deployTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="210">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:smartContract:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-cpu" @click="handleDeploy(scope.row)" v-hasPermi="['agri:smartContract:edit']">部署</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handleSecurityCheck(scope.row)" v-hasPermi="['agri:smartContract:edit']">体检</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:smartContract:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="合约名称" prop="contractName"><el-input v-model="form.contractName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="合约版本" prop="contractVersion"><el-input v-model="form.contractVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="链平台" prop="chainPlatform"><el-select v-model="form.chainPlatform" style="width: 100%"><el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部署状态" prop="deployStatus"><el-select v-model="form.deployStatus" style="width: 100%"><el-option v-for="item in deployStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="源码哈希" prop="sourceHash"><el-input v-model="form.sourceHash" /></el-form-item>
        <el-form-item label="ABI" prop="abiJson"><el-input v-model="form.abiJson" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="合约地址" prop="contractAddress"><el-input v-model="form.contractAddress" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="安全体检" :visible.sync="securityOpen" width="760px" append-to-body>
      <el-skeleton v-if="securityLoading" :rows="5" animated />
      <div v-else class="verify-panel">
        <el-row :gutter="12" class="verify-score-row">
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">安全评分</div>
              <div class="verify-value">{{ securityResult.securityScore || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">风险等级</div>
              <div class="verify-value">{{ securityResult.riskLevel || '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">体检算法</div>
              <div class="verify-value small">{{ securityResult.algorithm || '-' }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-alert :title="securityResult.summary || '暂无体检结果'" :type="riskAlertType(securityResult.riskLevel)" :closable="false" />
        <div class="verify-title">发现项</div>
        <el-tag v-for="item in securityResult.findings || []" :key="item" type="warning" size="mini" class="issue-tag">{{ item }}</el-tag>
        <el-empty v-if="!(securityResult.findings || []).length" description="未发现异常项" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="securityOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listSmartContract,
  getSmartContract,
  addSmartContract,
  updateSmartContract,
  deploySmartContract,
  delSmartContract,
  getSmartContractDashboardOps,
  smartSecurityContract
} from '@/api/agri/smartContract'

export default {
  name: 'SmartContract',
  data() {
    return {
      loading: true,
      opsLoading: false,
      securityOpen: false,
      securityLoading: false,
      securityResult: {},
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      contractList: [],
      pressureQueue: [],
      focusContract: {},
      suggestions: [],
      opsKpi: {},
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractName: undefined,
        contractVersion: undefined,
        chainPlatform: undefined,
        deployStatus: undefined
      },
      form: {},
      chainPlatformOptions: [
        { label: 'FISCO BCOS', value: 'FISCO_BCOS' },
        { label: 'Hyperchain', value: 'HYPERCHAIN' },
        { label: 'Fabric', value: 'FABRIC' }
      ],
      deployStatusOptions: [
        { label: '待部署', value: '0' },
        { label: '已部署', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        contractName: [{ required: true, message: '合约名称不能为空', trigger: 'blur' }],
        contractVersion: [{ required: true, message: '合约版本不能为空', trigger: 'blur' }],
        chainPlatform: [{ required: true, message: '链平台不能为空', trigger: 'change' }],
        sourceHash: [{ required: true, message: '源码哈希不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadDashboardOps()
  },
  methods: {
    getList() {
      this.loading = true
      listSmartContract(this.queryParams)
        .then(response => {
          this.contractList = response.rows
          this.total = response.total
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadDashboardOps() {
      this.opsLoading = true
      getSmartContractDashboardOps()
        .then(response => {
          const data = response.data || {}
          this.opsKpi = data.kpi || {}
          this.pressureQueue = data.pressureQueue || []
          this.focusContract = data.focus || {}
          this.suggestions = data.suggestions || []
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    refreshDashboard() {
      this.loadDashboardOps()
    },
    formatDeployStatus(value) {
      const option = this.deployStatusOptions.find(item => item.value === value)
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
      if (value === undefined || value === null) {
        return '0%'
      }
      return `${Number(value).toFixed(1)}%`
    },
    shortText(value) {
      if (!value) return '-'
      if (value.length <= 16) return value
      return `${value.slice(0, 8)}...${value.slice(-6)}`
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        deployId: undefined,
        contractName: undefined,
        contractVersion: undefined,
        chainPlatform: undefined,
        sourceHash: undefined,
        abiJson: undefined,
        contractAddress: undefined,
        deployTxHash: undefined,
        deployStatus: '0',
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.deployId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增智能合约部署'
    },
    handleUpdate(row) {
      this.reset()
      const deployId = row.deployId || this.ids[0]
      getSmartContract(deployId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改智能合约部署'
      })
    },
    handleDeploy(row) {
      const deployId = row && row.deployId ? row.deployId : this.ids[0]
      if (!deployId) {
        this.$modal.msgWarning('请选择一条任务执行部署')
        return
      }
      deploySmartContract(deployId).then(() => {
        this.$modal.msgSuccess('部署执行成功')
        this.getList()
        this.loadDashboardOps()
      })
    },
    handleSecurityCheck(row) {
      const deployId = row && row.deployId ? row.deployId : this.ids[0]
      if (!deployId) {
        this.$modal.msgWarning('请选择一条任务执行安全体检')
        return
      }
      this.securityOpen = true
      this.securityLoading = true
      this.securityResult = {}
      smartSecurityContract(deployId)
        .then(response => {
          this.securityResult = response.data || {}
        })
        .finally(() => {
          this.securityLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.deployId !== undefined) {
          updateSmartContract(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.loadDashboardOps()
          })
          return
        }
        addSmartContract(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
          this.loadDashboardOps()
        })
      })
    },
    handleDelete(row) {
      const deployIds = row && row.deployId ? row.deployId : this.ids
      this.$modal
        .confirm(`是否确认删除智能合约部署编号为"${deployIds}"的数据项？`)
        .then(() => delSmartContract(deployIds))
        .then(() => {
          this.getList()
          this.loadDashboardOps()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/smartContract/export',
        {
          ...this.queryParams
        },
        `smart_contract_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.ops-shell {
  margin-bottom: 16px;
}

.hero-banner {
  background: linear-gradient(120deg, #1d4ed8, #1e40af 55%, #172554);
  color: #eff6ff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-copy {
  max-width: 620px;
}

.hero-label {
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.9;
}

.hero-copy h2 {
  margin: 8px 0;
}

.hero-copy p {
  margin: 0;
  opacity: 0.92;
}

.hero-actions {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 10px;
  min-width: 280px;
}

.hero-stat-card {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  padding: 10px;
}

.hero-stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.hero-stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  margin-top: 4px;
}

.hero-stat-desc {
  font-size: 12px;
  opacity: 0.85;
}

.ops-grid {
  margin-top: 16px;
}

.ops-card {
  border-radius: 12px;
}

.ops-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ops-card-subtitle {
  font-size: 12px;
  color: #64748b;
}

.focus-panel {
  font-size: 13px;
}

.focus-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 10px;
}

.focus-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.focus-summary {
  margin-top: 10px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 8px;
}

.focus-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.suggestion-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 8px;
}

.suggestion-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-content {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
}

.mono {
  font-family: Consolas, Monaco, monospace;
}

.verify-score-row {
  margin-bottom: 12px;
}

.verify-card {
  text-align: center;
}

.verify-label {
  font-size: 12px;
  color: #64748b;
}

.verify-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
}

.verify-value.small {
  font-size: 14px;
  word-break: break-all;
}

.verify-title {
  margin: 12px 0 8px;
  font-weight: 600;
}

.issue-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

@media (max-width: 1200px) {
  .ops-grid .el-col {
    width: 100%;
    margin-bottom: 12px;
  }

  .hero-stats {
    grid-template-columns: repeat(2, minmax(110px, 1fr));
    width: 100%;
  }
}
</style>
