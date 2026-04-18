<template>
  <div class="app-container overview-center">
    <el-card shadow="never" class="hero-card mb16">
      <div class="hero-wrap" v-loading="dashboardLoading">
        <div>
          <div class="hero-title">经营总览调度台</div>
          <div class="hero-desc">聚焦产销、溯源、预警、设备与待办五类指标，帮助值班人员快速判断当天经营健康度。</div>
          <div class="hero-meta">
            <span>统计记录 {{ total }}</span>
            <span>最近刷新 {{ lastRefreshLabel }}</span>
            <span>模型 {{ insight.modelVersion || 'overview-health-v1' }}</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="loadDashboard">刷新看板</el-button>
          <el-button type="warning" plain size="mini" icon="el-icon-data-analysis" :disabled="!selectedOverviewId" @click="handleSmartInsight()">智能诊断</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12" class="mb16">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="12" :sm="12" :md="4">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-foot">{{ item.foot }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb16">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>总览健康洞察</span>
            <span class="panel-subtitle">按经营健康模型自动评估</span>
          </div>
          <div class="insight-box">
            <div class="insight-summary">{{ insight.overviewSummary || '暂无健康洞察，刷新看板后可查看系统评估结果。' }}</div>
            <div class="insight-meta">
              <el-tag size="mini" :type="healthTagType(insight.healthLevel)">{{ insight.healthLevel || '稳定' }}</el-tag>
              <span>置信度 {{ formatRate(insight.confidenceRate) }}</span>
              <span>模型 {{ insight.modelVersion || 'overview-health-v1' }}</span>
            </div>
            <div class="recommendation-list" v-if="recommendations.length">
              <div v-for="(item, index) in recommendations" :key="index" class="recommendation-item">{{ item }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-header">
            <span>最新看板记录</span>
            <span class="panel-subtitle">点击一条后可执行诊断</span>
          </div>
          <el-empty v-if="!topList.length" description="暂无总览记录" />
          <div v-else class="snapshot-list">
            <div v-for="item in topList" :key="item.overviewId" class="snapshot-item" :class="{ active: selectedOverviewId === item.overviewId }" @click="selectRow(item)">
              <div class="snapshot-head">
                <div>
                  <div class="snapshot-title">{{ item.statDate }}</div>
                  <div class="snapshot-sub">产销 {{ formatNumber(item.totalOutput) }} / {{ formatNumber(item.totalSales) }}</div>
                </div>
                <el-tag size="mini" type="info">待办 {{ item.pendingTaskCount || 0 }}</el-tag>
              </div>
              <div class="snapshot-desc">预警 {{ item.warningCount || 0 }} · 溯源 {{ item.traceQueryCount || 0 }} · 在线设备 {{ item.onlineDeviceCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:dashboardOverview:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:dashboardOverview:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:dashboardOverview:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:dashboardOverview:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px" class="mb16">
      <el-form-item label="开始日期" prop="beginStatDate">
        <el-date-picker clearable v-model="queryParams.beginStatDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endStatDate">
        <el-date-picker clearable v-model="queryParams.endStatDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="overviewList" @selection-change="handleSelectionChange" @row-click="selectRow">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="统计日期" align="center" prop="statDate" width="110" />
      <el-table-column label="总产量(吨)" align="center" prop="totalOutput" width="110" />
      <el-table-column label="总销量(万元)" align="center" prop="totalSales" width="120" />
      <el-table-column label="溯源查询" align="center" prop="traceQueryCount" width="100" />
      <el-table-column label="预警数量" align="center" prop="warningCount" width="90" />
      <el-table-column label="在线设备" align="center" prop="onlineDeviceCount" width="90" />
      <el-table-column label="待办数量" align="center" prop="pendingTaskCount" width="90" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click.stop="handleSmartInsight(scope.row)">诊断</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)" v-hasPermi="['agri:dashboardOverview:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)" v-hasPermi="['agri:dashboardOverview:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="统计日期" prop="statDate"><el-date-picker clearable v-model="form.statDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择统计日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总产量(吨)" prop="totalOutput"><el-input-number v-model="form.totalOutput" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="总销量(万元)" prop="totalSales"><el-input-number v-model="form.totalSales" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="溯源查询" prop="traceQueryCount"><el-input-number v-model="form.traceQueryCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警数量" prop="warningCount"><el-input-number v-model="form.warningCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在线设备" prop="onlineDeviceCount"><el-input-number v-model="form.onlineDeviceCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="待办数量" prop="pendingTaskCount"><el-input-number v-model="form.pendingTaskCount" :min="0" style="width: 100%" /></el-form-item>
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
  listDashboardOverview,
  getDashboardOverview,
  delDashboardOverview,
  addDashboardOverview,
  updateDashboardOverview,
  getDashboardOverviewDashboard,
  smartInsightDashboardOverview
} from '@/api/agri/dashboardOverview'

export default {
  name: 'DashboardOverview',
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      overviewList: [],
      dashboardSnapshot: null,
      selectedOverview: null,
      lastRefreshTime: null,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        beginStatDate: null,
        endStatDate: null
      },
      form: {},
      rules: {
        statDate: [{ required: true, message: '统计日期不能为空', trigger: 'change' }],
        totalOutput: [{ required: true, message: '总产量不能为空', trigger: 'blur' }],
        totalSales: [{ required: true, message: '总销量不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    summaryCards() {
      const kpi = (this.dashboardSnapshot && this.dashboardSnapshot.kpi) || {}
      return [
        { key: 'output', label: '总产量', value: this.formatNumber(kpi.totalOutput), foot: '累计产出规模' },
        { key: 'sales', label: '总销量', value: this.formatNumber(kpi.totalSales), foot: '累计销售规模' },
        { key: 'warning', label: '预警数量', value: kpi.warningCount || 0, foot: '需要关注的告警' },
        { key: 'trace', label: '溯源查询', value: kpi.traceQueryCount || 0, foot: '查询行为总量' },
        { key: 'device', label: '在线设备', value: kpi.onlineDeviceCount || 0, foot: '当前在线节点' },
        { key: 'task', label: '待办数量', value: kpi.pendingTaskCount || 0, foot: '待处理工作量' }
      ]
    },
    insight() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.insight) || {}
    },
    recommendations() {
      return (this.dashboardSnapshot && this.dashboardSnapshot.recommendations) || []
    },
    topList() {
      return (this.dashboardSnapshot && Array.isArray(this.dashboardSnapshot.topList)) ? this.dashboardSnapshot.topList : []
    },
    lastRefreshLabel() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '暂无'
    },
    selectedOverviewId() {
      return this.selectedOverview ? this.selectedOverview.overviewId : null
    }
  },
  created() {
    this.getList()
    this.loadDashboard()
  },
  methods: {
    getList() {
      this.loading = true
      listDashboardOverview(this.queryParams).then(response => {
        this.overviewList = response.rows || []
        this.total = response.total || 0
        this.lastRefreshTime = new Date()
        if (!this.selectedOverview && this.overviewList.length) {
          this.selectedOverview = this.overviewList[0]
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadDashboard() {
      this.dashboardLoading = true
      getDashboardOverviewDashboard().then(response => {
        this.dashboardSnapshot = response.data || null
        this.lastRefreshTime = new Date()
        if (!this.selectedOverview && this.topList.length) {
          this.selectedOverview = this.topList[0]
        }
      }).finally(() => {
        this.dashboardLoading = false
      })
    },
    selectRow(row) {
      if (row) {
        this.selectedOverview = row
      }
    },
    handleSmartInsight(row) {
      const overviewId = row && row.overviewId ? row.overviewId : this.selectedOverviewId
      if (!overviewId) {
        this.$modal.msgWarning('请先选择一条总览记录')
        return
      }
      smartInsightDashboardOverview(overviewId).then(response => {
        this.dashboardSnapshot = this.dashboardSnapshot || {}
        this.dashboardSnapshot.insight = response.data || {}
        this.$modal.msgSuccess('智能诊断完成')
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        overviewId: null,
        statDate: null,
        totalOutput: 0,
        totalSales: 0,
        traceQueryCount: 0,
        warningCount: 0,
        onlineDeviceCount: 0,
        pendingTaskCount: 0,
        remark: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      this.loadDashboard()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.overviewId)
      if (selection.length === 1) {
        this.selectedOverview = selection[0]
      }
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据总览看板'
    },
    handleUpdate(row) {
      this.reset()
      const overviewId = row && row.overviewId ? row.overviewId : this.ids[0]
      if (!overviewId) {
        return
      }
      getDashboardOverview(overviewId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '修改数据总览看板'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.overviewId != null) {
            updateDashboardOverview(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
              this.loadDashboard()
            })
          } else {
            addDashboardOverview(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
              this.loadDashboard()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const overviewIds = row && row.overviewId ? row.overviewId : this.ids
      this.$modal.confirm('是否确认删除数据总览看板编号为"' + overviewIds + '"的数据项？').then(() => {
        return delDashboardOverview(overviewIds)
      }).then(() => {
        this.getList()
        this.loadDashboard()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/dashboardOverview/export', {
        ...this.queryParams
      }, `dashboard_overview_${new Date().getTime()}.xlsx`)
    },
    formatNumber(value) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num.toFixed(2) : '0.00'
    },
    formatRate(value) {
      const num = Number(value || 0)
      return `${(Number.isFinite(num) ? num * 100 : 0).toFixed(1)}%`
    },
    healthTagType(level) {
      if (level === '高风险') {
        return 'danger'
      }
      if (level === '关注') {
        return 'warning'
      }
      return 'success'
    }
  }
}
</script>

<style scoped>
.overview-center {
  background: linear-gradient(180deg, rgba(241, 247, 252, 0.7), rgba(255, 255, 255, 1));
}

.hero-card,
.panel-card,
.kpi-card {
  border-radius: 14px;
}

.hero-wrap {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
}

.hero-desc {
  margin-top: 8px;
  color: #5a6a7a;
}

.hero-meta,
.insight-meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #6b7785;
  font-size: 12px;
}

.kpi-card {
  min-height: 92px;
}

.kpi-label,
.card-title {
  color: #768191;
  font-size: 13px;
}

.kpi-value,
.card-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #18222d;
}

.kpi-foot {
  margin-top: 6px;
  color: #97a2ad;
  font-size: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
}

.panel-subtitle {
  color: #8a97a4;
  font-size: 12px;
  font-weight: 400;
}

.insight-summary,
.snapshot-desc {
  color: #41505f;
  line-height: 1.8;
}

.snapshot-list {
  display: grid;
  gap: 10px;
}

.snapshot-item {
  padding: 12px;
  border: 1px solid #e8edf3;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.snapshot-item.active,
.snapshot-item:hover {
  border-color: #5a88ff;
  box-shadow: 0 8px 18px rgba(90, 136, 255, 0.12);
}

.snapshot-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.snapshot-title {
  font-weight: 600;
  color: #1f2d3d;
}

.snapshot-sub {
  margin-top: 4px;
  color: #7d8894;
  font-size: 12px;
}

.recommendation-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.recommendation-item {
  padding: 10px 12px;
  background: #f6f8fb;
  border-radius: 10px;
  color: #334155;
}
</style>
