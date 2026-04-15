<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="地块编码" prop="plotCode">
        <el-input v-model="queryParams.plotCode" placeholder="请输入地块编码" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="作物名称" prop="cropName">
        <el-input v-model="queryParams.cropName" placeholder="请输入作物名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="识别状态" prop="identifyStatus">
        <el-select v-model="queryParams.identifyStatus" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="item in identifyStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:pestIdentify:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:pestIdentify:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:pestIdentify:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:pestIdentify:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="taskId" align="center" width="70" />
      <el-table-column label="地块" prop="plotCode" align="center" width="110" />
      <el-table-column label="作物" prop="cropName" align="center" width="110" />
      <el-table-column label="图片地址" prop="imageUrl" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="识别状态" align="center" width="100">
        <template slot-scope="scope">{{ formatIdentifyStatus(scope.row.identifyStatus) }}</template>
      </el-table-column>
      <el-table-column label="识别结果" prop="identifyResult" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="置信度" prop="confidence" align="center" width="90" />
      <el-table-column label="模型版本" prop="modelVersion" align="center" width="100" />
      <el-table-column label="识别时间" prop="identifyTime" align="center" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.identifyTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:pestIdentify:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-cpu" @click="handleFeedback(scope.row)" v-hasPermi="['agri:pestIdentify:edit']">回写识别</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:pestIdentify:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12"><el-form-item label="地块编码" prop="plotCode"><el-input v-model="form.plotCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作物名称" prop="cropName"><el-input v-model="form.cropName" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="图片URL" prop="imageUrl"><el-input v-model="form.imageUrl" placeholder="上传后回填URL" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="识别状态" prop="identifyStatus"><el-select v-model="form.identifyStatus" style="width: 100%"><el-option v-for="item in identifyStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型版本" prop="modelVersion"><el-input v-model="form.modelVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="置信度" prop="confidence"><el-input-number v-model="form.confidence" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="识别时间" prop="identifyTime"><el-date-picker v-model="form.identifyTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="识别结果" prop="identifyResult"><el-input v-model="form.identifyResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="识别结果回写" :visible.sync="feedbackOpen" width="560px" append-to-body>
      <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="任务ID"><el-input v-model="feedbackForm.taskId" disabled /></el-form-item>
        <el-form-item label="识别状态" prop="identifyStatus">
          <el-select v-model="feedbackForm.identifyStatus" style="width: 100%">
            <el-option label="已识别" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="识别结果" prop="identifyResult"><el-input v-model="feedbackForm.identifyResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="置信度" prop="confidence"><el-input-number v-model="feedbackForm.confidence" :precision="4" :step="0.01" :min="0" :max="1" style="width: 100%" /></el-form-item>
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
  listPestIdentify,
  getPestIdentify,
  addPestIdentify,
  updatePestIdentify,
  feedbackPestIdentify,
  delPestIdentify
} from '@/api/agri/pestIdentify'

export default {
  name: 'PestIdentify',
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
        plotCode: undefined,
        cropName: undefined,
        identifyStatus: undefined
      },
      form: {},
      feedbackForm: {
        taskId: undefined,
        identifyStatus: '1',
        identifyResult: undefined,
        confidence: 0.9,
        modelVersion: 'pest-model-v1'
      },
      identifyStatusOptions: [
        { label: '待识别', value: '0' },
        { label: '已识别', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        cropName: [{ required: true, message: '作物名称不能为空', trigger: 'blur' }],
        imageUrl: [{ required: true, message: '图片URL不能为空', trigger: 'blur' }]
      },
      feedbackRules: {
        identifyStatus: [{ required: true, message: '识别状态不能为空', trigger: 'change' }],
        identifyResult: [{ required: true, message: '识别结果不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listPestIdentify(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatIdentifyStatus(value) {
      const option = this.identifyStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        taskId: undefined,
        plotCode: undefined,
        cropName: undefined,
        imageUrl: undefined,
        identifyStatus: '0',
        identifyResult: undefined,
        confidence: undefined,
        identifyTime: undefined,
        modelVersion: 'pest-model-v1',
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
      this.ids = selection.map(item => item.taskId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增病虫害识别任务'
    },
    handleUpdate(row) {
      this.reset()
      const taskId = row.taskId || this.ids
      getPestIdentify(taskId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改病虫害识别任务'
      })
    },
    handleFeedback(row) {
      this.feedbackForm = {
        taskId: row.taskId,
        identifyStatus: '1',
        identifyResult: row.identifyResult,
        confidence: row.confidence || 0.9,
        modelVersion: row.modelVersion || 'pest-model-v1'
      }
      this.feedbackOpen = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.taskId !== undefined) {
          updatePestIdentify(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addPestIdentify(this.form).then(() => {
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
        feedbackPestIdentify(this.feedbackForm).then(() => {
          this.$modal.msgSuccess('回写成功')
          this.feedbackOpen = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const taskIds = row.taskId || this.ids
      this.$modal
        .confirm('是否确认删除病虫害识别任务编号为"' + taskIds + '"的数据项？')
        .then(function () {
          return delPestIdentify(taskIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/pestIdentify/export',
        {
          ...this.queryParams
        },
        `pest_identify_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
