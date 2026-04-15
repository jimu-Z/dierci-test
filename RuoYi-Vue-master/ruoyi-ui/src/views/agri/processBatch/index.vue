<template>
  <div class="app-container">
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

    <el-table v-loading="loading" :data="linkList" @selection-change="handleSelectionChange">
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
  delProcessBatch
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
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listProcessBatch(this.queryParams).then(response => {
        this.linkList = response.rows
        this.total = response.total
        this.loading = false
      })
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
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.linkId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
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
