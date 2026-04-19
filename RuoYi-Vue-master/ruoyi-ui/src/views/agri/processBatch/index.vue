<template>
  <div class="app-container">
    <el-card shadow="never" class="batch-hero mb16">
      <div class="batch-hero-head">
        <div>
          <div class="batch-title">加工批次追踪台</div>
          <div class="batch-desc">串起种植批次、加工批次、产品编码和出料重量，重点查看未完成环节与异常批次。</div>
        </div>
        <div class="batch-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-s-check" @click="handleCheck">批次核验</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="batch-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="batch-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="batch-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="batch-panel">
            <div class="batch-panel-head"><span>流转台账</span><small>关注最近加工状态变化</small></div>
            <el-steps :active="batchStepActive" finish-status="success" simple class="batch-steps">
              <el-step title="待加工" :description="String(pendingCount) + ' 条'" />
              <el-step title="加工中" :description="String(processingCount) + ' 条'" />
              <el-step title="已完成" :description="String(finishedCount) + ' 条'" />
            </el-steps>
            <div class="batch-recent" v-if="recentRows.length">
              <div v-for="item in recentRows" :key="item.linkId" class="batch-recent-item" @click="handleCheck(item)">
                <div>
                  <b>{{ item.processBatchNo }}</b>
                  <p>{{ item.plantingBatchNo }} · {{ item.productCode || '-' }}</p>
                </div>
                <el-tag size="mini">{{ formatProcessStatus(item.processStatus) }}</el-tag>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="batch-panel">
            <div class="batch-panel-head"><span>智能核验</span><small>根据当前批次生成风险提示</small></div>
            <div v-if="smartResult.linkId" class="batch-check-box">
              <div class="batch-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
              <p>{{ smartResult.statusText }}</p>
              <ul><li v-for="item in smartResult.suggestions" :key="item">{{ item }}</li></ul>
            </div>
            <el-empty v-else description="请选择一条批次记录进行核验" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="种植批次号" prop="plantingBatchNo">
        <el-input v-model="queryParams.plantingBatchNo" placeholder="请输入种植批次号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="加工批次号" prop="processBatchNo">
        <el-input v-model="queryParams.processBatchNo" placeholder="请输入加工批次号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="加工状态" prop="processStatus">
        <el-select v-model="queryParams.processStatus" clearable style="width: 140px">
          <el-option v-for="item in processStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:processBatch:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:processBatch:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:processBatch:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:processBatch:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="linkList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="linkId" align="center" width="70" />
      <el-table-column label="种植批次号" prop="plantingBatchNo" align="center" width="150" />
      <el-table-column label="加工批次号" prop="processBatchNo" align="center" width="150" />
      <el-table-column label="产品编码" prop="productCode" align="center" width="120" />
      <el-table-column label="加工重量(kg)" prop="processWeightKg" align="center" width="110" />
      <el-table-column label="加工状态" align="center" width="100">
        <template slot-scope="scope">{{ formatProcessStatus(scope.row.processStatus) }}</template>
      </el-table-column>
      <el-table-column label="加工时间" prop="processTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.processTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-s-check" @click="handleCheck(scope.row)" v-hasPermi="['agri:processBatch:query']">核验</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:processBatch:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:processBatch:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="种植批次号" prop="plantingBatchNo"><el-input v-model="form.plantingBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="加工批次号" prop="processBatchNo"><el-input v-model="form.processBatchNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="加工重量(kg)" prop="processWeightKg"><el-input-number v-model="form.processWeightKg" :precision="2" :step="1" :min="0.01" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="加工状态" prop="processStatus"><el-select v-model="form.processStatus" style="width: 100%"><el-option v-for="item in processStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="加工时间" prop="processTime"><el-date-picker v-model="form.processTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item>
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
  listProcessBatch,
  getProcessBatch,
  addProcessBatch,
  updateProcessBatch,
  delProcessBatch,
  getProcessBatchDashboard,
  getProcessBatchCheck
} from '@/api/agri/processBatch'

