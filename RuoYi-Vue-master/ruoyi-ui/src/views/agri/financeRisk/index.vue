<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="指标编码" prop="indicatorCode">
        <el-input v-model="queryParams.indicatorCode" placeholder="请输入指标编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="指标名称" prop="indicatorName">
        <el-input v-model="queryParams.indicatorName" placeholder="请输入指标名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="风险维度" prop="riskDimension">
        <el-select v-model="queryParams.riskDimension" placeholder="请选择风险维度" clearable>
          <el-option v-for="dict in dict.type.agri_risk_dimension" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="请选择风险等级" clearable>
          <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:financeRisk:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:financeRisk:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:financeRisk:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:financeRisk:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="financeRiskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="riskId" width="80" />
      <el-table-column label="指标编码" align="center" prop="indicatorCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="指标名称" align="center" prop="indicatorName" min-width="130" show-overflow-tooltip />
      <el-table-column label="风险维度" align="center" prop="riskDimension">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_risk_dimension" :value="scope.row.riskDimension" />
        </template>
      </el-table-column>
      <el-table-column label="风险分值" align="center" prop="riskScore" width="100" />
      <el-table-column label="阈值" align="center" prop="thresholdValue" width="100" />
      <el-table-column label="风险等级" align="center" prop="riskLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_risk_level" :value="scope.row.riskLevel" />
        </template>
      </el-table-column>
      <el-table-column label="评估状态" align="center" prop="evaluateStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_evaluate_status" :value="scope.row.evaluateStatus" />
        </template>
      </el-table-column>
      <el-table-column label="评估时间" align="center" prop="evaluateTime" width="160" />
      <el-table-column label="评估人" align="center" prop="evaluator" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:financeRisk:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:financeRisk:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="指标编码" prop="indicatorCode"><el-input v-model="form.indicatorCode" placeholder="请输入指标编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="指标名称" prop="indicatorName"><el-input v-model="form.indicatorName" placeholder="请输入指标名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="风险维度" prop="riskDimension">
              <el-select v-model="form.riskDimension" placeholder="请选择风险维度" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_dimension" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风险等级" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="风险分值" prop="riskScore"><el-input-number v-model="form.riskScore" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="阈值" prop="thresholdValue"><el-input-number v-model="form.thresholdValue" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="评估状态" prop="evaluateStatus">
              <el-select v-model="form.evaluateStatus" placeholder="请选择评估状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_evaluate_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="评估人" prop="evaluator"><el-input v-model="form.evaluator" placeholder="请输入评估人" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="评估时间" prop="evaluateTime">
          <el-date-picker clearable v-model="form.evaluateTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择评估时间" style="width: 100%" />
        </el-form-item>
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
  listFinanceRisk,
  getFinanceRisk,
  delFinanceRisk,
  addFinanceRisk,
  updateFinanceRisk
} from '@/api/agri/financeRisk'

export default {
  name: 'FinanceRisk',
  dicts: ['agri_risk_dimension', 'agri_risk_level', 'agri_evaluate_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      financeRiskList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        indicatorCode: null,
        indicatorName: null,
        riskDimension: null,
        riskLevel: null
      },
      form: {},
      rules: {
        indicatorCode: [{ required: true, message: '指标编码不能为空', trigger: 'blur' }],
        indicatorName: [{ required: true, message: '指标名称不能为空', trigger: 'blur' }],
        riskDimension: [{ required: true, message: '风险维度不能为空', trigger: 'change' }],
        riskScore: [{ required: true, message: '风险分值不能为空', trigger: 'blur' }],
        riskLevel: [{ required: true, message: '风险等级不能为空', trigger: 'change' }],
        evaluateStatus: [{ required: true, message: '评估状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFinanceRisk(this.queryParams).then(response => {
        this.financeRiskList = response.rows
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
        riskId: null,
        indicatorCode: null,
        indicatorName: null,
        riskDimension: null,
        riskScore: 0,
        thresholdValue: 0,
        riskLevel: 'L',
        evaluateStatus: '0',
        evaluateTime: null,
        evaluator: null,
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
      this.ids = selection.map(item => item.riskId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增农业金融风控指标'
    },
    handleUpdate(row) {
      this.reset()
      const riskId = row.riskId || this.ids[0]
      getFinanceRisk(riskId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改农业金融风控指标'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.riskId != null) {
            updateFinanceRisk(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addFinanceRisk(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const riskIds = row.riskId || this.ids
      this.$modal.confirm('是否确认删除农业金融风控指标编号为"' + riskIds + '"的数据项？').then(() => {
        return delFinanceRisk(riskIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/financeRisk/export', {
        ...this.queryParams
      }, `finance_risk_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
