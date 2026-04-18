<template>
  <div class="app-container">
    <el-card shadow="never" class="warning-hero mb16">
      <div class="warning-head">
        <div>
          <div class="warning-title">在途预警处置台</div>
          <div class="warning-desc">用于追踪告警级别、处置状态和责任人，优先处理紧急和待分派告警。</div>
        </div>
        <div class="warning-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-s-opportunity" @click="handleTriage">智能分诊</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="warning-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="warning-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="warning-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="warning-panel">
            <div class="panel-head"><span>近期预警</span><small>优先看高等级未处理项</small></div>
            <div class="warning-list" v-if="recentWarnings.length">
              <div v-for="item in recentWarnings" :key="item.warningId" class="warning-item" @click="handleTriage(item)">
                <div>
                  <b>{{ item.warningTitle }}</b>
                  <p>{{ item.traceCode }} · {{ item.warningType }}</p>
                </div>
                <el-tag size="mini" :type="formatLevelType(item.warningLevel)">{{ formatWarningLevel(item.warningLevel) }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无预警记录" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="warning-panel">
            <div class="panel-head"><span>智能分诊</span><small>针对当前预警生成处置建议</small></div>
            <div v-if="smartResult.warningId" class="warning-box">
              <div class="warning-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
              <p>{{ smartResult.warning.warningTitle }}</p>
              <ul>
                <li v-for="item in smartResult.suggestions" :key="item">{{ item }}</li>
              </ul>
            </div>
            <el-empty v-else description="请选择一条预警进行分诊" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="运单号" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入运单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="预警级别" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" clearable style="width: 140px">
          <el-option v-for="item in warningLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警状态" prop="warningStatus">
        <el-select v-model="queryParams.warningStatus" clearable style="width: 140px">
          <el-option v-for="item in warningStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:logisticsWarning:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-warning" size="mini" @click="handleGenerate" v-hasPermi="['agri:logisticsWarning:add']">告警生成</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:logisticsWarning:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:logisticsWarning:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:logisticsWarning:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="warningList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="warningId" align="center" width="70" />
      <el-table-column label="运单号" prop="traceCode" align="center" width="150" />
      <el-table-column label="预警类型" prop="warningType" align="center" width="120" />
      <el-table-column label="预警级别" align="center" width="90">
        <template slot-scope="scope"><el-tag :type="formatLevelType(scope.row.warningLevel)">{{ formatWarningLevel(scope.row.warningLevel) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="预警状态" align="center" width="90">
        <template slot-scope="scope">{{ formatWarningStatus(scope.row.warningStatus) }}</template>
      </el-table-column>
      <el-table-column label="标题" prop="warningTitle" align="center" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="内容" prop="warningContent" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="预警时间" prop="warningTime" align="center" width="165">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.warningTime) }}</span></template>
      </el-table-column>
      <el-table-column label="处理人" prop="handler" align="center" width="90" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-s-opportunity" @click="handleTriage(scope.row)" v-hasPermi="['agri:logisticsWarning:query']">分诊</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:logisticsWarning:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:logisticsWarning:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="880px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="运单号" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警类型" prop="warningType"><el-input v-model="form.warningType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预警级别" prop="warningLevel"><el-select v-model="form.warningLevel" style="width: 100%"><el-option v-for="item in warningLevelOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警状态" prop="warningStatus"><el-select v-model="form.warningStatus" style="width: 100%"><el-option v-for="item in warningStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源记录ID" prop="sourceRecordId"><el-input-number v-model="form.sourceRecordId" :min="1" :step="1" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="预警标题" prop="warningTitle"><el-input v-model="form.warningTitle" /></el-form-item>
        <el-form-item label="预警内容" prop="warningContent"><el-input v-model="form.warningContent" type="textarea" :rows="3" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="预警时间" prop="warningTime"><el-date-picker v-model="form.warningTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="处理时间" prop="handleTime"><el-date-picker v-model="form.handleTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="处理人" prop="handler"><el-input v-model="form.handler" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="处理备注" prop="handleRemark"><el-input v-model="form.handleRemark" /></el-form-item></el-col>
        </el-row>
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
  listLogisticsWarning,
  getLogisticsWarning,
  addLogisticsWarning,
  updateLogisticsWarning,
  generateLogisticsWarning,
  delLogisticsWarning,
  getLogisticsWarningDashboard,
  triageLogisticsWarning
} from '@/api/agri/logisticsWarning'

