<template>
  <div class="home-wrap">
    <section class="hero-panel">
      <div class="hero-main">
        <p class="hero-kicker">{{ rolePortal.kicker }}</p>
        <h1>{{ greeting }}，{{ nickName }}</h1>
        <p class="hero-desc">
          {{ rolePortal.desc }}
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="small" @click="goPrimaryAction">{{ rolePortal.primaryAction.label }}</el-button>
          <el-button size="small" @click="goSecondaryAction">{{ rolePortal.secondaryAction.label }}</el-button>
        </div>
      </div>
      <div class="hero-side">
        <div class="clock">{{ nowText }}</div>
        <div class="status-grid">
          <div class="status-item" v-for="item in statusItems" :key="item.label">
            <span>{{ item.label }}</span>
            <b :class="item.warn ? 'warn' : ''">{{ item.value }}</b>
          </div>
        </div>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="item in kpiList" :key="item.title" class="kpi-card">
        <p class="kpi-title">{{ item.title }}</p>
        <h3>{{ item.value }}</h3>
        <p :class="['kpi-trend', item.trendClass]">{{ item.trend }}</p>
      </article>
    </section>

    <section class="main-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-header">
          <span>快捷入口</span>
        </div>
        <div class="quick-grid">
          <button v-for="entry in quickEntries" :key="entry.title" class="quick-item" @click="goByFeature(entry.feature)">
            <i :class="entry.icon"></i>
            <span>{{ entry.title }}</span>
            <small>{{ entry.tip }}</small>
          </button>
        </div>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-header">
          <span>今日待办</span>
        </div>
        <ul class="todo-list">
          <li v-for="todo in todos" :key="todo.key">
            <div>
              <b>{{ todo.title }}</b>
              <p>{{ todo.desc }}</p>
            </div>
            <el-tag size="mini" :type="todo.type">{{ todo.level }}</el-tag>
          </li>
        </ul>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-header">
          <span>业务健康度</span>
        </div>
        <div class="progress-block" v-for="item in healthItems" :key="item.key">
          <div class="progress-label">
            <span>{{ item.label }}</span>
            <b>{{ item.value }}%</b>
          </div>
          <el-progress :percentage="item.value" :color="item.color" :stroke-width="10" :show-text="false"></el-progress>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { listDashboardOverview } from '@/api/agri/dashboardOverview'
import { listWarningSummary } from '@/api/agri/warningSummary'
import { getDeviceStatusMonitorDashboard } from '@/api/agri/deviceStatusMonitor'
import { getOutputSalesDashboard } from '@/api/agri/outputSalesTrend'
import { listTodoTaskReminder } from '@/api/agri/todoTaskReminder'
import { getEnvSensorDashboard } from '@/api/agri/envSensor'
import { getFarmOpDashboard } from '@/api/agri/farmOp'
import { getPestIdentifyDashboard } from '@/api/agri/pestIdentify'
import { getYieldForecastDashboardOps } from '@/api/agri/yieldForecast'
import { getLogisticsTrackDashboardOps } from '@/api/agri/logisticsTrack'
import { getConsumerScanOpsDashboard } from '@/api/agri/consumerScan'
import { getQualityInspectDashboardOps } from '@/api/agri/qualityInspect'
import { getQualityReportDashboard } from '@/api/agri/qualityReport'
import { getDataUplinkDashboardOps } from '@/api/agri/dataUplink'
import { getAttestationOpsOverview } from '@/api/agri/attestationVerify'

