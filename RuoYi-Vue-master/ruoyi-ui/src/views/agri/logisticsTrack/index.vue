<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="运单号" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入运单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="运输状态" prop="trackStatus">
        <el-select v-model="queryParams.trackStatus" clearable style="width: 140px">
          <el-option v-for="item in trackStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:logisticsTrack:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-view" size="mini" :disabled="single" @click="handleTimeline" v-hasPermi="['agri:logisticsTrack:query']">轨迹回放</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:logisticsTrack:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:logisticsTrack:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="trackList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="trackId" align="center" width="70" />
      <el-table-column label="运单号" prop="traceCode" align="center" width="160" />
      <el-table-column label="订单号" prop="orderNo" align="center" width="150" />
      <el-table-column label="产品批次号" prop="productBatchNo" align="center" width="140" />
      <el-table-column label="当前位置" prop="currentLocation" align="center" width="140" />
      <el-table-column label="运输状态" align="center" width="100">
        <template slot-scope="scope">{{ formatTrackStatus(scope.row.trackStatus) }}</template>
      </el-table-column>
      <el-table-column label="温度" prop="temperature" align="center" width="80" />
      <el-table-column label="湿度" prop="humidity" align="center" width="80" />
      <el-table-column label="事件时间" prop="eventTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.eventTime) }}</span></template>
      </el-table-column>
      <el-table-column label="轨迹描述" prop="eventDesc" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:logisticsTrack:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:logisticsTrack:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="运单号" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="销售订单号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产品批次号" prop="productBatchNo"><el-input v-model="form.productBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="运输状态" prop="trackStatus"><el-select v-model="form.trackStatus" style="width: 100%"><el-option v-for="item in trackStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="起始地" prop="startLocation"><el-input v-model="form.startLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="当前位置" prop="currentLocation"><el-input v-model="form.currentLocation" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="目的地" prop="targetLocation"><el-input v-model="form.targetLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路线轨迹" prop="routePath"><el-input v-model="form.routePath" placeholder="如：基地->仓储->门店" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="车辆编号" prop="vehicleNo"><el-input v-model="form.vehicleNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="司机姓名" prop="driverName"><el-input v-model="form.driverName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="司机电话" prop="driverPhone"><el-input v-model="form.driverPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="事件时间" prop="eventTime"><el-date-picker v-model="form.eventTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="温度(°C)" prop="temperature"><el-input-number v-model="form.temperature" :precision="2" :step="0.1" :min="-40" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="湿度(%)" prop="humidity"><el-input-number v-model="form.humidity" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="经度" prop="longitude"><el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="纬度" prop="latitude"><el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="轨迹描述" prop="eventDesc"><el-input v-model="form.eventDesc" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="轨迹回放" :visible.sync="timelineOpen" width="860px" append-to-body>
      <el-table :data="timelineList">
        <el-table-column label="事件时间" prop="eventTime" align="center" width="170">
          <template slot-scope="scope"><span>{{ parseTime(scope.row.eventTime) }}</span></template>
        </el-table-column>
        <el-table-column label="位置" prop="currentLocation" align="center" width="180" />
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">{{ formatTrackStatus(scope.row.trackStatus) }}</template>
        </el-table-column>
        <el-table-column label="温度" prop="temperature" align="center" width="90" />
        <el-table-column label="湿度" prop="humidity" align="center" width="90" />
        <el-table-column label="描述" prop="eventDesc" align="center" :show-overflow-tooltip="true" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="timelineOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listLogisticsTrack,
  getLogisticsTrack,
  addLogisticsTrack,
  updateLogisticsTrack,
  delLogisticsTrack,
  listLogisticsTimeline
} from '@/api/agri/logisticsTrack'

export default {
  name: 'LogisticsTrack',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      trackList: [],
      timelineList: [],
      title: '',
      open: false,
      timelineOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        orderNo: undefined,
        trackStatus: undefined
      },
      form: {},
      trackStatusOptions: [
        { label: '待发车', value: '0' },
        { label: '运输中', value: '1' },
        { label: '已签收', value: '2' },
        { label: '异常', value: '3' }
      ],
      rules: {
        traceCode: [{ required: true, message: '运单号不能为空', trigger: 'blur' }],
        orderNo: [{ required: true, message: '销售订单号不能为空', trigger: 'blur' }],
        productBatchNo: [{ required: true, message: '产品批次号不能为空', trigger: 'blur' }],
        currentLocation: [{ required: true, message: '当前位置不能为空', trigger: 'blur' }],
        trackStatus: [{ required: true, message: '运输状态不能为空', trigger: 'change' }],
        eventTime: [{ required: true, message: '事件时间不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listLogisticsTrack(this.queryParams).then(response => {
        this.trackList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatTrackStatus(value) {
      const option = this.trackStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        trackId: undefined,
        traceCode: undefined,
        orderNo: undefined,
        productBatchNo: undefined,
        vehicleNo: undefined,
        driverName: undefined,
        driverPhone: undefined,
        startLocation: undefined,
        currentLocation: undefined,
        targetLocation: undefined,
        routePath: undefined,
        trackStatus: '0',
        eventDesc: undefined,
        eventTime: undefined,
        temperature: undefined,
        humidity: undefined,
        longitude: undefined,
        latitude: undefined,
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
      this.ids = selection.map(item => item.trackId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增物流路径追踪'
    },
    handleUpdate(row) {
      this.reset()
      const trackId = row.trackId || this.ids
      getLogisticsTrack(trackId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改物流路径追踪'
      })
    },
    handleTimeline() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条物流记录查看轨迹')
        return
      }
      const selected = this.trackList.find(item => item.trackId === this.ids[0])
      if (!selected || !selected.traceCode) {
        this.$modal.msgWarning('所选记录缺少运单号')
        return
      }
      listLogisticsTimeline(selected.traceCode).then(response => {
        this.timelineList = response.data || []
        this.timelineOpen = true
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.trackId !== undefined) {
          updateLogisticsTrack(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addLogisticsTrack(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const trackIds = row.trackId || this.ids
      this.$modal
        .confirm('是否确认删除物流路径追踪编号为"' + trackIds + '"的数据项？')
        .then(function () {
          return delLogisticsTrack(trackIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/logisticsTrack/export',
        {
          ...this.queryParams
        },
        `logistics_track_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
