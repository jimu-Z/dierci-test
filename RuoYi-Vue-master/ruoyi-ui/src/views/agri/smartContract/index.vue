<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="合约名称" prop="contractName">
        <el-input v-model="queryParams.contractName" placeholder="请输入合约名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="版本" prop="contractVersion">
        <el-input v-model="queryParams.contractVersion" placeholder="请输入版本" clearable style="width: 120px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="链平台" prop="chainPlatform">
        <el-select v-model="queryParams.chainPlatform" clearable style="width: 140px">
          <el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="部署状态" prop="deployStatus">
        <el-select v-model="queryParams.deployStatus" clearable style="width: 140px">
          <el-option v-for="item in deployStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:smartContract:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-cpu" size="mini" :disabled="single" @click="handleDeploy" v-hasPermi="['agri:smartContract:edit']">执行部署</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:smartContract:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:smartContract:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="deployId" align="center" width="70" />
      <el-table-column label="合约名称" prop="contractName" align="center" width="140" />
      <el-table-column label="版本" prop="contractVersion" align="center" width="90" />
      <el-table-column label="链平台" prop="chainPlatform" align="center" width="120" />
      <el-table-column label="源码哈希" prop="sourceHash" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="合约地址" prop="contractAddress" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="部署交易" prop="deployTxHash" align="center" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="部署状态" align="center" width="100">
        <template slot-scope="scope">{{ formatDeployStatus(scope.row.deployStatus) }}</template>
      </el-table-column>
      <el-table-column label="部署时间" prop="deployTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.deployTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:smartContract:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:smartContract:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="合约名称" prop="contractName"><el-input v-model="form.contractName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="合约版本" prop="contractVersion"><el-input v-model="form.contractVersion" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="链平台" prop="chainPlatform"><el-select v-model="form.chainPlatform" style="width: 100%"><el-option v-for="item in chainPlatformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部署状态" prop="deployStatus"><el-select v-model="form.deployStatus" style="width: 100%"><el-option v-for="item in deployStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="源码哈希" prop="sourceHash"><el-input v-model="form.sourceHash" /></el-form-item>
        <el-form-item label="ABI" prop="abiJson"><el-input v-model="form.abiJson" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="合约地址" prop="contractAddress"><el-input v-model="form.contractAddress" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
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
  listSmartContract,
  getSmartContract,
  addSmartContract,
  updateSmartContract,
  deploySmartContract,
  delSmartContract
} from '@/api/agri/smartContract'

export default {
  name: 'SmartContract',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      contractList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractName: undefined,
        contractVersion: undefined,
        chainPlatform: undefined,
        deployStatus: undefined
      },
      form: {},
      chainPlatformOptions: [
        { label: 'FISCO BCOS', value: 'FISCO_BCOS' },
        { label: 'Hyperchain', value: 'HYPERCHAIN' },
        { label: 'Fabric', value: 'FABRIC' }
      ],
      deployStatusOptions: [
        { label: '待部署', value: '0' },
        { label: '已部署', value: '1' },
        { label: '失败', value: '2' }
      ],
      rules: {
        contractName: [{ required: true, message: '合约名称不能为空', trigger: 'blur' }],
        contractVersion: [{ required: true, message: '合约版本不能为空', trigger: 'blur' }],
        chainPlatform: [{ required: true, message: '链平台不能为空', trigger: 'change' }],
        sourceHash: [{ required: true, message: '源码哈希不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSmartContract(this.queryParams).then(response => {
        this.contractList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatDeployStatus(value) {
      const option = this.deployStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        deployId: undefined,
        contractName: undefined,
        contractVersion: undefined,
        chainPlatform: undefined,
        sourceHash: undefined,
        abiJson: undefined,
        contractAddress: undefined,
        deployTxHash: undefined,
        deployStatus: '0',
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
      this.ids = selection.map(item => item.deployId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增智能合约部署'
    },
    handleUpdate(row) {
      this.reset()
      const deployId = row.deployId || this.ids
      getSmartContract(deployId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改智能合约部署'
      })
    },
    handleDeploy() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条任务执行部署')
        return
      }
      deploySmartContract(this.ids[0]).then(() => {
        this.$modal.msgSuccess('部署执行成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.deployId !== undefined) {
          updateSmartContract(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addSmartContract(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const deployIds = row.deployId || this.ids
      this.$modal
        .confirm('是否确认删除智能合约部署编号为"' + deployIds + '"的数据项？')
        .then(function () {
          return delSmartContract(deployIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/smartContract/export',
        {
          ...this.queryParams
        },
        `smart_contract_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
