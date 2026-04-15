<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="接入编码" prop="accessCode">
        <el-input v-model="queryParams.accessCode" placeholder="请输入接入编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="接入名称" prop="accessName">
        <el-input v-model="queryParams.accessName" placeholder="请输入接入名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="API类型" prop="apiType">
        <el-select v-model="queryParams.apiType" placeholder="请选择API类型" clearable>
          <el-option v-for="dict in dict.type.agri_api_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="调用状态" prop="callStatus">
        <el-select v-model="queryParams.callStatus" placeholder="请选择调用状态" clearable>
          <el-option v-for="dict in dict.type.agri_api_call_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:thirdApi:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['agri:thirdApi:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:thirdApi:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:thirdApi:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="thirdApiList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="accessId" width="80" />
      <el-table-column label="接入编码" align="center" prop="accessCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="接入名称" align="center" prop="accessName" min-width="130" show-overflow-tooltip />
      <el-table-column label="API类型" align="center" prop="apiType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_api_type" :value="scope.row.apiType" />
        </template>
      </el-table-column>
      <el-table-column label="供应商" align="center" prop="provider" width="120" />
      <el-table-column label="超时(秒)" align="center" prop="timeoutSec" width="90" />
      <el-table-column label="成功率(%)" align="center" prop="successRate" width="100" />
      <el-table-column label="调用状态" align="center" prop="callStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_api_call_status" :value="scope.row.callStatus" />
        </template>
      </el-table-column>
      <el-table-column label="最近调用时间" align="center" prop="lastCallTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:thirdApi:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:thirdApi:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="740px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="接入编码" prop="accessCode"><el-input v-model="form.accessCode" placeholder="请输入接入编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接入名称" prop="accessName"><el-input v-model="form.accessName" placeholder="请输入接入名称" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="API类型" prop="apiType">
              <el-select v-model="form.apiType" placeholder="请选择API类型" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_api_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="供应商" prop="provider"><el-input v-model="form.provider" placeholder="请输入供应商" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="请求地址" prop="endpointUrl"><el-input v-model="form.endpointUrl" placeholder="请输入请求地址" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="超时(秒)" prop="timeoutSec"><el-input-number v-model="form.timeoutSec" :min="1" :max="600" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="成功率(%)" prop="successRate"><el-input-number v-model="form.successRate" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="调用状态" prop="callStatus">
              <el-select v-model="form.callStatus" placeholder="请选择调用状态" style="width: 100%">
                <el-option v-for="dict in dict.type.agri_api_call_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最近调用时间" prop="lastCallTime">
              <el-date-picker clearable v-model="form.lastCallTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择最近调用时间" style="width: 100%" />
            </el-form-item>
          </el-col>
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
  listThirdApi,
  getThirdApi,
  delThirdApi,
  addThirdApi,
  updateThirdApi
} from '@/api/agri/thirdApi'

export default {
  name: 'ThirdApi',
  dicts: ['agri_api_type', 'agri_api_call_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      thirdApiList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accessCode: null,
        accessName: null,
        apiType: null,
        callStatus: null
      },
      form: {},
      rules: {
        accessCode: [{ required: true, message: '接入编码不能为空', trigger: 'blur' }],
        accessName: [{ required: true, message: '接入名称不能为空', trigger: 'blur' }],
        apiType: [{ required: true, message: 'API类型不能为空', trigger: 'change' }],
        endpointUrl: [{ required: true, message: '请求地址不能为空', trigger: 'blur' }],
        timeoutSec: [{ required: true, message: '超时不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listThirdApi(this.queryParams).then(response => {
        this.thirdApiList = response.rows
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
        accessId: null,
        accessCode: null,
        accessName: null,
        apiType: null,
        provider: null,
        endpointUrl: null,
        timeoutSec: 30,
        successRate: 0,
        callStatus: '0',
        lastCallTime: null,
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
      this.ids = selection.map(item => item.accessId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增第三方API接入'
    },
    handleUpdate(row) {
      this.reset()
      const accessId = row.accessId || this.ids[0]
      getThirdApi(accessId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改第三方API接入'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.accessId != null) {
            updateThirdApi(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addThirdApi(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const accessIds = row.accessId || this.ids
      this.$modal.confirm('是否确认删除第三方API接入编号为"' + accessIds + '"的数据项？').then(() => {
        return delThirdApi(accessIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/thirdApi/export', {
        ...this.queryParams
      }, `third_api_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