export default {
  name: 'LogisticsWarning',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      warningList: [],
      dashboardData: {},
      smartResult: {},
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        warningLevel: undefined,
        warningStatus: undefined
      },
      form: {},
      warningLevelOptions: [
        { label: '一般', value: '1' },
        { label: '严重', value: '2' },
        { label: '紧急', value: '3' }
      ],
      warningStatusOptions: [
        { label: '待处理', value: '0' },
        { label: '处理中', value: '1' },
        { label: '已关闭', value: '2' }
      ],
      rules: {
        traceCode: [{ required: true, message: '运单号不能为空', trigger: 'blur' }],
        orderNo: [{ required: true, message: '订单号不能为空', trigger: 'blur' }],
        warningType: [{ required: true, message: '预警类型不能为空', trigger: 'blur' }],
        warningLevel: [{ required: true, message: '预警级别不能为空', trigger: 'change' }],
        warningTitle: [{ required: true, message: '预警标题不能为空', trigger: 'blur' }],
        warningContent: [{ required: true, message: '预警内容不能为空', trigger: 'blur' }],
        warningTime: [{ required: true, message: '预警时间不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.refreshDashboard()
    this.getList()
  },
  computed: {
    dashboardCards() {
      const summary = this.dashboardData.summary || {}
      return [
        { key: 'total', label: '预警总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录' },
        { key: 'pending', label: '待处理', value: summary.pendingCount || 0, foot: '待分派记录' },
        { key: 'processing', label: '处理中', value: summary.processingCount || 0, foot: '已开始处置记录' },
        { key: 'closed', label: '已关闭', value: summary.closedCount || 0, foot: '已闭环记录' },
        { key: 'urgent', label: '紧急预警', value: summary.urgentCount || 0, foot: '三级及以上' }
      ]
    },
    recentWarnings() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    refreshDashboard() {
      getLogisticsWarningDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listLogisticsWarning(this.queryParams).then(response => {
        this.warningList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatWarningLevel(value) {
      const option = this.warningLevelOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    formatLevelType(value) {
      if (value === '3') {
        return 'danger'
      }
      if (value === '2') {
        return 'warning'
      }
      return 'info'
    },
    formatWarningStatus(value) {
      const option = this.warningStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        warningId: undefined,
        traceCode: undefined,
        orderNo: undefined,
        warningType: 'TEMP_HUMIDITY',
        warningLevel: '1',
        warningStatus: '0',
        sourceRecordId: undefined,
        warningTitle: undefined,
        warningContent: undefined,
        warningTime: undefined,
        handler: undefined,
        handleTime: undefined,
        handleRemark: undefined,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.refreshDashboard()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.refreshDashboard()
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.warningId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增在途异常预警'
    },
    handleGenerate() {
      this.$prompt('请输入温湿度记录ID', '一键生成预警', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '记录ID必须为正整数'
      }).then(({ value }) => {
        generateLogisticsWarning(value).then(() => {
          this.$modal.msgSuccess('预警生成成功')
          this.getList()
        })
      }).catch(() => {})
    },
    handleTriage(row) {
      const target = row || this.warningList[0]
      if (!target || !target.warningId) {
        this.$modal.msgWarning('请先选择一条预警记录')
        return
      }
      triageLogisticsWarning(target.warningId).then(response => {
        this.smartResult = response.data || {}
        this.$modal.msgSuccess('智能分诊已更新')
      })
    },
    handleUpdate(row) {
      this.reset()
      const warningId = row.warningId || this.ids
      getLogisticsWarning(warningId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改在途异常预警'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.warningId !== undefined) {
          updateLogisticsWarning(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addLogisticsWarning(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const warningIds = row.warningId || this.ids
      this.$modal
        .confirm('是否确认删除在途异常预警编号为"' + warningIds + '"的数据项？')
        .then(function () {
          return delLogisticsWarning(warningIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/logisticsWarning/export',
        {
          ...this.queryParams
        },
        `logistics_warning_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.warning-hero {
  border: 1px solid #eaded6;
  background: linear-gradient(135deg, #fff9f6 0%, #f7efea 100%);
}

.warning-head,
.panel-head,
.warning-item,
.warning-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.warning-title {
  font-size: 20px;
  font-weight: 700;
  color: #7c3829;
}

.warning-desc {
  margin-top: 6px;
  color: #8d6a5d;
}

.warning-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.warning-kpi-row,
.warning-overview-row {
  margin-top: 14px;
}

.warning-kpi-card,
.warning-panel {
  border: 1px solid #ecdcd4;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.warning-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.warning-kpi-card span,
.panel-head small,
.warning-kpi-card small,
.warning-item p,
.warning-box p {
  color: #8b7366;
}

.warning-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #7c3829;
}

.warning-item {
  padding: 8px 0;
  border-top: 1px solid #f5ece7;
  cursor: pointer;
}

.warning-item p {
  margin: 4px 0 0;
}

.warning-score strong {
  font-size: 26px;
  color: #b14a35;
}

.warning-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #6e564c;
}
</style>
