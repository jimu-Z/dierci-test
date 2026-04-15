<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="角色标识" prop="roleKey">
        <el-select v-model="queryParams.roleKey" clearable style="width: 160px">
          <el-option v-for="item in roleKeyOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="授权状态" prop="grantStatus">
        <el-select v-model="queryParams.grantStatus" clearable style="width: 140px">
          <el-option v-for="item in grantStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['agri:userAccess:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-key" size="mini" :disabled="single" @click="handleGrant" v-hasPermi="['agri:userAccess:edit']">授权</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['agri:userAccess:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['agri:userAccess:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="grantList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" prop="grantId" align="center" width="70" />
      <el-table-column label="用户账号" prop="userName" align="center" width="130" />
      <el-table-column label="用户昵称" prop="nickName" align="center" width="120" />
      <el-table-column label="角色标识" prop="roleKey" align="center" width="120" />
      <el-table-column label="数据权限" prop="dataScope" align="center" width="100" />
      <el-table-column label="菜单范围" prop="menuScope" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="授权状态" align="center" width="100">
        <template slot-scope="scope">{{ formatGrantStatus(scope.row.grantStatus) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" align="center" width="170">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['agri:userAccess:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['agri:userAccess:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="用户账号" prop="userName"><el-input v-model="form.userName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用户昵称" prop="nickName"><el-input v-model="form.nickName" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="角色标识" prop="roleKey"><el-select v-model="form.roleKey" style="width: 100%"><el-option v-for="item in roleKeyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数据权限" prop="dataScope"><el-select v-model="form.dataScope" style="width: 100%"><el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="菜单范围" prop="menuScope"><el-input v-model="form.menuScope" placeholder="例如 agri:baseRule,agri:consumerScan" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="授权状态" prop="grantStatus"><el-select v-model="form.grantStatus" style="width: 100%"><el-option v-for="item in grantStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" /></el-form-item></el-col>
        </el-row>
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
  listUserAccess,
  getUserAccess,
  addUserAccess,
  updateUserAccess,
  grantUserAccess,
  delUserAccess
} from '@/api/agri/userAccess'

export default {
  name: 'UserAccess',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      grantList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        roleKey: undefined,
        grantStatus: undefined
      },
      form: {},
      roleKeyOptions: [
        { label: 'agri_admin', value: 'agri_admin' },
        { label: 'agri_operator', value: 'agri_operator' },
        { label: 'agri_viewer', value: 'agri_viewer' }
      ],
      dataScopeOptions: [
        { label: '全部数据', value: 'ALL' },
        { label: '本部门', value: 'DEPT' },
        { label: '仅本人', value: 'SELF' }
      ],
      grantStatusOptions: [
        { label: '待审核', value: '0' },
        { label: '已授权', value: '1' },
        { label: '已驳回', value: '2' }
      ],
      rules: {
        userName: [{ required: true, message: '用户账号不能为空', trigger: 'blur' }],
        roleKey: [{ required: true, message: '角色标识不能为空', trigger: 'change' }],
        dataScope: [{ required: true, message: '数据权限不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listUserAccess(this.queryParams).then(response => {
        this.grantList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatGrantStatus(value) {
      const option = this.grantStatusOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        grantId: undefined,
        userName: undefined,
        nickName: undefined,
        roleKey: undefined,
        dataScope: 'SELF',
        menuScope: undefined,
        grantStatus: '0',
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
      this.ids = selection.map(item => item.grantId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增用户权限管理'
    },
    handleUpdate(row) {
      this.reset()
      const grantId = row.grantId || this.ids
      getUserAccess(grantId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改用户权限管理'
      })
    },
    handleGrant() {
      if (this.ids.length !== 1) {
        this.$modal.msgWarning('请选择一条记录进行授权')
        return
      }
      grantUserAccess(this.ids[0]).then(() => {
        this.$modal.msgSuccess('授权成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.grantId !== undefined) {
          updateUserAccess(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addUserAccess(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const grantIds = row.grantId || this.ids
      this.$modal
        .confirm('是否确认删除用户权限管理编号为"' + grantIds + '"的数据项？')
        .then(function () {
          return delUserAccess(grantIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/userAccess/export',
        {
          ...this.queryParams
        },
        `user_access_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>
