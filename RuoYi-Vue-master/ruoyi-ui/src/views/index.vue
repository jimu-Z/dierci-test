<template>
  <div class="home-wrap">
    <section class="hero-panel">
      <div class="hero-main">
        <p class="hero-kicker">启元农链运营中台</p>
        <h1>{{ greeting }}，{{ nickName }}</h1>
        <p class="hero-desc">
          围绕“生产-加工-物流-销售-存证”全链路，统一查看经营指标、风险预警与协同任务，支撑日常运营决策。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="small" @click="goByFeature('dashboardOverview')">查看总览看板</el-button>
          <el-button size="small" @click="goByFeature('warningSummary')">处理预警</el-button>
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
      todoRows: [],
      quickEntries: [
        { title: '数据上链接口', tip: '执行与记录上链任务', icon: 'el-icon-connection', feature: 'dataUplink' },
        { title: '数据存证与校验', tip: '处理一致性核验', icon: 'el-icon-document-checked', feature: 'attestationVerify' },
        { title: '溯源审计日志', tip: '快速检索审计记录', icon: 'el-icon-notebook-2', feature: 'auditLog' },
        { title: '设备接入管理', tip: '管理采集设备状态', icon: 'el-icon-cpu', feature: 'deviceAccess' },
        { title: '物流路径追踪', tip: '查看运输轨迹与风险', icon: 'el-icon-map-location', feature: 'logisticsTrack' },
        { title: '消费者扫码查询', tip: '模拟终端查询体验', icon: 'el-icon-mobile-phone', feature: 'consumerScan' }
      ]
    }
  },
  computed: {
    ...mapGetters([
      'nickName'
    ]),
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
    statusItems() {
      const totalDevice = this.toNumber(this.deviceKpi.totalCount)
      const onlineDevice = this.toNumber(this.deviceKpi.onlineCount)
      const pendingWarning = this.toNumber(this.latestWarning.pendingCount)
      const pendingTask = this.toNumber(this.latestOverview.pendingTaskCount)
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
    async fetchHomeData() {
      if (this.loading) {
        return
      }
      this.loading = true
      const [overviewRes, warningRes, deviceRes, outputRes, todoRes] = await Promise.allSettled([
        listDashboardOverview({ pageNum: 1, pageSize: 2 }),
        listWarningSummary({ pageNum: 1, pageSize: 2 }),
        getDeviceStatusMonitorDashboard({}),
        getOutputSalesDashboard({ futureMonths: 3 }),
        listTodoTaskReminder({ pageNum: 1, pageSize: 20 })
      ])

      if (overviewRes.status === 'fulfilled') {
        this.dashboardRows = (overviewRes.value && overviewRes.value.rows) || []
      }
      if (warningRes.status === 'fulfilled') {
        this.warningRows = (warningRes.value && warningRes.value.rows) || []
      }
      if (deviceRes.status === 'fulfilled') {
        this.deviceDashboard = (deviceRes.value && deviceRes.value.data) || null
      }
      if (outputRes.status === 'fulfilled') {
        this.outputDashboard = (outputRes.value && outputRes.value.data) || null
      }
      if (todoRes.status === 'fulfilled') {
        const rows = (todoRes.value && todoRes.value.rows) || []
        this.todoRows = rows.filter(item => item && String(item.status || '0') === '0')
      }
      this.loading = false
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