export default {
  name: 'ProcessBatch',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      linkList: [],
      dashboardData: {},
      smartResult: {},
      batchFocus: {},
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plantingBatchNo: undefined,
        processBatchNo: undefined,
        processStatus: undefined
      },
      form: {},
      processStatusOptions: [
        { label: '待加工', value: '0' },
        { label: '加工中', value: '1' },
        { label: '已完成', value: '2' }
      ],
      rules: {
        plantingBatchNo: [{ required: true, message: '种植批次号不能为空', trigger: 'blur' }],
        processBatchNo: [{ required: true, message: '加工批次号不能为空', trigger: 'blur' }],
        productCode: [{ required: true, message: '产品编码不能为空', trigger: 'blur' }],
        processWeightKg: [{ required: true, message: '加工重量不能为空', trigger: 'change' }]
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
        { key: 'total', label: '批次总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录' },
        { key: 'planting', label: '种植批次', value: summary.plantingBatchCount || 0, foot: '去重种植批次号' },
        { key: 'process', label: '加工批次', value: summary.processBatchCount || 0, foot: '去重加工批次号' },
        { key: 'weight', label: '累计重量', value: this.formatWeight(summary.totalWeight), foot: '加工出料重量汇总' }
      ]
    },
    recentRows() {
      return this.dashboardData.recentRows || []
    },
    pendingCount() {
      return (this.dashboardData.summary && this.dashboardData.summary.pendingCount) || 0
    },
    processingCount() {
      return (this.dashboardData.summary && this.dashboardData.summary.processingCount) || 0
    },
    finishedCount() {
      return (this.dashboardData.summary && this.dashboardData.summary.finishedCount) || 0
    },
    batchStepActive() {
      if (this.smartResult.statusText === '已完成') return 2
      if (this.smartResult.statusText === '加工中') return 1
      return 0
    }
  },
  methods: {
    refreshDashboard() {
      getProcessBatchDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listProcessBatch(this.queryParams).then(response => {
        this.linkList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatWeight(value) {
      const number = value === undefined || value === null || value === '' ? 0 : Number(value)
      return `${number.toFixed(2)}kg`
    },
    formatProcessStatus(value) {
      const option = this.processStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        linkId: undefined,
        plantingBatchNo: undefined,
        processBatchNo: undefined,
        productCode: undefined,
        processWeightKg: undefined,
        processStatus: '0',
        processTime: undefined,
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
      this.ids = selection.map(item => item.linkId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.handleSelectionChange([row])
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增加工批次关联'
    },
    handleUpdate(row) {
      this.reset()
      const linkId = row.linkId || this.ids
      getProcessBatch(linkId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改加工批次关联'
      })
    },
    handleCheck(row) {
      const target = row || this.linkList[0]
      if (!target || !target.linkId) {
        this.$modal.msgWarning('请先选择一条加工批次')
        return
      }
      this.batchFocus = target
      getProcessBatchCheck(target.linkId).then(response => {
        this.smartResult = response.data || {}
        this.$modal.msgSuccess('批次核验已更新')
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.linkId !== undefined) {
          updateProcessBatch(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addProcessBatch(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const linkIds = row.linkId || this.ids
      this.$modal
        .confirm('是否确认删除加工批次关联编号为"' + linkIds + '"的数据项？')
        .then(function () {
          return delProcessBatch(linkIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/processBatch/export',
        {
          ...this.queryParams
        },
        `process_batch_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.batch-hero {
  border: 1px solid #dbe6ec;
  background: linear-gradient(135deg, #f7fbfd 0%, #eef5fa 100%);
}

.batch-hero-head,
.batch-panel-head,
.batch-recent-item,
.batch-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f4461;
}

.batch-desc {
  margin-top: 6px;
  color: #5d7487;
}

.batch-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.batch-kpi-row,
.batch-overview-row {
  margin-top: 14px;
}

.batch-kpi-card,
.batch-panel {
  border: 1px solid #dfeaf1;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.batch-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.batch-kpi-card span,
.batch-panel-head small,
.batch-kpi-card small,
.batch-recent-item p,
.batch-check-box p {
  color: #637b8f;
}

.batch-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #1f4461;
}

.batch-steps {
  margin-bottom: 14px;
}

.batch-recent-item {
  padding: 8px 0;
  border-top: 1px solid #edf2f6;
  cursor: pointer;
}

.batch-recent-item p {
  margin: 4px 0 0;
}

.batch-score strong {
  font-size: 26px;
  color: #2c6b88;
}

.batch-check-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #4e6679;
}
</style>
