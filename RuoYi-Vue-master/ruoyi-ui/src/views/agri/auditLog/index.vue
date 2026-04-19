<template>
  <div class="app-container audit-ops-page">
    <div class="hero-panel">
      <div class="hero-title">溯源审计事件中枢</div>
      <div class="hero-subtitle">围绕事件流监控、异常研判、归因复核和闭环处置构建真实可用的审计模块</div>
      <div class="kpi-grid">
        <div class="kpi-card"><div class="kpi-label">日志总量</div><div class="kpi-value">{{ dashboard.kpi.total || 0 }}</div></div>
        <div class="kpi-card accent-green"><div class="kpi-label">成功事件</div><div class="kpi-value">{{ dashboard.kpi.success || 0 }}</div></div>
        <div class="kpi-card accent-red"><div class="kpi-label">失败事件</div><div class="kpi-value">{{ dashboard.kpi.failed || 0 }}</div></div>
        <div class="kpi-card accent-blue"><div class="kpi-label">成功率</div><div class="kpi-value">{{ formatRate(dashboard.kpi.successRate) }}%</div></div>
      </div>
    </div>

    <el-row :gutter="14" class="ops-row">
      <el-col :xs="24" :lg="9">
        <el-card class="queue-card" shadow="never">
          <div slot="header" class="card-header">
            <span>异常事件流</span>
            <el-tag size="mini" type="danger">{{ opsOverview.riskQueue.length }}条</el-tag>
          </div>
          <div v-if="!opsOverview.riskQueue.length" class="empty-tip">暂无异常事件</div>
          <div v-for="item in opsOverview.riskQueue" :key="item.auditId" class="queue-item" :class="{ active: selectedRecord && selectedRecord.auditId === item.auditId }" @click="focusRecord(item)">
            <div class="queue-main">
              <div class="queue-title">{{ item.bizNo || '未命名事件' }}</div>
              <div class="queue-sub">{{ item.moduleName || '-' }} | {{ item.operatorName || '未知操作者' }}</div>
            </div>
            <dict-tag :options="dict.type.agri_audit_result" :value="item.operateResult" />
          </div>
        </el-card>

        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header"><span>七日事件趋势</span></div>
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
            <span>焦点事件处置台</span>
            <div>
              <el-button size="mini" icon="el-icon-refresh" @click="loadOpsData">刷新态势</el-button>
              <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['agri:auditLog:add']">登记事件</el-button>
            </div>
          </div>

          <div v-if="selectedRecord" class="focus-body">
            <el-descriptions :column="2" border size="small" class="focus-desc">
              <el-descriptions-item label="业务编号">{{ selectedRecord.bizNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="模块">{{ selectedRecord.moduleName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="操作类型"><dict-tag :options="dict.type.agri_audit_action_type" :value="selectedRecord.actionType" /></el-descriptions-item>
              <el-descriptions-item label="结果"><dict-tag :options="dict.type.agri_audit_result" :value="selectedRecord.operateResult" /></el-descriptions-item>
              <el-descriptions-item label="交易哈希">{{ selectedRecord.txHash || '-' }}</el-descriptions-item>
              <el-descriptions-item label="来源IP">{{ selectedRecord.ipAddress || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="action-row">
              <el-button type="primary" :loading="smartLoading" @click="runSmartDetect(selectedRecord)">智能异常检测</el-button>
              <el-button type="success" plain :loading="resolveLoading" @click="closeIncident('closeSuccess')" v-hasPermi="['agri:auditLog:edit']">标记闭环成功</el-button>
              <el-button type="danger" plain :loading="resolveLoading" @click="closeIncident('markFailed')" v-hasPermi="['agri:auditLog:edit']">升级为失败事件</el-button>
              <el-button type="info" plain @click="handleUpdate(selectedRecord)" v-hasPermi="['agri:auditLog:edit']">编辑事件</el-button>
            </div>

            <div v-if="smartResult" class="smart-box">
              <div class="smart-title">智能检测结果</div>
              <div class="smart-line">算法：{{ smartResult.algorithm || '-' }} | 异常分：{{ smartResult.anomalyScore || 0 }} | 风险等级：{{ smartResult.riskLevel || '-' }}</div>
              <div class="smart-line">结论：{{ smartResult.summary || '-' }}</div>
              <div class="smart-line">告警项：{{ (smartResult.alerts || []).join('；') || '-' }}</div>
              <div v-if="smartResult.aiOriginalExcerpt" class="smart-line">AI原文摘录：{{ smartResult.aiOriginalExcerpt }}</div>
            </div>
          </div>

          <div v-else class="empty-tip">请从左侧异常流或下方台账中选择一个事件</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="ledger-card">
      <div slot="header" class="card-header">
        <span>审计事件台账</span>
        <div>
          <el-button size="mini" type="warning" icon="el-icon-download" @click="handleExport" v-hasPermi="['agri:auditLog:export']">导出</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" class="search-line">
        <el-form-item><el-input v-model="queryParams.bizNo" placeholder="业务编号" clearable @keyup.enter.native="handleQuery" /></el-form-item>
        <el-form-item><el-input v-model="queryParams.moduleName" placeholder="模块名称" clearable @keyup.enter.native="handleQuery" /></el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.operateResult" placeholder="事件结果" clearable>
            <el-option v-for="dictItem in dict.type.agri_audit_result" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">检索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="auditLogList" @row-click="focusRecord" @selection-change="handleSelectionChange" highlight-current-row>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="业务编号" align="center" prop="bizNo" min-width="140" show-overflow-tooltip />
        <el-table-column label="模块" align="center" prop="moduleName" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" align="center" prop="actionType" width="110"><template slot-scope="scope"><dict-tag :options="dict.type.agri_audit_action_type" :value="scope.row.actionType" /></template></el-table-column>
        <el-table-column label="结果" align="center" prop="operateResult" width="100"><template slot-scope="scope"><dict-tag :options="dict.type.agri_audit_result" :value="scope.row.operateResult" /></template></el-table-column>
        <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
        <el-table-column label="时间" align="center" prop="operateTime" width="170" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-cpu" @click.stop="runSmartDetect(scope.row)">智能检测</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:auditLog:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:auditLog:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-row>
          <el-col :span="12"><el-form-item label="业务编号" prop="bizNo"><el-input v-model="form.bizNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模块名称" prop="moduleName"><el-input v-model="form.moduleName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="操作类型" prop="actionType"><el-select v-model="form.actionType" style="width: 100%"><el-option v-for="dictItem in dict.type.agri_audit_action_type" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作结果" prop="operateResult"><el-select v-model="form.operateResult" style="width: 100%"><el-option v-for="dictItem in dict.type.agri_audit_result" :key="dictItem.value" :label="dictItem.label" :value="dictItem.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="操作人" prop="operatorName"><el-input v-model="form.operatorName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时间" prop="operateTime"><el-date-picker v-model="form.operateTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="交易哈希" prop="txHash"><el-input v-model="form.txHash" /></el-form-item>
        <el-form-item label="IP地址" prop="ipAddress"><el-input v-model="form.ipAddress" /></el-form-item>
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
  listAuditLog,
  getAuditLog,
  delAuditLog,
  addAuditLog,
  updateAuditLog,
  getAuditLogDashboard,
  getAuditOpsOverview,
  smartDetectAuditLog,
  closeAuditIncident
} from '@/api/agri/auditLog'
import { normalizeSmartResult } from '@/utils/agriSmartResult'

export default {
  name: 'AuditLog',
  dicts: ['agri_audit_action_type', 'agri_audit_result'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      auditLogList: [],
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
        bizNo: null,
        moduleName: null,
        actionType: null,
        operateResult: null
      },
      form: {},
      rules: {
        bizNo: [{ required: true, message: '业务编号不能为空', trigger: 'blur' }],
        moduleName: [{ required: true, message: '模块名称不能为空', trigger: 'blur' }],
        actionType: [{ required: true, message: '操作类型不能为空', trigger: 'change' }],
        operateResult: [{ required: true, message: '操作结果不能为空', trigger: 'change' }]
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
      getAuditLogDashboard().then(res => {
        this.dashboard = res.data || { kpi: {} }
      })
      getAuditOpsOverview().then(res => {
        this.opsOverview = res.data || { riskQueue: [], timeline: [] }
      })
    },
    getList() {
      this.loading = true
      listAuditLog(this.queryParams).then(response => {
        this.auditLogList = response.rows || []
        this.total = response.total
        if (!this.selectedRecord && this.auditLogList.length) {
          this.selectedRecord = this.auditLogList[0]
        }
        if (this.selectedRecord) {
          const latest = this.auditLogList.find(item => item.auditId === this.selectedRecord.auditId)
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
        auditId: null,
        bizNo: null,
        moduleName: null,
        actionType: null,
        operatorName: null,
        operateTime: null,
        operateResult: '1',
        txHash: null,
        ipAddress: null,
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
      this.ids = selection.map(item => item.auditId)
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
    runSmartDetect(row) {
      const target = row || this.selectedRecord
      if (!target || !target.auditId) {
        this.$message.warning('请先选择审计事件')
        return
      }
      this.smartLoading = true
      this.smartResult = null
      smartDetectAuditLog(target.auditId).then(res => {
        this.smartResult = this.normalizeSmartResult(res.data || {})
        this.$message.success('智能检测完成')
      }).finally(() => {
        this.smartLoading = false
      })
    },
    closeIncident(action) {
      const target = this.selectedRecord
      if (!target || !target.auditId) {
        this.$message.warning('请先选择审计事件')
        return
      }
      this.resolveLoading = true
      closeAuditIncident(target.auditId, { action }).then(() => {
        this.$modal.msgSuccess('事件处置完成')
        this.getList()
        this.loadOpsData()
      }).finally(() => {
        this.resolveLoading = false
      })
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增溯源审计日志'
    },
    handleUpdate(row) {
      this.reset()
      const auditId = row.auditId || this.ids[0]
      getAuditLog(auditId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改溯源审计日志'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.auditId != null) {
            updateAuditLog(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.loadOpsData()
            })
          } else {
            addAuditLog(this.form).then(() => {
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
      const auditIds = row.auditId || this.ids
      this.$modal.confirm('是否确认删除溯源审计日志编号为"' + auditIds + '"的数据项？').then(() => {
        return delAuditLog(auditIds)
      }).then(() => {
        this.getList()
        this.loadOpsData()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/auditLog/export', {
        ...this.queryParams
      }, `audit_log_${new Date().getTime()}.xlsx`)
    },
    formatRate(num) {
      const n = Number(num || 0)
      return n.toFixed(2)
    },
    timelinePercent(count) {
      const max = Math.max(...(this.opsOverview.timeline || []).map(item => item.count || 0), 1)
      return Math.round(((count || 0) / max) * 100)
    },
    normalizeSmartResult(payload) {
      return normalizeSmartResult(payload, 'alerts')
    }
  }
}
</script>

<style scoped>
.audit-ops-page {
  background: radial-gradient(circle at 90% 10%, #fff7ec 0%, #f9fbff 48%, #ffffff 100%);
}
.hero-panel {
  background: linear-gradient(120deg, #2f1f36 0%, #523b67 45%, #6a4b7b 100%);
  color: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 14px;
}
.hero-title { font-size: 22px; font-weight: 700; }
.hero-subtitle { margin-top: 6px; opacity: 0.88; }
.kpi-grid { margin-top: 14px; display: grid; grid-template-columns: repeat(4, minmax(120px, 1fr)); grid-gap: 12px; }
.kpi-card { background: rgba(255, 255, 255, 0.14); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 10px; padding: 12px; }
.kpi-label { font-size: 12px; opacity: 0.85; }
.kpi-value { font-size: 26px; font-weight: 700; margin-top: 2px; }
.accent-green .kpi-value { color: #8df4be; }
.accent-red .kpi-value { color: #ffd0d0; }
.accent-blue .kpi-value { color: #bde6ff; }
.ops-row { margin-bottom: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.queue-card,.chart-card,.focus-card,.ledger-card { border-radius: 12px; }
.queue-item {
  border: 1px solid #f0e6f7;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.queue-item.active { border-color: #8b63b6; background: #f6f0fc; }
.queue-title { font-weight: 600; color: #392a4f; }
.queue-sub { font-size: 12px; color: #7b6f8e; margin-top: 4px; }
.trend-item { display: grid; grid-template-columns: 100px 1fr 30px; grid-gap: 8px; align-items: center; margin-bottom: 8px; }
.trend-value { text-align: right; color: #5f4b76; }
.focus-body { display: flex; flex-direction: column; gap: 10px; }
.action-row { display: flex; flex-wrap: wrap; gap: 8px; }
.smart-box { background: #fff8f8; border: 1px solid #f1d8d8; border-radius: 8px; padding: 10px; line-height: 1.8; }
.smart-title { font-weight: 700; color: #5f2e2e; margin-bottom: 4px; }
.smart-line { font-size: 13px; color: #4f3c5d; }
.search-line { margin-bottom: 10px; }
.empty-tip { color: #8a97a5; padding: 14px 0; text-align: center; }
@media (max-width: 992px) {
  .kpi-grid { grid-template-columns: repeat(2, minmax(120px, 1fr)); }
}
</style>
