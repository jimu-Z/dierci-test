<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="地块编码" prop="plotCode">
        <el-input
          v-model="queryParams.plotCode"
          placeholder="请输入地块编码"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作业类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="请选择作业类型" clearable style="width: 180px">
          <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="作业人" prop="operatorName">
        <el-input
          v-model="queryParams.operatorName"
          placeholder="请输入作业人"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 130px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:farmOp:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:farmOp:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:farmOp:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:farmOp:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="farmOpList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="operationId" width="70" />
      <el-table-column label="地块编码" align="center" prop="plotCode" width="120" />
      <el-table-column label="作业类型" align="center" prop="operationType" width="120">
        <template slot-scope="scope">{{ formatOperationType(scope.row.operationType) }}</template>
      </el-table-column>
      <el-table-column label="作业内容" align="center" prop="operationContent" :show-overflow-tooltip="true" />
      <el-table-column label="作业人" align="center" prop="operatorName" width="100" />
      <el-table-column label="投入品" align="center" min-width="160">
        <template slot-scope="scope">
          {{ scope.row.inputName || '-' }} {{ scope.row.inputAmount || '' }} {{ scope.row.inputUnit || '' }}
        </template>
      </el-table-column>
      <el-table-column label="作业时间" align="center" prop="operationTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.operationTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:farmOp:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:farmOp:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="地块编码" prop="plotCode">
              <el-input v-model="form.plotCode" placeholder="请输入地块编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业类型" prop="operationType">
              <el-select v-model="form.operationType" placeholder="请选择作业类型" style="width: 100%">
                <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作业内容" prop="operationContent">
          <el-input v-model="form.operationContent" type="textarea" :rows="3" placeholder="请输入作业内容" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="作业人" prop="operatorName">
              <el-input v-model="form.operatorName" placeholder="请输入作业人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业时间" prop="operationTime">
              <el-date-picker
                v-model="form.operationTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择作业时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="10">
            <el-form-item label="投入品名称" prop="inputName">
              <el-input v-model="form.inputName" placeholder="如：有机肥" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="用量" prop="inputAmount">
              <el-input-number v-model="form.inputAmount" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="单位" prop="inputUnit">
              <el-input v-model="form.inputUnit" placeholder="kg / L" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
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
import { listFarmOp, getFarmOp, delFarmOp, addFarmOp, updateFarmOp } from '@/api/agri/farmOp'

export default {
  name: 'FarmOperation',
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      farmOpList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plotCode: undefined,
        operationType: undefined,
        operatorName: undefined,
        status: undefined
      },
      form: {},
      operationTypeOptions: [
        { label: '播种', value: 'SOWING' },
        { label: '施肥', value: 'FERTILIZATION' },
        { label: '灌溉', value: 'IRRIGATION' },
        { label: '用药', value: 'PEST_CONTROL' },
        { label: '采收', value: 'HARVEST' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        operationType: [{ required: true, message: '作业类型不能为空', trigger: 'change' }],
        operationContent: [{ required: true, message: '作业内容不能为空', trigger: 'blur' }],
        operatorName: [{ required: true, message: '作业人不能为空', trigger: 'blur' }],
        operationTime: [{ required: true, message: '作业时间不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFarmOp(this.queryParams).then(response => {
        this.farmOpList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatOperationType(value) {
      const option = this.operationTypeOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        operationId: undefined,
        plotCode: undefined,
        operationType: undefined,
        operationContent: undefined,
        operatorName: undefined,
        inputName: undefined,
        inputAmount: undefined,
        inputUnit: undefined,
        operationTime: undefined,
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
      this.ids = selection.map(item => item.operationId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增农事记录'
    },
    handleUpdate(row) {
      this.reset()
      const operationId = row.operationId || this.ids
      getFarmOp(operationId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改农事记录'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.operationId !== undefined) {
          updateFarmOp(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addFarmOp(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const operationIds = row.operationId || this.ids
      this.$modal
        .confirm('是否确认删除农事记录编号为"' + operationIds + '"的数据项？')
        .then(function () {
          return delFarmOp(operationIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/farmOp/export',
        {
          ...this.queryParams
        },
        `farm_op_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
