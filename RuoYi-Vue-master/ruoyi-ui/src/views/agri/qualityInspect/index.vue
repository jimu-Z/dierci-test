<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="加工批次号" prop="processBatchNo">
        <el-input v-model="queryParams.processBatchNo" placeholder="请输入加工批次号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="样品编码" prop="sampleCode">
        <el-input v-model="queryParams.sampleCode" placeholder="请输入样品编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="检测状态" prop="inspectStatus">
        <el-select v-model="queryParams.inspectStatus" clearable style="width: 140px">
          <el-option v-for="item in inspectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:qualityInspect:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:qualityInspect:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:qualityInspect:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:qualityInspect:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="inspectId" align="center" width="70" />
      <el-table-column label="加工批次号" prop="processBatchNo" align="center" width="150" />
      <el-table-column label="样品编码" prop="sampleCode" align="center" width="120" />
      <el-table-column label="检测状态" align="center" width="100">
        <template slot-scope="scope">{{ formatInspectStatus(scope.row.inspectStatus) }}</template>
      </el-table-column>
      <el-table-column label="品质等级" prop="qualityGrade" align="center" width="90" />
      <el-table-column label="缺陷率" prop="defectRate" align="center" width="90" />
      <el-table-column label="检测结果" prop="inspectResult" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="模型版本" prop="modelVersion" align="center" width="100" />
      <el-table-column label="检测时间" prop="inspectTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.inspectTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:qualityInspect:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-cpu" @click="handleFeedback(scope.row)" v-hasPermi="['agri:qualityInspect:edit']">回写检测</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:qualityInspect:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="加工批次号" prop="processBatchNo"><el-input v-model="form.processBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="样品编码" prop="sampleCode"><el-input v-model="form.sampleCode" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="图片URL" prop="imageUrl"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="检测状态" prop="inspectStatus"><el-select v-model="form.inspectStatus" style="width: 100%"><el-option v-for="item in inspectStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="品质等级" prop="qualityGrade"><el-input v-model="form.qualityGrade" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="缺陷率" prop="defectRate"><el-input-number v-model="form.defectRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="检测时间" prop="inspectTime"><el-date-picker v-model="form.inspectTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="检测结果" prop="inspectResult"><el-input v-model="form.inspectResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="检测结果回写" :visible.sync="feedbackOpen" width="560px" append-to-body>
      <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="feedbackForm.inspectId" disabled /></el-form-item>
        <el-form-item label="检测状态" prop="inspectStatus">
          <el-select v-model="feedbackForm.inspectStatus" style="width: 100%">
            <el-option label="已检测" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="品质等级" prop="qualityGrade"><el-input v-model="feedbackForm.qualityGrade" /></el-form-item>
        <el-form-item label="缺陷率" prop="defectRate"><el-input-number v-model="feedbackForm.defectRate" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item>
        <el-form-item label="检测结果" prop="inspectResult"><el-input v-model="feedbackForm.inspectResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="模型版本" prop="modelVersion"><el-input v-model="feedbackForm.modelVersion" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFeedback">确 定</el-button>
        <el-button @click="feedbackOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listQualityInspect,
  getQualityInspect,
  addQualityInspect,
  updateQualityInspect,
  feedbackQualityInspect,
  delQualityInspect
} from '@/api/agri/qualityInspect'

export default {
  name: 'QualityInspect',
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
      feedbackOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        processBatchNo: undefined,
        sampleCode: undefined,
        inspectStatus: undefined
      },
      form: {},
      feedbackForm: {
        inspectId: undefined,
        inspectStatus: '1',
        qualityGrade: 'A',
        defectRate: 0.02,
        inspectResult: undefined,
        modelVersion: 'vision-model-v1'
      },
      inspectStatusOptions: [
        { label: '待检测', value: '0' },
        { label: '已检测', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        processBatchNo: [{ required: true, message: '加工批次号不能为空', trigger: 'blur' }],
        sampleCode: [{ required: true, message: '样品编码不能为空', trigger: 'blur' }],
        imageUrl: [{ required: true, message: '图片URL不能为空', trigger: 'blur' }]
      },
      feedbackRules: {
        inspectStatus: [{ required: true, message: '检测状态不能为空', trigger: 'change' }],
        inspectResult: [{ required: true, message: '检测结果不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQualityInspect(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatInspectStatus(value) {
      const option = this.inspectStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        inspectId: undefined,
        processBatchNo: undefined,
        sampleCode: undefined,
        imageUrl: undefined,
        inspectStatus: '0',
        qualityGrade: undefined,
        defectRate: undefined,
        inspectResult: undefined,
        inspectTime: undefined,
        modelVersion: 'vision-model-v1',
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
      this.ids = selection.map(item => item.inspectId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增视觉品质检测任务'
    },
    handleUpdate(row) {
      this.reset()
      const inspectId = row.inspectId || this.ids
      getQualityInspect(inspectId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改视觉品质检测任务'
      })
    },
    handleFeedback(row) {
      this.feedbackForm = {
        inspectId: row.inspectId,
        inspectStatus: '1',
        qualityGrade: row.qualityGrade || 'A',
        defectRate: row.defectRate || 0.02,
        inspectResult: row.inspectResult,
        modelVersion: row.modelVersion || 'vision-model-v1'
      }
      this.feedbackOpen = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.inspectId !== undefined) {
          updateQualityInspect(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addQualityInspect(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (!valid) {
          return
        }
        feedbackQualityInspect(this.feedbackForm).then(() => {
          this.$modal.msgSuccess('回写成功')
          this.feedbackOpen = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const inspectIds = row.inspectId || this.ids
      this.$modal
        .confirm('是否确认删除视觉品质检测任务编号为"' + inspectIds + '"的数据项？')
        .then(function () {
          return delQualityInspect(inspectIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/qualityInspect/export',
        {
          ...this.queryParams
        },
        `quality_inspect_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
