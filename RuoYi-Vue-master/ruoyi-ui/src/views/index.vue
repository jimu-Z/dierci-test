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
          <div class="status-item">
            <span>链上节点</span>
            <b>在线</b>
          </div>
          <div class="status-item">
            <span>数据同步</span>
            <b>正常</b>
          </div>
          <div class="status-item">
            <span>今日上链</span>
            <b>2,184 笔</b>
          </div>
          <div class="status-item">
            <span>待核验</span>
            <b class="warn">18 条</b>
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
          <li v-for="todo in todos" :key="todo.title">
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
        <div class="progress-block" v-for="item in health" :key="item.label">
          <div class="progress-label">
            <span>{{ item.label }}</span>
            <b>{{ item.value }}%</b>
          </div>
          <el-progress :percentage="item.value" :color="item.color" :stroke-width="10"></el-progress>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Index',
  data() {
    return {
      timer: null,
      nowText: '',
      kpiList: [
        { title: '今日追溯查询', value: '12,486', trend: '+12.3% 较昨日', trendClass: 'up' },
        { title: '在途预警事件', value: '27', trend: '-6.8% 风险下降', trendClass: 'down' },
        { title: '已完成存证校验', value: '3,902', trend: '+5.1% 处理效率提升', trendClass: 'up' },
        { title: '设备在线率', value: '98.2%', trend: '+0.7% 运行稳定', trendClass: 'up' }
      ],
      quickEntries: [
        { title: '数据上链接口', tip: '执行与记录上链任务', icon: 'el-icon-connection', feature: 'dataUplink' },
        { title: '数据存证与校验', tip: '处理一致性核验', icon: 'el-icon-document-checked', feature: 'attestationVerify' },
        { title: '溯源审计日志', tip: '快速检索审计记录', icon: 'el-icon-notebook-2', feature: 'auditLog' },
        { title: '设备接入管理', tip: '管理采集设备状态', icon: 'el-icon-cpu', feature: 'deviceAccess' },
        { title: '物流路径追踪', tip: '查看运输轨迹与风险', icon: 'el-icon-map-location', feature: 'logisticsTrack' },
        { title: '消费者扫码查询', tip: '模拟终端查询体验', icon: 'el-icon-mobile-phone', feature: 'consumerScan' }
      ],
      todos: [
        { title: '完成批次 B2408-17 存证核验', desc: '需对比链上哈希并回写校验结果。', level: '高优', type: 'danger' },
        { title: '复核温湿度异常告警', desc: '运输车辆 T-09 连续 2 次超阈值。', level: '中优', type: 'warning' },
        { title: '同步品牌溯源页面发布', desc: '新批次文案与图片已待审核。', level: '低优', type: 'success' }
      ],
      health: [
        { label: '采集数据完整率', value: 96, color: '#2f7d53' },
        { label: '校验任务及时率', value: 91, color: '#3f8f66' },
        { label: '告警闭环完成率', value: 88, color: '#d88a28' }
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
    }
  },
  created() {
    this.updateClock()
    this.timer = setInterval(this.updateClock, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  methods: {
    updateClock() {
      const now = new Date()
      const pad = (n) => (n < 10 ? `0${n}` : `${n}`)
      this.nowText = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
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
