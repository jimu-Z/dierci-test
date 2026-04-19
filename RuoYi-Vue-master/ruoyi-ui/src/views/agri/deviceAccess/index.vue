<template>
  <div class="app-container">
    <div class="ops-shell device-shell" v-loading="opsLoading">
      <div class="hero-banner device-hero">
        <div class="hero-copy">
          <div class="hero-label">设备接入运营台</div>
          <h2>围绕在线率、离线风险和协议分布组织设备接入处置</h2>
          <p>把设备接入从单纯台账升级成可巡检、可诊断、可激活的运营中枢，支持批量定位高压节点。</p>
          <div class="hero-actions">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增设备</el-button>
            <el-button icon="el-icon-refresh" @click="refreshDashboard">刷新中枢</el-button>
            <el-button type="warning" icon="el-icon-data-analysis" :disabled="!focusNode.nodeId" @click="handleDiagnose(focusNode)">智能诊断</el-button>
          </div>
        </div>
        <div class="hero-stats">
          <div v-for="card in metricCards" :key="card.label" class="hero-stat-card">
            <div class="hero-stat-label">{{ card.label }}</div>
            <div class="hero-stat-value">{{ card.value }}</div>
            <div class="hero-stat-desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>

      <el-row :gutter="16" class="ops-grid">
        <el-col :span="15">
          <el-card shadow="never" class="ops-card">
            <div slot="header" class="ops-card-header">
              <span>接入压力队列</span>
              <span class="ops-card-subtitle">重点关注离线、异常和缺少最近在线时间的节点</span>
            </div>
            <el-table :data="pressureQueue" size="mini" v-loading="opsLoading">
              <el-table-column label="设备编码" prop="deviceCode" width="150" />
              <el-table-column label="设备名称" prop="deviceName" width="130" show-overflow-tooltip />
              <el-table-column label="设备类型" prop="deviceTypeLabel" width="120" />
              <el-table-column label="协议" prop="protocolLabel" width="90" />
              <el-table-column label="接入状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="statusTagType(scope.row.accessStatus)" size="mini">{{ scope.row.statusLabel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="风险等级" width="90">
                <template slot-scope="scope">
                  <el-tag :type="riskTagType(scope.row.riskLevel)" size="mini">{{ scope.row.riskLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最近在线时间" prop="lastOnlineTime" width="170">
                <template slot-scope="scope">{{ parseTime(scope.row.lastOnlineTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</template>
              </el-table-column>
              <el-table-column label="处置理由" prop="riskReason" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" icon="el-icon-video-play" @click="handleActivateNode(scope.row)">激活</el-button>
                  <el-button type="text" size="mini" icon="el-icon-data-analysis" @click="handleDiagnose(scope.row)">诊断</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="9">
          <el-card shadow="never" class="ops-card focus-card">
            <div slot="header" class="ops-card-header">
              <span>当前聚焦设备</span>
              <span class="ops-card-subtitle">自动选中首条高压节点</span>
            </div>
            <div v-if="focusNode.nodeId" class="focus-track">
              <div class="focus-track-title">{{ focusNode.deviceName || focusNode.deviceCode }}</div>
              <div class="focus-track-meta">
                <span>{{ focusNode.deviceCode || '-' }}</span>
                <span>{{ focusNode.bindArea || '-' }}</span>
              </div>
              <div class="focus-track-row"><span>状态</span><el-tag :type="statusTagType(focusNode.accessStatus)" size="mini">{{ focusNode.statusLabel }}</el-tag></div>
              <div class="focus-track-row"><span>风险</span><el-tag :type="riskTagType(focusNode.riskLevel)" size="mini">{{ focusNode.riskLevel }}</el-tag></div>
              <div class="focus-track-row"><span>协议</span><span>{{ focusNode.protocolLabel || '-' }}</span></div>
              <div class="focus-track-row"><span>固件</span><span>{{ focusNode.firmwareVersion || '-' }}</span></div>
              <div class="focus-track-row"><span>在线时间</span><span>{{ parseTime(focusNode.lastOnlineTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}</span></div>
              <div class="focus-track-summary">{{ focusNode.riskReason }}</div>
              <div class="focus-track-actions">
                <el-button type="primary" size="mini" @click="handleDiagnose(focusNode)">查看诊断</el-button>
                <el-button size="mini" @click="handleActivateNode(focusNode)">立即激活</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无聚焦设备" />
          </el-card>

          <el-card shadow="never" class="ops-card suggestions-card">
            <div slot="header" class="ops-card-header">
              <span>处置建议</span>
              <span class="ops-card-subtitle">基于接入状态和协议分布自动生成</span>
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
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input v-model="queryParams.deviceCode" placeholder="请输入设备编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select v-model="queryParams.deviceType" clearable style="width: 140px">
          <el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="接入状态" prop="accessStatus">
        <el-select v-model="queryParams.accessStatus" clearable style="width: 140px">
          <el-option v-for="item in accessStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:deviceAccess:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-video-play" size="mini" :disabled="single" @click="handleActivate" v-hasPermi="['agri:deviceAccess:edit']">激活</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-data-analysis" size="mini" :disabled="single" @click="handleDiagnoseSelected" v-hasPermi="['agri:deviceAccess:query']">诊断</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:deviceAccess:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:deviceAccess:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="nodeList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="nodeId" align="center" width="70" />
      <el-table-column label="设备编码" prop="deviceCode" align="center" width="160" />
      <el-table-column label="设备名称" prop="deviceName" align="center" width="130" />
      <el-table-column label="设备类型" prop="deviceType" align="center" width="100" />
      <el-table-column label="接入协议" prop="protocolType" align="center" width="110" />
      <el-table-column label="固件版本" prop="firmwareVersion" align="center" width="100" />
      <el-table-column label="绑定区域" prop="bindArea" align="center" width="130" />
      <el-table-column label="接入状态" align="center" width="100">
        <template slot-scope="scope">{{ formatAccessStatus(scope.row.accessStatus) }}</template>
      </el-table-column>
      <el-table-column label="最近在线时间" prop="lastOnlineTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.lastOnlineTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:deviceAccess:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handleDiagnose(scope.row)" v-hasPermi="['agri:deviceAccess:query']">诊断</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:deviceAccess:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="deviceName"><el-input v-model="form.deviceName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="设备类型" prop="deviceType"><el-select v-model="form.deviceType" style="width: 100%"><el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接入协议" prop="protocolType"><el-select v-model="form.protocolType" style="width: 100%"><el-option v-for="item in protocolTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="固件版本" prop="firmwareVersion"><el-input v-model="form.firmwareVersion" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="绑定区域" prop="bindArea"><el-input v-model="form.bindArea" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="接入状态" prop="accessStatus"><el-select v-model="form.accessStatus" style="width: 100%"><el-option v-for="item in accessStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="智能诊断" :visible.sync="diagnoseOpen" width="820px" append-to-body>
      <el-skeleton v-if="diagnoseLoading" :rows="6" animated />
      <div v-else class="analysis-panel">
        <el-row :gutter="12" class="analysis-card-row">
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">健康分数</div>
              <div class="analysis-value">{{ diagnoseResult.healthScore || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">风险等级</div>
              <div class="analysis-value">{{ diagnoseResult.risk || '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">算法</div>
              <div class="analysis-value">{{ diagnoseResult.algorithm || '-' }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="never" class="analysis-detail-card">
          <div class="analysis-detail-title">风险因子</div>
          <el-tag v-for="factor in diagnoseResult.factors || []" :key="factor" size="mini" type="warning" class="analysis-tag">{{ factor }}</el-tag>
          <div v-if="diagnoseResult.aiOriginalExcerpt" class="analysis-excerpt-wrap">
            <div class="analysis-detail-title">AI原文摘录</div>
            <pre class="analysis-excerpt">{{ diagnoseResult.aiOriginalExcerpt }}</pre>
          </div>
          <div class="analysis-detail-title">摘要</div>
          <div class="analysis-summary-text">{{ diagnoseResult.summary || '暂无摘要' }}</div>
        </el-card>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="diagnoseOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDeviceAccess,
  getDeviceAccess,
  addDeviceAccess,
  updateDeviceAccess,
  activateDeviceAccess,
  delDeviceAccess,
  getDeviceAccessDashboardOps,
  smartDiagnoseDeviceAccess
} from '@/api/agri/deviceAccess'
import { normalizeSmartResult } from '@/utils/agriSmartResult'

export default {
  name: 'DeviceAccess',
  data() {
    return {
      loading: true,
      opsLoading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      nodeList: [],
      opsData: {},
      title: '',
      open: false,
      focusNode: {},
      diagnoseOpen: false,
      diagnoseLoading: false,
      diagnoseResult: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceCode: undefined,
        deviceName: undefined,
        deviceType: undefined,
        accessStatus: undefined
      },
      form: {},
      deviceTypeOptions: [
        { label: '温湿度传感器', value: 'TEMP_HUMIDITY' },
        { label: '摄像头', value: 'CAMERA' },
        { label: '网关', value: 'GATEWAY' },
        { label: 'RFID终端', value: 'RFID' }
      ],
      protocolTypeOptions: [
        { label: 'MQTT', value: 'MQTT' },
        { label: 'HTTP', value: 'HTTP' },
        { label: 'CoAP', value: 'COAP' },
        { label: 'Modbus', value: 'MODBUS' }
      ],
      accessStatusOptions: [
        { label: '待接入', value: '0' },
        { label: '在线', value: '1' },
        { label: '离线', value: '2' },
        { label: '异常', value: '3' }
      ],
      rules: {
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
        deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
        protocolType: [{ required: true, message: '接入协议不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.refreshDashboard()
    this.getList()
  },
  computed: {
    pressureQueue() {
      return this.opsData.pressureQueue || []
    },
    suggestions() {
      return this.opsData.suggestions || []
    },
    metricCards() {
      const kpi = this.opsData.kpi || {}
      return [
        { label: '接入总数', value: kpi.total || 0, desc: `在线 ${kpi.online || 0} 台` },
        { label: '离线节点', value: kpi.offline || 0, desc: `异常 ${kpi.abnormal || 0} 台` },
        { label: '在线率', value: `${kpi.onlineRate || 0}%`, desc: `激活率 ${kpi.activationRate || 0}%` },
        { label: '高压节点', value: kpi.highRiskCount || 0, desc: `待接入 ${kpi.pending || 0} 台` }
      ]
    }
  },
  methods: {
    refreshDashboard() {
      this.opsLoading = true
      getDeviceAccessDashboardOps()
        .then(response => {
          this.opsData = response.data || {}
          this.focusNode = this.pressureQueue.length ? this.pressureQueue[0] : {}
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    getList() {
      this.loading = true
      listDeviceAccess(this.queryParams).then(response => {
        this.nodeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatAccessStatus(value) {
      const option = this.accessStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    statusTagType(value) {
      if (value === '3') {
        return 'danger'
      }
      if (value === '1') {
        return 'success'
      }
      if (value === '2') {
        return 'info'
      }
      return 'warning'
    },
    riskTagType(value) {
      if (value === '高') {
        return 'danger'
      }
      if (value === '中') {
        return 'warning'
      }
      return 'success'
    },
    priorityTagType(value) {
      if (value === 'P0') {
        return 'danger'
      }
      if (value === 'P1') {
        return 'warning'
      }
      return 'info'
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        nodeId: undefined,
        deviceCode: undefined,
        deviceName: undefined,
        deviceType: undefined,
        protocolType: undefined,
        firmwareVersion: undefined,
        bindArea: undefined,
        accessStatus: '0',
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
      this.ids = selection.map(item => item.nodeId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      if (selection.length === 1) {
        this.focusNode = selection[0]
      }
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.focusNode = row
      this.handleSelectionChange([row])
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增设备接入节点'
    },
    handleUpdate(row) {
      this.reset()
      const target = this.resolveNodeTarget(row)
      const nodeId = target && target.nodeId
      if (!nodeId) {
        this.$modal.msgWarning('请选择一条设备记录进行修改')
        return
      }
      getDeviceAccess(nodeId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改设备接入节点'
      })
    },
    handleActivate() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条记录进行激活')
        return
      }
      activateDeviceAccess(this.ids[0]).then(() => {
        this.$modal.msgSuccess('设备激活成功')
        this.getList()
      })
    },
    handleActivateNode(row) {
      const target = this.resolveNodeTarget(row)
      if (!target || !target.nodeId) {
        this.$modal.msgWarning('请选择一条设备记录进行激活')
        return
      }
      activateDeviceAccess(target.nodeId).then(() => {
        this.$modal.msgSuccess('设备激活成功')
        this.refreshDashboard()
        this.getList()
      })
    },
    handleDiagnose(row) {
      const target = this.resolveNodeTarget(row)
      if (!target || !target.nodeId) {
        this.$modal.msgWarning('请选择一条设备记录进行诊断')
        return
      }
      this.focusNode = target
      this.diagnoseLoading = true
      smartDiagnoseDeviceAccess(target.nodeId)
        .then(response => {
          this.diagnoseResult = this.normalizeDiagnoseResult(response.data || {})
          this.diagnoseOpen = true
        })
        .finally(() => {
          this.diagnoseLoading = false
        })
    },
    handleDiagnoseSelected() {
      this.handleDiagnose()
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.nodeId !== undefined) {
          updateDeviceAccess(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addDeviceAccess(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const target = this.resolveNodeTarget(row)
      const nodeIds = target && target.nodeId ? target.nodeId : this.ids
      if (!nodeIds || !nodeIds.length) {
        this.$modal.msgWarning('请先选择要删除的记录')
        return
      }
      this.$modal
        .confirm('是否确认删除设备接入节点编号为"' + nodeIds + '"的数据项？')
        .then(function () {
          return delDeviceAccess(nodeIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/deviceAccess/export',
        {
          ...this.queryParams
        },
        `device_access_${new Date().getTime()}.xlsx`
      )
    },
    resolveNodeTarget(row) {
      if (row && row.nodeId) {
        return row
      }
      if (this.focusNode && this.focusNode.nodeId) {
        return this.focusNode
      }
      if (this.ids.length) {
        const selected = this.nodeList.find(item => item.nodeId === this.ids[0])
        if (selected) {
          return selected
        }
      }
      if (this.nodeList.length) {
        return this.nodeList[0]
      }
      return (this.pressureQueue || [])[0]
    },
    normalizeDiagnoseResult(payload) {
      return normalizeSmartResult(payload, 'factors')
    }
  }
}
</script>

<style scoped>
.ops-shell {
  margin-bottom: 20px;
}

.hero-banner {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 24px;
  margin-bottom: 18px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #142033 0%, #284a6d 55%, #14686b 100%);
  box-shadow: 0 20px 40px rgba(21, 61, 92, 0.16);
}

.hero-copy {
  max-width: 560px;
}

.hero-label {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  margin-bottom: 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  letter-spacing: 1px;
}

.hero-copy h2 {
  margin: 0 0 10px;
  font-size: 26px;
  line-height: 1.35;
}

.hero-copy p {
  margin: 0 0 16px;
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  gap: 12px;
  min-width: 320px;
}

.hero-stat-card {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.hero-stat-label,
.hero-stat-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.78);
}

.hero-stat-value {
  margin: 6px 0;
  font-size: 24px;
  font-weight: 700;
}

.ops-grid {
  margin-bottom: 18px;
}

.ops-card {
  border: 1px solid #e8edf4;
  border-radius: 16px;
  overflow: hidden;
}

.ops-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1f2d3d;
}

.ops-card-subtitle {
  font-weight: 400;
  font-size: 12px;
  color: #8a94a6;
}

.focus-track {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.focus-track-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
}

.focus-track-meta,
.focus-track-row,
.suggestion-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.focus-track-meta {
  color: #667085;
  font-size: 12px;
}

.focus-track-row {
  padding: 8px 12px;
  border-radius: 10px;
  background: #f6f9fc;
  color: #425466;
}

.focus-track-summary,
.suggestion-content {
  color: #52616f;
  line-height: 1.7;
}

.focus-track-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.suggestion-item {
  padding: 12px 0;
  border-bottom: 1px solid #edf1f6;
}

.suggestion-item:last-child {
  border-bottom: 0;
}

.suggestions-card,
.focus-card {
  margin-top: 16px;
}

.analysis-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.analysis-card-row {
  margin-bottom: 4px;
}

.analysis-card {
  text-align: center;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
}

.analysis-value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
}

.analysis-detail-card {
  border-radius: 12px;
}

.analysis-detail-title {
  margin: 14px 0 8px;
  font-weight: 600;
  color: #1f2d3d;
}

.analysis-tag {
  margin: 0 8px 8px 0;
}

.analysis-summary-text {
  color: #52616f;
  line-height: 1.8;
}

.analysis-excerpt-wrap {
  margin-top: 8px;
}

.analysis-excerpt {
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fffaf0;
  border: 1px solid #f3e7cc;
  color: #6b4b17;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 180px;
  overflow: auto;
}

</style>
