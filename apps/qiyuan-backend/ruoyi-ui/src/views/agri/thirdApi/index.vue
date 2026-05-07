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

    <el-table v-loading="loading" :data="thirdApiList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="accessId" width="80" />
      <el-table-column label="接入编码" align="center" prop="accessCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="接入名称" align="center" prop="accessName" min-width="130" show-overflow-tooltip />
      <el-table-column label="API类型" align="center" prop="apiType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.agri_api_type" :value="scope.row.apiType" />
        </template>
      </el-table-column>
            <el-card shadow="never" class="api-hero mb16">
              <div class="api-head">
                <div>
                  <div class="api-title">第三方接入运维台</div>
                  <div class="api-desc">围绕天气、地图和通用接口做健康巡检，优先处理成功率下降、超时偏高和请求地址异常的配置。</div>
                </div>
                <div class="api-actions">
                  <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
                  <el-button size="mini" icon="el-icon-connection" @click="handleAdvice">接入建议</el-button>
                </div>
              </div>
              <el-row :gutter="12" class="api-kpi-row">
                <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
                  <div class="api-kpi-card">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                    <small>{{ item.foot }}</small>
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="12" class="api-overview-row">
                <el-col :xs="24" :lg="14">
                  <div class="api-panel">
                    <div class="panel-head"><span>近期接入</span><small>优先处理失败和未调用项</small></div>
                    <div class="api-list" v-if="recentRows.length">
                      <div v-for="item in recentRows" :key="item.accessId" class="api-item" @click="handleAdvice(item)">
                        <div>
                          <b>{{ item.accessName }}</b>
                          <p>{{ item.accessCode }} · {{ item.provider || '-' }}</p>
                        </div>
                        <el-tag size="mini" :type="item.callStatus === '1' ? 'success' : item.callStatus === '2' ? 'danger' : 'info'">{{ formatCallStatus(item.callStatus) }}</el-tag>
                      </div>
                    </div>
                    <el-empty v-else description="暂无接入记录" />
                  </div>
                </el-col>
                <el-col :xs="24" :lg="10">
                  <div class="api-panel">
                    <div class="panel-head"><span>接入建议</span><small>根据成功率和类型给出调整建议</small></div>
                    <div v-if="smartResult.accessId" class="api-box">
                      <div class="api-score"><strong>{{ smartResult.riskScore }}</strong><span>{{ smartResult.riskLevel }}风险</span></div>
                      <p>{{ smartResult.access.accessName }}</p>
                      <ul>
                        <li v-for="item in smartResult.suggestions" :key="item">{{ item }}</li>
                      </ul>
                    </div>
                    <el-empty v-else description="请选择一条接入配置进行建议分析" />
                  </div>
                </el-col>
              </el-row>
            </el-card>

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
          <el-button size="mini" type="text" icon="el-icon-connection" @click="handleAdvice(scope.row)" v-hasPermi="['agri:thirdApi:query']">建议</el-button>
          <el-button size="mini" type="text" icon="el-icon-connection" @click="handleProbe(scope.row)" v-hasPermi="['agri:thirdApi:edit']">探活</el-button>
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
              <el-select v-model="form.apiType" placeholder="请选择API类型" style="width: 100%" @change="handleApiTypeChange">
                <el-option v-for="dict in dict.type.agri_api_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="供应商" prop="provider"><el-input v-model="form.provider" placeholder="请输入供应商" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="请求地址" prop="endpointUrl"><el-input v-model="form.endpointUrl" :placeholder="getEndpointPlaceholder()" /></el-form-item>
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
  updateThirdApi,
  probeThirdApi,
  getThirdApiDashboard,
  getThirdApiAdvice
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
      dashboardData: {},
      smartResult: {},
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
    this.refreshDashboard()
    this.getList()
  },
  computed: {
    dashboardCards() {
      const summary = this.dashboardData.summary || {}
      return [
        { key: 'total', label: '接入总数', value: summary.totalCount || 0, foot: '当前筛选范围内记录' },
        { key: 'success', label: '成功调用', value: summary.successCount || 0, foot: '调用成功记录' },
        { key: 'failed', label: '失败调用', value: summary.failedCount || 0, foot: '调用失败记录' },
        { key: 'idle', label: '未调用', value: summary.idleCount || 0, foot: '尚未探活记录' }
      ]
    },
    recentRows() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    refreshDashboard() {
      getThirdApiDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listThirdApi(this.queryParams).then(response => {
        this.thirdApiList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatCallStatus(value) {
      const dict = {
        '1': '成功',
        '2': '失败',
        '0': '未调用'
      }
      return dict[value] || value
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
      this.refreshDashboard()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.refreshDashboard()
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.accessId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      if (!row) {
        return
      }
      this.handleSelectionChange([row])
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
    handleProbe(row) {
      const accessId = row.accessId || this.ids[0]
      this.$modal.confirm('是否确认对接入编号为"' + accessId + '"的第三方API执行探活？').then(() => {
        return probeThirdApi(accessId)
      }).then(response => {
        this.$modal.msgSuccess(response.msg || '探活完成')
        this.getList()
      }).catch(() => {})
    },
    handleAdvice(row) {
      const target = row || this.thirdApiList[0]
      if (!target || !target.accessId) {
        this.$modal.msgWarning('请先选择一条接入配置')
        return
      }
      getThirdApiAdvice(target.accessId).then(response => {
        this.smartResult = response.data || {}
        this.$modal.msgSuccess('接入建议已更新')
      })
    },
    handleApiTypeChange(value) {
      if (value === 'weather') {
        this.form.provider = this.form.provider || '和风天气'
        this.form.endpointUrl = 'https://devapi.qweather.com/v7/weather/now'
      } else if (value === 'map' || value === 'amap') {
        this.form.provider = this.form.provider || '高德地图'
        this.form.endpointUrl = 'https://restapi.amap.com/v3/direction/driving'
      }
    },
    getEndpointPlaceholder() {
      if (this.form.apiType === 'weather') {
        return '示例：https://devapi.qweather.com/v7/weather/now'
      }
      if (this.form.apiType === 'map' || this.form.apiType === 'amap') {
        return '示例：https://restapi.amap.com/v3/direction/driving'
      }
      return '请输入请求地址'
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

<style scoped>
.api-hero {
  border: 1px solid #dde7e1;
  background: linear-gradient(135deg, #f7fbf8 0%, #eef6f0 100%);
}

.api-head,
.panel-head,
.api-item,
.api-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.api-title {
  font-size: 20px;
  font-weight: 700;
  color: #28513e;
}

.api-desc {
  margin-top: 6px;
  color: #5c7268;
}

.api-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.api-kpi-row,
.api-overview-row {
  margin-top: 14px;
}

.api-kpi-card,
.api-panel {
  border: 1px solid #e2ece6;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.api-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.api-kpi-card span,
.panel-head small,
.api-kpi-card small,
.api-item p,
.api-box p {
  color: #667a70;
}

.api-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #28513e;
}

.api-item {
  padding: 8px 0;
  border-top: 1px solid #eef4ef;
  cursor: pointer;
}

.api-item p {
  margin: 4px 0 0;
}

.api-score strong {
  font-size: 26px;
  color: #2f6f55;
}

.api-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #54665d;
}
</style>
