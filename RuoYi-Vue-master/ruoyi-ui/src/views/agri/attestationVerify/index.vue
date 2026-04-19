<template>
  <div class="app-container verify-ops-page">
    <div class="hero-panel">
      <div class="hero-title">数据存证校验作战台</div>
      <div class="hero-subtitle">把链上链下一致性核验、风险分拨、智能复核和人工闭环放到同一工作面</div>
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">校验总量</div>
          <div class="kpi-value">{{ dashboard.kpi.total || 0 }}</div>
        </div>
        <div class="kpi-card accent-green">
          <div class="kpi-label">一致通过</div>
          <div class="kpi-value">{{ dashboard.kpi.matched || 0 }}</div>
        </div>
        <div class="kpi-card accent-red">
          <div class="kpi-label">不一致</div>
          <div class="kpi-value">{{ dashboard.kpi.mismatched || 0 }}</div>
        </div>
        <div class="kpi-card accent-blue">
          <div class="kpi-label">通过率</div>
          <div class="kpi-value">{{ formatRate(dashboard.kpi.matchRate) }}%</div>
        </div>
      </div>
    </div>

    <el-row :gutter="14" class="ops-row">
      <el-col :xs="24" :lg="9">
        <el-card class="queue-card" shadow="never">
          <div slot="header" class="card-header">
            <span>高优先级校验队列</span>
            <el-tag size="mini" type="danger">{{ opsOverview.riskQueue.length }}条</el-tag>
          </div>
          <div v-if="!opsOverview.riskQueue.length" class="empty-tip">暂无待处置高风险记录</div>
          <div v-for="item in opsOverview.riskQueue" :key="item.verifyId" class="queue-item" :class="{ active: selectedRecord && selectedRecord.verifyId === item.verifyId }" @click="focusRecord(item)">
            <div class="queue-main">
              <div class="queue-title">{{ item.attestationNo || '未命名存证' }}</div>
              <div class="queue-sub">批次：{{ item.batchNo || '-' }} | 校验人：{{ item.verifyByUser || '待分配' }}</div>
            </div>
            <dict-tag :options="dict.type.agri_verify_status" :value="item.verifyStatus" />
          </div>
        </el-card>

        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header"><span>七日校验趋势</span></div>
          <div v-for="node in opsOverview.timeline" :key="node.day" class="trend-item">
            <span>{{ node.day }}</span>
            <el-progress :percentage="timelinePercent(node.count)" :stroke-width="10" :show-text="false"></el-progress>
            <span class="trend-value">{{ node.count }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card class="focus-card" shadow="never">
          <div slot="header" class="card-header">
            <span>焦点校验工单</span>
            <div>
              <el-button size="mini" icon="el-icon-refresh" @click="loadOpsData">刷新态势</el-button>
              <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['agri:attestationVerify:add']">新建工单</el-button>
            </div>
          </div>

          <div v-if="selectedRecord" class="focus-body">
            <el-descriptions :column="2" border size="small" class="focus-desc">
              <el-descriptions-item label="存证编号">{{ selectedRecord.attestationNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业务批次">{{ selectedRecord.batchNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="数据类型">
                <dict-tag :options="dict.type.agri_data_type" :value="selectedRecord.dataType" />
              </el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <dict-tag :options="dict.type.agri_verify_status" :value="selectedRecord.verifyStatus" />
              </el-descriptions-item>
            </el-descriptions>

            <div class="hash-box">
              <div><strong>原始哈希：</strong>{{ selectedRecord.originHash || '-' }}</div>
              <div><strong>链上哈希：</strong>{{ selectedRecord.chainHash || '-' }}</div>
            </div>

            <div class="action-row">
              <el-button type="primary" :loading="smartLoading" @click="runSmartVerify(selectedRecord)">智能一致性校验</el-button>
              <el-button type="success" plain :loading="resolveLoading" @click="resolveIssue('pass')" v-hasPermi="['agri:attestationVerify:edit']">一键闭环通过</el-button>
              <el-button type="warning" plain :loading="resolveLoading" @click="resolveIssue('manualReview')" v-hasPermi="['agri:attestationVerify:edit']">转人工复核</el-button>
              <el-button type="info" plain @click="handleUpdate(selectedRecord)" v-hasPermi="['agri:attestationVerify:edit']">编辑工单</el-button>
            </div>

            <div v-if="smartResult" class="smart-box">
              <div class="smart-title">智能校验结果</div>
              <div class="smart-line">算法：{{ smartResult.algorithm || '-' }} | 风险等级：{{ smartResult.riskLevel || '-' }}</div>
              <div class="smart-line">推断状态：{{ statusText(smartResult.inferredStatus) }} | 哈希相似度：{{ formatRate((smartResult.similarity || 0) * 100) }}%</div>
              <div class="smart-line">结论：{{ smartResult.summary || '-' }}</div>
              <div class="smart-line">处置建议：{{ (smartResult.hints || []).join('；') || '-' }}</div>
              <div v-if="smartResult.aiOriginalExcerpt" class="smart-line">AI原文摘录：{{ smartResult.aiOriginalExcerpt }}</div>
            </div>
          </div>

          <div v-else class="empty-tip">请从左侧风险队列或下方台账选择一条校验记录</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="ledger-card">
      <div slot="header" class="card-header">
        <span>校验台账</span>
        <div>
          <el-button size="mini" type="warning" icon="el-icon-download" @click="handleExport" v-hasPermi="['agri:attestationVerify:export']">导出</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" class="search-line">
        <el-form-item prop="attestationNo">
          <el-input v-model="queryParams.attestationNo" placeholder="存证编号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="业务批次号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item prop="verifyStatus">
          <el-select v-model="queryParams.verifyStatus" placeholder="校验状态" clearable>
            <el-option v-for="dictItem in dict.type.agri_verify_status" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">检索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="attestationVerifyList" @row-click="focusRecord" @selection-change="handleSelectionChange" highlight-current-row>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="存证编号" align="center" prop="attestationNo" min-width="150" show-overflow-tooltip />
        <el-table-column label="业务批次" align="center" prop="batchNo" min-width="130" show-overflow-tooltip />
        <el-table-column label="数据类型" align="center" prop="dataType" width="110">
          <template slot-scope="scope"><dict-tag :options="dict.type.agri_data_type" :value="scope.row.dataType" /></template>
        </el-table-column>
        <el-table-column label="校验状态" align="center" prop="verifyStatus" width="110">
          <template slot-scope="scope"><dict-tag :options="dict.type.agri_verify_status" :value="scope.row.verifyStatus" /></template>
        </el-table-column>
        <el-table-column label="校验时间" align="center" prop="verifyTime" width="170" />
        <el-table-column label="校验人" align="center" prop="verifyByUser" width="100" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-cpu" @click.stop="runSmartVerify(scope.row)">智能校验</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:attestationVerify:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:attestationVerify:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-row>
          <el-col :span="12"><el-form-item label="存证编号" prop="attestationNo"><el-input v-model="form.attestationNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="业务批次" prop="batchNo"><el-input v-model="form.batchNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="数据类型" prop="dataType"><el-select v-model="form.dataType" style="width: 100%"><el-option v-for="dictItem in dict.type.agri_data_type" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="校验状态" prop="verifyStatus"><el-select v-model="form.verifyStatus" style="width: 100%"><el-option v-for="dictItem in dict.type.agri_verify_status" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="原始哈希" prop="originHash"><el-input v-model="form.originHash" /></el-form-item>
        <el-form-item label="链上哈希" prop="chainHash"><el-input v-model="form.chainHash" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAttestationVerify,
  getAttestationVerify,
  delAttestationVerify,
  addAttestationVerify,
  updateAttestationVerify,
  getAttestationVerifyDashboard,
  getAttestationOpsOverview,
  smartVerifyAttestation,
  resolveAttestation
} from '@/api/agri/attestationVerify'

export default {
  name: 'AttestationVerify',
  dicts: ['agri_data_type', 'agri_verify_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      attestationVerifyList: [],
      selectedRecord: null,
      smartResult: null,
      smartLoading: false,
      resolveLoading: false,
      dashboard: {
        kpi: {}
      },
      opsOverview: {
        riskQueue: [],
        timeline: []
      },
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        attestationNo: null,
        batchNo: null,
        dataType: null,
        verifyStatus: null
      },
      form: {},
      rules: {
        attestationNo: [{ required: true, message: '存证编号不能为空', trigger: 'blur' }],
        batchNo: [{ required: true, message: '业务批次号不能为空', trigger: 'blur' }],
        dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }],
        originHash: [{ required: true, message: '原始哈希不能为空', trigger: 'blur' }],
        verifyStatus: [{ required: true, message: '校验状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    loadAll() {
      this.getList()
      this.loadOpsData()
    },
    loadOpsData() {
      getAttestationVerifyDashboard().then(res => {
        this.dashboard = res.data || { kpi: {} }
      })
      getAttestationOpsOverview().then(res => {
        this.opsOverview = res.data || { riskQueue: [], timeline: [] }
      })
    },
    getList() {
      this.loading = true
      listAttestationVerify(this.queryParams).then(response => {
        this.attestationVerifyList = response.rows || []
        this.total = response.total
        if (!this.selectedRecord && this.attestationVerifyList.length) {
          this.selectedRecord = this.attestationVerifyList[0]
        }
        if (this.selectedRecord) {
          const latest = this.attestationVerifyList.find(item => item.verifyId === this.selectedRecord.verifyId)
          if (latest) {
            this.selectedRecord = latest
          }
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        verifyId: null,
        attestationNo: null,
        batchNo: null,
        dataType: null,
        originHash: null,
        chainHash: null,
        verifyStatus: '0',
        verifyTime: null,
        verifyByUser: null,
        remark: null
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
      this.ids = selection.map(item => item.verifyId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (selection.length === 1) {
        this.focusRecord(selection[0])
      }
    },
    focusRecord(row) {
      if (!row) {
        return
      }
      this.selectedRecord = row
    },
    runSmartVerify(row) {
      const target = row || this.selectedRecord
      if (!target || !target.verifyId) {
        this.$message.warning('请先选择校验工单')
        return
      }
      this.smartLoading = true
      this.smartResult = null
      smartVerifyAttestation(target.verifyId).then(res => {
        this.smartResult = this.normalizeSmartResult(res.data || {})
        this.$message.success('智能校验完成')
      }).finally(() => {
        this.smartLoading = false
      })
    },
    resolveIssue(action) {
      const target = this.selectedRecord
      if (!target || !target.verifyId) {
        this.$message.warning('请先选择校验工单')
        return
      }
      this.resolveLoading = true
      const actionText = action === 'manualReview' ? '转人工复核' : '闭环通过'
      resolveAttestation(target.verifyId, { action }).then(() => {
        this.$modal.msgSuccess(actionText + '完成')
        this.getList()
        this.loadOpsData()
      }).finally(() => {
        this.resolveLoading = false
      })
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据存证与校验'
    },
    handleUpdate(row) {
      this.reset()
      const verifyId = row.verifyId || this.ids[0]
      getAttestationVerify(verifyId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改数据存证与校验'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.verifyId != null) {
            updateAttestationVerify(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.loadOpsData()
            })
          } else {
            addAttestationVerify(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.loadOpsData()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const verifyIds = row.verifyId || this.ids
      this.$modal.confirm('是否确认删除数据存证与校验编号为"' + verifyIds + '"的数据项？').then(() => {
        return delAttestationVerify(verifyIds)
      }).then(() => {
        this.getList()
        this.loadOpsData()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/attestationVerify/export', {
        ...this.queryParams
      }, `attestation_verify_${new Date().getTime()}.xlsx`)
    },
    formatRate(num) {
      const n = Number(num || 0)
      return n.toFixed(2)
    },
    statusText(status) {
      if (status === '1') {
        return '一致'
      }
      if (status === '2') {
        return '不一致'
      }
      return '待校验'
    },
    timelinePercent(count) {
      const max = Math.max(...(this.opsOverview.timeline || []).map(item => item.count || 0), 1)
      return Math.round(((count || 0) / max) * 100)
    },
    normalizeSmartResult(payload) {
      const hints = Array.isArray(payload.hints)
        ? payload.hints.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
        : []
      return {
        ...payload,
        hints,
        aiOriginalExcerpt: payload.aiOriginalExcerpt || payload.ai_original_excerpt || ''
      }
    }
  }
}
</script>

<style scoped>
.verify-ops-page {
  background: radial-gradient(circle at 10% 20%, #f4fbff 0%, #f7f9fc 45%, #ffffff 100%);
}
.hero-panel {
  background: linear-gradient(120deg, #17324f 0%, #1f4e72 45%, #2f6d96 100%);
  color: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 14px;
}
.hero-title {
  font-size: 22px;
  font-weight: 700;
}
.hero-subtitle {
  margin-top: 6px;
  opacity: 0.88;
}
.kpi-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  grid-gap: 12px;
}
.kpi-card {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  padding: 12px;
}
.kpi-label { font-size: 12px; opacity: 0.85; }
.kpi-value { font-size: 26px; font-weight: 700; margin-top: 2px; }
.accent-green .kpi-value { color: #8df4be; }
.accent-red .kpi-value { color: #ffd0d0; }
.accent-blue .kpi-value { color: #bde6ff; }
.ops-row { margin-bottom: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.queue-card,.chart-card,.focus-card,.ledger-card { border-radius: 12px; }
.queue-item {
  border: 1px solid #e6edf5;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.queue-item.active { border-color: #3e8ef7; background: #eef5ff; }
.queue-title { font-weight: 600; color: #1b2f45; }
.queue-sub { font-size: 12px; color: #6c7f93; margin-top: 4px; }
.trend-item { display: grid; grid-template-columns: 100px 1fr 30px; grid-gap: 8px; align-items: center; margin-bottom: 8px; }
.trend-value { text-align: right; color: #35506c; }
.focus-body { display: flex; flex-direction: column; gap: 10px; }
.hash-box { background: #f6fbff; border: 1px solid #dfeaf5; border-radius: 8px; padding: 10px; line-height: 1.8; }
.action-row { display: flex; flex-wrap: wrap; gap: 8px; }
.smart-box { background: #f8fff8; border: 1px solid #dcefdc; border-radius: 8px; padding: 10px; line-height: 1.8; }
.smart-title { font-weight: 700; color: #274b2f; margin-bottom: 4px; }
.smart-line { font-size: 13px; color: #30485f; }
.search-line { margin-bottom: 10px; }
.empty-tip { color: #8a97a5; padding: 14px 0; text-align: center; }
@media (max-width: 992px) {
  .kpi-grid { grid-template-columns: repeat(2, minmax(120px, 1fr)); }
}
</style>
