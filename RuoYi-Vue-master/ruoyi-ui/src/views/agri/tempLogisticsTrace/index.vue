<template>
  <div class="app-container logistics-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="hero-title">物流追踪运营台</div>
          <div class="hero-desc">运单号 LD202604160001，展示地图轨迹、节点摘要与温湿度监控。</div>
        </div>
        <div class="hero-tags">
          <el-tag type="success" effect="dark">30秒刷新</el-tag>
          <el-tag type="warning" effect="dark">冷链跟踪</el-tag>
        </div>
      </div>
      <el-row :gutter="12" class="query-row">
        <el-col :xs="24" :sm="10" :md="8">
          <el-input v-model="waybillNo" readonly>
            <template slot="prepend">运单号</template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="10" :md="8">
          <el-input value="寿光蔬菜批发市场 -> 北京新发地市场" readonly>
            <template slot="prepend">线路</template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="4" :md="8" class="query-btn-wrap">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="12" class="main-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-head">
            <span>轨迹地图（高德效果示意）</span>
            <small>已行驶与预计路线分色展示</small>
          </div>
          <div class="route-legend">
            <span><i class="legend-line legend-past"></i>已行驶路线（蓝色）</span>
            <span><i class="legend-line legend-future"></i>预计路线（灰色虚线）</span>
          </div>
          <div class="map-stage">
            <svg class="route-svg" viewBox="0 0 1000 540" preserveAspectRatio="none">
              <path class="route-halo" d="M 80 130 C 180 60, 250 180, 335 210 S 480 290, 560 330 S 710 420, 820 400 S 920 320, 950 220" />
              <path class="route-past" d="M 80 130 C 180 60, 250 180, 335 210 S 480 290, 560 330 S 710 420, 820 400" />
              <path class="route-future" d="M 820 400 S 920 320, 950 220" />
            </svg>
            <div class="map-point start" style="left: 10%; top: 22%"></div>
            <div class="map-point end" style="left: 91%; top: 29%"></div>
            <div class="map-point current" :style="{ left: vehiclePosition.left, top: vehiclePosition.top }"></div>
            <div class="map-label start" style="left: 10%; top: 19%">起点 寿光蔬菜批发市场</div>
            <div class="map-label end" style="left: 91%; top: 26%">终点 北京新发地市场</div>
            <div class="map-label current" :style="{ left: vehiclePosition.left, top: '39%' }">当前位置 潍坊服务区附近</div>
            <div class="vehicle" :style="{ left: vehiclePosition.left, top: vehiclePosition.top }">🚚</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-head">
            <span>轨迹摘要</span>
            <small>实时运营信息</small>
          </div>
          <el-row :gutter="8" class="kpi-wrap">
            <el-col :span="12"><div class="kpi"><span>总里程</span><strong>326 km</strong></div></el-col>
            <el-col :span="12"><div class="kpi"><span>已行驶</span><strong>218 km</strong></div></el-col>
            <el-col :span="12"><div class="kpi"><span>剩余</span><strong>108 km</strong></div></el-col>
            <el-col :span="12"><div class="kpi"><span>预计到达</span><strong>2.5 h</strong></div></el-col>
          </el-row>
          <el-table :data="nodeList" size="mini" class="node-table" border>
            <el-table-column label="节点" prop="name" min-width="120" />
            <el-table-column label="时间" prop="time" min-width="110" />
            <el-table-column label="说明" prop="desc" min-width="130" />
            <el-table-column label="状态" min-width="110">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.warning" type="danger" size="mini">⚠️异常停留</el-tag>
                <el-tag v-else type="success" size="mini">正常</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card temp-panel">
      <div slot="header" class="panel-head">
        <span>温湿度监控</span>
        <small>当前温度 2.3℃（正常范围 0-4℃）</small>
      </div>
      <div class="temp-chart">
        <svg viewBox="0 0 1200 240" preserveAspectRatio="none">
          <rect x="0" y="40" width="1200" height="110" class="normal-range" />
          <rect x="0" y="150" width="1200" height="90" class="warn-range" />
          <path d="M 20 155 C 120 145, 190 118, 250 115 S 360 100, 450 95 S 600 88, 680 84 S 780 95, 860 90 S 1010 80, 1180 86" class="temp-line" />
        </svg>
        <button class="event-dot" @click="showEventDetail = !showEventDetail" title="查看超标事件"></button>
        <div v-if="showEventDetail" class="event-detail">
          <div class="event-title">温度超标事件</div>
          <div>发生时间：10:30</div>
          <div>持续时长：15 分钟</div>
          <div>最大值：6℃</div>
          <div>可能原因：制冷设备短暂波动或车厢门未及时关闭。</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'TempLogisticsTracePage',
  data() {
    return {
      waybillNo: 'LD202604160001',
      showEventDetail: false,
      vehiclePositions: [
        { left: '55%', top: '47%' },
        { left: '56%', top: '46%' },
        { left: '57%', top: '45%' },
        { left: '58%', top: '44%' }
      ],
      positionIndex: 0,
      vehicleTimer: null,
      nodeList: [
        { name: '寿光出发', time: '08:30', desc: '起运', warning: false },
        { name: '潍坊服务区', time: '10:15-11:00', desc: '停留45分钟', warning: true },
        { name: '济南东', time: '预计13:00', desc: '在途节点', warning: false },
        { name: '北京新发地', time: '预计15:30', desc: '终点签收', warning: false }
      ]
    }
  },
  computed: {
    vehiclePosition() {
      return this.vehiclePositions[this.positionIndex]
    }
  },
  created() {
    this.startVehicleTimer()
  },
  beforeDestroy() {
    if (this.vehicleTimer) {
      clearInterval(this.vehicleTimer)
    }
  },
  methods: {
    handleQuery() {
      this.$message.success('已刷新运单轨迹信息')
      this.moveVehicle()
    },
    moveVehicle() {
      this.positionIndex = (this.positionIndex + 1) % this.vehiclePositions.length
    },
    startVehicleTimer() {
      this.vehicleTimer = setInterval(() => {
        this.moveVehicle()
      }, 30000)
    }
  }
}
</script>

