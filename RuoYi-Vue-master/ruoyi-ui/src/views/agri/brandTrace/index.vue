<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="溯源码" prop="traceCode">
        <el-input v-model="queryParams.traceCode" placeholder="请输入溯源码" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="品牌名称" prop="brandName">
        <el-input v-model="queryParams.brandName" placeholder="请输入品牌名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" clearable style="width: 140px">
          <el-option v-for="item in publishStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:brandTrace:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-upload2" size="mini" :disabled="single" @click="handlePublish" v-hasPermi="['agri:brandTrace:edit']">发布</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-view" size="mini" :disabled="single" @click="handlePreview" v-hasPermi="['agri:brandTrace:query']">预览</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:brandTrace:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:brandTrace:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="pageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="pageId" align="center" width="70" />
      <el-table-column label="溯源码" prop="traceCode" align="center" width="170" />
      <el-table-column label="品牌" prop="brandName" align="center" width="110" />
      <el-table-column label="产品" prop="productName" align="center" width="110" />
      <el-table-column label="产地" prop="originPlace" align="center" width="120" />
      <el-table-column label="发布状态" align="center" width="100">
        <template slot-scope="scope">{{ formatPublishStatus(scope.row.publishStatus) }}</template>
      </el-table-column>
      <el-table-column label="详情页地址" prop="pageUrl" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:brandTrace:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:brandTrace:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="溯源码" prop="traceCode"><el-input v-model="form.traceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品牌名称" prop="brandName"><el-input v-model="form.brandName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="产地" prop="originPlace"><el-input v-model="form.originPlace" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发布状态" prop="publishStatus"><el-select v-model="form.publishStatus" style="width: 100%"><el-option v-for="item in publishStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="种植批次号" prop="plantingBatchNo"><el-input v-model="form.plantingBatchNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="加工批次号" prop="processBatchNo"><el-input v-model="form.processBatchNo" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="物流运单号" prop="logisticsTraceCode"><el-input v-model="form.logisticsTraceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="二维码地址" prop="qrCodeUrl"><el-input v-model="form.qrCodeUrl" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="封面图地址" prop="coverImageUrl"><el-input v-model="form.coverImageUrl" /></el-form-item>
        <el-form-item label="详情页地址" prop="pageUrl"><el-input v-model="form.pageUrl" /></el-form-item>
        <el-form-item label="品牌故事" prop="brandStory"><el-input v-model="form.brandStory" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="溯源码页面预览" :visible.sync="previewOpen" width="820px" append-to-body>
      <el-descriptions :column="2" border v-if="previewData">
        <el-descriptions-item label="溯源码">{{ previewData.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="品牌名称">{{ previewData.brandName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ previewData.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ previewData.productCode }}</el-descriptions-item>
        <el-descriptions-item label="产地">{{ previewData.originPlace }}</el-descriptions-item>
        <el-descriptions-item label="物流运单号">{{ previewData.logisticsTraceCode }}</el-descriptions-item>
        <el-descriptions-item label="详情页地址" :span="2">{{ previewData.pageUrl }}</el-descriptions-item>
        <el-descriptions-item label="品牌故事" :span="2">{{ previewData.brandStory }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBrandTrace,
  getBrandTrace,
  previewBrandTrace,
  addBrandTrace,
  updateBrandTrace,
  publishBrandTrace,
  delBrandTrace
} from '@/api/agri/brandTrace'

export default {
  name: 'BrandTrace',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      pageList: [],
      title: '',
      open: false,
      previewOpen: false,
      previewData: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        traceCode: undefined,
        brandName: undefined,
        publishStatus: undefined
      },
      form: {},
      publishStatusOptions: [
        { label: '草稿', value: '0' },
        { label: '已发布', value: '1' }
      ],
      rules: {
        traceCode: [{ required: true, message: '溯源码不能为空', trigger: 'blur' }],
        brandName: [{ required: true, message: '品牌名称不能为空', trigger: 'blur' }],
        productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listBrandTrace(this.queryParams).then(response => {
        this.pageList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatPublishStatus(value) {
      const option = this.publishStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        pageId: undefined,
        traceCode: undefined,
        brandName: undefined,
        productName: undefined,
        productCode: undefined,
        originPlace: undefined,
        plantingBatchNo: undefined,
        processBatchNo: undefined,
        logisticsTraceCode: undefined,
        coverImageUrl: undefined,
        pageUrl: undefined,
        qrCodeUrl: undefined,
        brandStory: undefined,
        publishStatus: '0',
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
      this.ids = selection.map(item => item.pageId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增品牌溯源页面'
    },
    handleUpdate(row) {
      this.reset()
      const pageId = row.pageId || this.ids
      getBrandTrace(pageId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改品牌溯源页面'
      })
    },
    handlePublish() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条溯源页面进行发布')
        return
      }
      publishBrandTrace(this.ids[0]).then(() => {
        this.$modal.msgSuccess('发布成功')
        this.getList()
      })
    },
    handlePreview() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条溯源页面预览')
        return
      }
      const selected = this.pageList.find(item => item.pageId === this.ids[0])
      if (!selected || !selected.traceCode) {
        this.$modal.msgWarning('所选记录缺少溯源码')
        return
      }
      previewBrandTrace(selected.traceCode).then(response => {
        this.previewData = response.data
        this.previewOpen = true
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.pageId !== undefined) {
          updateBrandTrace(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addBrandTrace(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const pageIds = row.pageId || this.ids
      this.$modal
        .confirm('是否确认删除品牌溯源页面编号为"' + pageIds + '"的数据项？')
        .then(function () {
          return delBrandTrace(pageIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/brandTrace/export',
        {
          ...this.queryParams
        },
        `brand_trace_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
