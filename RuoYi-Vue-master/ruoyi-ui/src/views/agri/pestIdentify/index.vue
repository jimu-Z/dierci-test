<template>
  <div class="app-container pest-lab">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px" class="query-bar">
      <el-form-item label="地块编码" prop="plotCode">
        <el-input v-model="queryParams.plotCode" placeholder="请输入地块编码" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="作物名称" prop="cropName">
        <el-input v-model="queryParams.cropName" placeholder="请输入作物名称" clearable style="width: 170px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="识别状态" prop="identifyStatus">
        <el-select v-model="queryParams.identifyStatus" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="item in identifyStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">刷新实验台</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="lab-head" v-loading="dashboardLoading">
      <div class="lab-title">病虫害智能识别实验台</div>
      <div class="lab-subtitle">面向地块巡检场景，集中展示图片、识别过程、算法标签与处置建议。</div>
      <el-row :gutter="12" class="quick-entry-row">
        <el-col :xs="24" :lg="16">
          <div class="quick-entry">
            <div class="quick-title">上传图片即识别</div>
            <el-row :gutter="10">
              <el-col :xs="24" :sm="8"><el-input v-model="quickForm.plotCode" size="small" placeholder="地块编码，如 PLOT-001" /></el-col>
              <el-col :xs="24" :sm="8"><el-input v-model="quickForm.cropName" size="small" placeholder="作物名称，如 番茄" /></el-col>
              <el-col :xs="24" :sm="8"><el-input v-model="quickForm.modelVersion" size="small" placeholder="模型版本（可选）" /></el-col>
            </el-row>
            <div class="quick-upload-wrap">
              <ImageUpload v-model="quickForm.imageUrl" :limit="1" :fileSize="5" :isShowTip="false" :drag="false" />
              <el-button type="primary" icon="el-icon-cpu" :loading="quickInvoking" @click="handleQuickInvoke" v-hasPermi="['agri:pestIdentify:add']">上传并智能识别</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="10" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">任务总量</div><div class="kpi-value">{{ dashboardKpi.total || 0 }}</div></div></el-col>
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">已识别</div><div class="kpi-value">{{ dashboardKpi.successCount || 0 }}</div></div></el-col>
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">待识别</div><div class="kpi-value">{{ dashboardKpi.pendingCount || 0 }}</div></div></el-col>
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">失败率</div><div class="kpi-value">{{ formatPercent(dashboardKpi.failRate) }}</div></div></el-col>
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">平均置信度</div><div class="kpi-value">{{ formatRate(dashboardKpi.avgConfidence) }}</div></div></el-col>
        <el-col :xs="12" :sm="8" :md="4"><div class="kpi"><div class="kpi-label">高风险样本</div><div class="kpi-value">{{ dashboardKpi.highRiskCount || 0 }}</div></div></el-col>
      </el-row>
    </div>

    <el-row :gutter="12" class="workbench-row">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="gallery-card">
          <div slot="header" class="panel-head">
            <span>识别图片任务墙</span>
            <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['agri:pestIdentify:add']">新增任务</el-button>
          </div>
          <div class="gallery-grid" v-loading="loading">
            <div
              class="task-tile"
              v-for="item in taskList"
              :key="item.taskId"
              :class="{ active: selectedTask && selectedTask.taskId === item.taskId }"
              @click="selectTask(item)">
              <el-image class="tile-image" :src="resolveImageUrl(item.imageUrl)" fit="cover" :preview-src-list="resolveImagePreviewList(item.imageUrl)">
                <div slot="error" class="tile-image-fallback">无图像</div>
              </el-image>
              <div class="tile-meta">
                <div class="tile-main">
                  <strong>#{{ item.taskId }}</strong>
                  <el-tag size="mini" :type="statusTagType(item.identifyStatus)">{{ formatIdentifyStatus(item.identifyStatus) }}</el-tag>
                </div>
                <div class="tile-line">地块 {{ item.plotCode || '-' }} · 作物 {{ item.cropName || '-' }}</div>
                <div class="tile-line">置信度 {{ formatRate(item.confidence) }} · 风险 {{ item.riskLevel || inferRisk(item) }}</div>
                <div class="tile-result">{{ item.identifyResult || '暂无识别结果' }}</div>
                <div class="tile-actions">
                  <el-button size="mini" type="text" icon="el-icon-video-play" @click.stop="handleInvoke(item)" v-hasPermi="['agri:pestIdentify:edit']">智能识别</el-button>
                  <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(item)" v-hasPermi="['agri:pestIdentify:edit']">编辑</el-button>
                  <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)" v-hasPermi="['agri:pestIdentify:remove']">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-if="!taskList.length" description="暂无识别任务" :image-size="80" />
          </div>
          <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="console-card">
          <div slot="header" class="panel-head">
            <span>算法输出控制台</span>
            <el-button type="success" size="mini" icon="el-icon-cpu" @click="handleFeedback(selectedTask)" :disabled="!selectedTask" v-hasPermi="['agri:pestIdentify:edit']">人工回写</el-button>
          </div>

          <div v-if="selectedTask" class="console-body">
            <el-image class="console-image" :src="resolveImageUrl(selectedTask.imageUrl)" fit="cover" :preview-src-list="resolveImagePreviewList(selectedTask.imageUrl)">
              <div slot="error" class="tile-image-fallback">图片不可用</div>
            </el-image>
            <div class="console-title">任务 #{{ selectedTask.taskId }} · {{ selectedTask.cropName || '-' }}</div>
            <div class="console-line">地块：{{ selectedTask.plotCode || '-' }} | 状态：{{ formatIdentifyStatus(selectedTask.identifyStatus) }}</div>
            <div class="console-line">模型：{{ algorithm.engine || selectedTask.modelVersion || 'deepseek-chat' }} | 风险：{{ algorithm.riskLevel || inferRisk(selectedTask) }}</div>
            <div class="console-line">置信度：{{ formatRate(algorithm.confidence || selectedTask.confidence) }}</div>

            <div class="tag-wrap">
              <el-tag v-for="(tag, idx) in algorithm.tags" :key="idx" size="mini" effect="dark" class="algo-tag">{{ tag }}</el-tag>
              <el-tag v-if="!algorithm.tags || !algorithm.tags.length" size="mini" type="info">待识别</el-tag>
            </div>

            <div class="conclusion">{{ algorithm.conclusion || selectedTask.identifyResult || '暂无算法结论' }}</div>

            <div class="timeline-title">识别过程输出</div>
            <el-timeline class="process-timeline">
              <el-timeline-item
                v-for="(node, index) in processTrace"
                :key="index"
                :timestamp="node.time"
                :type="timelineType(node.status)">
                <div class="node-stage">{{ node.stage }}</div>
                <div class="node-msg">{{ node.message }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <el-empty v-else description="请从左侧任务墙选择一条识别任务" :image-size="88" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="ledger-card">
      <div slot="header" class="panel-head">
        <span>任务台账维护</span>
        <div>
          <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:pestIdentify:export']">导出</el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>
      <el-table v-loading="loading" :data="taskList" size="mini" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="taskId" align="center" width="70" />
        <el-table-column label="地块" prop="plotCode" align="center" width="110" />
        <el-table-column label="作物" prop="cropName" align="center" width="110" />
        <el-table-column label="识别状态" align="center" width="100"><template slot-scope="scope">{{ formatIdentifyStatus(scope.row.identifyStatus) }}</template></el-table-column>
        <el-table-column label="置信度" align="center" width="90"><template slot-scope="scope">{{ formatRate(scope.row.confidence) }}</template></el-table-column>
        <el-table-column label="识别时间" align="center" width="160"><template slot-scope="scope">{{ parseTime(scope.row.identifyTime) }}</template></el-table-column>
        <el-table-column label="结果摘要" prop="identifyResult" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12"><el-form-item label="地块编码" prop="plotCode"><el-input v-model="form.plotCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作物名称" prop="cropName"><el-input v-model="form.cropName" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="图片URL" prop="imageUrl"><el-input v-model="form.imageUrl" placeholder="上传后回填URL" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="识别状态" prop="identifyStatus"><el-select v-model="form.identifyStatus" style="width: 100%"><el-option v-for="item in identifyStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="置信度" prop="confidence"><el-input-number v-model="form.confidence" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="识别时间" prop="identifyTime"><el-date-picker v-model="form.identifyTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="识别结果" prop="identifyResult"><el-input v-model="form.identifyResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="识别结果回写" :visible.sync="feedbackOpen" width="560px" append-to-body>
      <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="feedbackForm.taskId" disabled /></el-form-item>
        <el-form-item label="识别状态" prop="identifyStatus">
          <el-select v-model="feedbackForm.identifyStatus" style="width: 100%">
            <el-option label="已识别" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="识别结果" prop="identifyResult"><el-input v-model="feedbackForm.identifyResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="置信度" prop="confidence"><el-input-number v-model="feedbackForm.confidence" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="feedbackForm.modelVersion" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFeedback">确 定</el-button>
        <el-button @click="feedbackOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listPestIdentify,
  getPestIdentify,
  addPestIdentify,
  updatePestIdentify,
  getPestIdentifyDashboard,
  invokePestIdentify,
  quickInvokePestIdentify,
  feedbackPestIdentify,
  delPestIdentify
} from '@/api/agri/pestIdentify'

