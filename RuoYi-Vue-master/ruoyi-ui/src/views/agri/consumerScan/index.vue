<template>
  <div class="app-container consumer-scan-ops">
    <section class="scan-hero">
      <div>
        <p class="scan-tag">扫码风控运营台</p>
        <h2>风险监测、渠道画像、处置闭环</h2>
      </div>
      <div class="scan-inline">
        <el-input v-model="scanForm.traceCode" placeholder="输入溯源码发起模拟扫码" clearable size="small" class="scan-input" @keyup.enter.native="handleMockScan" />
        <el-select v-model="scanForm.scanChannel" size="small" class="scan-channel">
          <el-option v-for="item in scanChannelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-camera" @click="handleMockScan">模拟扫码</el-button>
      </div>
    </section>

    <el-row :gutter="14" class="scan-kpi-row">
      <el-col :xs="12" :sm="8" :md="4.8" v-for="card in kpiCards" :key="card.label">
        <div class="scan-kpi-card">
          <div class="scan-kpi-label">{{ card.label }}</div>
          <div class="scan-kpi-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="scan-panel">
          <div slot="header" class="scan-header">
            <span>近7日风险趋势</span>
          </div>
          <div class="trend-list">
            <div class="trend-item" v-for="item in opsData.riskTrend" :key="item.day">
              <span>{{ item.day }}</span>
              <el-progress :percentage="trendPercent(item.riskCount)" :stroke-width="9" color="#c65b1d" />
              <b>{{ item.riskCount }}</b>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="scan-panel">
          <div slot="header" class="scan-header">
            <span>热点溯源码</span>
          </div>
          <div class="hotspot-list">
            <div class="hotspot-item" v-for="item in opsData.traceHotspots" :key="item.traceCode">
              <span>{{ item.traceCode }}</span>
              <el-tag size="mini" type="warning">{{ item.scanCount }} 次</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="scan-panel">
          <div slot="header" class="scan-header">
            <span>实时风险事件流</span>
            <el-button type="text" @click="refreshAll">刷新</el-button>
          </div>
          <div class="alert-stream" v-loading="opsLoading">
            <div class="alert-item" v-for="item in opsData.alertStream" :key="item.queryId">
              <div>
                <b>{{ item.traceCode || '-' }}</b>
                <p>{{ parseTime(item.queryTime, '{y}-{m}-{d} {h}:{i}:{s}') }} · {{ item.scanIp || '-' }} · {{ item.scanChannel || '-' }}</p>
              </div>
              <div class="alert-actions">
                <el-tag size="mini" :type="riskTagType(item.level)">{{ item.level }}风险</el-tag>
                <el-button type="text" size="mini" @click="handleAnalyzeById(item.queryId)">分析</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="scan-panel">
      <div slot="header" class="scan-header">
        <span>查询台账</span>
        <div>
          <el-button type="text" @click="showSearch = !showSearch">筛选</el-button>
          <el-button type="text" @click="handleExport" v-hasPermi="['agri:consumerScan:export']">导出</el-button>
        </div>
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="溯源码" prop="traceCode">
          <el-input v-model="queryParams.traceCode" placeholder="请输入溯源码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="姓名" prop="consumerName">
          <el-input v-model="queryParams.consumerName" placeholder="消费者姓名" clearable style="width: 140px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="渠道" prop="scanChannel">
          <el-select v-model="queryParams.scanChannel" clearable style="width: 120px">
            <el-option v-for="item in scanChannelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:consumerScan:remove']">删除选中</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="queryList" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="溯源码" prop="traceCode" min-width="170" />
        <el-table-column label="消费者" prop="consumerName" min-width="110" />
        <el-table-column label="渠道" prop="scanChannel" width="100" align="center" />
        <el-table-column label="结果" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.scanResult === '1' ? 'success' : 'danger'">{{ formatScanResult(scope.row.scanResult) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="riskTagType(calcRiskLevel(scope.row))">{{ calcRiskLevel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="扫码时间" min-width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.queryTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleAnalyze(scope.row)">智能分析</el-button>
            <el-button size="mini" type="text" @click="handleDelete(scope.row)" v-hasPermi="['agri:consumerScan:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog title="扫码结果" :visible.sync="scanResultOpen" width="760px" append-to-body>
      <el-alert v-if="scanResultMessage" :title="scanResultMessage" type="info" :closable="false" show-icon style="margin-bottom: 12px" />
      <el-descriptions :column="2" border v-if="scanResultData">
        <el-descriptions-item label="溯源码">{{ scanResultData.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="品牌名称">{{ scanResultData.brandName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ scanResultData.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ scanResultData.productCode }}</el-descriptions-item>
        <el-descriptions-item label="产地">{{ scanResultData.originPlace }}</el-descriptions-item>
        <el-descriptions-item label="物流运单号">{{ scanResultData.logisticsTraceCode }}</el-descriptions-item>
        <el-descriptions-item label="详情页地址" :span="2">{{ scanResultData.pageUrl }}</el-descriptions-item>
        <el-descriptions-item label="品牌故事" :span="2">{{ scanResultData.brandStory }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog title="风险智能分析" :visible.sync="analyzeOpen" width="720px" append-to-body>
      <div v-loading="analyzeLoading" class="analyze-body">
        <el-row :gutter="12">
          <el-col :span="8"><div class="analyze-kpi"><span>异常分</span><b>{{ analyzeData.anomalyScore }}</b></div></el-col>
          <el-col :span="8"><div class="analyze-kpi"><span>等级</span><b>{{ analyzeData.riskLevel }}</b></div></el-col>
          <el-col :span="8"><div class="analyze-kpi"><span>算法</span><b>{{ analyzeData.algorithm }}</b></div></el-col>
        </el-row>
        <el-alert :title="analyzeData.summary || '正在生成智能分析结果...'" type="warning" :closable="false" style="margin-top: 12px" />

        <div class="analyze-section">
          <div class="analyze-section-title">风险标记</div>
          <div v-if="analyzeData.flags && analyzeData.flags.length" class="analyze-tag-list">
            <el-tag v-for="item in analyzeData.flags" :key="item" type="danger" size="mini">{{ item }}</el-tag>
          </div>
          <div v-else class="analyze-empty">暂无风险标记</div>
        </div>

        <div class="analyze-section">
          <div class="analyze-section-title">AI建议</div>
          <div v-if="analyzeData.suggestions && analyzeData.suggestions.length" class="analyze-suggestion-list">
            <div class="analyze-suggestion-item" v-for="(item, index) in analyzeData.suggestions" :key="index">{{ item }}</div>
          </div>
          <div v-else class="analyze-empty">暂无建议</div>
        </div>

        <div class="analyze-section" v-if="analyzeData.aiOriginalExcerpt">
          <div class="analyze-section-title">AI原文摘录</div>
          <pre class="analyze-excerpt">{{ analyzeData.aiOriginalExcerpt }}</pre>
        </div>

        <div class="analyze-footnote" v-if="analyzeData.requestTime">最近分析时间：{{ analyzeData.requestTime }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listConsumerScan, scanConsumerTrace, delConsumerScan, smartAnalyzeConsumerScan, getConsumerScanOpsDashboard } from '@/api/agri/consumerScan'

export default {
  name: 'ConsumerScan',
  data() {
    return {
      loading: false,
      opsLoading: false,
      analyzeLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      queryList: [],
      scanResultOpen: false,
      scanResultData: null,
      scanResultMessage: '',
      analyzeOpen: false,
      analyzeData: {
        anomalyScore: '--',
        riskLevel: '--',
        algorithm: '--',
        summary: '',
        flags: [],
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        consumerName: undefined,
        scanChannel: undefined,
        scanResult: undefined
      },
      scanForm: {
        traceCode: '',
        scanChannel: 'WECHAT'
      },
      scanChannelOptions: [
        { label: '微信', value: 'WECHAT' },
        { label: '支付宝', value: 'ALIPAY' },
        { label: '抖音', value: 'DOUYIN' },
        { label: '线下终端', value: 'OFFLINE' }
      ],
      scanResultOptions: [
        { label: '未命中', value: '0' },
        { label: '命中已发布', value: '1' },
        { label: '命中未发布', value: '2' }
      ],
      opsData: {
        kpi: {},
        riskTrend: [],
        traceHotspots: [],
        alertStream: []
      }
    }
  },
  computed: {
    kpiCards() {
      const kpi = this.opsData.kpi || {}
      return [
        { label: '扫码总量', value: kpi.total || 0 },
        { label: '命中次数', value: kpi.hitCount || 0 },
        { label: '命中率', value: this.toPercent(kpi.hitRate) },
        { label: '疑似风险', value: kpi.suspiciousCount || 0 },
        { label: '高风险', value: kpi.highRiskCount || 0 }
      ]
    }
  },
  created() {
    this.refreshAll()
  },
  methods: {
    refreshAll() {
      this.getList()
      this.getOpsData()
    },
    getList() {
      this.loading = true
      listConsumerScan(this.queryParams)
        .then(response => {
          this.queryList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    getOpsData() {
      this.opsLoading = true
      getConsumerScanOpsDashboard()
        .then(response => {
          this.opsData = response.data || this.opsData
        })
        .finally(() => {
          this.opsLoading = false
        })
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
      this.ids = selection.map(item => item.queryId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleMockScan() {
      const traceCode = (this.scanForm.traceCode || '').trim()
      if (!traceCode) {
        this.$modal.msgWarning('请输入要查询的溯源码')
        return
      }
      scanConsumerTrace(traceCode, {
        scanChannel: this.scanForm.scanChannel,
        consumerName: '运营模拟用户',
        remark: '扫码风控运营台模拟查询'
      })
        .then(response => {
          this.scanResultData = response.data
          this.scanResultMessage = '扫码成功，已返回溯源详情。'
          this.scanResultOpen = true
          this.refreshAll()
        })
        .catch(error => {
          this.scanResultData = null
          this.scanResultMessage = (error && error.message) || '扫码失败'
          this.scanResultOpen = true
          this.refreshAll()
        })
    },
    handleAnalyze(row) {
      if (!row || !row.queryId) {
        this.$modal.msgWarning('缺少查询编号，无法分析')
        return
      }
      this.handleAnalyzeById(row.queryId)
    },
    handleAnalyzeById(queryId) {
      this.analyzeOpen = true
      this.analyzeLoading = true
      this.analyzeData = {
        anomalyScore: '--',
        riskLevel: '--',
        algorithm: '--',
        summary: '',
        flags: [],
        suggestions: [],
        aiOriginalExcerpt: '',
        requestTime: ''
      }
      smartAnalyzeConsumerScan(queryId)
        .then(response => {
          const data = response.data || {}
          const suggestions = Array.isArray(data.suggestions)
            ? data.suggestions.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
            : []
          this.analyzeData = {
            anomalyScore: data.anomalyScore != null ? data.anomalyScore : '--',
            riskLevel: data.riskLevel || '--',
            algorithm: data.algorithm || '--',
            summary: data.summary || '',
            flags: Array.isArray(data.flags) ? data.flags : [],
            suggestions,
            aiOriginalExcerpt: data.aiOriginalExcerpt || '',
            requestTime: new Date().toLocaleString()
          }
        })
        .catch(() => {
          this.$modal.msgError('智能分析调用失败，请稍后重试')
        })
        .finally(() => {
          this.analyzeLoading = false
        })
    },
    handleDelete(row) {
      const queryIds = row && row.queryId ? row.queryId : this.ids
      if (!queryIds || queryIds.length === 0) {
        this.$modal.msgWarning('请先选择要删除的记录')
        return
      }
      this.$modal
        .confirm('是否确认删除消费者扫码查询编号为"' + queryIds + '"的数据项？')
        .then(() => delConsumerScan(queryIds))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.refreshAll()
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/consumerScan/export',
        {
          ...this.queryParams
        },
        `consumer_scan_${new Date().getTime()}.xlsx`
      )
    },
    formatScanResult(value) {
      const option = this.scanResultOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    calcRiskLevel(row) {
      let score = 20
      if (row.scanResult === '0') {
        score += 35
      }
      if (row.scanResult === '2') {
        score += 25
      }
      if (!row.consumerPhone) {
        score += 10
      }
      if (row.scanIp && (row.scanIp.indexOf('10.') === 0 || row.scanIp.indexOf('192.168.') === 0)) {
        score += 10
      }
      if (score >= 80) {
        return '高'
      }
      if (score >= 50) {
        return '中'
      }
      return '低'
    },
    riskTagType(level) {
      if (level === '高') {
        return 'danger'
      }
      if (level === '中') {
        return 'warning'
      }
      return 'success'
    },
    trendPercent(count) {
      const max = Math.max(...(this.opsData.riskTrend || []).map(item => item.riskCount || 0), 1)
      return Number((((count || 0) * 100) / max).toFixed(1))
    },
    toPercent(value) {
      const number = Number(value || 0)
      return `${number.toFixed(1)}%`
    }
  }
}
</script>

<style scoped>
.consumer-scan-ops {
  background: linear-gradient(170deg, #fff5ef 0%, #fffef8 58%, #f7fcff 100%);
}

.scan-hero {
  background: linear-gradient(120deg, #4f1f0f 0%, #7a3118 55%, #9d3f1f 100%);
  color: #fff7f4;
  border-radius: 14px;
  padding: 18px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.scan-tag {
  margin: 0;
  color: #ffd0bc;
  letter-spacing: 1px;
}

.scan-hero h2 {
  margin: 6px 0 0;
  font-size: 22px;
}

.scan-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.scan-input {
  width: 260px;
}

.scan-channel {
  width: 120px;
}

.scan-kpi-row {
  margin-bottom: 14px;
}

.scan-kpi-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 14px;
  min-height: 88px;
  box-shadow: 0 6px 14px rgba(127, 67, 20, 0.08);
}

.scan-kpi-label {
  color: #6f6760;
  font-size: 13px;
}

.scan-kpi-value {
  margin-top: 8px;
  color: #4f250f;
  font-size: 25px;
  font-weight: 700;
}

.scan-panel {
  border-radius: 12px;
  margin-bottom: 14px;
}

.scan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.trend-item {
  display: grid;
  grid-template-columns: 90px 1fr 34px;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.trend-item span {
  color: #7a6f66;
  font-size: 12px;
}

.trend-item b {
  color: #9d3f1f;
}

.hotspot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #fff9f3;
}

.alert-stream {
  min-height: 190px;
}

.alert-item {
  padding: 10px 0;
  border-bottom: 1px dashed #ecd9cc;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.alert-item p {
  margin: 5px 0 0;
  color: #816e61;
  font-size: 12px;
}

.alert-actions {
  min-width: 110px;
  text-align: right;
}

.analyze-kpi {
  border-radius: 8px;
  background: #fff6f0;
  padding: 10px;
  text-align: center;
}

.analyze-kpi span {
  color: #7f6558;
  display: block;
  font-size: 12px;
}

.analyze-kpi b {
  margin-top: 6px;
  display: block;
  color: #7a3118;
  font-size: 20px;
}

.analyze-body {
  min-height: 120px;
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 2px;
}

.analyze-section {
  margin-top: 12px;
}

.analyze-section-title {
  margin-bottom: 8px;
  color: #7a2d18;
  font-weight: 600;
}

.analyze-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.analyze-suggestion-list {
  display: grid;
  gap: 8px;
}

.analyze-suggestion-item {
  border-radius: 8px;
  border: 1px solid #f1d6d6;
  background: #fff2f2;
  color: #d9534f;
  padding: 8px 10px;
  line-height: 1.5;
  word-break: break-word;
}

.analyze-excerpt {
  border-radius: 8px;
  border: 1px solid #f0d9d9;
  background: #fff7f7;
  color: #b44d4b;
  padding: 10px;
  margin: 0;
  max-height: 180px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
}

.analyze-empty {
  color: #9a8a80;
}

.analyze-footnote {
  margin-top: 10px;
  font-size: 12px;
  color: #9a8a80;
}

@media (max-width: 768px) {
  .scan-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .scan-inline {
    width: 100%;
    flex-wrap: wrap;
  }

  .scan-input {
    width: 100%;
  }
}
</style>
