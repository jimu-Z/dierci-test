<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="业务编号" prop="bizNo">
        <el-input
          v-model="queryParams.bizNo"
          placeholder="请输入业务编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模块名称" prop="moduleName">
        <el-input
          v-model="queryParams.moduleName"
          placeholder="请输入模块名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作类型" prop="actionType">
        <el-select v-model="queryParams.actionType" placeholder="请选择操作类型" clearable>
          <el-option
            v-for="dict in dict.type.agri_audit_action_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="操作结果" prop="operateResult">
        <el-select v-model="queryParams.operateResult" placeholder="请选择操作结果" clearable>
          <el-option
            v-for="dict in dict.type.agri_audit_result"
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
          v-hasPermi="['agri:auditLog:add']"
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
          v-hasPermi="['agri:auditLog:edit']"
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
          v-hasPermi="['agri:auditLog:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['agri:auditLog:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="auditLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="auditId" width="80" />
      <el-table-column label="业务编号" align="center" prop="bizNo" min-width="140" show-overflow-tooltip />
      <el-table-column label="模块名称" align="center" prop="moduleName" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作类型" align="center" prop="actionType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_audit_action_type" :value="scope.row.actionType" />
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
      <el-table-column label="操作时间" align="center" prop="operateTime" width="160" />
      <el-table-column label="操作结果" align="center" prop="operateResult">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_audit_result" :value="scope.row.operateResult" />
        </template>
      </el-table-column>
      <el-table-column label="交易哈希" align="center" prop="txHash" min-width="160" show-overflow-tooltip />
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="140" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['agri:auditLog:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['agri:auditLog:remove']"
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
            <el-form-item label="业务编号" prop="bizNo">
              <el-input v-model="form.bizNo" placeholder="请输入业务编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块名称" prop="moduleName">
              <el-input v-model="form.moduleName" placeholder="请输入模块名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作类型" prop="actionType">
              <el-select v-model="form.actionType" placeholder="请选择操作类型" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.agri_audit_action_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作结果" prop="operateResult">
              <el-select v-model="form.operateResult" placeholder="请选择操作结果" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.agri_audit_result"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作人" prop="operatorName">
              <el-input v-model="form.operatorName" placeholder="请输入操作人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作时间" prop="operateTime">
              <el-date-picker
                clearable
                v-model="form.operateTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择操作时间"
                style="width: 100%"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="交易哈希" prop="txHash">
          <el-input v-model="form.txHash" placeholder="请输入交易哈希" />
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="form.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
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
  listAuditLog,
  getAuditLog,
  delAuditLog,
  addAuditLog,
  updateAuditLog
} from '@/api/agri/auditLog'

export default {
  name: 'AuditLog',
  dicts: ['agri_audit_action_type', 'agri_audit_result'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      auditLogList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        bizNo: null,
        moduleName: null,
        actionType: null,
        operateResult: null
      },
      form: {},
      rules: {
        bizNo: [{ required: true, message: '业务编号不能为空', trigger: 'blur' }],
        moduleName: [{ required: true, message: '模块名称不能为空', trigger: 'blur' }],
        actionType: [{ required: true, message: '操作类型不能为空', trigger: 'change' }],
        operateResult: [{ required: true, message: '操作结果不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAuditLog(this.queryParams).then(response => {
        this.auditLogList = response.rows
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
        auditId: null,
        bizNo: null,
        moduleName: null,
        actionType: null,
        operatorName: null,
        operateTime: null,
        operateResult: '1',
        txHash: null,
        ipAddress: null,
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
      this.ids = selection.map(item => item.auditId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增溯源审计日志'
    },
    handleUpdate(row) {
      this.reset()
      const auditId = row.auditId || this.ids[0]
      getAuditLog(auditId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改溯源审计日志'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.auditId != null) {
            updateAuditLog(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addAuditLog(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const auditIds = row.auditId || this.ids
      this.$modal.confirm('是否确认删除溯源审计日志编号为"' + auditIds + '"的数据项？').then(() => {
        return delAuditLog(auditIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('agri/auditLog/export', {
        ...this.queryParams
      }, `audit_log_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
