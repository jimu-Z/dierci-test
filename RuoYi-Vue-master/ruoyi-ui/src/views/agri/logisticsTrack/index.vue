<template>
  <div class="app-container">
    <div class="ops-shell logistics-shell" v-loading="opsLoading">
      <div class="hero-banner logistics-hero">
        <div class="hero-copy">
          <div class="hero-label">物流路径追踪运营台</div>
          <h2>把运单轨迹、温控压力和停留风险汇成处置中枢</h2>
          <p>实时看板聚焦异常路径、在途压力、回放复盘与智能研判，底层仍保留轨迹维护与导出能力。</p>
          <div class="hero-actions">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增轨迹</el-button>
            <el-button icon="el-icon-view" :disabled="!focusTrack.traceCode" @click="handlePreview(focusTrack)">轨迹复盘</el-button>
            <el-button type="warning" icon="el-icon-data-analysis" :disabled="!focusTrack.trackId" @click="handleAnalyze(focusTrack)">智能研判</el-button>
            <el-button icon="el-icon-refresh" @click="refreshDashboard">刷新中枢</el-button>
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
              <span>异常压力队列</span>
              <span class="ops-card-subtitle">优先处理高风险、无轨迹和温控异常记录</span>
            </div>
            <el-table :data="pressureQueue" size="mini" v-loading="opsLoading">
              <el-table-column label="运单号" prop="traceCode" width="150" />
              <el-table-column label="当前位置" prop="currentLocation" width="140" show-overflow-tooltip />
              <el-table-column label="状态" width="90">
                <template slot-scope="scope">
                  <el-tag :type="statusTagType(scope.row.trackStatus)" size="mini">{{ scope.row.statusLabel || formatTrackStatus(scope.row.trackStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="风险等级" width="90">
                <template slot-scope="scope">
                  <el-tag :type="riskTagType(scope.row.riskLevel)" size="mini">{{ scope.row.riskLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="温度" width="90">
                <template slot-scope="scope">{{ formatNumber(scope.row.temperature, '℃') }}</template>
              </el-table-column>
              <el-table-column label="湿度" width="90">
                <template slot-scope="scope">{{ formatNumber(scope.row.humidity, '%') }}</template>
              </el-table-column>
              <el-table-column label="处置理由" prop="riskReason" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" icon="el-icon-map-location" @click="handlePreview(scope.row)">复盘</el-button>
                  <el-button type="text" size="mini" icon="el-icon-data-analysis" @click="handleAnalyze(scope.row)">研判</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="9">
          <el-card shadow="never" class="ops-card focus-card">
            <div slot="header" class="ops-card-header">
              <span>当前聚焦运单</span>
              <span class="ops-card-subtitle">随队列自动取首条高压记录</span>
            </div>
            <div v-if="focusTrack.traceCode" class="focus-track">
              <div class="focus-track-title">{{ focusTrack.traceCode }}</div>
              <div class="focus-track-meta">
                <span>{{ focusTrack.orderNo || '-' }}</span>
                <span>{{ focusTrack.productBatchNo || '-' }}</span>
              </div>
              <div class="focus-track-row"><span>状态</span><el-tag :type="statusTagType(focusTrack.trackStatus)" size="mini">{{ focusTrack.statusLabel || formatTrackStatus(focusTrack.trackStatus) }}</el-tag></div>
              <div class="focus-track-row"><span>风险</span><el-tag :type="riskTagType(focusTrack.riskLevel)" size="mini">{{ focusTrack.riskLevel }}</el-tag></div>
              <div class="focus-track-row"><span>位置</span><span>{{ focusTrack.currentLocation || '-' }}</span></div>
              <div class="focus-track-row"><span>温度</span><span>{{ formatNumber(focusTrack.temperature, '℃') }}</span></div>
              <div class="focus-track-row"><span>湿度</span><span>{{ formatNumber(focusTrack.humidity, '%') }}</span></div>
              <div class="focus-track-summary">{{ focusTrack.summary || focusTrack.riskReason }}</div>
              <div class="focus-track-actions">
                <el-button type="primary" size="mini" @click="handlePreview(focusTrack)">查看复盘</el-button>
                <el-button size="mini" @click="handleAnalyze(focusTrack)">智能研判</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无聚焦运单" />
          </el-card>

          <el-card shadow="never" class="ops-card suggestions-card">
            <div slot="header" class="ops-card-header">
              <span>处置建议</span>
              <span class="ops-card-subtitle">基于当前轨迹压力自动生成</span>
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
      <el-form-item label="运单号" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入运单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="运输状态" prop="trackStatus">
        <el-select v-model="queryParams.trackStatus" clearable style="width: 140px">
          <el-option v-for="item in trackStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:logisticsTrack:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-view" size="mini" :disabled="single" @click="handleTimeline" v-hasPermi="['agri:logisticsTrack:query']">轨迹回放</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:logisticsTrack:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:logisticsTrack:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="trackList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="trackId" align="center" width="70" />
      <el-table-column label="运单号" prop="traceCode" align="center" width="160" />
      <el-table-column label="订单号" prop="orderNo" align="center" width="150" />
      <el-table-column label="产品批次号" prop="productBatchNo" align="center" width="140" />
      <el-table-column label="当前位置" prop="currentLocation" align="center" width="140" />
      <el-table-column label="运输状态" align="center" width="100">
        <template slot-scope="scope">{{ formatTrackStatus(scope.row.trackStatus) }}</template>
      </el-table-column>
      <el-table-column label="温度" prop="temperature" align="center" width="80" />
      <el-table-column label="湿度" prop="humidity" align="center" width="80" />
      <el-table-column label="事件时间" prop="eventTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.eventTime) }}</span></template>
      </el-table-column>
      <el-table-column label="轨迹描述" prop="eventDesc" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:logisticsTrack:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-map-location" @click="handlePreview(scope.row)" v-hasPermi="['agri:logisticsTrack:query']">预览</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handleAnalyze(scope.row)" v-hasPermi="['agri:logisticsTrack:query']">研判</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:logisticsTrack:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="运单号" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="销售订单号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产品批次号" prop="productBatchNo"><el-input v-model="form.productBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="运输状态" prop="trackStatus"><el-select v-model="form.trackStatus" style="width: 100%"><el-option v-for="item in trackStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="起始地" prop="startLocation"><el-input v-model="form.startLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="当前位置" prop="currentLocation"><el-input v-model="form.currentLocation" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="目的地" prop="targetLocation"><el-input v-model="form.targetLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路线轨迹" prop="routePath"><el-input v-model="form.routePath" placeholder="如：基地->仓储->门店" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="车辆编号" prop="vehicleNo"><el-input v-model="form.vehicleNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="司机姓名" prop="driverName"><el-input v-model="form.driverName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="司机电话" prop="driverPhone"><el-input v-model="form.driverPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="事件时间" prop="eventTime"><el-date-picker v-model="form.eventTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度(°C)" prop="temperature"><el-input-number v-model="form.temperature" :precision="2" :step="0.1" :min="-40" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度(%)" prop="humidity"><el-input-number v-model="form.humidity" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="经度" prop="longitude"><el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="纬度" prop="latitude"><el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="轨迹描述" prop="eventDesc"><el-input v-model="form.eventDesc" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="轨迹回放" :visible.sync="timelineOpen" width="860px" append-to-body>
      <el-table :data="timelineList">
        <el-table-column label="事件时间" prop="eventTime" align="center" width="170">
          <template slot-scope="scope"><span>{{ parseTime(scope.row.eventTime) }}</span></template>
        </el-table-column>
        <el-table-column label="位置" prop="currentLocation" align="center" width="180" />
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">{{ formatTrackStatus(scope.row.trackStatus) }}</template>
        </el-table-column>
        <el-table-column label="温度" prop="temperature" align="center" width="90" />
        <el-table-column label="湿度" prop="humidity" align="center" width="90" />
        <el-table-column label="描述" prop="eventDesc" align="center" :show-overflow-tooltip="true" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="timelineOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="轨迹地图预览" :visible.sync="previewOpen" width="940px" append-to-body>
      <el-row :gutter="12" class="summary-card-row">
        <el-col :span="6">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">记录数</div>
            <div class="summary-value">{{ summaryInfo.recordCount || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">节点数</div>
            <div class="summary-value">{{ summaryInfo.nodeCount || routeSteps.length || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">停留风险</div>
            <div class="summary-value" :class="{ danger: summaryInfo.stopRiskFlag }">{{ summaryInfo.stopRiskFlag ? `${summaryInfo.stopRiskCount || 0} 次` : '未发现' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">耗时</div>
            <div class="summary-value">{{ formatMinutes(summaryInfo.durationMinutes) }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="14">
          <div class="route-map-panel">
            <div class="route-map-head">
              <span>路线节点</span>
              <span>{{ routeSteps.length }} 个节点</span>
            </div>
            <div v-if="routeSteps.length" class="route-map-flow">
              <div v-for="(step, index) in routeSteps" :key="index" class="route-map-node">
                <div class="route-map-dot" :class="{ active: index === 0 || index === routeSteps.length - 1 }">{{ index + 1 }}</div>
                <div class="route-map-line" v-if="index < routeSteps.length - 1"></div>
                <div class="route-map-text">{{ step }}</div>
              </div>
            </div>
            <div v-else class="route-map-empty">当前记录尚未填写路线轨迹，后续接入高德地图后可直接叠加轨迹线。</div>
          </div>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never" class="route-info-card">
            <div class="route-info-item"><span>运单号</span><span>{{ previewTrack.traceCode || '-' }}</span></div>
            <div class="route-info-item"><span>订单号</span><span>{{ previewTrack.orderNo || '-' }}</span></div>
            <div class="route-info-item"><span>起始地</span><span>{{ previewTrack.startLocation || '-' }}</span></div>
            <div class="route-info-item"><span>当前位置</span><span>{{ previewTrack.currentLocation || '-' }}</span></div>
            <div class="route-info-item"><span>目的地</span><span>{{ previewTrack.targetLocation || '-' }}</span></div>
            <div class="route-info-item"><span>经纬度</span><span>{{ formatCoordinate(previewTrack.longitude, previewTrack.latitude) }}</span></div>
            <div class="route-info-item"><span>车辆编号</span><span>{{ previewTrack.vehicleNo || '-' }}</span></div>
            <div class="route-info-item"><span>司机</span><span>{{ previewTrack.driverName || '-' }}</span></div>
            <div class="route-info-item"><span>最高温度</span><span>{{ formatNumber(summaryInfo.maxTemperature, '℃') }}</span></div>
            <div class="route-info-item"><span>最低温度</span><span>{{ formatNumber(summaryInfo.minTemperature, '℃') }}</span></div>
            <div class="route-info-item"><span>平均湿度</span><span>{{ formatNumber(summaryInfo.avgHumidity, '%') }}</span></div>
            <div class="route-info-item"><span>最新状态</span><span>{{ formatTrackStatus(summaryInfo.latestStatus) || '-' }}</span></div>
          </el-card>
        </el-col>
      </el-row>
      <el-alert
        v-if="summaryInfo.summaryText"
        :title="summaryInfo.summaryText"
        :type="summaryInfo.stopRiskFlag ? 'warning' : 'success'"
        :closable="false"
        class="summary-alert"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="智能研判" :visible.sync="analysisOpen" width="840px" append-to-body>
      <el-skeleton v-if="analysisLoading" :rows="6" animated />
      <div v-else class="analysis-panel">
        <el-row :gutter="12" class="analysis-card-row">
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">风险分数</div>
              <div class="analysis-value">{{ analysisResult.riskScore || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">风险等级</div>
              <div class="analysis-value">{{ analysisResult.riskLevel || '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="analysis-card">
              <div class="summary-label">算法</div>
              <div class="analysis-value">{{ analysisResult.algorithm || '-' }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="never" class="analysis-detail-card">
          <div class="analysis-detail-title">风险因子</div>
          <el-tag v-for="factor in analysisResult.factors || []" :key="factor" size="mini" type="warning" class="analysis-tag">{{ factor }}</el-tag>
          <div class="analysis-detail-title">处置建议</div>
          <div v-for="item in analysisResult.suggestions || []" :key="item.title" class="analysis-suggestion">
            <strong>{{ item.title }}</strong>
            <span>{{ item.content }}</span>
          </div>
          <div class="analysis-detail-title">摘要</div>
          <div class="analysis-summary-text">{{ analysisResult.summary && analysisResult.summary.summaryText ? analysisResult.summary.summaryText : '暂无摘要' }}</div>
        </el-card>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="analysisOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listLogisticsTrack,
  getLogisticsTrack,
  addLogisticsTrack,
  updateLogisticsTrack,
  delLogisticsTrack,
  listLogisticsTimeline,
  getLogisticsTrackSummary,
  getLogisticsTrackDashboardOps,
  smartAnalyzeLogisticsTrack
} from '@/api/agri/logisticsTrack'

export default {
  name: 'LogisticsTrack',
  data() {
    return {
      loading: true,
      opsLoading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      trackList: [],
      opsData: {},
      timelineList: [],
      summaryInfo: {},
      routeSteps: [],
      previewTrack: {},
      focusTrack: {},
      analysisOpen: false,
      analysisLoading: false,
      analysisResult: {},
      title: '',
      open: false,
      timelineOpen: false,
      previewOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        orderNo: undefined,
        trackStatus: undefined
      },
      form: {},
      trackStatusOptions: [
        { label: '待发车', value: '0' },
        { label: '运输中', value: '1' },
        { label: '已签收', value: '2' },
        { label: '异常', value: '3' }
      ],
      rules: {
        traceCode: [{ required: true, message: '运单号不能为空', trigger: 'blur' }],
        orderNo: [{ required: true, message: '销售订单号不能为空', trigger: 'blur' }],
        productBatchNo: [{ required: true, message: '产品批次号不能为空', trigger: 'blur' }],
        currentLocation: [{ required: true, message: '当前位置不能为空', trigger: 'blur' }],
        trackStatus: [{ required: true, message: '运输状态不能为空', trigger: 'change' }],
        eventTime: [{ required: true, message: '事件时间不能为空', trigger: 'change' }]
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
    warningList() {
      return this.opsData.warningList || []
    },
    metricCards() {
      const kpi = this.opsData.kpi || {}
      return [
        { label: '轨迹总数', value: kpi.total || 0, desc: `待发车 ${kpi.pending || 0} 条` },
        { label: '异常轨迹', value: kpi.abnormal || 0, desc: `高压告警 ${kpi.highRiskCount || 0} 条` },
        { label: '路线覆盖率', value: `${kpi.routeCoverage || 0}%`, desc: '路线轨迹完整度' },
        { label: '平均温度', value: this.formatNumber(kpi.avgTemperature, '℃'), desc: `平均湿度 ${this.formatNumber(kpi.avgHumidity, '%')}` }
      ]
    }
  },
  methods: {
    refreshDashboard() {
      this.opsLoading = true
      getLogisticsTrackDashboardOps()
        .then(response => {
          this.opsData = response.data || {}
          this.focusTrack = this.pressureQueue.length ? this.pressureQueue[0] : {}
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    getList() {
      this.loading = true
      listLogisticsTrack(this.queryParams).then(response => {
        this.trackList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatTrackStatus(value) {
      const option = this.trackStatusOptions.find(item => item.value === value)
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
        trackId: undefined,
        traceCode: undefined,
        orderNo: undefined,
        productBatchNo: undefined,
        vehicleNo: undefined,
        driverName: undefined,
        driverPhone: undefined,
        startLocation: undefined,
        currentLocation: undefined,
        targetLocation: undefined,
        routePath: undefined,
        trackStatus: '0',
        eventDesc: undefined,
        eventTime: undefined,
        temperature: undefined,
        humidity: undefined,
        longitude: undefined,
        latitude: undefined,
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
      this.ids = selection.map(item => item.trackId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增物流路径追踪'
    },
    handleUpdate(row) {
      this.reset()
      const trackId = row.trackId || this.ids
      getLogisticsTrack(trackId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改物流路径追踪'
      })
    },
    handleTimeline() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条物流记录查看轨迹')
        return
      }
      const selected = this.trackList.find(item => item.trackId === this.ids[0])
      if (!selected || !selected.traceCode) {
        this.$modal.msgWarning('所选记录缺少运单号')
        return
      }
      listLogisticsTimeline(selected.traceCode).then(response => {
        this.timelineList = response.data || []
        this.timelineOpen = true
      })
    },
    handleAnalyze(row) {
      if (!row || !row.trackId) {
        this.$modal.msgWarning('请选择一条物流记录进行研判')
        return
      }
      this.focusTrack = row
      this.analysisLoading = true
      smartAnalyzeLogisticsTrack(row.trackId)
        .then(response => {
          this.analysisResult = response.data || {}
          this.analysisOpen = true
        })
        .finally(() => {
          this.analysisLoading = false
        })
    },
    handlePreview(row) {
      if (!row) {
        this.$modal.msgWarning('请选择一条物流记录预览')
        return
      }
      this.previewTrack = row
      this.routeSteps = this.parseRouteSteps(row.routePath)
      this.previewOpen = true
      this.summaryInfo = {}
      getLogisticsTrackSummary(row.traceCode).then(response => {
        this.summaryInfo = response.data || {}
        if (!this.routeSteps.length && this.summaryInfo.routeSteps && this.summaryInfo.routeSteps.length) {
          this.routeSteps = this.summaryInfo.routeSteps
        }
      })
    },
    parseRouteSteps(routePath) {
      if (!routePath) {
        return []
      }
      return routePath
        .split(/\s*(?:->|→|—>|\/|,|，|\||;|>)+\s*/)
        .map(item => item.trim())
        .filter(Boolean)
    },
    formatCoordinate(longitude, latitude) {
      if (longitude === undefined || longitude === null || latitude === undefined || latitude === null) {
        return '-'
      }
      return `${longitude}, ${latitude}`
    },
    formatNumber(value, suffix) {
      if (value === undefined || value === null || value === '') {
        return '-'
      }
      return `${value}${suffix || ''}`
    },
    formatMinutes(value) {
      if (value === undefined || value === null) {
        return '-'
      }
      if (value < 60) {
        return `${value} 分钟`
      }
      const hours = Math.floor(value / 60)
      const minutes = value % 60
      return `${hours} 小时 ${minutes} 分钟`
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.trackId !== undefined) {
          updateLogisticsTrack(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addLogisticsTrack(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const trackIds = row.trackId || this.ids
      this.$modal
        .confirm('是否确认删除物流路径追踪编号为"' + trackIds + '"的数据项？')
        .then(function () {
          return delLogisticsTrack(trackIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/logisticsTrack/export',
        {
          ...this.queryParams
        },
        `logistics_track_${new Date().getTime()}.xlsx`
      )
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
  background: linear-gradient(135deg, #17324d 0%, #215d8b 55%, #0f7c7d 100%);
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

.analysis-suggestion {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f6f9fc;
}

.analysis-summary-text {
  color: #52616f;
  line-height: 1.8;
}

.route-map-panel {
  min-height: 320px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: linear-gradient(180deg, #f9fbff 0%, #ffffff 100%);
}

.route-map-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
  font-weight: 600;
  color: #303133;
}

.route-map-flow {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-map-node {
  display: flex;
  align-items: center;
  gap: 12px;
}

.route-map-dot {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
  flex: 0 0 auto;
}

.route-map-dot.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.route-map-line {
  width: 2px;
  height: 22px;
  margin: 0 0 0 13px;
  background: linear-gradient(180deg, #b3d8ff 0%, #e4e7ed 100%);
}

.route-map-text {
  flex: 1;
  color: #606266;
  word-break: break-all;
}

.route-map-empty {
  padding: 48px 20px;
  color: #909399;
  text-align: center;
  line-height: 1.8;
}

.route-info-card {
  min-height: 320px;
}

.summary-card-row {
  margin-bottom: 16px;
}

.summary-card {
  min-height: 92px;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.summary-label {
  color: #909399;
  font-size: 12px;
}

.summary-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.summary-value.danger {
  color: #e6a23c;
}

.summary-alert {
  margin-top: 14px;
}

.route-info-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
  color: #303133;
}

.route-info-item span:first-child {
  color: #909399;
  flex: 0 0 88px;
}

.route-info-item span:last-child {
  flex: 1;
  text-align: right;
  word-break: break-all;
}
</style>
