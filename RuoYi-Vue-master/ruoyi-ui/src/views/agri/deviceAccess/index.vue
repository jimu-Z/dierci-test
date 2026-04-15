<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input v-model="queryParams.deviceCode" placeholder="请输入设备编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select v-model="queryParams.deviceType" clearable style="width: 140px">
          <el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="接入状态" prop="accessStatus">
        <el-select v-model="queryParams.accessStatus" clearable style="width: 140px">
          <el-option v-for="item in accessStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:deviceAccess:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-video-play" size="mini" :disabled="single" @click="handleActivate" v-hasPermi="['agri:deviceAccess:edit']">激活</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:deviceAccess:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:deviceAccess:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="nodeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="nodeId" align="center" width="70" />
      <el-table-column label="设备编码" prop="deviceCode" align="center" width="160" />
      <el-table-column label="设备名称" prop="deviceName" align="center" width="130" />
      <el-table-column label="设备类型" prop="deviceType" align="center" width="100" />
      <el-table-column label="接入协议" prop="protocolType" align="center" width="110" />
      <el-table-column label="固件版本" prop="firmwareVersion" align="center" width="100" />
      <el-table-column label="绑定区域" prop="bindArea" align="center" width="130" />
      <el-table-column label="接入状态" align="center" width="100">
        <template slot-scope="scope">{{ formatAccessStatus(scope.row.accessStatus) }}</template>
      </el-table-column>
      <el-table-column label="最近在线时间" prop="lastOnlineTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.lastOnlineTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:deviceAccess:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:deviceAccess:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="deviceName"><el-input v-model="form.deviceName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="设备类型" prop="deviceType"><el-select v-model="form.deviceType" style="width: 100%"><el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接入协议" prop="protocolType"><el-select v-model="form.protocolType" style="width: 100%"><el-option v-for="item in protocolTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="固件版本" prop="firmwareVersion"><el-input v-model="form.firmwareVersion" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="绑定区域" prop="bindArea"><el-input v-model="form.bindArea" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="接入状态" prop="accessStatus"><el-select v-model="form.accessStatus" style="width: 100%"><el-option v-for="item in accessStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item></el-col>
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
  listDeviceAccess,
  getDeviceAccess,
  addDeviceAccess,
  updateDeviceAccess,
  activateDeviceAccess,
  delDeviceAccess
} from '@/api/agri/deviceAccess'

export default {
  name: 'DeviceAccess',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      nodeList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceCode: undefined,
        deviceName: undefined,
        deviceType: undefined,
        accessStatus: undefined
      },
      form: {},
      deviceTypeOptions: [
        { label: '温湿度传感器', value: 'TEMP_HUMIDITY' },
        { label: '摄像头', value: 'CAMERA' },
        { label: '网关', value: 'GATEWAY' },
        { label: 'RFID终端', value: 'RFID' }
      ],
      protocolTypeOptions: [
        { label: 'MQTT', value: 'MQTT' },
        { label: 'HTTP', value: 'HTTP' },
        { label: 'CoAP', value: 'COAP' },
        { label: 'Modbus', value: 'MODBUS' }
      ],
      accessStatusOptions: [
        { label: '待接入', value: '0' },
        { label: '在线', value: '1' },
        { label: '离线', value: '2' },
        { label: '异常', value: '3' }
      ],
      rules: {
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
        deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
        protocolType: [{ required: true, message: '接入协议不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDeviceAccess(this.queryParams).then(response => {
        this.nodeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatAccessStatus(value) {
      const option = this.accessStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        nodeId: undefined,
        deviceCode: undefined,
        deviceName: undefined,
        deviceType: undefined,
        protocolType: undefined,
        firmwareVersion: undefined,
        bindArea: undefined,
        accessStatus: '0',
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
      this.ids = selection.map(item => item.nodeId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增设备接入节点'
    },
    handleUpdate(row) {
      this.reset()
      const nodeId = row.nodeId || this.ids
      getDeviceAccess(nodeId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改设备接入节点'
      })
    },
    handleActivate() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条记录进行激活')
        return
      }
      activateDeviceAccess(this.ids[0]).then(() => {
        this.$modal.msgSuccess('设备激活成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.nodeId !== undefined) {
          updateDeviceAccess(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addDeviceAccess(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const nodeIds = row.nodeId || this.ids
      this.$modal
        .confirm('是否确认删除设备接入节点编号为"' + nodeIds + '"的数据项？')
        .then(function () {
          return delDeviceAccess(nodeIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/deviceAccess/export',
        {
          ...this.queryParams
        },
        `device_access_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
