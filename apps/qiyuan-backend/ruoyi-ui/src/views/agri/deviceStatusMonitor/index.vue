<template>
  <div class="app-container monitor-page">
    <el-alert
      v-if="latestAlertBanner"
      :title="latestAlertBanner"
      type="warning"
      :closable="false"
      show-icon
      class="mb16"
    />

    <el-card shadow="never" class="hero-card mb16">
      <div class="hero-wrap">
        <div>
          <div class="hero-title">设备状态监控中心</div>
          <div class="hero-desc">聚焦在线率、预警等级、能耗与传感器稳定性，优先呈现异常设备和实时波动。</div>
          <div class="hero-meta">
            <span>当前监控设备 {{ total }} 台</span>
            <span>最近刷新 {{ lastRefreshLabel }}</span>
            <span>自动刷新 {{ refreshInterval / 1000 }} 秒 / 次</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click="refreshMonitor">刷新监控</el-button>
          <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:deviceStatusMonitor:export']">导出快照</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="refreshMonitor"></right-toolbar>
        </div>
      </div>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="96px" class="mb16 monitor-search">
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input v-model="queryParams.deviceCode" placeholder="请输入设备编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="在线状态" prop="onlineStatus">
        <el-select v-model="queryParams.onlineStatus" placeholder="在线状态" clearable style="width: 140px">
          <el-option label="在线" value="1" />
          <el-option label="离线" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警等级" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="预警等级" clearable style="width: 140px">
          <el-option label="正常" value="0" />
          <el-option label="提示" value="1" />
          <el-option label="预警" value="2" />
          <el-option label="严重" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="kpi-row mb16">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in summaryCards" :key="item.key">
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
            <span>实时设备态势</span>
            <span class="panel-subtitle">点击设备卡可聚焦详情</span>
          </div>
          <el-empty v-if="!deviceList.length && !loading" description="暂无设备监控数据" />
          <el-row :gutter="12" v-else class="device-grid">
            <el-col v-for="item in deviceList" :key="item.monitorId" :xs="24" :sm="12" :xl="8">
              <el-card
                shadow="hover"
                class="device-card"
                :class="{ active: selectedDeviceId === item.monitorId }"
                @click.native="selectDevice(item)"
              >
                <div class="device-head">
                  <div>
                    <div class="device-name">{{ item.deviceName || '未命名设备' }}</div>
                    <div class="device-code">{{ item.deviceCode || '-' }}</div>
                  </div>
                  <el-tag size="mini" :type="onlineTagType(item.onlineStatus)">{{ onlineLabel(item.onlineStatus) }}</el-tag>
                </div>
                <div class="device-tags">
                  <el-tag size="mini" effect="dark" :type="warningTagType(item.warningLevel)">{{ warningLabel(item.warningLevel) }}</el-tag>
                  <el-tag size="mini" effect="plain">{{ item.deviceType || '未知类型' }}</el-tag>
                </div>
                <div class="metric-list">
                  <div class="metric-item"><span>电量</span><strong>{{ formatPercent(item.batteryLevel) }}</strong></div>
                  <el-progress :percentage="normalizeNumber(item.batteryLevel, 0, 100)" :status="batteryStatus(item.batteryLevel)" :show-text="false" :stroke-width="8" />
                  <div class="metric-item"><span>信号</span><strong>{{ formatPercent(item.signalStrength) }}</strong></div>
                  <el-progress :percentage="normalizeNumber(item.signalStrength, 0, 100)" :status="signalStatus(item.signalStrength)" :show-text="false" :stroke-width="8" />
                  <div class="metric-item"><span>温度</span><strong>{{ formatValue(item.temperature, '℃') }}</strong></div>
                  <div class="metric-item"><span>湿度</span><strong>{{ formatValue(item.humidity, '%') }}</strong></div>
                </div>
                <div class="device-footer">
                  <span>{{ getReportText(item.lastReportTime) }}</span>
                  <span>{{ freshnessLabel(item.lastReportTime) }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel-card mb12">
          <div slot="header" class="panel-header">
            <span>异常与关注</span>
            <span class="panel-subtitle">优先处理离线和高等级预警</span>
          </div>
          <div class="alert-list" v-if="alertList.length">
            <div v-for="item in alertList" :key="item.monitorId" class="alert-item" @click="selectDevice(item)">
              <div class="alert-title">
                <span>{{ item.deviceName || item.deviceCode }}</span>
                <el-tag size="mini" :type="warningTagType(item.warningLevel)">{{ warningLabel(item.warningLevel) }}</el-tag>
              </div>
              <div class="alert-desc">{{ alertDescription(item) }}</div>
            </div>
          </div>
          <el-empty v-else description="当前无异常设备" />
        </el-card>

        <el-card shadow="never" class="panel-card mb12">
          <div slot="header" class="panel-header">
            <span>在线结构</span>
            <span class="panel-subtitle">状态分布与风险密度</span>
          </div>
          <div class="distribution-item" v-for="item in statusDistribution" :key="item.key">
            <div class="distribution-top">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
            </div>
            <el-progress :percentage="item.rate" :status="item.type" :show-text="false" :stroke-width="10" />
          </div>
        </el-card>

        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>当前聚焦</span>
            <span class="panel-subtitle">用于快速查看单设备详情</span>
          </div>
          <div v-if="selectedDevice" class="focus-block">
            <div class="focus-title">{{ selectedDevice.deviceName || '未命名设备' }}</div>
            <div class="focus-code">{{ selectedDevice.deviceCode || '-' }}</div>
            <div class="focus-grid">
              <div><span>在线状态</span><strong>{{ onlineLabel(selectedDevice.onlineStatus) }}</strong></div>
              <div><span>预警等级</span><strong>{{ warningLabel(selectedDevice.warningLevel) }}</strong></div>
              <div><span>电量</span><strong>{{ formatPercent(selectedDevice.batteryLevel) }}</strong></div>
              <div><span>信号</span><strong>{{ formatPercent(selectedDevice.signalStrength) }}</strong></div>
              <div><span>温度</span><strong>{{ formatValue(selectedDevice.temperature, '℃') }}</strong></div>
              <div><span>湿度</span><strong>{{ formatValue(selectedDevice.humidity, '%') }}</strong></div>
            </div>
            <div class="focus-note">{{ selectedDevice.remark || '暂无备注' }}</div>
          </div>
          <el-empty v-else description="请选择一个设备查看详情" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card mb16">
      <div slot="header" class="panel-header">
        <span>实时明细台账</span>
        <span class="panel-subtitle">保留维护能力，但不作为页面主视觉</span>
      </div>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:deviceStatusMonitor:add']">新增</el-button></el-col>
        <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:deviceStatusMonitor:edit']">修改</el-button></el-col>
        <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:deviceStatusMonitor:remove']">删除</el-button></el-col>
        <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:deviceStatusMonitor:export']">导出</el-button></el-col>
      </el-row>

      <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange" @row-click="selectDevice">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="设备编码" align="center" prop="deviceCode" width="140" />
        <el-table-column label="设备名称" align="center" prop="deviceName" min-width="140" />
        <el-table-column label="在线状态" align="center" prop="onlineStatus" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="onlineTagType(scope.row.onlineStatus)">{{ onlineLabel(scope.row.onlineStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" align="center" prop="warningLevel" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="warningTagType(scope.row.warningLevel)">{{ warningLabel(scope.row.warningLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="电量" align="center" prop="batteryLevel" width="90">
          <template slot-scope="scope">{{ formatPercent(scope.row.batteryLevel) }}</template>
        </el-table-column>
        <el-table-column label="信号" align="center" prop="signalStrength" width="90">
          <template slot-scope="scope">{{ formatPercent(scope.row.signalStrength) }}</template>
        </el-table-column>
        <el-table-column label="温度" align="center" prop="temperature" width="90">
          <template slot-scope="scope">{{ formatValue(scope.row.temperature, '℃') }}</template>
        </el-table-column>
        <el-table-column label="湿度" align="center" prop="humidity" width="90">
          <template slot-scope="scope">{{ formatValue(scope.row.humidity, '%') }}</template>
        </el-table-column>
        <el-table-column label="最后上报时间" align="center" prop="lastReportTime" width="170">
          <template slot-scope="scope">{{ getReportText(scope.row.lastReportTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click.stop="selectDevice(scope.row)">查看</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:deviceStatusMonitor:edit']">修改</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery" />
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" placeholder="请输入设备编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="deviceName"><el-input v-model="form.deviceName" placeholder="请输入设备名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="设备类型" prop="deviceType"><el-input v-model="form.deviceType" placeholder="请输入设备类型" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在线状态" prop="onlineStatus"><el-select v-model="form.onlineStatus" placeholder="请选择在线状态" style="width: 100%"><el-option label="在线" value="1" /><el-option label="离线" value="0" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="电量(%)" prop="batteryLevel"><el-input-number v-model="form.batteryLevel" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="信号强度" prop="signalStrength"><el-input-number v-model="form.signalStrength" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度(℃)" prop="temperature"><el-input-number v-model="form.temperature" :precision="2" :min="-50" :max="80" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度(%)" prop="humidity"><el-input-number v-model="form.humidity" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警等级" prop="warningLevel"><el-select v-model="form.warningLevel" placeholder="请选择预警等级" style="width: 100%"><el-option label="正常" value="0" /><el-option label="提示" value="1" /><el-option label="预警" value="2" /><el-option label="严重" value="3" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上报时间" prop="lastReportTime"><el-date-picker clearable v-model="form.lastReportTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择上报时间" style="width: 100%" /></el-form-item></el-col>
        </el-row>
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
import {
  listDeviceStatusMonitor,
  getDeviceStatusMonitor,
  delDeviceStatusMonitor,
  addDeviceStatusMonitor,
  updateDeviceStatusMonitor,
  getDeviceStatusMonitorDashboard
  ,
  getDeviceStatusMonitorAlerts
} from '@/api/agri/deviceStatusMonitor'

export default {
  name: 'DeviceStatusMonitor',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      monitorList: [],
      dashboardSnapshot: null,
      alertSnapshot: null,
      selectedDeviceId: null,
      title: '',
      open: false,
      lastRefreshTime: null,
      refreshInterval: 60000,
      alertRefreshInterval: 15000,
      refreshTimer: null,
      alertTimer: null,
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        deviceCode: null,
        deviceName: null,
        onlineStatus: null,
        warningLevel: null
      },
      form: {},
      rules: {
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
        onlineStatus: [{ required: true, message: '在线状态不能为空', trigger: 'change' }],
        batteryLevel: [{ required: true, message: '电量不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    deviceList() {
      return (this.monitorList || []).map(item => this.normalizeRow(item))
    },
    selectedDevice() {
      if (this.dashboardSnapshot && this.dashboardSnapshot.focus) {
        const focusId = this.dashboardSnapshot.focus.monitorId
        return this.deviceList.find(item => item.monitorId === focusId) || this.dashboardSnapshot.focus
      }
      return this.deviceList.find(item => item.monitorId === this.selectedDeviceId) || this.deviceList[0] || null
    },
    alertList() {
      if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.alertList) && this.dashboardSnapshot.alertList.length) {
        return this.dashboardSnapshot.alertList
      }
      return this.deviceList
        .filter(item => this.isAbnormal(item))
        .sort((a, b) => this.alertScore(b) - this.alertScore(a))
        .slice(0, 6)
    },
    summaryCards() {
      const kpi = (this.dashboardSnapshot && this.dashboardSnapshot.kpi) || {}
      const total = kpi.totalCount != null ? Number(kpi.totalCount) : this.deviceList.length
      const onlineCount = kpi.onlineCount != null ? Number(kpi.onlineCount) : this.deviceList.filter(item => item.onlineStatus === '1').length
      const offlineCount = kpi.offlineCount != null ? Number(kpi.offlineCount) : total - onlineCount
      const warningCount = kpi.warningCount != null ? Number(kpi.warningCount) : this.deviceList.filter(item => ['2', '3'].includes(item.warningLevel)).length
      const avgBattery = kpi.avgBattery != null ? Number(kpi.avgBattery) : this.averageOf('batteryLevel')
      const avgTemp = kpi.avgTemperature != null ? Number(kpi.avgTemperature) : this.averageOf('temperature')
      const avgHumidity = kpi.avgHumidity != null ? Number(kpi.avgHumidity) : this.averageOf('humidity')
      const lowSignalCount = kpi.staleCount != null ? Number(kpi.staleCount) : this.deviceList.filter(item => this.normalizeNumber(item.signalStrength, 0, 100) < 35).length
      return [
        { key: 'total', label: '监控设备', value: total, foot: '覆盖全部接入设备' },
        { key: 'online', label: '在线设备', value: onlineCount, foot: `离线 ${offlineCount} 台` },
        { key: 'warning', label: '异常设备', value: warningCount, foot: '包含预警与严重状态' },
        { key: 'battery', label: '平均电量', value: `${avgBattery}%`, foot: `低信号设备 ${lowSignalCount} 台` },
        { key: 'temp', label: '平均温度', value: `${avgTemp}℃`, foot: '用于观察环境波动' },
        { key: 'humidity', label: '平均湿度', value: `${avgHumidity}%`, foot: '用于观察环境波动' }
      ]
    },
    statusDistribution() {
      if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.statusDistribution) && this.dashboardSnapshot.statusDistribution.length) {
        return this.dashboardSnapshot.statusDistribution
      }
      const total = this.deviceList.length || 1
      const onlineCount = this.deviceList.filter(item => item.onlineStatus === '1').length
      const offlineCount = total - onlineCount
      const warningCount = this.deviceList.filter(item => ['2', '3'].includes(item.warningLevel)).length
      const normalCount = total - warningCount
      return [
        { key: 'online', label: '在线率', count: onlineCount, rate: Math.round((onlineCount / total) * 100), type: 'success' },
        { key: 'offline', label: '离线率', count: offlineCount, rate: Math.round((offlineCount / total) * 100), type: 'exception' },
        { key: 'warning', label: '异常率', count: warningCount, rate: Math.round((warningCount / total) * 100), type: warningCount > 0 ? 'warning' : 'success' },
        { key: 'normal', label: '正常率', count: normalCount, rate: Math.round((normalCount / total) * 100), type: 'success' }
      ]
    },
    lastRefreshLabel() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '暂无'
    },
    latestAlertBanner() {
      const latestAlert = this.alertSnapshot && this.alertSnapshot.latestAlert
      if (!latestAlert) {
        return ''
      }
      const parts = [latestAlert.deviceName || latestAlert.deviceCode || '未知设备']
      if (latestAlert.riskLabel) {
        parts.push(`风险${latestAlert.riskLabel}`)
      }
      if (latestAlert.stale) {
        parts.push('上报超时')
      }
      if (latestAlert.onlineStatus === '0') {
        parts.push('设备离线')
      }
      return `实时告警：${parts.join('，')}`
    }
  },
  created() {
    this.getList()
    this.getDashboardSnapshot()
  },
  mounted() {
    this.refreshTimer = setInterval(() => {
      this.refreshMonitor()
    }, this.refreshInterval)
    this.alertTimer = setInterval(() => {
      this.getAlertSnapshot()
    }, this.alertRefreshInterval)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
    if (this.alertTimer) {
      clearInterval(this.alertTimer)
      this.alertTimer = null
    }
  },
  methods: {
    getList() {
      this.loading = true
      listDeviceStatusMonitor(this.queryParams).then(response => {
        this.monitorList = response.rows || []
        this.total = response.total || 0
        this.lastRefreshTime = new Date()
        this.loading = false
        this.$nextTick(() => {
          if (!this.selectedDevice && this.deviceList.length) {
            this.selectedDeviceId = this.deviceList[0].monitorId
          }
        })
      }).catch(() => {
        this.loading = false
      })
    },
    getDashboardSnapshot() {
      getDeviceStatusMonitorDashboard(this.queryParams).then(response => {
        this.dashboardSnapshot = response.data || null
        if (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.recentList) && this.dashboardSnapshot.recentList.length) {
          this.selectedDeviceId = this.dashboardSnapshot.recentList[0].monitorId
        }
      }).catch(() => {})
    },
    getAlertSnapshot() {
      getDeviceStatusMonitorAlerts(this.queryParams).then(response => {
        this.alertSnapshot = response.data || null
      }).catch(() => {})
    },
    refreshMonitor() {
      this.handleQuery()
      this.getDashboardSnapshot()
      this.getAlertSnapshot()
      this.$message && this.$message.success('已刷新监控数据')
    },
    normalizeRow(row) {
      return {
        ...row,
        onlineStatus: String(row.onlineStatus ?? '0'),
        warningLevel: String(row.warningLevel ?? '0'),
        batteryLevel: this.toNumber(row.batteryLevel),
        signalStrength: this.toNumber(row.signalStrength),
        temperature: this.toNumber(row.temperature),
        humidity: this.toNumber(row.humidity)
      }
    },
    toNumber(value) {
      const num = Number(value)
      return Number.isFinite(num) ? num : 0
    },
    normalizeNumber(value, min = 0, max = 100) {
      const num = this.toNumber(value)
      const clamped = Math.min(max, Math.max(min, num))
      return Math.round(clamped)
    },
    averageOf(field) {
      const list = this.deviceList
      if (!list.length) {
        return 0
      }
      const total = list.reduce((sum, item) => sum + this.toNumber(item[field]), 0)
      return Math.round((total / list.length) * 10) / 10
    },
    selectDevice(row) {
      if (!row) {
        return
      }
      this.selectedDeviceId = row.monitorId
    },
    isAbnormal(row) {
      return row.onlineStatus === '0' || ['2', '3'].includes(row.warningLevel) || this.normalizeNumber(row.signalStrength) < 35
    },
    alertScore(row) {
      let score = 0
      if (row.onlineStatus === '0') score += 5
      if (row.warningLevel === '3') score += 4
      if (row.warningLevel === '2') score += 3
      if (this.normalizeNumber(row.signalStrength) < 35) score += 2
      if (this.normalizeNumber(row.batteryLevel) < 20) score += 1
      return score
    },
    alertDescription(row) {
      const parts = []
      if (row.onlineStatus === '0') parts.push('设备离线')
      if (row.warningLevel === '2') parts.push('进入预警')
      if (row.warningLevel === '3') parts.push('严重告警')
      if (this.normalizeNumber(row.signalStrength) < 35) parts.push('信号偏弱')
      if (this.normalizeNumber(row.batteryLevel) < 20) parts.push('电量偏低')
      return parts.join('，') || '状态正常'
    },
    getReportText(value) {
      return value ? this.parseTime(value) : '未上报'
    },
    freshnessLabel(value) {
      if (!value) {
        return '暂无时效'
      }
      const diff = Date.now() - new Date(value).getTime()
      if (Number.isNaN(diff)) {
        return '暂无时效'
      }
      const minutes = Math.floor(diff / 60000)
      if (minutes < 5) return '刚刚上报'
      if (minutes < 30) return `${minutes} 分钟前`
      if (minutes < 120) return '2 小时内'
      return '需关注'
    },
    formatPercent(value) {
      return `${this.normalizeNumber(value)}%`
    },
    formatValue(value, unit) {
      const num = this.toNumber(value)
      return `${Math.round(num * 10) / 10}${unit}`
    },
    onlineLabel(value) {
      return String(value) === '1' ? '在线' : '离线'
    },
    onlineTagType(value) {
      return String(value) === '1' ? 'success' : 'info'
    },
    warningLabel(level) {
      const map = { '0': '正常', '1': '提示', '2': '预警', '3': '严重' }
      return map[String(level)] || '未知'
    },
    warningTagType(level) {
      const map = { '0': 'success', '1': 'warning', '2': 'danger', '3': 'danger' }
      return map[String(level)] || 'info'
    },
    batteryStatus(value) {
      const battery = this.normalizeNumber(value)
      if (battery < 20) return 'exception'
      if (battery < 50) return 'warning'
      return 'success'
    },
    signalStatus(value) {
      const signal = this.normalizeNumber(value)
      if (signal < 35) return 'exception'
      if (signal < 60) return 'warning'
      return 'success'
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        monitorId: null,
        deviceCode: null,
        deviceName: null,
        deviceType: null,
        onlineStatus: '1',
        batteryLevel: 100,
        signalStrength: 80,
        temperature: 25,
        humidity: 60,
        warningLevel: '0',
        lastReportTime: null,
        remark: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      this.getDashboardSnapshot()
      this.getAlertSnapshot()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.monitorId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增设备状态监控'
    },
    handleUpdate(row) {
      this.reset()
      const monitorId = row && row.monitorId ? row.monitorId : this.ids[0]
      if (!monitorId) {
        return
      }
      getDeviceStatusMonitor(monitorId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改设备状态监控'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.monitorId != null) {
            updateDeviceStatusMonitor(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.getDashboardSnapshot()
              this.getAlertSnapshot()
            })
          } else {
            addDeviceStatusMonitor(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.getDashboardSnapshot()
              this.getAlertSnapshot()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const monitorIds = row && row.monitorId ? row.monitorId : this.ids
      this.$modal.confirm('是否确认删除设备状态监控编号为"' + monitorIds + '"的数据项？').then(() => {
        return delDeviceStatusMonitor(monitorIds)
      }).then(() => {
        this.getList()
        this.getDashboardSnapshot()
        this.getAlertSnapshot()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/deviceStatusMonitor/export', {
        ...this.queryParams
      }, `device_status_monitor_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.monitor-page {
  background: linear-gradient(180deg, #f5f8ff 0%, #ffffff 32%, #ffffff 100%);
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
  color: #1f2937;
  margin-bottom: 8px;
}

.hero-desc {
  color: #5f6b7a;
  line-height: 1.6;
  margin-bottom: 10px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #6b7280;
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
  color: #111827;
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
  color: #111827;
}

.panel-subtitle {
  color: #9ca3af;
  font-size: 12px;
  font-weight: 400;
}

.device-grid {
  margin-top: -4px;
}

.device-card {
  margin-bottom: 12px;
  cursor: pointer;
  border: 1px solid #eef2f7;
  transition: all 0.2s ease;
}

.device-card.active {
  border-color: #409eff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
}

.device-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.device-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.device-code {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.device-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.metric-list {
  display: grid;
  gap: 8px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #4b5563;
  font-size: 13px;
}

.device-footer {
  display: flex;
  justify-content: space-between;
  color: #9ca3af;
  font-size: 12px;
  margin-top: 12px;
}

.alert-list {
  display: grid;
  gap: 10px;
}

.alert-item {
  padding: 12px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #eef2f7;
  cursor: pointer;
}

.alert-title {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
  font-weight: 600;
}

.alert-desc {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
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

.focus-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.focus-code {
  color: #9ca3af;
  margin: 6px 0 12px;
}

.focus-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.focus-grid div {
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
}

.focus-grid span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 6px;
}

.focus-grid strong {
  color: #111827;
  font-size: 14px;
}

.focus-note {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #fff7ed;
  color: #9a3412;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .hero-wrap {
    flex-direction: column;
  }

  .hero-actions {
    width: 100%;
  }
}
</style>