const PORTAL_PRESET = {
  super: {
    kicker: '启元农链超级管理中心',
    desc: '聚合全链路经营、设备、上链与合规指标，支撑跨角色协同与全局调度。',
    primaryAction: { label: '查看全局总览', feature: 'dashboardOverview' },
    secondaryAction: { label: '处理全局预警', feature: 'warningSummary' },
    quickEntries: [
      { title: '数据上链接口', tip: '执行与记录上链任务', icon: 'el-icon-connection', feature: 'dataUplink' },
      { title: '数据存证与校验', tip: '处理一致性核验', icon: 'el-icon-document-checked', feature: 'attestationVerify' },
      { title: '溯源审计日志', tip: '快速检索审计记录', icon: 'el-icon-notebook-2', feature: 'auditLog' },
      { title: '设备接入管理', tip: '管理采集设备状态', icon: 'el-icon-cpu', feature: 'deviceAccess' },
      { title: '物流路径追踪', tip: '查看运输轨迹与风险', icon: 'el-icon-map-location', feature: 'logisticsTrack' },
      { title: '用户权限管理', tip: '按角色控制数据权限', icon: 'el-icon-lock', feature: 'userAccess' }
    ]
  },
  admin: {
    kicker: '启元农链运营管理台',
    desc: '关注日常运营闭环与风险处置效率，保障生产、物流、销售链路稳定运行。',
    primaryAction: { label: '进入运营总览', feature: 'dashboardOverview' },
    secondaryAction: { label: '查看今日待办', feature: 'todoTaskReminder' },
    quickEntries: [
      { title: '预警信息汇总', tip: '聚焦待处理风险事件', icon: 'el-icon-warning-outline', feature: 'warningSummary' },
      { title: '设备状态监控', tip: '掌握设备在线与告警', icon: 'el-icon-monitor', feature: 'deviceStatusMonitor' },
      { title: '物流路径追踪', tip: '跟踪运输时效与风险', icon: 'el-icon-truck', feature: 'logisticsTrack' },
      { title: '视觉质量检测', tip: '查看批次质检结果', icon: 'el-icon-camera', feature: 'qualityInspect' },
      { title: '消费者扫码查询', tip: '洞察终端查询热度', icon: 'el-icon-mobile-phone', feature: 'consumerScan' },
      { title: '任务待办提醒', tip: '推进跨岗协作任务', icon: 'el-icon-message', feature: 'todoTaskReminder' }
    ]
  },
  farmer: {
    kicker: '启元农链种植作业台',
    desc: '围绕地块作业、环境监测、病虫害识别与产量预测，提升农事执行质量。',
    primaryAction: { label: '查看作业看板', feature: 'farmOp' },
    secondaryAction: { label: '进入病虫害识别', feature: 'pestIdentify' },
    quickEntries: [
      { title: '农事操作管理', tip: '记录播种施肥等作业', icon: 'el-icon-s-order', feature: 'farmOp' },
      { title: '环境传感监测', tip: '关注温湿度与土壤指标', icon: 'el-icon-data-line', feature: 'envSensor' },
      { title: '病虫害识别', tip: '上传样本获取诊断建议', icon: 'el-icon-view', feature: 'pestIdentify' },
      { title: '产量预测', tip: '评估当前产量趋势', icon: 'el-icon-pie-chart', feature: 'yieldForecast' },
      { title: '任务待办提醒', tip: '追踪农事执行任务', icon: 'el-icon-alarm-clock', feature: 'todoTaskReminder' },
      { title: '预警信息汇总', tip: '快速处理异常预警', icon: 'el-icon-warning', feature: 'warningSummary' }
    ]
  },
  sales: {
    kicker: '启元农链产销运营台',
    desc: '聚焦产销量变化、消费端扫码热度与市场反馈，支撑销售策略迭代。',
    primaryAction: { label: '查看产销趋势', feature: 'outputSalesTrend' },
    secondaryAction: { label: '查看扫码态势', feature: 'consumerScan' },
    quickEntries: [
      { title: '产销趋势分析', tip: '跟踪产量与销量变化', icon: 'el-icon-trend-charts', feature: 'outputSalesTrend' },
      { title: '消费者扫码查询', tip: '观察地区与渠道热度', icon: 'el-icon-mobile-phone', feature: 'consumerScan' },
      { title: '品牌溯源页面', tip: '维护品牌展示信息', icon: 'el-icon-collection-tag', feature: 'brandTrace' },
      { title: '市场预测分析', tip: '辅助下阶段销售计划', icon: 'el-icon-data-analysis', feature: 'marketForecast' },
      { title: '溯源查询统计', tip: '掌握查询量变化', icon: 'el-icon-search', feature: 'traceQueryStats' },
      { title: '任务待办提醒', tip: '推进销售协同事项', icon: 'el-icon-message', feature: 'todoTaskReminder' }
    ]
  },
  logistics: {
    kicker: '启元农链物流调度台',
    desc: '围绕在途轨迹、时效风险与预警闭环，保障冷链运输稳定可控。',
    primaryAction: { label: '查看物流总览', feature: 'logisticsTrack' },
    secondaryAction: { label: '查看物流预警', feature: 'warningSummary' },
    quickEntries: [
      { title: '物流路径追踪', tip: '实时查看运输轨迹', icon: 'el-icon-map-location', feature: 'logisticsTrack' },
      { title: '温湿度监测', tip: '关注在途环境波动', icon: 'el-icon-odometer', feature: 'logisticsTempHumidity' },
      { title: '物流预警处置', tip: '快速闭环异常节点', icon: 'el-icon-bell', feature: 'logisticsWarning' },
      { title: '预警信息汇总', tip: '统一查看跨模块风险', icon: 'el-icon-warning-outline', feature: 'warningSummary' },
      { title: '设备状态监控', tip: '同步车载设备状态', icon: 'el-icon-monitor', feature: 'deviceStatusMonitor' },
      { title: '任务待办提醒', tip: '跟进运输工单', icon: 'el-icon-message', feature: 'todoTaskReminder' }
    ]
  },
  quality: {
    kicker: '启元农链质检合规台',
    desc: '集中处理视觉质检、报告审核与异常闭环，保障批次质量可追溯。',
    primaryAction: { label: '进入视觉质检', feature: 'qualityInspect' },
    secondaryAction: { label: '查看质量报告', feature: 'qualityReport' },
    quickEntries: [
      { title: '视觉质量检测', tip: '执行批次图像质检', icon: 'el-icon-camera', feature: 'qualityInspect' },
      { title: '质量报告管理', tip: '审阅与归档检测报告', icon: 'el-icon-document', feature: 'qualityReport' },
      { title: '数据存证与校验', tip: '处理质量存证核验', icon: 'el-icon-document-checked', feature: 'attestationVerify' },
      { title: '预警信息汇总', tip: '响应质量异常预警', icon: 'el-icon-warning', feature: 'warningSummary' },
      { title: '溯源审计日志', tip: '核查质量链路操作', icon: 'el-icon-notebook-2', feature: 'auditLog' },
      { title: '任务待办提醒', tip: '推进质检闭环任务', icon: 'el-icon-message', feature: 'todoTaskReminder' }
    ]
  }
}

