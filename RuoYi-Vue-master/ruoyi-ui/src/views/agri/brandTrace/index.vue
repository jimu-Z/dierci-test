<template>
  <div class="app-container brand-trace-ops">
    <section class="hero-panel">
      <div>
        <p class="hero-tag">品牌溯源运营中枢</p>
        <h2>内容质检、发布漏斗、扫码可见性一体联动</h2>
      </div>
      <div class="hero-actions">
        <el-button icon="el-icon-refresh" size="mini" @click="refreshAll">刷新面板</el-button>
        <el-button type="primary" icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:brandTrace:export']">导出台账</el-button>
      </div>
    </section>

    <el-row :gutter="14" class="kpi-row">
      <el-col :xs="12" :sm="8" :md="4.8" v-for="card in kpiCards" :key="card.label">
        <div class="kpi-card">
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="board-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>待发布处置队列</span>
            <el-tag size="mini" type="warning">Top {{ (opsData.pendingPages || []).length }}</el-tag>
          </div>
          <el-table :data="opsData.pendingPages" size="mini" v-loading="opsLoading" stripe>
            <el-table-column label="溯源码" prop="traceCode" min-width="150" />
            <el-table-column label="品牌/产品" min-width="140">
              <template slot-scope="scope">{{ scope.row.brandName || '-' }} / {{ scope.row.productName || '-' }}</template>
            </el-table-column>
            <el-table-column label="准备度" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="qualityTagType(calcPageScore(scope.row))" size="mini">{{ calcPageScore(scope.row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="handleInspect(scope.row)">质检</el-button>
                <el-button type="text" size="mini" @click="handleQuickPublish(scope.row)" v-hasPermi="['agri:brandTrace:edit']">发布</el-button>
                <el-button type="text" size="mini" @click="handlePreview(scope.row)">预览</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="card-header">
            <span>发布漏斗与质量分层</span>
          </div>

          <div class="funnel-item">
            <span>已创建页面</span>
            <b>{{ opsData.publishFunnel.created || 0 }}</b>
          </div>
          <div class="funnel-item">
            <span>内容准备完成</span>
            <b>{{ opsData.publishFunnel.contentReady || 0 }}</b>
          </div>
          <div class="funnel-item">
            <span>已发布页面</span>
            <b>{{ opsData.publishFunnel.published || 0 }}</b>
          </div>

          <div class="quality-block">
            <div class="quality-line">
              <span>优质</span>
              <el-progress :percentage="qualityPercent('excellent')" :stroke-width="10" color="#00a870" />
            </div>
            <div class="quality-line">
              <span>待复核</span>
              <el-progress :percentage="qualityPercent('review')" :stroke-width="10" color="#ff9f1a" />
            </div>
            <div class="quality-line">
              <span>需补齐</span>
              <el-progress :percentage="qualityPercent('poor')" :stroke-width="10" color="#f56c6c" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <div slot="header" class="card-header">
        <span>已发布页面动态</span>
      </div>
      <div class="published-wall">
        <div class="published-item" v-for="item in opsData.latestPublished" :key="item.pageId">
          <div>
            <b>{{ item.brandName || '-' }}</b>
            <p>{{ item.productName || '-' }} · {{ item.traceCode || '-' }}</p>
          </div>
          <el-button type="text" size="mini" @click="handlePreview(item)">查看</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div slot="header" class="card-header">
        <span>台账检索</span>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
      </div>

      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="溯源码" prop="traceCode">
          <el-input v-model="queryParams.traceCode" placeholder="请输入溯源码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="品牌名" prop="brandName">
          <el-input v-model="queryParams.brandName" placeholder="请输入品牌" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="publishStatus">
          <el-select v-model="queryParams.publishStatus" clearable style="width: 120px">
            <el-option v-for="item in publishStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="el-icon-upload2" size="mini" :disabled="single" @click="handleBatchPublish" v-hasPermi="['agri:brandTrace:edit']">发布选中</el-button>
          <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:brandTrace:remove']">删除选中</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="pageList" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="溯源码" prop="traceCode" min-width="170" />
        <el-table-column label="品牌" prop="brandName" min-width="110" />
        <el-table-column label="产品" prop="productName" min-width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.publishStatus === '1' ? 'success' : 'info'">{{ formatPublishStatus(scope.row.publishStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="qualityTagType(calcPageScore(scope.row))">{{ calcPageScore(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleInspect(scope.row)">质检</el-button>
            <el-button size="mini" type="text" @click="handleQuickPublish(scope.row)" v-hasPermi="['agri:brandTrace:edit']">发布</el-button>
            <el-button size="mini" type="text" @click="handlePreview(scope.row)">预览</el-button>
            <el-button size="mini" type="text" @click="handleDelete(scope.row)" v-hasPermi="['agri:brandTrace:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog title="页面发布质检" :visible.sync="inspectOpen" width="720px" append-to-body>
      <div v-loading="smartLoading" class="inspect-body" v-if="inspectData">
        <el-row :gutter="12">
          <el-col :span="8"><div class="inspect-kpi"><span>内容分</span><b>{{ inspectData.contentScore }}</b></div></el-col>
          <el-col :span="8"><div class="inspect-kpi"><span>状态</span><b>{{ inspectData.readiness }}</b></div></el-col>
          <el-col :span="8"><div class="inspect-kpi"><span>算法</span><b>{{ inspectData.algorithm }}</b></div></el-col>
        </el-row>
        <el-alert :title="inspectData.summary" type="info" :closable="false" style="margin: 12px 0" />
        <el-tag v-for="item in inspectData.missing" :key="item" type="danger" size="mini" style="margin-right: 8px">{{ item }}</el-tag>
        <el-divider content-position="left">处置建议</el-divider>
        <p v-for="(item, index) in inspectData.suggestions" :key="index" class="suggestion">{{ index + 1 }}. {{ item }}</p>
      </div>
    </el-dialog>

    <el-dialog title="溯源页预览" :visible.sync="previewOpen" width="820px" append-to-body>
      <el-descriptions :column="2" border v-if="previewData">
        <el-descriptions-item label="溯源码">{{ previewData.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="品牌名称">{{ previewData.brandName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ previewData.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ previewData.productCode }}</el-descriptions-item>
        <el-descriptions-item label="产地">{{ previewData.originPlace }}</el-descriptions-item>
        <el-descriptions-item label="物流运单号">{{ previewData.logisticsTraceCode }}</el-descriptions-item>
        <el-descriptions-item label="详情页地址" :span="2">{{ previewData.pageUrl }}</el-descriptions-item>
        <el-descriptions-item label="品牌故事" :span="2">{{ previewData.brandStory }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listBrandTrace, previewBrandTrace, publishBrandTrace, delBrandTrace, smartInspectBrandTrace, getBrandTraceOpsDashboard } from '@/api/agri/brandTrace'

export default {
  name: 'BrandTrace',
  data() {
    return {
      loading: false,
      opsLoading: false,
      smartLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      pageList: [],
      previewOpen: false,
      previewData: null,
      inspectOpen: false,
      inspectData: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        brandName: undefined,
        publishStatus: undefined
      },
      publishStatusOptions: [
        { label: '草稿', value: '0' },
        { label: '已发布', value: '1' }
      ],
      opsData: {
        kpi: {},
        publishFunnel: {},
        qualityBucket: {},
        pendingPages: [],
        latestPublished: []
      }
    }
  },
  computed: {
    kpiCards() {
      const kpi = this.opsData.kpi || {}
      return [
        { label: '总页面数', value: kpi.total || 0 },
        { label: '已发布', value: kpi.published || 0 },
        { label: '待发布', value: kpi.draft || 0 },
        { label: '发布率', value: this.toPercent(kpi.publishRate) },
        { label: '封面完备率', value: this.toPercent(kpi.coverRate) }
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
      listBrandTrace(this.queryParams)
        .then(response => {
          this.pageList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    getOpsData() {
      this.opsLoading = true
      getBrandTraceOpsDashboard()
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
      this.ids = selection.map(item => item.pageId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleBatchPublish() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请在台账中选择一条记录发布')
        return
      }
      const row = this.pageList.find(item => item.pageId === this.ids[0])
      if (!row) {
        this.$modal.msgWarning('未找到选中记录')
        return
      }
      this.handleQuickPublish(row)
    },
    handleQuickPublish(row) {
      if (!row || !row.pageId) {
        this.$modal.msgWarning('页面记录无效，无法发布')
        return
      }
      publishBrandTrace(row.pageId).then(() => {
        this.$modal.msgSuccess('发布成功')
        this.refreshAll()
      })
    },
    handlePreview(row) {
      if (!row || !row.traceCode) {
        this.$modal.msgWarning('缺少溯源码，无法预览')
        return
      }
      previewBrandTrace(row.traceCode)
        .then(response => {
          this.previewData = response.data
          this.previewOpen = true
        })
        .catch(error => {
          const message = (error && error.message) || '预览失败'
          this.$modal.msgWarning(message)
        })
    },
    handleInspect(row) {
      if (!row || !row.pageId) {
        this.$modal.msgWarning('缺少页面编号，无法执行质检')
        return
      }
      this.smartLoading = true
      smartInspectBrandTrace(row.pageId)
        .then(response => {
          this.inspectData = response.data
          this.inspectOpen = true
        })
        .finally(() => {
          this.smartLoading = false
        })
    },
    handleDelete(row) {
      const pageIds = row && row.pageId ? row.pageId : this.ids
      if (!pageIds || pageIds.length === 0) {
        this.$modal.msgWarning('请先选择要删除的记录')
        return
      }
      this.$modal
        .confirm('是否确认删除品牌溯源页面编号为"' + pageIds + '"的数据项？')
        .then(() => delBrandTrace(pageIds))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.refreshAll()
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/brandTrace/export',
        {
          ...this.queryParams
        },
        `brand_trace_${new Date().getTime()}.xlsx`
      )
    },
    formatPublishStatus(value) {
      const option = this.publishStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    toPercent(value) {
      const number = Number(value || 0)
      return `${number.toFixed(1)}%`
    },
    calcPageScore(row) {
      let score = 40
      score += row.brandName ? 10 : 0
      score += row.productName ? 10 : 0
      score += row.originPlace ? 10 : 0
      score += row.coverImageUrl ? 8 : 0
      score += row.qrCodeUrl ? 8 : 0
      score += row.brandStory ? 8 : 0
      score += row.publishStatus === '1' ? 6 : 0
      return score
    },
    qualityPercent(key) {
      const total = Number((this.opsData.kpi && this.opsData.kpi.total) || 0)
      const value = Number((this.opsData.qualityBucket && this.opsData.qualityBucket[key]) || 0)
      if (!total) {
        return 0
      }
      return Number(((value * 100) / total).toFixed(1))
    },
    qualityTagType(score) {
      if (score >= 85) {
        return 'success'
      }
      if (score >= 65) {
        return 'warning'
      }
      return 'danger'
    }
  }
}
</script>

<style scoped>
.brand-trace-ops {
  background: linear-gradient(165deg, #f2f9f1 0%, #f7f8ee 45%, #fff9ef 100%);
}

.hero-panel {
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 14px;
  background: linear-gradient(115deg, #073b26 0%, #0b5b36 55%, #0f7846 100%);
  color: #f7fffb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-panel h2 {
  margin: 6px 0 0;
  font-size: 22px;
  font-weight: 700;
}

.hero-tag {
  margin: 0;
  color: #9ce6bd;
  letter-spacing: 1px;
}

.hero-actions .el-button + .el-button {
  margin-left: 8px;
}

.kpi-row {
  margin-bottom: 14px;
}

.kpi-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 6px 14px rgba(12, 70, 39, 0.08);
  min-height: 88px;
}

.kpi-label {
  color: #56706a;
  font-size: 13px;
}

.kpi-value {
  margin-top: 8px;
  color: #10342b;
  font-size: 26px;
  font-weight: 700;
}

.panel-card {
  border-radius: 12px;
  margin-bottom: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.funnel-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #42595a;
}

.quality-block {
  margin-top: 14px;
}

.quality-line {
  margin-bottom: 12px;
}

.quality-line span {
  display: inline-block;
  width: 60px;
  color: #59716b;
  font-size: 13px;
}

.published-wall {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  grid-gap: 10px;
}

.published-item {
  border: 1px solid #e8efe3;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fcfffa;
}

.published-item p {
  margin: 6px 0 0;
  color: #6b7f7a;
  font-size: 12px;
}

.inspect-body {
  min-height: 180px;
}

.inspect-kpi {
  border-radius: 8px;
  background: #f4faf5;
  padding: 10px;
  text-align: center;
}

.inspect-kpi span {
  display: block;
  color: #698078;
  font-size: 12px;
}

.inspect-kpi b {
  display: block;
  margin-top: 6px;
  color: #154338;
  font-size: 20px;
}

.suggestion {
  margin: 0 0 8px;
  color: #385450;
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .hero-panel h2 {
    font-size: 18px;
  }
}
</style>
