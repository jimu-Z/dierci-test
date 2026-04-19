<template>
  <div class="app-container">
    <div class="ops-shell uplink-shell" v-loading="opsLoading">
      <div class="hero-banner uplink-hero">
        <div class="hero-copy">
          <div class="hero-label">数据上链接入调度台</div>
          <h2>让链上提交从“记录维护”升级为“调度闭环”</h2>
          <p>首屏展示待上链压力、缺失凭证与平台分布，支持直接执行上链和完整性核验。</p>
          <div class="hero-actions">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建上链任务</el-button>
            <el-button type="success" icon="el-icon-upload2" :disabled="!focusTask.uplinkId" @click="handleExecuteUplink(focusTask)">立即上链</el-button>
            <el-button type="warning" icon="el-icon-data-analysis" :disabled="!focusTask.uplinkId" @click="handleVerify(focusTask)">完整性核验</el-button>
            <el-button icon="el-icon-refresh" @click="refreshDashboard">刷新调度台</el-button>
          </div>
        </div>
        <div class="hero-stats">
          <div class="hero-stat-card">
            <div class="hero-stat-label">任务总量</div>
            <div class="hero-stat-value">{{ opsKpi.total || 0 }}</div>
            <div class="hero-stat-desc">当前在管上链台账</div>
          </div>
          <div class="hero-stat-card">
            <div class="hero-stat-label">成功率</div>
            <div class="hero-stat-value">{{ toPercent(opsKpi.successRate) }}</div>
            <div class="hero-stat-desc">已上链 {{ opsKpi.success || 0 }} 条</div>
          </div>
          <div class="hero-stat-card danger-card">
            <div class="hero-stat-label">失败 / 待处理</div>
            <div class="hero-stat-value">{{ opsKpi.failed || 0 }} / {{ opsKpi.pending || 0 }}</div>
            <div class="hero-stat-desc">优先处理高压条目</div>
          </div>
          <div class="hero-stat-card warning-card">
            <div class="hero-stat-label">凭证缺失</div>
            <div class="hero-stat-value">{{ opsKpi.missingTx || 0 }} / {{ opsKpi.missingContractAddress || 0 }}</div>
            <div class="hero-stat-desc">交易哈希 / 合约地址</div>
          </div>
        </div>
      </div>

      <el-row :gutter="16" class="ops-grid">
        <el-col :span="15">
          <el-card shadow="never" class="ops-card">
            <div slot="header" class="ops-card-header">
              <span>上链压力队列</span>
              <span class="ops-card-subtitle">按风险分自动排序，直达高优先条目</span>
            </div>
            <el-table :data="pressureQueue" size="mini">
              <el-table-column label="批次号" prop="batchNo" width="150" />
              <el-table-column label="数据类型" width="120">
                <template slot-scope="scope">{{ formatDataType(scope.row.dataType) }}</template>
              </el-table-column>
              <el-table-column label="链平台" prop="chainPlatform" width="120" />
              <el-table-column label="状态" width="90">
                <template slot-scope="scope">
                  <el-tag :type="statusTagType(scope.row.uplinkStatus)" size="mini">{{ scope.row.statusLabel || formatUplinkStatus(scope.row.uplinkStatus) }}</el-tag>
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
                  <el-button type="text" size="mini" icon="el-icon-upload2" @click="handleExecuteUplink(scope.row)">执行</el-button>
                  <el-button type="text" size="mini" icon="el-icon-data-analysis" @click="handleVerify(scope.row)">核验</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="9">
          <el-card shadow="never" class="ops-card focus-card">
            <div slot="header" class="ops-card-header">
              <span>聚焦任务</span>
              <span class="ops-card-subtitle">默认锁定最高风险任务</span>
            </div>
            <div v-if="focusTask.uplinkId" class="focus-panel">
              <div class="focus-title">{{ focusTask.batchNo }}</div>
              <div class="focus-row"><span>数据类型</span><span>{{ formatDataType(focusTask.dataType) }}</span></div>
              <div class="focus-row"><span>链平台</span><span>{{ focusTask.chainPlatform || '-' }}</span></div>
              <div class="focus-row"><span>状态</span><el-tag :type="statusTagType(focusTask.uplinkStatus)" size="mini">{{ focusTask.statusLabel || formatUplinkStatus(focusTask.uplinkStatus) }}</el-tag></div>
              <div class="focus-row"><span>风险分</span><span>{{ focusTask.riskScore || 0 }}</span></div>
              <div class="focus-row"><span>交易哈希</span><span class="mono">{{ shortText(focusTask.txHash) }}</span></div>
              <div class="focus-row"><span>合约地址</span><span class="mono">{{ shortText(focusTask.contractAddress) }}</span></div>
              <div class="focus-summary">{{ focusTask.summary || focusTask.riskReason }}</div>
              <div class="focus-actions">
                <el-button type="primary" size="mini" @click="handleExecuteUplink(focusTask)">执行上链</el-button>
                <el-button size="mini" @click="handleVerify(focusTask)">完整性核验</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无聚焦任务" />
          </el-card>

          <el-card shadow="never" class="ops-card suggestions-card">
            <div slot="header" class="ops-card-header">
              <span>调度建议</span>
              <span class="ops-card-subtitle">基于实时压力自动生成</span>
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
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入业务批次号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <el-select v-model="queryParams.dataType" clearable style="width: 140px">
          <el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="链平台" prop="chainPlatform">
        <el-select v-model="queryParams.chainPlatform" clearable style="width: 140px">
          <el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="上链状态" prop="uplinkStatus">
        <el-select v-model="queryParams.uplinkStatus" clearable style="width: 140px">
          <el-option v-for="item in uplinkStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:dataUplink:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-upload2" size="mini" :disabled="single" @click="handleExecuteUplink()" v-hasPermi="['agri:dataUplink:edit']">执行上链</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-data-analysis" size="mini" :disabled="single" @click="handleVerify()" v-hasPermi="['agri:dataUplink:edit']">完整性核验</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:dataUplink:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:dataUplink:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="uplinkId" align="center" width="70" />
      <el-table-column label="批次号" prop="batchNo" align="center" width="150" />
      <el-table-column label="数据类型" align="center" width="120">
        <template slot-scope="scope">{{ formatDataType(scope.row.dataType) }}</template>
      </el-table-column>
      <el-table-column label="数据哈希" prop="dataHash" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="链平台" prop="chainPlatform" align="center" width="110" />
      <el-table-column label="合约地址" prop="contractAddress" align="center" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="交易哈希" prop="txHash" align="center" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="上链状态" align="center" width="100">
        <template slot-scope="scope"><el-tag :type="statusTagType(scope.row.uplinkStatus)" size="mini">{{ formatUplinkStatus(scope.row.uplinkStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="上链时间" prop="uplinkTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.uplinkTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:dataUplink:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-upload2" @click="handleExecuteUplink(scope.row)" v-hasPermi="['agri:dataUplink:edit']">执行</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handleVerify(scope.row)" v-hasPermi="['agri:dataUplink:edit']">核验</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:dataUplink:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="820px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="业务批次号" prop="batchNo"><el-input v-model="form.batchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数据类型" prop="dataType"><el-select v-model="form.dataType" style="width: 100%"><el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="数据哈希" prop="dataHash"><el-input v-model="form.dataHash" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="链平台" prop="chainPlatform"><el-select v-model="form.chainPlatform" style="width: 100%"><el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="合约地址" prop="contractAddress"><el-input v-model="form.contractAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上链状态" prop="uplinkStatus"><el-select v-model="form.uplinkStatus" style="width: 100%"><el-option v-for="item in uplinkStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="完整性核验" :visible.sync="verifyOpen" width="760px" append-to-body>
      <el-skeleton v-if="verifyLoading" :rows="5" animated />
      <div v-else class="verify-panel">
        <el-row :gutter="12" class="verify-score-row">
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">完整性分数</div>
              <div class="verify-value">{{ verifyResult.integrity || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">风险等级</div>
              <div class="verify-value">{{ verifyResult.riskLevel || '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="verify-card">
              <div class="verify-label">核验算法</div>
              <div class="verify-value small">{{ verifyResult.algorithm || '-' }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-alert :title="verifyResult.summary || '暂无核验结果'" :type="riskAlertType(verifyResult.riskLevel)" :closable="false" />
        <div class="verify-title">问题列表</div>
        <el-tag v-for="item in verifyResult.issues || []" :key="item" type="warning" size="mini" class="issue-tag">{{ item }}</el-tag>
        <el-empty v-if="!(verifyResult.issues || []).length" description="未发现异常项" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="verifyOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDataUplink,
  getDataUplink,
  addDataUplink,
  updateDataUplink,
  executeDataUplink,
  delDataUplink,
  getDataUplinkDashboardOps,
  smartVerifyDataUplink
} from '@/api/agri/dataUplink'

export default {
  name: 'DataUplink',
  data() {
    return {
      loading: true,
      opsLoading: false,
      verifyLoading: false,
      verifyOpen: false,
      verifyResult: {},
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      taskList: [],
      pressureQueue: [],
      suggestions: [],
      focusTask: {},
      opsKpi: {},
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchNo: undefined,
        dataType: undefined,
        chainPlatform: undefined,
        uplinkStatus: undefined
      },
      form: {},
      dataTypeOptions: [
        { label: '物流轨迹', value: 'LOGISTICS_TRACE' },
        { label: '质检报告', value: 'QUALITY_REPORT' },
        { label: '设备数据', value: 'DEVICE_METRIC' },
        { label: '溯源页面', value: 'TRACE_PAGE' }
      ],
      chainPlatformOptions: [
        { label: 'Hyperchain', value: 'HYPERCHAIN' },
        { label: 'FISCO BCOS', value: 'FISCO_BCOS' },
        { label: 'Fabric', value: 'FABRIC' }
      ],
      uplinkStatusOptions: [
        { label: '待上链', value: '0' },
        { label: '已上链', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        batchNo: [{ required: true, message: '业务批次号不能为空', trigger: 'blur' }],
        dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }],
        dataHash: [{ required: true, message: '数据哈希不能为空', trigger: 'blur' }],
        chainPlatform: [{ required: true, message: '链平台不能为空', trigger: 'change' }]
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
      listDataUplink(this.queryParams)
        .then(response => {
          this.taskList = response.rows
          this.total = response.total
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadDashboardOps() {
      this.opsLoading = true
      getDataUplinkDashboardOps()
        .then(response => {
          const data = response.data || {}
          this.opsKpi = data.kpi || {}
          this.pressureQueue = data.pressureQueue || []
          this.focusTask = data.focus || {}
          this.suggestions = data.suggestions || []
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    refreshDashboard() {
      this.loadDashboardOps()
    },
    formatUplinkStatus(value) {
      const option = this.uplinkStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    formatDataType(value) {
      const option = this.dataTypeOptions.find(item => item.value === value)
      return option ? option.label : value || '-'
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
        uplinkId: undefined,
        batchNo: undefined,
        dataType: undefined,
        dataHash: undefined,
        chainPlatform: undefined,
        contractAddress: undefined,
        txHash: undefined,
        uplinkStatus: '0',
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
      this.ids = selection.map(item => item.uplinkId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (selection.length === 1) {
        this.focusTask = selection[0]
      }
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.focusTask = row
      this.handleSelectionChange([row])
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据上链任务'
    },
    handleUpdate(row) {
      this.reset()
      const target = this.resolveTargetTask(row)
      const uplinkId = target && target.uplinkId
      if (!uplinkId) {
        this.$modal.msgWarning('请选择一条任务进行修改')
        return
      }
      getDataUplink(uplinkId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改数据上链任务'
      })
    },
    handleExecuteUplink(row) {
      const target = this.resolveTargetTask(row)
      const uplinkId = target && target.uplinkId
      if (!uplinkId) {
        this.$modal.msgWarning('请选择一条任务执行上链')
        return
      }
      executeDataUplink(uplinkId).then(() => {
        this.$modal.msgSuccess('上链执行成功')
        this.getList()
        this.loadDashboardOps()
      })
    },
    handleVerify(row) {
      const target = this.resolveTargetTask(row)
      const uplinkId = target && target.uplinkId
      if (!uplinkId) {
        this.$modal.msgWarning('请选择一条任务执行完整性核验')
        return
      }
      this.verifyOpen = true
      this.verifyLoading = true
      this.verifyResult = {}
      smartVerifyDataUplink(uplinkId)
        .then(response => {
          this.verifyResult = response.data || {}
        })
        .finally(() => {
          this.verifyLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.uplinkId !== undefined) {
          updateDataUplink(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.loadDashboardOps()
          })
          return
        }
        addDataUplink(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
          this.loadDashboardOps()
        })
      })
    },
    handleDelete(row) {
      const uplinkIds = row && row.uplinkId ? row.uplinkId : this.ids
      this.$modal
        .confirm(`是否确认删除数据上链任务编号为"${uplinkIds}"的数据项？`)
        .then(() => delDataUplink(uplinkIds))
        .then(() => {
          this.getList()
          this.loadDashboardOps()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/dataUplink/export',
        {
          ...this.queryParams
        },
        `data_uplink_${new Date().getTime()}.xlsx`
      )
    },
    resolveTargetTask(row) {
      if (row && row.uplinkId) {
        return row
      }
      if (this.focusTask && this.focusTask.uplinkId) {
        return this.focusTask
      }
      if (this.ids.length) {
        const selected = this.taskList.find(item => item.uplinkId === this.ids[0])
        if (selected) {
          return selected
        }
      }
      if (this.taskList.length) {
        return this.taskList[0]
      }
      return this.pressureQueue[0]
    }
  }
}
</script>

<style scoped>
.ops-shell {
  margin-bottom: 16px;
}

.hero-banner {
  background: linear-gradient(120deg, #0f766e, #115e59 55%, #134e4a);
  color: #ecfeff;
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
  background: #ecfeff;
  border: 1px solid #99f6e4;
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