export default {
  name: 'Index',
  data() {
    return {
      timer: null,
      nowText: '',
      refreshTimer: null,
      loading: false,
      dashboardRows: [],
      warningRows: [],
      deviceDashboard: null,
      outputDashboard: null,
      envSensorDashboard: null,
      farmOpDashboard: null,
      pestDashboard: null,
      yieldOpsDashboard: null,
      logisticsOpsDashboard: null,
      consumerOpsDashboard: null,
      qualityInspectOpsDashboard: null,
      qualityReportDashboard: null,
      dataUplinkOpsDashboard: null,
      attestationOpsDashboard: null,
      todoRows: [],
      roleKeyPriority: ['admin', 'agri_admin', 'agri_farmer', 'agri_sales', 'agri_logistics', 'agri_quality']
    }
  },
  computed: {
    ...mapGetters([
      'nickName',
      'roles'
    ]),
    activeRoleKey() {
      const roles = Array.isArray(this.roles) ? this.roles : []
      for (const role of this.roleKeyPriority) {
        if (roles.includes(role)) {
          return role
        }
      }
      return roles.length ? roles[0] : 'common'
    },
    activeRoleType() {
      const role = this.activeRoleKey
      if (role === 'admin') return 'super'
      if (role === 'agri_farmer') return 'farmer'
      if (role === 'agri_sales') return 'sales'
      if (role === 'agri_logistics') return 'logistics'
      if (role === 'agri_quality') return 'quality'
      return 'admin'
    },
    rolePortal() {
      return PORTAL_PRESET[this.activeRoleType] || PORTAL_PRESET.admin
    },
    quickEntries() {
      return this.rolePortal.quickEntries || []
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 12) return '早上好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    latestOverview() {
      return this.dashboardRows.length > 0 ? this.dashboardRows[0] : {}
    },
    prevOverview() {
      return this.dashboardRows.length > 1 ? this.dashboardRows[1] : {}
    },
    latestWarning() {
      return this.warningRows.length > 0 ? this.warningRows[0] : {}
    },
    prevWarning() {
      return this.warningRows.length > 1 ? this.warningRows[1] : {}
    },
    deviceKpi() {
      return (this.deviceDashboard && this.deviceDashboard.kpi) || {}
    },
    outputKpi() {
      return (this.outputDashboard && this.outputDashboard.kpi) || {}
    },
    envKpi() {
      return (this.envSensorDashboard && this.envSensorDashboard.kpi) || {}
    },
    farmKpi() {
      return (this.farmOpDashboard && this.farmOpDashboard.kpi) || {}
    },
    pestKpi() {
      return (this.pestDashboard && this.pestDashboard.kpi) || {}
    },
    yieldOpsKpi() {
      return (this.yieldOpsDashboard && this.yieldOpsDashboard.kpi) || {}
    },
    logisticsOpsKpi() {
      return (this.logisticsOpsDashboard && this.logisticsOpsDashboard.kpi) || {}
    },
    consumerOpsKpi() {
      return (this.consumerOpsDashboard && this.consumerOpsDashboard.kpi) || {}
    },
    qualityInspectKpi() {
      return (this.qualityInspectOpsDashboard && this.qualityInspectOpsDashboard.kpi) || {}
    },
    qualityReportKpi() {
      return (this.qualityReportDashboard && this.qualityReportDashboard.kpi) || {}
    },
    dataUplinkOpsKpi() {
      return (this.dataUplinkOpsDashboard && this.dataUplinkOpsDashboard.kpi) || {}
    },
    attestationOpsKpi() {
      return (this.attestationOpsDashboard && this.attestationOpsDashboard.kpi) || {}
    },
    statusItems() {
      const totalDevice = this.toNumber(this.deviceKpi.totalCount)
      const onlineDevice = this.toNumber(this.deviceKpi.onlineCount)
      const pendingWarning = this.toNumber(this.latestWarning.pendingCount)
      const pendingTask = this.toNumber(this.latestOverview.pendingTaskCount)
      if (this.activeRoleType === 'farmer') {
        return [
          { label: '今日作业', value: this.formatInt(this.farmKpi.todayCount) },
          { label: '病虫告警', value: this.formatInt(this.pestKpi.highRiskCount), warn: this.toNumber(this.pestKpi.highRiskCount) > 0 },
          { label: '土壤异常', value: this.formatInt(this.envKpi.abnormalSoilCount), warn: this.toNumber(this.envKpi.abnormalSoilCount) > 0 },
          { label: '待办任务', value: this.formatInt(this.todoRows.length), warn: this.todoRows.length > 0 }
        ]
      }
      if (this.activeRoleType === 'sales') {
        return [
          { label: '本月销量', value: this.formatInt(this.outputKpi.monthSalesTotal) },
          { label: '扫码总量', value: this.formatInt(this.consumerOpsKpi.totalScanCount) },
          { label: '热销品类', value: this.formatInt(this.outputKpi.hotCategoryCount) },
          { label: '待办任务', value: this.formatInt(this.todoRows.length), warn: this.todoRows.length > 0 }
        ]
      }
      if (this.activeRoleType === 'logistics') {
        return [
          { label: '在途运单', value: this.formatInt(this.logisticsOpsKpi.inTransitCount) },
          { label: '超时运单', value: this.formatInt(this.logisticsOpsKpi.overtimeCount), warn: this.toNumber(this.logisticsOpsKpi.overtimeCount) > 0 },
          { label: '异常预警', value: this.formatInt(this.latestWarning.totalWarningCount), warn: this.toNumber(this.latestWarning.totalWarningCount) > 0 },
          { label: '待办任务', value: this.formatInt(this.todoRows.length), warn: this.todoRows.length > 0 }
        ]
      }
      if (this.activeRoleType === 'quality') {
        return [
          { label: '待检批次', value: this.formatInt(this.qualityInspectKpi.pendingCount), warn: this.toNumber(this.qualityInspectKpi.pendingCount) > 0 },
          { label: '不合格批次', value: this.formatInt(this.qualityInspectKpi.rejectCount), warn: this.toNumber(this.qualityInspectKpi.rejectCount) > 0 },
          { label: '报告待审', value: this.formatInt(this.qualityReportKpi.pendingReviewCount), warn: this.toNumber(this.qualityReportKpi.pendingReviewCount) > 0 },
          { label: '待办任务', value: this.formatInt(this.todoRows.length), warn: this.todoRows.length > 0 }
        ]
      }
      if (this.activeRoleType === 'super') {
        return [
          { label: '在线设备', value: `${onlineDevice}/${totalDevice || 0}` },
          { label: '上链成功', value: this.formatInt(this.dataUplinkOpsKpi.successCount) },
          { label: '存证异常', value: this.formatInt(this.attestationOpsKpi.abnormalCount), warn: this.toNumber(this.attestationOpsKpi.abnormalCount) > 0 },
          { label: '全局待办', value: this.formatInt(Math.max(pendingTask, pendingWarning)), warn: Math.max(pendingTask, pendingWarning) > 0 }
        ]
      }
      return [
        { label: '在线设备', value: `${onlineDevice}/${totalDevice || 0}` },
        { label: '离线设备', value: this.formatInt(this.deviceKpi.offlineCount) },
        { label: '今日预警', value: this.formatInt(this.latestWarning.totalWarningCount) },
        { label: '待处理任务', value: this.formatInt(Math.max(pendingTask, pendingWarning)), warn: Math.max(pendingTask, pendingWarning) > 0 }
      ]
    },
    kpiList() {
      const onlineRate = this.percent(this.deviceKpi.onlineCount, this.deviceKpi.totalCount)
      const traceDiff = this.toNumber(this.latestOverview.traceQueryCount) - this.toNumber(this.prevOverview.traceQueryCount)
      const warningDiff = this.toNumber(this.latestWarning.totalWarningCount) - this.toNumber(this.prevWarning.totalWarningCount)
      const outputDiff = this.toNumber(this.outputKpi.outputGrowth)
      const salesDiff = this.toNumber(this.outputKpi.salesGrowth)
      if (this.activeRoleType === 'farmer') {
        return [
          {
            title: '今日农事作业量',
            value: this.formatInt(this.farmKpi.todayCount),
            trend: this.diffText(this.toNumber(this.farmKpi.todayCount) - this.toNumber(this.farmKpi.yesterdayCount), '较昨日'),
            trendClass: this.toNumber(this.farmKpi.todayCount) >= this.toNumber(this.farmKpi.yesterdayCount) ? 'up' : 'down'
          },
          {
            title: '病虫害高风险数',
            value: this.formatInt(this.pestKpi.highRiskCount),
            trend: this.diffText(this.toNumber(this.pestKpi.highRiskCount) - this.toNumber(this.pestKpi.lastHighRiskCount), '较昨日'),
            trendClass: this.toNumber(this.pestKpi.highRiskCount) <= this.toNumber(this.pestKpi.lastHighRiskCount) ? 'up' : 'down'
          },
          {
            title: '土壤监测达标率',
            value: `${this.percent(this.envKpi.normalSoilCount, this.envKpi.totalSoilCount)}%`,
            trend: `异常点位 ${this.formatInt(this.envKpi.abnormalSoilCount)}`,
            trendClass: this.percent(this.envKpi.normalSoilCount, this.envKpi.totalSoilCount) >= 85 ? 'up' : 'down'
          },
          {
            title: '产量预测偏差率',
            value: `${this.formatNumber(this.yieldOpsKpi.biasRate)}%`,
            trend: `预测批次 ${this.formatInt(this.yieldOpsKpi.forecastCount)}`,
            trendClass: this.toNumber(this.yieldOpsKpi.biasRate) <= 10 ? 'up' : 'down'
          }
        ]
      }
      if (this.activeRoleType === 'sales') {
        return [
          {
            title: '本月销量',
            value: this.formatInt(this.outputKpi.monthSalesTotal),
            trend: this.diffText(this.outputKpi.salesGrowth, '环比'),
            trendClass: this.toNumber(this.outputKpi.salesGrowth) >= 0 ? 'up' : 'down'
          },
          {
            title: '本月产量',
            value: this.formatInt(this.outputKpi.monthOutputTotal),
            trend: this.diffText(this.outputKpi.outputGrowth, '环比'),
            trendClass: this.toNumber(this.outputKpi.outputGrowth) >= 0 ? 'up' : 'down'
          },
          {
            title: '消费者扫码量',
            value: this.formatInt(this.consumerOpsKpi.totalScanCount),
            trend: this.diffText(this.consumerOpsKpi.dayGrowthRate, '日环比'),
            trendClass: this.toNumber(this.consumerOpsKpi.dayGrowthRate) >= 0 ? 'up' : 'down'
          },
          {
            title: '复购用户占比',
            value: `${this.formatNumber(this.consumerOpsKpi.repurchaseRate)}%`,
            trend: `新增用户 ${this.formatInt(this.consumerOpsKpi.newUserCount)}`,
            trendClass: this.toNumber(this.consumerOpsKpi.repurchaseRate) >= 30 ? 'up' : 'down'
          }
        ]
      }
      if (this.activeRoleType === 'logistics') {
        return [
          {
            title: '在途运单数',
            value: this.formatInt(this.logisticsOpsKpi.inTransitCount),
            trend: `已送达 ${this.formatInt(this.logisticsOpsKpi.deliveredCount)}`,
            trendClass: 'up'
          },
          {
            title: '超时运单率',
            value: `${this.percent(this.logisticsOpsKpi.overtimeCount, this.logisticsOpsKpi.totalOrderCount)}%`,
            trend: `超时 ${this.formatInt(this.logisticsOpsKpi.overtimeCount)} 单`,
            trendClass: this.percent(this.logisticsOpsKpi.overtimeCount, this.logisticsOpsKpi.totalOrderCount) <= 5 ? 'up' : 'down'
          },
          {
            title: '温控异常次数',
            value: this.formatInt(this.logisticsOpsKpi.tempAlarmCount),
            trend: this.diffText(this.logisticsOpsKpi.tempAlarmDiff, '较昨日'),
            trendClass: this.toNumber(this.logisticsOpsKpi.tempAlarmDiff) <= 0 ? 'up' : 'down'
          },
          {
            title: '预警闭环完成率',
            value: `${this.percent(this.latestWarning.handledCount, this.latestWarning.totalWarningCount)}%`,
            trend: `待处理 ${this.formatInt(this.latestWarning.pendingCount)}`,
            trendClass: this.percent(this.latestWarning.handledCount, this.latestWarning.totalWarningCount) >= 90 ? 'up' : 'down'
          }
        ]
      }
      if (this.activeRoleType === 'quality') {
        return [
          {
            title: '待检批次数',
            value: this.formatInt(this.qualityInspectKpi.pendingCount),
            trend: `已完成 ${this.formatInt(this.qualityInspectKpi.finishedCount)}`,
            trendClass: this.toNumber(this.qualityInspectKpi.pendingCount) <= this.toNumber(this.qualityInspectKpi.finishedCount) ? 'up' : 'down'
          },
          {
            title: '质检通过率',
            value: `${this.percent(this.qualityInspectKpi.passCount, this.qualityInspectKpi.totalCount)}%`,
            trend: `不合格 ${this.formatInt(this.qualityInspectKpi.rejectCount)}`,
            trendClass: this.percent(this.qualityInspectKpi.passCount, this.qualityInspectKpi.totalCount) >= 95 ? 'up' : 'down'
          },
          {
            title: '报告待审核数',
            value: this.formatInt(this.qualityReportKpi.pendingReviewCount),
            trend: `本日新增 ${this.formatInt(this.qualityReportKpi.todayCount)}`,
            trendClass: this.toNumber(this.qualityReportKpi.pendingReviewCount) <= 5 ? 'up' : 'down'
          },
          {
            title: '存证一致率',
            value: `${this.percent(this.attestationOpsKpi.successCount, this.attestationOpsKpi.totalCount)}%`,
            trend: `异常 ${this.formatInt(this.attestationOpsKpi.abnormalCount)}`,
            trendClass: this.percent(this.attestationOpsKpi.successCount, this.attestationOpsKpi.totalCount) >= 95 ? 'up' : 'down'
          }
        ]
      }
      if (this.activeRoleType === 'super') {
        return [
          {
            title: '追溯查询量',
            value: this.formatInt(this.latestOverview.traceQueryCount),
            trend: this.diffText(traceDiff, '较上日'),
            trendClass: traceDiff >= 0 ? 'up' : 'down'
          },
          {
            title: '预警事件总量',
            value: this.formatInt(this.latestWarning.totalWarningCount),
            trend: this.diffText(warningDiff, '较上日'),
            trendClass: warningDiff <= 0 ? 'up' : 'down'
          },
          {
            title: '上链成功率',
            value: `${this.percent(this.dataUplinkOpsKpi.successCount, this.dataUplinkOpsKpi.totalCount)}%`,
            trend: `失败 ${this.formatInt(this.dataUplinkOpsKpi.failCount)}`,
            trendClass: this.percent(this.dataUplinkOpsKpi.successCount, this.dataUplinkOpsKpi.totalCount) >= 95 ? 'up' : 'down'
          },
          {
            title: '设备在线率',
            value: `${onlineRate}%`,
            trend: `在线 ${this.formatInt(this.deviceKpi.onlineCount)} / 总计 ${this.formatInt(this.deviceKpi.totalCount)}`,
            trendClass: onlineRate >= 90 ? 'up' : 'down'
          }
        ]
      }
      return [
        {
          title: '追溯查询量',
          value: this.formatInt(this.latestOverview.traceQueryCount),
          trend: this.diffText(traceDiff, '较上日'),
          trendClass: traceDiff >= 0 ? 'up' : 'down'
        },
        {
          title: '预警事件总量',
          value: this.formatInt(this.latestWarning.totalWarningCount),
          trend: this.diffText(warningDiff, '较上日'),
          trendClass: warningDiff <= 0 ? 'up' : 'down'
        },
        {
          title: '产量增长率',
          value: `${this.formatNumber(outputDiff)}%`,
          trend: `销量增长 ${this.formatNumber(salesDiff)}%`,
          trendClass: outputDiff >= 0 ? 'up' : 'down'
        },
        {
          title: '设备在线率',
          value: `${onlineRate}%`,
          trend: `在线 ${this.formatInt(this.deviceKpi.onlineCount)} / 总计 ${this.formatInt(this.deviceKpi.totalCount)}`,
          trendClass: onlineRate >= 90 ? 'up' : 'down'
        }
      ]
    },
    todos() {
      if (!this.todoRows.length) {
        return [{ key: 'empty', title: '暂无待办任务', desc: '当前没有需要处理的任务提醒。', level: '正常', type: 'info' }]
      }
      return this.todoRows.slice(0, 4).map((item, index) => ({
        key: `${item.reminderId || index}`,
        title: item.taskTitle || item.taskCode || '未命名任务',
        desc: `${item.assignee || '未分配'} · 截止 ${item.deadlineTime || '未设置'}`,
        level: this.priorityLabel(item.priorityLevel),
        type: this.priorityTagType(item.priorityLevel)
      }))
    },
    healthItems() {
      if (this.activeRoleType === 'farmer') {
        return [
          {
            key: 'farm-complete-rate',
            label: '农事执行完成率',
            value: this.percent(this.farmKpi.finishedCount, this.farmKpi.totalCount),
            color: '#2f7d53'
          },
          {
            key: 'pest-review-rate',
            label: '病虫害复核完成率',
            value: this.percent(this.pestKpi.reviewedCount, this.pestKpi.totalCount),
            color: '#3f8f66'
          },
          {
            key: 'yield-hit-rate',
            label: '产量预测命中率',
            value: this.percent(this.yieldOpsKpi.hitCount, this.yieldOpsKpi.totalCount),
            color: '#d88a28'
          }
        ]
      }
      if (this.activeRoleType === 'sales') {
        return [
          {
            key: 'sales-growth-health',
            label: '销售增长健康度',
            value: Math.max(0, Math.min(100, 50 + Math.round(this.toNumber(this.outputKpi.salesGrowth)))),
            color: '#2f7d53'
          },
          {
            key: 'scan-convert-rate',
            label: '扫码转化率',
            value: Math.round(this.toNumber(this.consumerOpsKpi.convertRate)),
            color: '#3f8f66'
          },
          {
            key: 'repurchase-rate',
            label: '复购稳定度',
            value: Math.round(this.toNumber(this.consumerOpsKpi.repurchaseRate)),
            color: '#d88a28'
          }
        ]
      }
      if (this.activeRoleType === 'logistics') {
        return [
          {
            key: 'delivery-ontime-rate',
            label: '准时履约率',
            value: this.percent(this.logisticsOpsKpi.onTimeCount, this.logisticsOpsKpi.totalOrderCount),
            color: '#2f7d53'
          },
          {
            key: 'warning-handle-rate',
            label: '预警闭环完成率',
            value: this.percent(this.latestWarning.handledCount, this.latestWarning.totalWarningCount),
            color: '#3f8f66'
          },
          {
            key: 'temp-stable-rate',
            label: '温控稳定率',
            value: this.percent(this.logisticsOpsKpi.normalTempCount, this.logisticsOpsKpi.totalMonitorCount),
            color: '#d88a28'
          }
        ]
      }
      if (this.activeRoleType === 'quality') {
        return [
          {
            key: 'quality-pass-rate',
            label: '质检通过率',
            value: this.percent(this.qualityInspectKpi.passCount, this.qualityInspectKpi.totalCount),
            color: '#2f7d53'
          },
          {
            key: 'report-review-rate',
            label: '报告审核完成率',
            value: this.percent(this.qualityReportKpi.reviewedCount, this.qualityReportKpi.totalCount),
            color: '#3f8f66'
          },
          {
            key: 'attestation-success-rate',
            label: '存证一致率',
            value: this.percent(this.attestationOpsKpi.successCount, this.attestationOpsKpi.totalCount),
            color: '#d88a28'
          }
        ]
      }
      const list = [
        {
          key: 'device-online-rate',
          label: '设备在线率',
          value: this.percent(this.deviceKpi.onlineCount, this.deviceKpi.totalCount),
          color: '#2f7d53'
        },
        {
          key: 'warning-handle-rate',
          label: '告警闭环完成率',
          value: this.percent(this.latestWarning.handledCount, this.latestWarning.totalWarningCount),
          color: '#3f8f66'
        },
        {
          key: 'todo-complete-rate',
          label: '待办完成率',
          value: this.percent(
            this.todoRows.filter(item => String(item.reminderStatus) === '2').length,
            this.todoRows.length
          ),
          color: '#d88a28'
        }
      ]
      const unique = new Map()
      list.forEach(item => {
        if (!unique.has(item.key)) {
          unique.set(item.key, {
            ...item,
            value: Math.max(0, Math.min(100, Math.round(this.toNumber(item.value))))
          })
        }
      })
      return Array.from(unique.values())
    }
  },
  created() {
    this.updateClock()
    this.timer = setInterval(this.updateClock, 1000)
    this.fetchHomeData()
    this.refreshTimer = setInterval(this.fetchHomeData, 60000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  },
  methods: {
    buildRequests() {
      const requests = [
        { key: 'overview', request: listDashboardOverview({ pageNum: 1, pageSize: 2 }) },
        { key: 'warning', request: listWarningSummary({ pageNum: 1, pageSize: 2 }) },
        { key: 'todo', request: listTodoTaskReminder({ pageNum: 1, pageSize: 20 }) }
      ]
      if (this.activeRoleType === 'super' || this.activeRoleType === 'admin') {
        requests.push(
          { key: 'device', request: getDeviceStatusMonitorDashboard({}) },
          { key: 'output', request: getOutputSalesDashboard({ futureMonths: 3 }) }
        )
      }
      if (this.activeRoleType === 'super') {
        requests.push(
          { key: 'dataUplinkOps', request: getDataUplinkDashboardOps() },
          { key: 'attestationOps', request: getAttestationOpsOverview() }
        )
      }
      if (this.activeRoleType === 'farmer') {
        requests.push(
          { key: 'env', request: getEnvSensorDashboard({}) },
          { key: 'farm', request: getFarmOpDashboard({}) },
          { key: 'pest', request: getPestIdentifyDashboard({}) },
          { key: 'yieldOps', request: getYieldForecastDashboardOps() }
        )
      }
      if (this.activeRoleType === 'sales') {
        requests.push(
          { key: 'output', request: getOutputSalesDashboard({ futureMonths: 3 }) },
          { key: 'consumerOps', request: getConsumerScanOpsDashboard() }
        )
      }
      if (this.activeRoleType === 'logistics') {
        requests.push(
          { key: 'logisticsOps', request: getLogisticsTrackDashboardOps() },
          { key: 'device', request: getDeviceStatusMonitorDashboard({}) }
        )
      }
      if (this.activeRoleType === 'quality') {
        requests.push(
          { key: 'qualityInspectOps', request: getQualityInspectDashboardOps() },
          { key: 'qualityReport', request: getQualityReportDashboard({}) },
          { key: 'attestationOps', request: getAttestationOpsOverview() }
        )
      }
      return requests
    },
    applyRequestData(key, payload) {
      switch (key) {
        case 'overview':
          this.dashboardRows = (payload && payload.rows) || []
          break
        case 'warning':
          this.warningRows = (payload && payload.rows) || []
          break
        case 'todo':
          this.todoRows = ((payload && payload.rows) || []).filter(item => item && String(item.status || '0') === '0')
          break
        case 'device':
          this.deviceDashboard = (payload && payload.data) || null
          break
        case 'output':
          this.outputDashboard = (payload && payload.data) || null
          break
        case 'env':
          this.envSensorDashboard = (payload && payload.data) || null
          break
        case 'farm':
          this.farmOpDashboard = (payload && payload.data) || null
          break
        case 'pest':
          this.pestDashboard = (payload && payload.data) || null
          break
        case 'yieldOps':
          this.yieldOpsDashboard = (payload && payload.data) || null
          break
        case 'logisticsOps':
          this.logisticsOpsDashboard = (payload && payload.data) || null
          break
        case 'consumerOps':
          this.consumerOpsDashboard = (payload && payload.data) || null
          break
        case 'qualityInspectOps':
          this.qualityInspectOpsDashboard = (payload && payload.data) || null
          break
        case 'qualityReport':
          this.qualityReportDashboard = (payload && payload.data) || null
          break
        case 'dataUplinkOps':
          this.dataUplinkOpsDashboard = (payload && payload.data) || null
          break
        case 'attestationOps':
          this.attestationOpsDashboard = (payload && payload.data) || null
          break
        default:
          break
      }
    },
    async fetchHomeData() {
      if (this.loading) {
        return
      }
      this.loading = true
      try {
        const requestList = this.buildRequests()
        const results = await Promise.allSettled(requestList.map(item => item.request))
        results.forEach((result, index) => {
          if (result.status === 'fulfilled') {
            this.applyRequestData(requestList[index].key, result.value)
          }
        })
      } finally {
        this.loading = false
      }
    },
    updateClock() {
      const now = new Date()
      const pad = (n) => (n < 10 ? `0${n}` : `${n}`)
      this.nowText = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
    },
    toNumber(value) {
      const num = Number(value)
      return Number.isFinite(num) ? num : 0
    },
    formatInt(value) {
      return this.toNumber(value).toLocaleString()
    },
    formatNumber(value) {
      return Math.round(this.toNumber(value) * 100) / 100
    },
    percent(numerator, denominator) {
      const denominatorNum = this.toNumber(denominator)
      if (denominatorNum <= 0) {
        return 0
      }
      return Math.round((this.toNumber(numerator) / denominatorNum) * 100)
    },
    diffText(diff, suffix) {
      const number = this.toNumber(diff)
      const sign = number > 0 ? '+' : ''
      return `${sign}${this.formatNumber(number)} ${suffix}`
    },
    priorityLabel(level) {
      const map = { '1': '低优', '2': '中优', '3': '高优', '4': '紧急' }
      return map[String(level)] || '普通'
    },
    priorityTagType(level) {
      const map = { '1': 'success', '2': 'info', '3': 'warning', '4': 'danger' }
      return map[String(level)] || 'info'
    },
    resolveFeaturePath(feature) {
      const candidates = [`/agri/${feature}`, `/${feature}`]
      for (const candidate of candidates) {
        const resolved = this.$router.resolve(candidate)
        const matched = resolved && resolved.route ? resolved.route.matched : []
        const hasMatched = matched && matched.length > 0
        const is404 = resolved && resolved.route && (resolved.route.path === '/404' || resolved.route.name === '404')
        const has404Record = hasMatched && matched.some(item => item && (item.path === '/404' || item.path === '*'))
        if (hasMatched && !is404 && !has404Record) {
          return candidate
        }
      }
      return null
    },
    goByFeature(feature) {
      const targetPath = this.resolveFeaturePath(feature)
      if (!targetPath) {
        this.$message.warning('目标功能路由不存在，已返回首页')
        this.$router.push('/index')
        return
      }
      this.$router.push(targetPath)
    },
    goPrimaryAction() {
      if (this.rolePortal.primaryAction && this.rolePortal.primaryAction.feature) {
        this.goByFeature(this.rolePortal.primaryAction.feature)
      }
    },
    goSecondaryAction() {
      if (this.rolePortal.secondaryAction && this.rolePortal.secondaryAction.feature) {
        this.goByFeature(this.rolePortal.secondaryAction.feature)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.home-wrap {
  display: grid;
  gap: 18px;
}

.hero-panel {
  display: grid;
  grid-template-columns: 2.2fr 1fr;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(130deg, #1f5a3c 0%, #2f7d53 45%, #3f8f66 100%);
  color: #fff;
}

.hero-kicker {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 1px;
  opacity: 0.9;
}

.hero-main h1 {
  margin: 0;
  font-size: 30px;
}

.hero-desc {
  margin: 12px 0 18px;
  max-width: 740px;
  line-height: 1.7;
  opacity: 0.95;
}

.hero-actions .el-button + .el-button {
  margin-left: 10px;
}

.hero-side {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.13);
  padding: 14px;
}

.clock {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
}

.status-grid {
  display: grid;
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.16);
}

.status-item span {
  opacity: 0.9;
}

.status-item b {
  font-weight: 700;
}

.status-item .warn {
  color: #ffd98d;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.kpi-card {
  padding: 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #dce8e0;
}

.kpi-title {
  margin: 0;
  color: #5f7270;
  font-size: 13px;
}

.kpi-card h3 {
  margin: 8px 0 4px;
  font-size: 28px;
  color: #164732;
}

.kpi-trend {
  margin: 0;
  font-size: 12px;
}

.kpi-trend.up {
  color: #1f8a54;
}

.kpi-trend.down {
  color: #d88a28;
}

.main-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr;
  gap: 14px;
}

.panel-card {
  border-radius: 14px;
  border: 1px solid #dce8e0;
}

.panel-header {
  font-weight: 700;
  color: #1f5a3c;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-item {
  border: 1px solid #dce8e0;
  background: #f7fbf8;
  border-radius: 10px;
  min-height: 88px;
  padding: 10px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.quick-item:hover {
  border-color: #7fb497;
  transform: translateY(-1px);
}

.quick-item i {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
  color: #2f7d53;
}

.quick-item span {
  display: block;
  color: #133b2a;
  font-weight: 600;
}

.quick-item small {
  display: block;
  margin-top: 4px;
  color: #6f827f;
}

.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.todo-list li {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px dashed #dce8e0;
}

.todo-list li:last-child {
  border-bottom: none;
}

.todo-list b {
  color: #123928;
}

.todo-list p {
  margin: 4px 0 0;
  color: #6f827f;
  font-size: 12px;
}

.progress-block + .progress-block {
  margin-top: 14px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  color: #33584a;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-main h1 {
    font-size: 24px;
  }

  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
