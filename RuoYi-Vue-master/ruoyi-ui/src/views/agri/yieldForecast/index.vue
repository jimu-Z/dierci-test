<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="地块编码" prop="plotCode">
        <el-input v-model="queryParams.plotCode" placeholder="请输入地块编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="作物名称" prop="cropName">
        <el-input v-model="queryParams.cropName" placeholder="请输入作物名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="预测状态" prop="forecastStatus">
        <el-select v-model="queryParams.forecastStatus" clearable style="width: 140px">
          <el-option v-for="item in forecastStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:yieldForecast:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:yieldForecast:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:yieldForecast:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:yieldForecast:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="forecastId" align="center" width="70" />
      <el-table-column label="地块" prop="plotCode" align="center" width="110" />
      <el-table-column label="作物" prop="cropName" align="center" width="110" />
      <el-table-column label="季节" prop="season" align="center" width="90" />
      <el-table-column label="面积(亩)" prop="areaMu" align="center" width="90" />
      <el-table-column label="预测产量(kg)" prop="forecastYieldKg" align="center" width="110" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">{{ formatForecastStatus(scope.row.forecastStatus) }}</template>
      </el-table-column>
      <el-table-column label="模型版本" prop="modelVersion" align="center" width="100" />
      <el-table-column label="预测时间" prop="forecastTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.forecastTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="210">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:yieldForecast:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-data-analysis" @click="handlePredict(scope.row)" v-hasPermi="['agri:yieldForecast:edit']">执行预测</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:yieldForecast:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12"><el-form-item label="地块编码" prop="plotCode"><el-input v-model="form.plotCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作物名称" prop="cropName"><el-input v-model="form.cropName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="季节" prop="season"><el-input v-model="form.season" placeholder="春季/夏季" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="播种日期" prop="sowDate"><el-date-picker v-model="form.sowDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="面积(亩)" prop="areaMu"><el-input-number v-model="form.areaMu" :precision="2" :step="0.1" :min="0.1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测状态" prop="forecastStatus"><el-select v-model="form.forecastStatus" style="width: 100%"><el-option v-for="item in forecastStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="预测产量" prop="forecastYieldKg"><el-input-number v-model="form.forecastYieldKg" :precision="2" :step="1" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="执行预测" :visible.sync="predictOpen" width="520px" append-to-body>
      <el-form ref="predictForm" :model="predictForm" :rules="predictRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="predictForm.forecastId" disabled /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="predictForm.modelVersion" /></el-form-item>
        <el-form-item label="预测产量(kg)" prop="forecastYieldKg"><el-input-number v-model="predictForm.forecastYieldKg" :precision="2" :step="1" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPredict">确 定</el-button>
        <el-button @click="predictOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listYieldForecast,
  getYieldForecast,
  addYieldForecast,
  updateYieldForecast,
  predictYieldForecast,
  delYieldForecast
} from '@/api/agri/yieldForecast'

export default {
  name: 'YieldForecast',
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
      predictOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plotCode: undefined,
        cropName: undefined,
        forecastStatus: undefined
      },
      form: {},
      predictForm: {
        forecastId: undefined,
        modelVersion: 'yield-model-v1',
        forecastYieldKg: undefined
      },
      forecastStatusOptions: [
        { label: '待预测', value: '0' },
        { label: '已预测', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        cropName: [{ required: true, message: '作物名称不能为空', trigger: 'blur' }],
        season: [{ required: true, message: '季节不能为空', trigger: 'blur' }],
        sowDate: [{ required: true, message: '播种日期不能为空', trigger: 'change' }],
        areaMu: [{ required: true, message: '种植面积不能为空', trigger: 'change' }]
      },
      predictRules: {
        modelVersion: [{ required: true, message: '模型版本不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listYieldForecast(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatForecastStatus(value) {
      const option = this.forecastStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        forecastId: undefined,
        plotCode: undefined,
        cropName: undefined,
        season: undefined,
        sowDate: undefined,
        areaMu: undefined,
        forecastYieldKg: undefined,
        modelVersion: 'yield-model-v1',
        forecastStatus: '0',
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
      this.ids = selection.map(item => item.forecastId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增产量预测任务'
    },
    handleUpdate(row) {
      this.reset()
      const forecastId = row.forecastId || this.ids
      getYieldForecast(forecastId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改产量预测任务'
      })
    },
    handlePredict(row) {
      this.predictForm = {
        forecastId: row.forecastId,
        modelVersion: row.modelVersion || 'yield-model-v1',
        forecastYieldKg: row.forecastYieldKg
      }
      this.predictOpen = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.forecastId !== undefined) {
          updateYieldForecast(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addYieldForecast(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    submitPredict() {
      this.$refs.predictForm.validate(valid => {
        if (!valid) {
          return
        }
        predictYieldForecast(this.predictForm).then(() => {
          this.$modal.msgSuccess('预测回写成功')
          this.predictOpen = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const forecastIds = row.forecastId || this.ids
      this.$modal
        .confirm('是否确认删除产量预测任务编号为"' + forecastIds + '"的数据项？')
        .then(function () {
          return delYieldForecast(forecastIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/yieldForecast/export',
        {
          ...this.queryParams
        },
        `yield_forecast_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
