<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="开始日期" prop="beginStatDate">
        <el-date-picker clearable v-model="queryParams.beginStatDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endStatDate">
        <el-date-picker clearable v-model="queryParams.endStatDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="mb8" v-if="dashboardCards">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover"><div class="card-title">总产量(吨)</div><div class="card-value">{{ dashboardCards.totalOutput || 0 }}</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover"><div class="card-title">总销量(万元)</div><div class="card-value">{{ dashboardCards.totalSales || 0 }}</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover"><div class="card-title">预警数量</div><div class="card-value">{{ dashboardCards.warningCount || 0 }}</div></el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover"><div class="card-title">在线设备</div><div class="card-value">{{ dashboardCards.onlineDeviceCount || 0 }}</div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:dashboardOverview:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:dashboardOverview:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:dashboardOverview:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:dashboardOverview:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="overviewList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="overviewId" width="80" />
      <el-table-column label="统计日期" align="center" prop="statDate" width="110" />
      <el-table-column label="总产量(吨)" align="center" prop="totalOutput" width="110" />
      <el-table-column label="总销量(万元)" align="center" prop="totalSales" width="120" />
      <el-table-column label="溯源查询" align="center" prop="traceQueryCount" width="100" />
      <el-table-column label="预警数量" align="center" prop="warningCount" width="90" />
      <el-table-column label="在线设备" align="center" prop="onlineDeviceCount" width="90" />
      <el-table-column label="待办数量" align="center" prop="pendingTaskCount" width="90" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:dashboardOverview:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:dashboardOverview:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="统计日期" prop="statDate"><el-date-picker clearable v-model="form.statDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择统计日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总产量(吨)" prop="totalOutput"><el-input-number v-model="form.totalOutput" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="总销量(万元)" prop="totalSales"><el-input-number v-model="form.totalSales" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="溯源查询" prop="traceQueryCount"><el-input-number v-model="form.traceQueryCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预警数量" prop="warningCount"><el-input-number v-model="form.warningCount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在线设备" prop="onlineDeviceCount"><el-input-number v-model="form.onlineDeviceCount" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="待办数量" prop="pendingTaskCount"><el-input-number v-model="form.pendingTaskCount" :min="0" style="width: 100%" /></el-form-item>
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
  listDashboardOverview,
  getDashboardOverview,
  delDashboardOverview,
  addDashboardOverview,
  updateDashboardOverview
} from '@/api/agri/dashboardOverview'

export default {
  name: 'DashboardOverview',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      overviewList: [],
      dashboardCards: null,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        beginStatDate: null,
        endStatDate: null
      },
      form: {},
      rules: {
        statDate: [{ required: true, message: '统计日期不能为空', trigger: 'change' }],
        totalOutput: [{ required: true, message: '总产量不能为空', trigger: 'blur' }],
        totalSales: [{ required: true, message: '总销量不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDashboardOverview(this.queryParams).then(response => {
        this.overviewList = response.rows
        this.total = response.total
        this.dashboardCards = this.overviewList.length > 0 ? this.overviewList[0] : null
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        overviewId: null,
        statDate: null,
        totalOutput: 0,
        totalSales: 0,
        traceQueryCount: 0,
        warningCount: 0,
        onlineDeviceCount: 0,
        pendingTaskCount: 0,
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
      this.ids = selection.map(item => item.overviewId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据总览看板'
    },
    handleUpdate(row) {
      this.reset()
      const overviewId = row.overviewId || this.ids[0]
      getDashboardOverview(overviewId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改数据总览看板'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.overviewId != null) {
            updateDashboardOverview(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addDashboardOverview(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const overviewIds = row.overviewId || this.ids
      this.$modal.confirm('是否确认删除数据总览看板编号为"' + overviewIds + '"的数据项？').then(() => {
        return delDashboardOverview(overviewIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/dashboardOverview/export', {
        ...this.queryParams
      }, `dashboard_overview_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.card-title {
  color: #606266;
  font-size: 13px;
}
.card-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
</style>
