<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入业务批次号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <el-select v-model="queryParams.dataType" clearable style="width: 140px">
          <el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="链平台" prop="chainPlatform">
        <el-select v-model="queryParams.chainPlatform" clearable style="width: 140px">
          <el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="上链状态" prop="uplinkStatus">
        <el-select v-model="queryParams.uplinkStatus" clearable style="width: 140px">
          <el-option v-for="item in uplinkStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:dataUplink:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-upload2" size="mini" :disabled="single" @click="handleExecuteUplink" v-hasPermi="['agri:dataUplink:edit']">执行上链</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:dataUplink:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:dataUplink:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="uplinkId" align="center" width="70" />
      <el-table-column label="批次号" prop="batchNo" align="center" width="150" />
      <el-table-column label="数据类型" prop="dataType" align="center" width="110" />
      <el-table-column label="数据哈希" prop="dataHash" align="center" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="链平台" prop="chainPlatform" align="center" width="110" />
      <el-table-column label="合约地址" prop="contractAddress" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="交易哈希" prop="txHash" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="上链状态" align="center" width="100">
        <template slot-scope="scope">{{ formatUplinkStatus(scope.row.uplinkStatus) }}</template>
      </el-table-column>
      <el-table-column label="上链时间" prop="uplinkTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.uplinkTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:dataUplink:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:dataUplink:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="820px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="业务批次号" prop="batchNo"><el-input v-model="form.batchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数据类型" prop="dataType"><el-select v-model="form.dataType" style="width: 100%"><el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="数据哈希" prop="dataHash"><el-input v-model="form.dataHash" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="链平台" prop="chainPlatform"><el-select v-model="form.chainPlatform" style="width: 100%"><el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="合约地址" prop="contractAddress"><el-input v-model="form.contractAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上链状态" prop="uplinkStatus"><el-select v-model="form.uplinkStatus" style="width: 100%"><el-option v-for="item in uplinkStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
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
  listDataUplink,
  getDataUplink,
  addDataUplink,
  updateDataUplink,
  executeDataUplink,
  delDataUplink
} from '@/api/agri/dataUplink'

export default {
  name: 'DataUplink',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      taskList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchNo: undefined,
        dataType: undefined,
        chainPlatform: undefined,
        uplinkStatus: undefined
      },
      form: {},
      dataTypeOptions: [
        { label: '物流轨迹', value: 'LOGISTICS_TRACE' },
        { label: '质检报告', value: 'QUALITY_REPORT' },
        { label: '设备数据', value: 'DEVICE_METRIC' },
        { label: '溯源页面', value: 'TRACE_PAGE' }
      ],
      chainPlatformOptions: [
        { label: 'Hyperchain', value: 'HYPERCHAIN' },
        { label: 'FISCO BCOS', value: 'FISCO_BCOS' },
        { label: 'Fabric', value: 'FABRIC' }
      ],
      uplinkStatusOptions: [
        { label: '待上链', value: '0' },
        { label: '已上链', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        batchNo: [{ required: true, message: '业务批次号不能为空', trigger: 'blur' }],
        dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }],
        dataHash: [{ required: true, message: '数据哈希不能为空', trigger: 'blur' }],
        chainPlatform: [{ required: true, message: '链平台不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDataUplink(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatUplinkStatus(value) {
      const option = this.uplinkStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        uplinkId: undefined,
        batchNo: undefined,
        dataType: undefined,
        dataHash: undefined,
        chainPlatform: undefined,
        contractAddress: undefined,
        txHash: undefined,
        uplinkStatus: '0',
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
      this.ids = selection.map(item => item.uplinkId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据上链任务'
    },
    handleUpdate(row) {
      this.reset()
      const uplinkId = row.uplinkId || this.ids
      getDataUplink(uplinkId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改数据上链任务'
      })
    },
    handleExecuteUplink() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条任务执行上链')
        return
      }
      executeDataUplink(this.ids[0]).then(() => {
        this.$modal.msgSuccess('上链执行成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.uplinkId !== undefined) {
          updateDataUplink(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addDataUplink(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const uplinkIds = row.uplinkId || this.ids
      this.$modal
        .confirm('是否确认删除数据上链任务编号为"' + uplinkIds + '"的数据项？')
        .then(function () {
          return delDataUplink(uplinkIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/dataUplink/export',
        {
          ...this.queryParams
        },
        `data_uplink_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
