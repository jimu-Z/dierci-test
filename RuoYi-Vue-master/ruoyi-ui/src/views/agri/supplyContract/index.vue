<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="合约编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入合约编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="合约名称" prop="contractName">
        <el-input v-model="queryParams.contractName" placeholder="请输入合约名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="合约状态" prop="contractStatus">
        <el-select v-model="queryParams.contractStatus" placeholder="请选择合约状态" clearable>
          <el-option v-for="dict in dict.type.agri_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="风控等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="请选择风控等级" clearable>
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:supplyContract:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:supplyContract:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:supplyContract:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:supplyContract:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="supplyContractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="contractId" width="80" />
      <el-table-column label="合约编号" align="center" prop="contractNo" min-width="120" show-overflow-tooltip />
      <el-table-column label="合约名称" align="center" prop="contractName" min-width="130" show-overflow-tooltip />
      <el-table-column label="融资主体" align="center" prop="financeSubject" min-width="130" show-overflow-tooltip />
      <el-table-column label="融资金额" align="center" prop="financeAmount" width="110" />
      <el-table-column label="利率(%)" align="center" prop="interestRate" width="90" />
      <el-table-column label="合约状态" align="center" prop="contractStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_contract_status" :value="scope.row.contractStatus" />
        </template>
      </el-table-column>
      <el-table-column label="风控等级" align="center" prop="riskLevel">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_risk_level" :value="scope.row.riskLevel" />
        </template>
      </el-table-column>
      <el-table-column label="到期日期" align="center" prop="endDate" width="110" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:supplyContract:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:supplyContract:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="合约编号" prop="contractNo"><el-input v-model="form.contractNo" placeholder="请输入合约编号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="合约名称" prop="contractName"><el-input v-model="form.contractName" placeholder="请输入合约名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="融资主体" prop="financeSubject"><el-input v-model="form.financeSubject" placeholder="请输入融资主体" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="融资金额" prop="financeAmount"><el-input-number v-model="form.financeAmount" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="利率(%)" prop="interestRate"><el-input-number v-model="form.interestRate" :precision="4" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="合约状态" prop="contractStatus">
              <el-select v-model="form.contractStatus" placeholder="请选择合约状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="风控等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风控等级" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_risk_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起始日期" prop="startDate">
              <el-date-picker clearable v-model="form.startDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择起始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="到期日期" prop="endDate">
          <el-date-picker clearable v-model="form.endDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择到期日期" style="width: 100%" />
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
  listSupplyContract,
  getSupplyContract,
  delSupplyContract,
  addSupplyContract,
  updateSupplyContract
} from '@/api/agri/supplyContract'

export default {
  name: 'SupplyContract',
  dicts: ['agri_contract_status', 'agri_risk_level'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      supplyContractList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractNo: null,
        contractName: null,
        contractStatus: null,
        riskLevel: null
      },
      form: {},
      rules: {
        contractNo: [{ required: true, message: '合约编号不能为空', trigger: 'blur' }],
        contractName: [{ required: true, message: '合约名称不能为空', trigger: 'blur' }],
        financeSubject: [{ required: true, message: '融资主体不能为空', trigger: 'blur' }],
        financeAmount: [{ required: true, message: '融资金额不能为空', trigger: 'blur' }],
        contractStatus: [{ required: true, message: '合约状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSupplyContract(this.queryParams).then(response => {
        this.supplyContractList = response.rows
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
        contractId: null,
        contractNo: null,
        contractName: null,
        financeSubject: null,
        financeAmount: 0,
        interestRate: 0,
        startDate: null,
        endDate: null,
        contractStatus: '0',
        riskLevel: 'L',
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
      this.ids = selection.map(item => item.contractId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增供应链金融合约'
    },
    handleUpdate(row) {
      this.reset()
      const contractId = row.contractId || this.ids[0]
      getSupplyContract(contractId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改供应链金融合约'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.contractId != null) {
            updateSupplyContract(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addSupplyContract(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const contractIds = row.contractId || this.ids
      this.$modal.confirm('是否确认删除供应链金融合约编号为"' + contractIds + '"的数据项？').then(() => {
        return delSupplyContract(contractIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/supplyContract/export', {
        ...this.queryParams
      }, `supply_contract_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
