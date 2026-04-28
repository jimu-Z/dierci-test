<template>
  <div class="app-container quality-station">
    <div class="station-hero" v-loading="opsLoading">
      <div class="station-hero-main">
        <div class="station-label">视觉品质检测分拨台</div>
        <h2>用图像质检驱动放行决策，而不是维护一张台账</h2>
        <p>首屏聚焦样本压力、缺陷率、等级分布与高风险批次，支持直接触发AI检测与智能复核。</p>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增样本任务</el-button>
          <el-button type="success" icon="el-icon-video-play" :disabled="!canInvoke" @click="handleInvoke()">执行检测</el-button>
          <el-button type="warning" icon="el-icon-data-analysis" :disabled="!canReviewTask" @click="handleSmartReview()">智能复核</el-button>
          <el-button icon="el-icon-refresh" @click="refreshOps">刷新分拨台</el-button>
        </div>
      </div>
      <div class="hero-kpis">
        <div class="kpi-card">
          <div class="kpi-label">任务总量</div>
          <div class="kpi-value">{{ opsKpi.total || 0 }}</div>
          <div class="kpi-desc">样本池规模</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">检测完成率</div>
          <div class="kpi-value">{{ toPercent(opsKpi.successRate) }}</div>
          <div class="kpi-desc">已检测 {{ opsKpi.success || 0 }} 条</div>
        </div>
        <div class="kpi-card warning">
          <div class="kpi-label">平均缺陷率</div>
          <div class="kpi-value">{{ toPercent(opsKpi.avgDefectRate) }}</div>
          <div class="kpi-desc">越低越稳</div>
        </div>
        <div class="kpi-card danger">
          <div class="kpi-label">高风险样本</div>
          <div class="kpi-value">{{ opsKpi.highRiskCount || 0 }}</div>
          <div class="kpi-desc">需人工复检</div>
        </div>
      </div>
    </div>

    <el-row :gutter="14" class="ops-panels">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="panel-card">
          <div slot="header" class="panel-head">
            <span>样本压力队列</span>
            <span class="panel-sub">按风险分排序，先处理最可能影响放行的样本</span>
          </div>
          <el-table :data="pressureQueue" size="mini" @row-click="handlePressureRowClick">
            <el-table-column label="样本图" width="90" align="center">
              <template slot-scope="scope">
                <el-image class="sample-thumb" :src="resolveImageUrl(scope.row.imageUrl)" fit="cover" :preview-src-list="resolvePreview(scope.row.imageUrl)">
                  <div slot="error" class="thumb-fallback">无图</div>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column label="批次" prop="processBatchNo" width="130" />
            <el-table-column label="样品编码" prop="sampleCode" width="120" />
            <el-table-column label="状态" width="90">
              <template slot-scope="scope"><el-tag :type="statusTagType(scope.row.inspectStatus)" size="mini">{{ scope.row.statusLabel || formatInspectStatus(scope.row.inspectStatus) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="品质等级" prop="qualityGrade" width="90" />
            <el-table-column label="缺陷率" width="90">
              <template slot-scope="scope">{{ toPercentRate(scope.row.defectRate) }}</template>
            </el-table-column>
            <el-table-column label="风险" width="86">
              <template slot-scope="scope"><el-tag :type="riskTagType(scope.row.riskLevel)" size="mini">{{ scope.row.riskLevel }}</el-tag></template>
            </el-table-column>
            <el-table-column label="处置理由" prop="riskReason" min-width="170" show-overflow-tooltip />
            <el-table-column label="操作" width="160" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="mini" icon="el-icon-video-play" @click="handleInvoke(scope.row)">检测</el-button>
                <el-button type="text" size="mini" icon="el-icon-data-analysis" @click="handleSmartReview(scope.row)">复核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="panel-card focus-card">
          <div slot="header" class="panel-head">
            <span>聚焦样本</span>
            <span class="panel-sub">当前最高风险记录</span>
          </div>
          <div v-if="focusTask.inspectId" class="focus-body">
            <el-image class="focus-image" :src="resolveImageUrl(focusTask.imageUrl)" fit="cover" :preview-src-list="resolvePreview(focusTask.imageUrl)">
              <div slot="error" class="thumb-fallback">图片不可用</div>
            </el-image>
            <div class="focus-title">{{ focusTask.sampleCode || '-' }}</div>
            <div class="focus-row"><span>批次</span><span>{{ focusTask.processBatchNo || '-' }}</span></div>
            <div class="focus-row"><span>状态</span><el-tag :type="statusTagType(focusTask.inspectStatus)" size="mini">{{ focusTask.statusLabel || formatInspectStatus(focusTask.inspectStatus) }}</el-tag></div>
            <div class="focus-row"><span>品质等级</span><span>{{ focusTask.qualityGrade || '-' }}</span></div>
            <div class="focus-row"><span>缺陷率</span><span>{{ toPercentRate(focusTask.defectRate) }}</span></div>
            <div class="focus-row focus-row-wrap"><span>检测结果</span><span class="focus-result">{{ focusTask.inspectResult || '暂无检测结果' }}</span></div>
            <div class="focus-row"><span>风险分</span><span>{{ focusTask.riskScore || 0 }}</span></div>
            <div class="focus-reason">{{ focusTask.riskReason || '暂无风险说明' }}</div>
            <div class="focus-actions">
              <el-button type="primary" size="mini" @click="handleInvoke()">执行检测</el-button>
              <el-button size="mini" @click="handleSmartReview(focusTask)">智能复核</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无聚焦样本" :image-size="80" />
        </el-card>

        <el-card shadow="never" class="panel-card advice-card">
          <div slot="header" class="panel-head">
            <span>处置建议</span>
            <span class="panel-sub">自动生成</span>
          </div>
          <div v-for="item in suggestions" :key="item.title" class="advice-item">
            <div class="advice-top">
              <strong>{{ item.title }}</strong>
              <el-tag size="mini" :type="priorityTagType(item.priority)">{{ item.priority }}</el-tag>
            </div>
            <div class="advice-content">{{ item.content }}</div>
          </div>
          <el-empty v-if="!suggestions.length" description="暂无建议" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px" class="query-form">
      <el-form-item label="加工批次号" prop="processBatchNo">
        <el-input v-model="queryParams.processBatchNo" placeholder="请输入加工批次号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="样品编码" prop="sampleCode">
        <el-input v-model="queryParams.sampleCode" placeholder="请输入样品编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="检测状态" prop="inspectStatus">
        <el-select v-model="queryParams.inspectStatus" clearable style="width: 140px">
          <el-option v-for="item in inspectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:qualityInspect:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate()" v-hasPermi="['agri:qualityInspect:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-data-analysis" size="mini" :disabled="single" @click="handleSmartReview()" v-hasPermi="['agri:qualityInspect:query']">复核</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['agri:qualityInspect:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:qualityInspect:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="样本图" width="90" align="center">
        <template slot-scope="scope">
          <el-image class="sample-thumb" :src="resolveImageUrl(scope.row.imageUrl)" fit="cover" :preview-src-list="resolvePreview(scope.row.imageUrl)">
            <div slot="error" class="thumb-fallback">无图</div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="主键" prop="inspectId" align="center" width="70" />
      <el-table-column label="加工批次号" prop="processBatchNo" align="center" width="150" />
      <el-table-column label="样品编码" prop="sampleCode" align="center" width="120" />
      <el-table-column label="检测状态" align="center" width="100">
        <template slot-scope="scope"><el-tag :type="statusTagType(scope.row.inspectStatus)" size="mini">{{ formatInspectStatus(scope.row.inspectStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="品质等级" prop="qualityGrade" align="center" width="90" />
      <el-table-column label="缺陷率" align="center" width="90">
        <template slot-scope="scope">{{ toPercentRate(scope.row.defectRate) }}</template>
      </el-table-column>
      <el-table-column label="检测结果" prop="inspectResult" align="left" min-width="220" show-overflow-tooltip />
      <el-table-column label="模型版本" prop="modelVersion" align="center" width="110" />
      <el-table-column label="检测时间" prop="inspectTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.inspectTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:qualityInspect:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-video-play" @click="handleInvoke(scope.row)" v-hasPermi="['agri:qualityInspect:edit']">调用AI</el-button>
          <el-button size="mini" type="text" icon="el-icon-cpu" @click="handleFeedback(scope.row)" v-hasPermi="['agri:qualityInspect:edit']">回写检测</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:qualityInspect:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="加工批次号" prop="processBatchNo"><el-input v-model="form.processBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="样品编码" prop="sampleCode"><el-input v-model="form.sampleCode" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="图片URL" prop="imageUrl"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="检测状态" prop="inspectStatus"><el-select v-model="form.inspectStatus" style="width: 100%"><el-option v-for="item in inspectStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="品质等级" prop="qualityGrade"><el-input v-model="form.qualityGrade" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="缺陷率" prop="defectRate"><el-input-number v-model="form.defectRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="检测时间" prop="inspectTime"><el-date-picker v-model="form.inspectTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="检测结果" prop="inspectResult"><el-input v-model="form.inspectResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="检测结果回写" :visible.sync="feedbackOpen" width="560px" append-to-body>
      <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="feedbackForm.inspectId" disabled /></el-form-item>
        <el-form-item label="检测状态" prop="inspectStatus">
          <el-select v-model="feedbackForm.inspectStatus" style="width: 100%">
            <el-option label="已检测" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="品质等级" prop="qualityGrade"><el-input v-model="feedbackForm.qualityGrade" /></el-form-item>
        <el-form-item label="缺陷率" prop="defectRate"><el-input-number v-model="feedbackForm.defectRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item>
        <el-form-item label="检测结果" prop="inspectResult"><el-input v-model="feedbackForm.inspectResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="feedbackForm.modelVersion" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFeedback">确 定</el-button>
        <el-button @click="feedbackOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="智能复核" :visible.sync="reviewOpen" width="760px" append-to-body>
      <el-skeleton v-if="reviewLoading" :rows="5" animated />
      <div v-else class="review-box">
        <el-row :gutter="12" class="review-kpi">
          <el-col :span="8"><el-card shadow="never" class="review-card"><div class="review-label">健康分</div><div class="review-value">{{ reviewResult.healthScore || 0 }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="never" class="review-card"><div class="review-label">风险等级</div><div class="review-value">{{ reviewResult.riskLevel || '-' }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="never" class="review-card"><div class="review-label">算法</div><div class="review-value small">{{ reviewResult.algorithm || '-' }}</div></el-card></el-col>
        </el-row>
        <el-alert :title="reviewResult.summary || '暂无复核结果'" :type="riskAlertType(reviewResult.riskLevel)" :closable="false" />
        <div class="review-title">复核项</div>
        <el-tag v-for="item in reviewResult.checks || []" :key="item" class="review-tag" type="warning" size="mini">{{ item }}</el-tag>
        <el-empty v-if="!(reviewResult.checks || []).length" description="未发现异常项" :image-size="60" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listQualityInspect,
  getQualityInspect,
  addQualityInspect,
  updateQualityInspect,
  invokeQualityInspect,
  feedbackQualityInspect,
  delQualityInspect,
  getQualityInspectDashboardOps,
  smartReviewQualityInspect
} from '@/api/agri/qualityInspect'

export default {
  name: 'QualityInspect',
  data() {
    return {
      loading: true,
      opsLoading: false,
      reviewOpen: false,
      reviewLoading: false,
      reviewResult: {},
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      taskList: [],
      pressureQueue: [],
      focusTask: {},
      suggestions: [],
      opsKpi: {},
      selectedTask: {},
      title: '',
      open: false,
      feedbackOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        processBatchNo: undefined,
        sampleCode: undefined,
        inspectStatus: undefined
      },
      form: {},
      feedbackForm: {
        inspectId: undefined,
        inspectStatus: '1',
        qualityGrade: 'A',
        defectRate: 0.02,
        inspectResult: undefined,
        modelVersion: 'vision-model-v1'
      },
      inspectStatusOptions: [
        { label: '待检测', value: '0' },
        { label: '已检测', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        processBatchNo: [{ required: true, message: '加工批次号不能为空', trigger: 'blur' }],
        sampleCode: [{ required: true, message: '样品编码不能为空', trigger: 'blur' }],
        imageUrl: [{ required: true, message: '图片URL不能为空', trigger: 'blur' }]
      },
      feedbackRules: {
        inspectStatus: [{ required: true, message: '检测状态不能为空', trigger: 'change' }],
        inspectResult: [{ required: true, message: '检测结果不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.loadOps()
  },
  computed: {
    canInvoke() {
      return Boolean(this.getPreferredTask() && this.getPreferredTask().inspectId)
    },
    canReviewTask() {
      return Boolean(this.getPreferredTask() && this.getPreferredTask().inspectId)
    }
  },
  methods: {
    getList() {
      this.loading = true
      listQualityInspect(this.queryParams)
        .then(response => {
          this.taskList = response.rows || []
          this.total = response.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadOps() {
      this.opsLoading = true
      getQualityInspectDashboardOps()
        .then(response => {
          const data = response.data || {}
          this.opsKpi = data.kpi || {}
          this.pressureQueue = data.pressureQueue || []
          this.focusTask = data.focus || {}
          this.suggestions = data.suggestions || []
        })
        .finally(() => {
          this.opsLoading = false
        })
    },
    refreshOps() {
      this.loadOps()
    },
    normalizeSingleImage(value) {
      if (!value) {
        return ''
      }

      if (Array.isArray(value)) {
        return String(value[0] || '').trim()
      }

      const raw = String(value).trim()
      if (!raw) {
        return ''
      }

      if ((raw.startsWith('[') && raw.endsWith(']')) || (raw.startsWith('{') && raw.endsWith('}'))) {
        try {
          const parsed = JSON.parse(raw)
          if (Array.isArray(parsed) && parsed.length) {
            return String(parsed[0] || '').trim()
          }
          if (parsed && typeof parsed === 'object') {
            const candidate = parsed.url || parsed.imageUrl || parsed.path
            return String(candidate || '').trim()
          }
        } catch (e) {
          // 非JSON字符串时走后续兼容分支
        }
      }

      const first = raw.split(',')[0]
      const normalized = String(first || '').trim().replace(/^['"]|['"]$/g, '')
      return this.extractImageUrlFromPageUrl(normalized)
    },
    extractImageUrlFromPageUrl(urlText) {
      if (!urlText) {
        return ''
      }

      const cleaned = String(urlText).trim()
      if (!/^https?:\/\//i.test(cleaned)) {
        return cleaned
      }

      try {
        const parsed = new URL(cleaned)
        const host = parsed.hostname.toLowerCase()
        const pathname = parsed.pathname.toLowerCase()
        const isSearchPage = pathname.includes('/images/search') || pathname.includes('/imgres') || pathname.includes('/s')

        if (isSearchPage) {
          const direct =
            parsed.searchParams.get('mediaurl') ||
            parsed.searchParams.get('imgurl') ||
            parsed.searchParams.get('objurl') ||
            parsed.searchParams.get('src')

          if (direct) {
            return direct
          }

          if (host.includes('bing.com') && parsed.hash) {
            const hash = parsed.hash.startsWith('#') ? parsed.hash.substring(1) : parsed.hash
            const hashParams = new URLSearchParams(hash)
            const hashDirect = hashParams.get('mediaurl') || hashParams.get('imgurl')
            if (hashDirect) {
              return hashDirect
            }
          }
        }
      } catch (e) {
        return cleaned
      }

      return cleaned
    },
    resolveImageUrl(value) {
      const first = this.normalizeSingleImage(value)
      if (!first) {
        return ''
      }

      const cleaned = String(first).trim().replace(/\\/g, '/')
      if (!cleaned) {
        return ''
      }
      if (cleaned.startsWith('//')) {
        return `${window.location.protocol}${cleaned}`
      }
      if (/^www\./i.test(cleaned)) {
        return `https://${cleaned}`
      }
      if (/^(https?:\/\/|data:image|blob:)/i.test(cleaned)) {
        return cleaned
      }
      const baseApi = process.env.VUE_APP_BASE_API || ''
      const normalized = cleaned.startsWith('/') ? cleaned : `/${cleaned}`
      const isDev = process.env.NODE_ENV === 'development'
      if (isDev && (normalized.startsWith('/profile/') || normalized.startsWith('/common/download/resource'))) {
        return `${window.location.protocol}//${window.location.hostname}:8080${normalized}`
      }
      if (normalized.startsWith('/dev-api/')) {
        return normalized
      }
      return `${baseApi}${normalized}`
    },
    resolvePreview(value) {
      const url = this.resolveImageUrl(value)
      return url ? [url] : []
    },
    formatInspectStatus(value) {
      const option = this.inspectStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    statusTagType(status) {
      if (status === '1') return 'success'
      if (status === '2') return 'danger'
      return 'warning'
    },
    riskTagType(level) {
      if (level === '高') return 'danger'
      if (level === '中') return 'warning'
      return 'success'
    },
    priorityTagType(priority) {
      if (priority === '高') return 'danger'
      if (priority === '中') return 'warning'
      return 'success'
    },
    riskAlertType(level) {
      if (level === '高') return 'error'
      if (level === '中') return 'warning'
      return 'success'
    },
    toPercent(value) {
      if (value === undefined || value === null) return '0%'
      return `${Number(value).toFixed(1)}%`
    },
    toPercentRate(value) {
      if (value === undefined || value === null) return '-'
      return `${(Number(value) * 100).toFixed(2)}%`
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        inspectId: undefined,
        processBatchNo: undefined,
        sampleCode: undefined,
        imageUrl: undefined,
        inspectStatus: '0',
        qualityGrade: undefined,
        defectRate: undefined,
        inspectResult: undefined,
        inspectTime: undefined,
        modelVersion: 'vision-model-v1',
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      this.loadOps()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.inspectId)
      this.selectedTask = selection.length ? selection[0] : {}
      this.focusTask = selection.length ? { ...selection[0] } : this.focusTask
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      if (!row || !row.inspectId) {
        return
      }
      this.handleSelectionChange([row])
    },
    handlePressureRowClick(row) {
      if (!row || !row.inspectId) {
        return
      }
      this.focusTask = { ...row }
    },
    getPreferredTask(row) {
      if (row && row.inspectId) {
        return row
      }
      if (this.selectedTask && this.selectedTask.inspectId) {
        return this.selectedTask
      }
      if (this.focusTask && this.focusTask.inspectId) {
        return this.focusTask
      }
      return null
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增视觉品质检测任务'
    },
    handleUpdate(row) {
      this.reset()
      const inspectId = row && row.inspectId ? row.inspectId : this.ids[0]
      if (!inspectId) {
        this.$modal.msgWarning('请选择一条任务')
        return
      }
      getQualityInspect(inspectId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改视觉品质检测任务'
      })
    },
    handleFeedback(row) {
      this.feedbackForm = {
        inspectId: row.inspectId,
        inspectStatus: '1',
        qualityGrade: row.qualityGrade || 'A',
        defectRate: row.defectRate || 0.02,
        inspectResult: row.inspectResult,
        modelVersion: row.modelVersion || 'vision-model-v1'
      }
      this.feedbackOpen = true
    },
    handleInvoke(row) {
      const target = this.getPreferredTask(row)
      const inspectId = target && target.inspectId ? target.inspectId : this.ids[0]
      if (!inspectId) {
        this.$modal.msgWarning('请选择一条任务执行检测')
        return
      }
      this.$modal
        .confirm(`是否确认调用AI质检任务编号为"${inspectId}"的数据项？`)
        .then(() => invokeQualityInspect(inspectId))
        .then(() => {
          this.$modal.msgSuccess('AI调用成功')
          this.getList()
          this.loadOps()
        })
        .catch(() => {})
    },
    handleSmartReview(row) {
      const target = this.getPreferredTask(row)
      const inspectId = target && target.inspectId ? target.inspectId : this.ids[0]
      if (!inspectId) {
        this.$modal.msgWarning('请选择一条任务执行智能复核')
        return
      }
      this.reviewOpen = true
      this.reviewLoading = true
      this.reviewResult = {}
      smartReviewQualityInspect(inspectId)
        .then(response => {
          this.reviewResult = response.data || {}
        })
        .finally(() => {
          this.reviewLoading = false
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.inspectId !== undefined) {
          updateQualityInspect(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.loadOps()
          })
          return
        }
        addQualityInspect(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
          this.loadOps()
        })
      })
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (!valid) {
          return
        }
        feedbackQualityInspect(this.feedbackForm).then(() => {
          this.$modal.msgSuccess('回写成功')
          this.feedbackOpen = false
          this.getList()
          this.loadOps()
        })
      })
    },
    handleDelete(row) {
      const inspectIds = row && row.inspectId ? row.inspectId : this.ids
      this.$modal
        .confirm(`是否确认删除视觉品质检测任务编号为"${inspectIds}"的数据项？`)
        .then(() => delQualityInspect(inspectIds))
        .then(() => {
          this.getList()
          this.loadOps()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/qualityInspect/export',
        {
          ...this.queryParams
        },
        `quality_inspect_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.quality-station {
  background: radial-gradient(circle at 12% 8%, #fff6eb, #fff 42%);
}

.station-hero {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  background: linear-gradient(115deg, #7c2d12, #9a3412 46%, #c2410c);
  color: #fff7ed;
  border-radius: 14px;
  padding: 18px;
  margin-bottom: 14px;
}

.station-hero-main {
  flex: 1;
  min-width: 300px;
}

.station-label {
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.9;
}

.station-hero-main h2 {
  margin: 8px 0;
}

.station-hero-main p {
  margin: 0;
  opacity: 0.9;
}

.hero-actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-kpis {
  width: 360px;
  display: grid;
  grid-template-columns: repeat(2, minmax(140px, 1fr));
  gap: 8px;
}

.kpi-card {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 10px;
  padding: 10px;
}

.kpi-card.warning {
  background: rgba(254, 243, 199, 0.22);
}

.kpi-card.danger {
  background: rgba(254, 202, 202, 0.22);
}

.kpi-label {
  font-size: 12px;
  opacity: 0.92;
}

.kpi-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
}

.kpi-desc {
  font-size: 12px;
  opacity: 0.88;
}

.ops-panels {
  margin-bottom: 12px;
}

.panel-card {
  border-radius: 12px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-sub {
  color: #64748b;
  font-size: 12px;
}

.sample-thumb {
  width: 58px;
  height: 58px;
  border-radius: 8px;
  background: #f1f5f9;
}

.thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 12px;
}

.focus-body {
  font-size: 13px;
}

.focus-image {
  width: 100%;
  height: 164px;
  border-radius: 10px;
  display: block;
  background: #f1f5f9;
}

.focus-title {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #7c2d12;
}

.focus-row {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  padding-bottom: 6px;
  border-bottom: 1px dashed #e2e8f0;
}

.focus-row-wrap {
  align-items: flex-start;
}

.focus-result {
  max-width: 72%;
  text-align: right;
  line-height: 1.5;
  word-break: break-all;
}

.focus-reason {
  margin-top: 9px;
  border-radius: 8px;
  padding: 8px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #7c2d12;
}

.focus-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.advice-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 8px;
}

.advice-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.advice-content {
  margin-top: 6px;
  font-size: 12px;
  color: #475569;
  line-height: 1.55;
}

.query-form {
  margin-bottom: 6px;
}

.review-kpi {
  margin-bottom: 12px;
}

.review-card {
  text-align: center;
}

.review-label {
  color: #64748b;
  font-size: 12px;
}

.review-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
}

.review-value.small {
  font-size: 14px;
  word-break: break-all;
}

.review-title {
  margin: 12px 0 8px;
  font-weight: 600;
}

.review-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

@media (max-width: 1200px) {
  .hero-kpis {
    width: 100%;
  }
}
</style>
