<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="模型编码" prop="modelCode">
        <el-input v-model="queryParams.modelCode" placeholder="请输入模型编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品类型" prop="productType">
        <el-select v-model="queryParams.productType" placeholder="请选择产品类型" clearable>
          <el-option v-for="dict in dict.type.agri_product_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="核算状态" prop="calcStatus">
        <el-select v-model="queryParams.calcStatus" placeholder="请选择核算状态" clearable>
          <el-option v-for="dict in dict.type.agri_calc_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:carbonFootprint:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:carbonFootprint:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:carbonFootprint:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:carbonFootprint:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="carbonFootprintList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="modelId" width="80" />
      <el-table-column label="模型编码" align="center" prop="modelCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="模型名称" align="center" prop="modelName" min-width="120" show-overflow-tooltip />
      <el-table-column label="产品类型" align="center" prop="productType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_product_type" :value="scope.row.productType" />
        </template>
      </el-table-column>
      <el-table-column label="排放因子" align="center" prop="emissionFactor" width="110" />
      <el-table-column label="碳排放量" align="center" prop="carbonEmission" width="110" />
      <el-table-column label="核算状态" align="center" prop="calcStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_calc_status" :value="scope.row.calcStatus" />
        </template>
      </el-table-column>
      <el-table-column label="核算时间" align="center" prop="calcTime" width="160" />
      <el-table-column label="复核人" align="center" prop="verifier" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:carbonFootprint:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:carbonFootprint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="模型编码" prop="modelCode"><el-input v-model="form.modelCode" placeholder="请输入模型编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型名称" prop="modelName"><el-input v-model="form.modelName" placeholder="请输入模型名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="产品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择产品类型" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_product_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="核算状态" prop="calcStatus">
              <el-select v-model="form.calcStatus" placeholder="请选择核算状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_calc_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="排放因子" prop="emissionFactor"><el-input-number v-model="form.emissionFactor" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="碳排放量" prop="carbonEmission"><el-input-number v-model="form.carbonEmission" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="核算边界" prop="boundaryScope"><el-input v-model="form.boundaryScope" placeholder="请输入核算边界" /></el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="核算时间" prop="calcTime">
              <el-date-picker clearable v-model="form.calcTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择核算时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="复核人" prop="verifier"><el-input v-model="form.verifier" placeholder="请输入复核人" /></el-form-item></el-col>
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
  listCarbonFootprint,
  getCarbonFootprint,
  delCarbonFootprint,
  addCarbonFootprint,
  updateCarbonFootprint
} from '@/api/agri/carbonFootprint'

export default {
  name: 'CarbonFootprint',
  dicts: ['agri_product_type', 'agri_calc_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      carbonFootprintList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelCode: null,
        modelName: null,
        productType: null,
        calcStatus: null
      },
      form: {},
      rules: {
        modelCode: [{ required: true, message: '模型编码不能为空', trigger: 'blur' }],
        modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
        productType: [{ required: true, message: '产品类型不能为空', trigger: 'change' }],
        emissionFactor: [{ required: true, message: '排放因子不能为空', trigger: 'blur' }],
        calcStatus: [{ required: true, message: '核算状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCarbonFootprint(this.queryParams).then(response => {
        this.carbonFootprintList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        modelId: null,
        modelCode: null,
        modelName: null,
        productType: null,
        boundaryScope: null,
        emissionFactor: 0,
        carbonEmission: 0,
        calcStatus: '0',
        calcTime: null,
        verifier: null,
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
      this.ids = selection.map(item => item.modelId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增碳足迹核算模型'
    },
    handleUpdate(row) {
      this.reset()
      const modelId = row.modelId || this.ids[0]
      getCarbonFootprint(modelId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改碳足迹核算模型'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.modelId != null) {
            updateCarbonFootprint(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addCarbonFootprint(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const modelIds = row.modelId || this.ids
      this.$modal.confirm('是否确认删除碳足迹核算模型编号为"' + modelIds + '"的数据项？').then(() => {
        return delCarbonFootprint(modelIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/carbonFootprint/export', {
        ...this.queryParams
      }, `carbon_footprint_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
