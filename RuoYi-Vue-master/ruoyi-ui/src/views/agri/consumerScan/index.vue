<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="溯源码" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入溯源码" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="消费者姓名" prop="consumerName">
        <el-input v-model="queryParams.consumerName" placeholder="请输入消费者姓名" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="扫码渠道" prop="scanChannel">
        <el-select v-model="queryParams.scanChannel" clearable style="width: 140px">
          <el-option v-for="item in scanChannelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="查询结果" prop="scanResult">
        <el-select v-model="queryParams.scanResult" clearable style="width: 140px">
          <el-option v-for="item in scanResultOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:consumerScan:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-camera" size="mini" @click="handleMockScan">扫码模拟</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:consumerScan:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:consumerScan:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="queryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="queryId" align="center" width="70" />
      <el-table-column label="溯源码" prop="traceCode" align="center" width="180" />
      <el-table-column label="消费者姓名" prop="consumerName" align="center" width="120" />
      <el-table-column label="消费者手机号" prop="consumerPhone" align="center" width="130" />
      <el-table-column label="扫码渠道" prop="scanChannel" align="center" width="100" />
      <el-table-column label="查询结果" align="center" width="120">
        <template slot-scope="scope">{{ formatScanResult(scope.row.scanResult) }}</template>
      </el-table-column>
      <el-table-column label="品牌" prop="brandName" align="center" width="110" />
      <el-table-column label="产品" prop="productName" align="center" width="110" />
      <el-table-column label="扫码IP" prop="scanIp" align="center" width="130" />
      <el-table-column label="查询时间" prop="queryTime" align="center" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.queryTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:consumerScan:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:consumerScan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="溯源码" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="查询结果" prop="scanResult"><el-select v-model="form.scanResult" style="width: 100%"><el-option v-for="item in scanResultOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="消费者姓名" prop="consumerName"><el-input v-model="form.consumerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="消费者手机号" prop="consumerPhone"><el-input v-model="form.consumerPhone" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="扫码渠道" prop="scanChannel"><el-select v-model="form.scanChannel" style="width: 100%"><el-option v-for="item in scanChannelOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="扫码地址" prop="scanAddress"><el-input v-model="form.scanAddress" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="扫码IP" prop="scanIp"><el-input v-model="form.scanIp" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="扫码查询结果" :visible.sync="scanResultOpen" width="760px" append-to-body>
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
      <div slot="footer" class="dialog-footer">
        <el-button @click="scanResultOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listConsumerScan,
  getConsumerScan,
  addConsumerScan,
  updateConsumerScan,
  scanConsumerTrace,
  delConsumerScan
} from '@/api/agri/consumerScan'

export default {
  name: 'ConsumerScan',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      queryList: [],
      title: '',
      open: false,
      scanResultOpen: false,
      scanResultData: null,
      scanResultMessage: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        consumerName: undefined,
        scanChannel: undefined,
        scanResult: undefined
      },
      form: {},
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
      rules: {
        traceCode: [{ required: true, message: '溯源码不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listConsumerScan(this.queryParams).then(response => {
        this.queryList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatScanResult(value) {
      const option = this.scanResultOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        queryId: undefined,
        traceCode: undefined,
        consumerName: undefined,
        consumerPhone: undefined,
        scanChannel: 'WECHAT',
        scanAddress: undefined,
        scanIp: undefined,
        scanResult: '0',
        status: '0',
        queryTime: undefined,
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
      this.ids = selection.map(item => item.queryId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增消费者扫码查询'
    },
    handleUpdate(row) {
      this.reset()
      const queryId = row.queryId || this.ids
      getConsumerScan(queryId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改消费者扫码查询'
      })
    },
    handleMockScan() {
      this.$prompt('请输入要查询的溯源码', '扫码模拟查询', {
        confirmButtonText: '查询',
        cancelButtonText: '取消',
        inputPlaceholder: '例如 TRACE-202607-APPLE-001'
      })
        .then(({ value }) => {
          if (!value || !value.trim()) {
            this.$modal.msgWarning('溯源码不能为空')
            return
          }
          return scanConsumerTrace(value.trim(), {
            scanChannel: 'WECHAT',
            consumerName: '系统模拟用户',
            remark: '后台页面扫码模拟'
          })
        })
        .then(response => {
          if (!response) {
            return
          }
          this.scanResultData = response.data
          this.scanResultMessage = '扫码成功，已命中并返回已发布溯源页面。'
          this.scanResultOpen = true
          this.getList()
        })
        .catch(error => {
          if (error && error.message) {
            this.scanResultData = null
            this.scanResultMessage = error.message
            this.scanResultOpen = true
            this.getList()
          }
        })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.queryId !== undefined) {
          updateConsumerScan(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addConsumerScan(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const queryIds = row.queryId || this.ids
      this.$modal
        .confirm('是否确认删除消费者扫码查询编号为"' + queryIds + '"的数据项？')
        .then(function () {
          return delConsumerScan(queryIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
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
    }
  }
}
</script>
