<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="存证编号" prop="attestationNo">
        <el-input
          v-model="queryParams.attestationNo"
          placeholder="请输入存证编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务批次号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入业务批次号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <el-select v-model="queryParams.dataType" placeholder="请选择数据类型" clearable>
          <el-option
            v-for="dict in dict.type.agri_data_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="校验状态" prop="verifyStatus">
        <el-select v-model="queryParams.verifyStatus" placeholder="请选择校验状态" clearable>
          <el-option
            v-for="dict in dict.type.agri_verify_status"
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['agri:attestationVerify:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['agri:attestationVerify:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['agri:attestationVerify:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['agri:attestationVerify:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="attestationVerifyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="verifyId" width="80" />
      <el-table-column label="存证编号" align="center" prop="attestationNo" min-width="140" show-overflow-tooltip />
      <el-table-column label="业务批次号" align="center" prop="batchNo" min-width="120" show-overflow-tooltip />
      <el-table-column label="数据类型" align="center" prop="dataType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_data_type" :value="scope.row.dataType" />
        </template>
      </el-table-column>
      <el-table-column label="原始哈希" align="center" prop="originHash" min-width="160" show-overflow-tooltip />
      <el-table-column label="链上哈希" align="center" prop="chainHash" min-width="160" show-overflow-tooltip />
      <el-table-column label="校验状态" align="center" prop="verifyStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_verify_status" :value="scope.row.verifyStatus" />
        </template>
      </el-table-column>
      <el-table-column label="校验时间" align="center" prop="verifyTime" width="160" />
      <el-table-column label="校验人" align="center" prop="verifyByUser" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['agri:attestationVerify:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['agri:attestationVerify:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="存证编号" prop="attestationNo">
              <el-input v-model="form.attestationNo" placeholder="请输入存证编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入业务批次号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据类型" prop="dataType">
              <el-select v-model="form.dataType" placeholder="请选择数据类型" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.agri_data_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="校验状态" prop="verifyStatus">
              <el-select v-model="form.verifyStatus" placeholder="请选择校验状态" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.agri_verify_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="原始哈希" prop="originHash">
          <el-input v-model="form.originHash" placeholder="请输入原始哈希" />
        </el-form-item>
        <el-form-item label="链上哈希" prop="chainHash">
          <el-input v-model="form.chainHash" placeholder="请输入链上哈希" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="校验时间" prop="verifyTime">
              <el-date-picker
                clearable
                v-model="form.verifyTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择校验时间"
                style="width: 100%"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="校验人" prop="verifyByUser">
              <el-input v-model="form.verifyByUser" placeholder="请输入校验人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
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
  listAttestationVerify,
  getAttestationVerify,
  delAttestationVerify,
  addAttestationVerify,
  updateAttestationVerify
} from '@/api/agri/attestationVerify'

export default {
  name: 'AttestationVerify',
  dicts: ['agri_data_type', 'agri_verify_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      attestationVerifyList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        attestationNo: null,
        batchNo: null,
        dataType: null,
        verifyStatus: null
      },
      form: {},
      rules: {
        attestationNo: [{ required: true, message: '存证编号不能为空', trigger: 'blur' }],
        batchNo: [{ required: true, message: '业务批次号不能为空', trigger: 'blur' }],
        dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }],
        originHash: [{ required: true, message: '原始哈希不能为空', trigger: 'blur' }],
        verifyStatus: [{ required: true, message: '校验状态不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAttestationVerify(this.queryParams).then(response => {
        this.attestationVerifyList = response.rows
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
        verifyId: null,
        attestationNo: null,
        batchNo: null,
        dataType: null,
        originHash: null,
        chainHash: null,
        verifyStatus: '0',
        verifyTime: null,
        verifyByUser: null,
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
      this.ids = selection.map(item => item.verifyId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据存证与校验'
    },
    handleUpdate(row) {
      this.reset()
      const verifyId = row.verifyId || this.ids[0]
      getAttestationVerify(verifyId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改数据存证与校验'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.verifyId != null) {
            updateAttestationVerify(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addAttestationVerify(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const verifyIds = row.verifyId || this.ids
      this.$modal.confirm('是否确认删除数据存证与校验编号为"' + verifyIds + '"的数据项？').then(() => {
        return delAttestationVerify(verifyIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/attestationVerify/export', {
        ...this.queryParams
      }, `attestation_verify_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