export default {
  name: 'PestIdentify',
  data() {
    return {
      loading: true,
      dashboardLoading: false,
      quickInvoking: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      taskList: [],
      selectedTask: null,
      dashboardKpi: {},
      processTrace: [],
      algorithm: {},
      statusPollingTimer: null,
      title: '',
      open: false,
      feedbackOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plotCode: undefined,
        cropName: undefined,
        identifyStatus: undefined
      },
      form: {},
      feedbackForm: {
        taskId: undefined,
        identifyStatus: '1',
        identifyResult: undefined,
        confidence: 0.9,
        modelVersion: 'pest-model-v1'
      },
      quickForm: {
        plotCode: '',
        cropName: '',
        imageUrl: '',
        modelVersion: 'deepseek-chat'
      },
      identifyStatusOptions: [
        { label: '待识别', value: '0' },
        { label: '已识别', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        cropName: [{ required: true, message: '作物名称不能为空', trigger: 'blur' }],
        imageUrl: [{ required: true, message: '图片URL不能为空', trigger: 'blur' }]
      },
      feedbackRules: {
        identifyStatus: [{ required: true, message: '识别状态不能为空', trigger: 'change' }],
        identifyResult: [{ required: true, message: '识别结果不能为空', trigger: 'blur' }]
      }
    }
  },
  beforeDestroy() {
    this.stopStatusPolling()
  },
  created() {
    this.getList()
    this.loadDashboard()
  },
  methods: {
    loadDashboard() {
      this.dashboardLoading = true
      getPestIdentifyDashboard({ topN: 12 }).then(response => {
        const data = response.data || {}
        this.dashboardKpi = data.kpi || {}
      }).finally(() => {
        this.dashboardLoading = false
      })
    },
    getList() {
      this.loading = true
      return listPestIdentify(this.queryParams).then(response => {
        this.taskList = response.rows || []
        this.total = response.total || 0
        if (!this.selectedTask && this.taskList.length) {
          this.selectTask(this.taskList[0])
        }
      }).finally(() => {
        this.loading = false
      })
    },
    selectTask(item) {
      this.selectedTask = item
      this.algorithm = {
        engine: item.modelVersion || 'deepseek-chat',
        confidence: item.confidence,
        riskLevel: item.riskLevel || this.inferRisk(item),
        tags: this.inferTags(item.identifyResult),
        conclusion: item.identifyResult
      }
      this.processTrace = this.buildDefaultTrace(item)
    },
    buildDefaultTrace(item) {
      return [
        { stage: '任务入队', status: 'success', time: this.parseTime(item.createTime) || '-', message: '采集到巡检图片并完成任务登记。' },
        { stage: '图像预处理', status: 'success', time: this.parseTime(item.updateTime) || '-', message: '完成图像清洗、分辨率检查与地块绑定。' },
        { stage: '智能识别算法', status: item.identifyStatus === '2' ? 'error' : (item.identifyStatus === '1' ? 'success' : 'running'), time: this.parseTime(item.identifyTime) || '-', message: item.identifyResult || '等待识别触发。' },
        { stage: '结果输出', status: item.identifyStatus === '1' ? 'success' : 'info', time: this.parseTime(item.identifyTime) || '-', message: item.identifyStatus === '1' ? '已完成结果回写并生成风险标记。' : '尚未完成结果回写。' }
      ]
    },
    timelineType(status) {
      if (status === 'success') {
        return 'success'
      }
      if (status === 'error') {
        return 'danger'
      }
      if (status === 'running') {
        return 'warning'
      }
      return 'info'
    },
    inferTags(result) {
      const text = result || ''
      const tags = []
      if (text.includes('病') || text.includes('霉')) {
        tags.push('病害风险')
      }
      if (text.includes('虫')) {
        tags.push('虫害风险')
      }
      if (text.includes('叶') || text.includes('斑')) {
        tags.push('叶面异常')
      }
      return tags
    },
    inferRisk(item) {
      const confidence = Number(item.confidence || 0)
      const text = item.identifyResult || ''
      if (item.identifyStatus === '2') {
        return '高'
      }
      if ((text.includes('病') || text.includes('虫') || text.includes('霉')) && confidence >= 0.75) {
        return '高'
      }
      if (confidence >= 0.85) {
        return '低'
      }
      return '中'
    },
    statusTagType(status) {
      if (status === '1') {
        return 'success'
      }
      if (status === '2') {
        return 'danger'
      }
      return 'info'
    },
    formatRate(value) {
      const num = Number(value || 0)
      if (!Number.isFinite(num)) {
        return '0%'
      }
      return `${(num <= 1 ? num * 100 : num).toFixed(2)}%`
    },
    formatPercent(value) {
      const num = Number(value || 0)
      return `${num.toFixed(2)}%`
    },
    formatIdentifyStatus(value) {
      const option = this.identifyStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        taskId: undefined,
        plotCode: undefined,
        cropName: undefined,
        imageUrl: undefined,
        identifyStatus: '0',
        identifyResult: undefined,
        confidence: undefined,
        identifyTime: undefined,
        modelVersion: 'pest-model-v1',
        status: '0',
        remark: undefined
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
      this.ids = selection.map(item => item.taskId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增病虫害识别任务'
    },
    handleUpdate(row) {
      this.reset()
      const taskId = row.taskId || this.ids
      getPestIdentify(taskId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改病虫害识别任务'
      })
    },
    handleFeedback(row) {
      this.feedbackForm = {
        taskId: row.taskId,
        identifyStatus: '1',
        identifyResult: row.identifyResult,
        confidence: row.confidence || 0.9,
        modelVersion: row.modelVersion || 'pest-model-v1'
      }
      this.feedbackOpen = true
    },
    handleInvoke(row) {
      this.processTrace = [
        { stage: '任务入队', status: 'success', time: this.parseTime(new Date()), message: '识别任务已接收。' },
        { stage: '图像预处理', status: 'success', time: this.parseTime(new Date()), message: '图像质量检查和特征提取中。' },
        { stage: '智能识别算法', status: 'running', time: this.parseTime(new Date()), message: '调用模型推理，请稍候...' }
      ]
      this.selectedTask = row
      this.$modal
        .confirm('是否确认调用AI识别任务编号为"' + row.taskId + '"的数据项？')
        .then(() => invokePestIdentify(row.taskId))
        .then(response => {
          const data = response.data || {}
          if (data.process && data.process.length) {
            this.processTrace = data.process
          }
          if (data.aiOriginalExcerpt) {
            this.processTrace.push({
              stage: 'AI原文摘录',
              status: 'success',
              time: this.parseTime(new Date()),
              message: data.aiOriginalExcerpt
            })
          }
          if (data.algorithm) {
            this.algorithm = data.algorithm
          }
          if (data.task) {
            this.selectedTask = data.task
          }
          const message = data.invokeSuccess === false ? (data.message || '识别调用失败') : 'AI调用成功'
          if (data.invokeSuccess === false) {
            this.$message.warning(message)
          } else {
            this.$modal.msgSuccess(message)
          }
          this.getList()
          this.loadDashboard()
          const targetTaskId = (data.task && data.task.taskId) || row.taskId
          if (targetTaskId) {
            this.pollTaskStatus(targetTaskId)
          }
        })
        .catch(() => {})
    },
    handleQuickInvoke() {
      const imageUrl = this.normalizeSingleImage(this.quickForm.imageUrl)
      if (!this.quickForm.plotCode) {
        this.$message.warning('请先填写地块编码')
        return
      }
      if (!this.quickForm.cropName) {
        this.$message.warning('请先填写作物名称')
        return
      }
      if (!imageUrl) {
        this.$message.warning('请先上传识别图片')
        return
      }

      this.quickInvoking = true
      quickInvokePestIdentify({
        plotCode: this.quickForm.plotCode,
        cropName: this.quickForm.cropName,
        imageUrl: imageUrl,
        modelVersion: this.quickForm.modelVersion || 'deepseek-chat',
        status: '0'
      }).then(response => {
        const data = response.data || {}
        if (data.process && data.process.length) {
          this.processTrace = data.process
        }
        if (data.aiOriginalExcerpt) {
          this.processTrace.push({
            stage: 'AI原文摘录',
            status: 'success',
            time: this.parseTime(new Date()),
            message: data.aiOriginalExcerpt
          })
        }
        if (data.algorithm) {
          this.algorithm = data.algorithm
        }
        if (data.task) {
          this.selectedTask = data.task
        }
        const message = data.invokeSuccess === false ? (data.message || '识别调用失败') : '上传并识别成功'
        if (data.invokeSuccess === false) {
          this.$message.warning(message)
        } else {
          this.$modal.msgSuccess(message)
        }
        this.quickForm.imageUrl = ''
        this.getList()
        this.loadDashboard()
        const targetTaskId = data.task && data.task.taskId
        if (targetTaskId) {
          this.pollTaskStatus(targetTaskId)
        }
      }).finally(() => {
        this.quickInvoking = false
      })
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

        // 支持常见图片搜索结果页链接自动提取真实图片地址。
        const isSearchPage =
          pathname.includes('/images/search') ||
          pathname.includes('/imgres') ||
          pathname.includes('/s')

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

      // 开发环境优先直连后端静态资源，避免dev proxy对图片路径解析差异导致“无图像”。
      if (isDev && (normalized.startsWith('/profile/') || normalized.startsWith('/common/download/resource'))) {
        return `${window.location.protocol}//${window.location.hostname}:8080${normalized}`
      }

      if (baseApi && normalized.startsWith(`${baseApi}/`)) {
        return normalized
      }
      if (normalized.startsWith('/profile/') || normalized.startsWith('/common/') || normalized.startsWith('/dev-api/')) {
        if (normalized.startsWith('/dev-api/')) {
          return normalized
        }
        return `${baseApi}${normalized}`
      }
      return `${baseApi}${normalized}`
    },
    resolveImagePreviewList(value) {
      const url = this.resolveImageUrl(value)
      return url ? [url] : []
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.taskId !== undefined) {
          updatePestIdentify(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.loadDashboard()
          })
          return
        }
        addPestIdentify(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.queryParams.pageNum = 1
          this.getList().then(() => {
            const newest = this.taskList && this.taskList.length ? this.taskList[0] : null
            if (newest) {
              this.selectTask(newest)
              this.pollTaskStatus(newest.taskId)
            }
          })
          this.loadDashboard()
        })
      })
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (!valid) {
          return
        }
        feedbackPestIdentify(this.feedbackForm).then(() => {
          this.$modal.msgSuccess('回写成功')
          this.feedbackOpen = false
          this.getList()
          this.loadDashboard()
        })
      })
    },
    handleDelete(row) {
      const taskIds = row.taskId || this.ids
      this.$modal
        .confirm('是否确认删除病虫害识别任务编号为"' + taskIds + '"的数据项？')
        .then(function () {
          return delPestIdentify(taskIds)
        })
        .then(() => {
          this.getList()
          this.loadDashboard()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/pestIdentify/export',
        {
          ...this.queryParams
        },
        `pest_identify_${new Date().getTime()}.xlsx`
      )
    },
    stopStatusPolling() {
      if (this.statusPollingTimer) {
        clearInterval(this.statusPollingTimer)
        this.statusPollingTimer = null
      }
    },
    applyTaskUpdate(task) {
      if (!task || !task.taskId) {
        return
      }
      const index = this.taskList.findIndex(item => item.taskId === task.taskId)
      if (index >= 0) {
        this.$set(this.taskList, index, { ...this.taskList[index], ...task })
      }
      if (this.selectedTask && this.selectedTask.taskId === task.taskId) {
        this.selectTask({ ...this.selectedTask, ...task })
      }
    },
    pollTaskStatus(taskId) {
      if (!taskId) {
        return
      }
      this.stopStatusPolling()

      let rounds = 0
      const maxRounds = 10
      const intervalMs = 2000

      const queryStatus = () => {
        rounds++
        getPestIdentify(taskId).then(response => {
          const task = response.data
          if (!task) {
            return
          }
          this.applyTaskUpdate(task)
          if (task.identifyStatus === '1' || task.identifyStatus === '2') {
            this.stopStatusPolling()
            this.loadDashboard()
          }
        }).finally(() => {
          if (rounds >= maxRounds) {
            this.stopStatusPolling()
            this.getList()
            this.loadDashboard()
          }
        })
      }

      queryStatus()
      this.statusPollingTimer = setInterval(queryStatus, intervalMs)
    }
  }
}
</script>

