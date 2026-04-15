<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="运单号" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入运单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input v-model="queryParams.deviceCode" placeholder="请输入设备编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="告警标记" prop="alertFlag">
        <el-select v-model="queryParams.alertFlag" clearable style="width: 140px">
          <el-option v-for="item in alertOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:logisticsTemp:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:logisticsTemp:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:logisticsTemp:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:logisticsTemp:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="recordId" align="center" width="70" />
      <el-table-column label="运单号" prop="traceCode" align="center" width="150" />
      <el-table-column label="设备编码" prop="deviceCode" align="center" width="130" />
      <el-table-column label="采集位置" prop="collectLocation" align="center" width="140" />
      <el-table-column label="温度" prop="temperature" align="center" width="90" />
      <el-table-column label="湿度" prop="humidity" align="center" width="90" />
      <el-table-column label="告警" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.alertFlag === '1' ? 'danger' : 'success'">{{ formatAlert(scope.row.alertFlag) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="告警信息" prop="alertMessage" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="采集时间" prop="collectTime" align="center" width="165">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.collectTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:logisticsTemp:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:logisticsTemp:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="运单号" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采集位置" prop="collectLocation"><el-input v-model="form.collectLocation" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度(°C)" prop="temperature"><el-input-number v-model="form.temperature" :precision="2" :step="0.1" :min="-40" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度(%)" prop="humidity"><el-input-number v-model="form.humidity" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度上限" prop="tempUpperLimit"><el-input-number v-model="form.tempUpperLimit" :precision="2" :step="0.1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="温度下限" prop="tempLowerLimit"><el-input-number v-model="form.tempLowerLimit" :precision="2" :step="0.1" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="湿度上限" prop="humidityUpperLimit"><el-input-number v-model="form.humidityUpperLimit" :precision="2" :step="0.1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度下限" prop="humidityLowerLimit"><el-input-number v-model="form.humidityLowerLimit" :precision="2" :step="0.1" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="采集时间" prop="collectTime"><el-date-picker v-model="form.collectTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item>
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
  listLogisticsTemp,
  getLogisticsTemp,
  addLogisticsTemp,
  updateLogisticsTemp,
  delLogisticsTemp
} from '@/api/agri/logisticsTemp'

export default {
  name: 'LogisticsTemp',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      recordList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        deviceCode: undefined,
        alertFlag: undefined
      },
      form: {},
      alertOptions: [
        { label: '正常', value: '0' },
        { label: '超阈值', value: '1' }
      ],
      rules: {
        traceCode: [{ required: true, message: '运单号不能为空', trigger: 'blur' }],
        orderNo: [{ required: true, message: '订单号不能为空', trigger: 'blur' }],
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        temperature: [{ required: true, message: '温度不能为空', trigger: 'change' }],
        humidity: [{ required: true, message: '湿度不能为空', trigger: 'change' }],
        collectTime: [{ required: true, message: '采集时间不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listLogisticsTemp(this.queryParams).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatAlert(value) {
      const option = this.alertOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        recordId: undefined,
        traceCode: undefined,
        orderNo: undefined,
        deviceCode: undefined,
        collectLocation: undefined,
        temperature: undefined,
        humidity: undefined,
        tempUpperLimit: 8,
        tempLowerLimit: 2,
        humidityUpperLimit: 75,
        humidityLowerLimit: 45,
        alertFlag: '0',
        alertMessage: undefined,
        collectTime: undefined,
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
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增温湿度监控'
    },
    handleUpdate(row) {
      this.reset()
      const recordId = row.recordId || this.ids
      getLogisticsTemp(recordId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改温湿度监控'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.recordId !== undefined) {
          updateLogisticsTemp(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addLogisticsTemp(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal
        .confirm('是否确认删除温湿度监控编号为"' + recordIds + '"的数据项？')
        .then(function () {
          return delLogisticsTemp(recordIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/logisticsTemp/export',
        {
          ...this.queryParams
        },
        `logistics_temp_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
