<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input v-model="queryParams.deviceCode" placeholder="请输入设备编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="在线状态" prop="onlineStatus">
        <el-select v-model="queryParams.onlineStatus" placeholder="在线状态" clearable>
          <el-option label="在线" value="1" />
          <el-option label="离线" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警等级" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="预警等级" clearable>
          <el-option label="正常" value="0" />
          <el-option label="提示" value="1" />
          <el-option label="预警" value="2" />
          <el-option label="严重" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:deviceStatusMonitor:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:deviceStatusMonitor:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:deviceStatusMonitor:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:deviceStatusMonitor:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="monitorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="monitorId" width="80" />
      <el-table-column label="设备编码" align="center" prop="deviceCode" width="140" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="140" />
      <el-table-column label="在线状态" align="center" prop="onlineStatus" width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.onlineStatus === '1' ? 'success' : 'info'">{{ scope.row.onlineStatus === '1' ? '在线' : '离线' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="电量(%)" align="center" prop="batteryLevel" width="90" />
      <el-table-column label="信号" align="center" prop="signalStrength" width="80" />
      <el-table-column label="温度(℃)" align="center" prop="temperature" width="90" />
      <el-table-column label="湿度(%)" align="center" prop="humidity" width="90" />
      <el-table-column label="预警等级" align="center" prop="warningLevel" width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="warningTagType(scope.row.warningLevel)">{{ warningLabel(scope.row.warningLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后上报时间" align="center" prop="lastReportTime" width="170" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:deviceStatusMonitor:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:deviceStatusMonitor:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" placeholder="请输入设备编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="deviceName"><el-input v-model="form.deviceName" placeholder="请输入设备名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="设备类型" prop="deviceType"><el-input v-model="form.deviceType" placeholder="请输入设备类型" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在线状态" prop="onlineStatus"><el-select v-model="form.onlineStatus" placeholder="请选择在线状态" style="width: 100%"><el-option label="在线" value="1" /><el-option label="离线" value="0" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="电量(%)" prop="batteryLevel"><el-input-number v-model="form.batteryLevel" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="信号强度" prop="signalStrength"><el-input-number v-model="form.signalStrength" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度(℃)" prop="temperature"><el-input-number v-model="form.temperature" :precision="2" :min="-50" :max="80" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度(%)" prop="humidity"><el-input-number v-model="form.humidity" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警等级" prop="warningLevel"><el-select v-model="form.warningLevel" placeholder="请选择预警等级" style="width: 100%"><el-option label="正常" value="0" /><el-option label="提示" value="1" /><el-option label="预警" value="2" /><el-option label="严重" value="3" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上报时间" prop="lastReportTime"><el-date-picker clearable v-model="form.lastReportTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择上报时间" style="width: 100%" /></el-form-item></el-col>
        </el-row>
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
  listDeviceStatusMonitor,
  getDeviceStatusMonitor,
  delDeviceStatusMonitor,
  addDeviceStatusMonitor,
  updateDeviceStatusMonitor
} from '@/api/agri/deviceStatusMonitor'

export default {
  name: 'DeviceStatusMonitor',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      monitorList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceCode: null,
        deviceName: null,
        onlineStatus: null,
        warningLevel: null
      },
      form: {},
      rules: {
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
        onlineStatus: [{ required: true, message: '在线状态不能为空', trigger: 'change' }],
        batteryLevel: [{ required: true, message: '电量不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDeviceStatusMonitor(this.queryParams).then(response => {
        this.monitorList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    warningLabel(level) {
      const map = { '0': '正常', '1': '提示', '2': '预警', '3': '严重' }
      return map[level] || '未知'
    },
    warningTagType(level) {
      const map = { '0': 'success', '1': 'warning', '2': 'danger', '3': 'danger' }
      return map[level] || 'info'
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        monitorId: null,
        deviceCode: null,
        deviceName: null,
        deviceType: null,
        onlineStatus: '1',
        batteryLevel: 100,
        signalStrength: 80,
        temperature: 25,
        humidity: 60,
        warningLevel: '0',
        lastReportTime: null,
        remark: null
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
      this.ids = selection.map(item => item.monitorId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增设备状态监控'
    },
    handleUpdate(row) {
      this.reset()
      const monitorId = row.monitorId || this.ids[0]
      getDeviceStatusMonitor(monitorId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改设备状态监控'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.monitorId != null) {
            updateDeviceStatusMonitor(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addDeviceStatusMonitor(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const monitorIds = row.monitorId || this.ids
      this.$modal.confirm('是否确认删除设备状态监控编号为"' + monitorIds + '"的数据项？').then(() => {
        return delDeviceStatusMonitor(monitorIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/deviceStatusMonitor/export', {
        ...this.queryParams
      }, `device_status_monitor_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