<style scoped>
.pest-lab {
  background: linear-gradient(165deg, #f4f9f2 0%, #f8fcfa 50%, #ffffff 100%);
}

.query-bar {
  background: #fff;
  border-radius: 8px;
  padding: 10px 12px 2px;
  margin-bottom: 12px;
}

.lab-head {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  padding: 14px;
}

.lab-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f4d38;
}

.lab-subtitle {
  margin-top: 5px;
  color: #688777;
  font-size: 13px;
}

.quick-entry-row {
  margin-top: 12px;
}

.quick-entry {
  border: 1px solid #dceadf;
  border-radius: 10px;
  background: #f7fcf8;
  padding: 12px;
}

.quick-title {
  color: #2f5c45;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
}

.quick-upload-wrap {
  margin-top: 10px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.kpi-row {
  margin-top: 10px;
}

.kpi {
  background: #f5fbf7;
  border: 1px solid #dfeee4;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
}

.kpi-label {
  color: #557666;
  font-size: 12px;
}

.kpi-value {
  margin-top: 6px;
  color: #1f4d38;
  font-size: 22px;
  font-weight: 700;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c4b3a;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
}

.task-tile {
  border: 1px solid #dceadf;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.task-tile:hover,
.task-tile.active {
  border-color: #7ab08d;
  box-shadow: 0 8px 18px rgba(55, 98, 68, 0.12);
}

.tile-image {
  width: 100%;
  height: 150px;
  display: block;
  background: #f2f5f3;
}

.tile-image-fallback {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #95a89d;
}

.tile-meta {
  padding: 10px;
}

.tile-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tile-line {
  margin-top: 4px;
  color: #5f7f6f;
  font-size: 12px;
}

.tile-result {
  margin-top: 8px;
  min-height: 34px;
  color: #334f41;
  font-size: 13px;
  line-height: 1.45;
}

.tile-actions {
  margin-top: 8px;
}

.console-card {
  min-height: 640px;
}

.console-image {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  background: #f2f5f3;
}

.console-title {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 700;
  color: #264c39;
}

.console-line {
  margin-top: 6px;
  font-size: 13px;
  color: #5a7d6c;
}

.tag-wrap {
  margin-top: 8px;
}

.algo-tag {
  margin-right: 6px;
  margin-bottom: 6px;
}

.conclusion {
  margin-top: 10px;
  padding: 8px;
  border-radius: 6px;
  background: #f3faf5;
  color: #2f5442;
  line-height: 1.5;
}

.timeline-title {
  margin-top: 12px;
  margin-bottom: 6px;
  color: #365845;
  font-weight: 600;
}

.node-stage {
  font-weight: 600;
  color: #2f5541;
}

.node-msg {
  color: #587868;
  margin-top: 2px;
}

.ledger-card {
  margin-top: 12px;
}

@media (max-width: 768px) {
  .kpi-value {
    font-size: 18px;
  }
}
</style>