<style scoped>
.logistics-page {
  background: linear-gradient(180deg, #eef6f2 0%, #ffffff 50%);
}
.hero-card,
.panel-card {
  border-radius: 12px;
  border: 1px solid #e5ece8;
}
.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #24483a;
}
.hero-desc {
  color: #6c7d75;
  margin-top: 6px;
}
.hero-tags {
  display: flex;
  gap: 8px;
}
.query-row {
  margin-top: 14px;
}
.query-btn-wrap {
  display: flex;
  align-items: center;
}
.main-row,
.temp-panel {
  margin-top: 12px;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.panel-head small {
  color: #88968f;
}
.route-legend {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 10px;
  color: #5e6f67;
  font-size: 12px;
}
.legend-line {
  display: inline-block;
  width: 34px;
  height: 0;
  border-top: 4px solid #2f6fed;
  vertical-align: middle;
  margin-right: 6px;
}
.legend-future {
  border-top-color: #98a1b3;
  border-top-style: dashed;
}
.map-stage {
  height: 520px;
  border: 1px solid #dbe7e2;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(13, 78, 61, 0.06) 1px, transparent 1px) 0 0 / 70px 70px,
    linear-gradient(rgba(13, 78, 61, 0.06) 1px, transparent 1px) 0 0 / 70px 70px,
    linear-gradient(180deg, #e8f3ed 0%, #f8fcfa 100%);
}
.route-svg {
  position: absolute;
  inset: 0;
}
.route-halo {
  stroke: rgba(255, 255, 255, 0.75);
  stroke-width: 16;
  fill: none;
}
.route-past {
  stroke: #2f6fed;
  stroke-width: 10;
  fill: none;
}
.route-future {
  stroke: #98a1b3;
  stroke-width: 10;
  fill: none;
  stroke-dasharray: 14 8;
}
.map-point {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid #fff;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 6px rgba(0, 0, 0, 0.08);
}
.map-point.start { background: #22a44f; }
.map-point.end { background: #d86b29; }
.map-point.current { background: #2f6fed; }
.map-label {
  position: absolute;
  transform: translate(-50%, -100%);
  padding: 6px 10px;
  border-radius: 8px;
  color: #fff;
  font-size: 12px;
  white-space: nowrap;
}
.map-label.start { background: #22a44f; }
.map-label.end { background: #d86b29; }
.map-label.current { background: #2f6fed; }
.vehicle {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border: 1px solid #dfe8ff;
  transition: all .8s ease;
}
.kpi-wrap {
  margin-bottom: 10px;
}
.kpi {
  border: 1px solid #e6eee9;
  border-radius: 10px;
  padding: 10px;
  margin-bottom: 8px;
  background: #f9fcfb;
}
.kpi span {
  color: #73857d;
  font-size: 12px;
}
.kpi strong {
  display: block;
  margin-top: 4px;
  font-size: 20px;
  color: #1d5f44;
}
.temp-chart {
  position: relative;
  height: 260px;
  border: 1px solid #dce8e2;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}
.temp-chart svg {
  width: 100%;
  height: 100%;
}
.normal-range {
  fill: rgba(34, 197, 94, 0.16);
}
.warn-range {
  fill: rgba(239, 68, 68, 0.12);
}
.temp-line {
  stroke: #1f9d5f;
  stroke-width: 4;
  fill: none;
}
.event-dot {
  position: absolute;
  left: 57%;
  top: 34%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: none;
  background: #e11d48;
  box-shadow: 0 0 0 10px rgba(225, 29, 72, 0.15);
  cursor: pointer;
}
.event-detail {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 300px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e5e8ea;
  border-radius: 10px;
  padding: 10px 12px;
  line-height: 1.7;
  color: #4f5f58;
}
.event-title {
  font-weight: 700;
  color: #d03a4a;
  margin-bottom: 4px;
}
</style>
