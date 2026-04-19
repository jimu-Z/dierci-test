<template>
  <div class="app-container">
    <el-card shadow="never" class="env-hero mb16">
      <div class="env-hero-head">
        <div>
          <div class="env-title">环境监测指挥台</div>
          <div class="env-desc">围绕温度、湿度、CO2 和告警点位做快速巡检，先看态势，再处理明细记录。</div>
        </div>
        <div class="env-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="refreshDashboard">刷新态势</el-button>
          <el-button size="mini" icon="el-icon-magic-stick" :loading="smartLoading" @click="handleDiagnose">智能诊断</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="env-kpi-row">
        <el-col v-for="item in dashboardCards" :key="item.key" :xs="12" :sm="12" :md="6">
          <div class="env-kpi-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.foot }}</small>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="env-overview-row">
        <el-col :xs="24" :lg="14">
          <div class="env-panel">
            <div class="env-panel-head"><span>异常点位</span><small>优先处理高温、高湿、CO2 偏高记录</small></div>
            <el-empty v-if="!dashboardAlerts.length" description="暂无告警记录" />
            <div v-else class="env-alert-list">
              <div v-for="item in dashboardAlerts" :key="item.recordId" class="env-alert-item" @click="handleDiagnose(item)">
                <div>
                  <b>{{ item.deviceCode || '未命名设备' }}</b>
                  <p>{{ item.plotCode || '-' }} · {{ parseTime(item.collectTime) }}</p>
                </div>
                <el-tag size="mini" type="danger">{{ item.severity }}</el-tag>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="env-panel">
            <div class="env-panel-head"><span>智能诊断</span><small>基于最新记录生成可执行建议</small></div>
            <div v-if="smartResult.recordId" class="env-smart-box">
              <div class="env-smart-score">
                <strong>{{ smartResult.riskScore }}</strong>
                <span>{{ smartResult.riskLevel }}风险</span>
              </div>
              <p>{{ smartResult.deviceCode }} / {{ smartResult.plotCode }}</p>
              <div class="env-smart-summary">{{ smartResult.summary || '暂无诊断摘要' }}</div>
              <ul v-if="smartResult.findings.length">
                <li v-for="item in smartResult.findings" :key="item">{{ item }}</li>
              </ul>
              <div v-if="smartResult.aiOriginalExcerpt" class="env-smart-excerpt-box">
                <div class="env-smart-excerpt-title">AI原文摘录</div>
                <pre class="env-smart-excerpt">{{ smartResult.aiOriginalExcerpt }}</pre>
              </div>
              <div v-if="smartResult.requestTime" class="env-smart-footnote">最近诊断时间：{{ smartResult.requestTime }}</div>
            </div>
            <el-empty v-else description="请选择一条记录进行诊断" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="98px">
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input
          v-model="queryParams.deviceCode"
          placeholder="请输入设备编码"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="地块编码" prop="plotCode">
        <el-input
          v-model="queryParams.plotCode"
          placeholder="请输入地块编码"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
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
          v-hasPermi="['agri:envSensor:add']"
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
          v-hasPermi="['agri:envSensor:edit']"
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
          v-hasPermi="['agri:envSensor:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['agri:envSensor:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="envSensorList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="recordId" width="70" />
      <el-table-column label="设备编码" align="center" prop="deviceCode" width="130" />
      <el-table-column label="地块编码" align="center" prop="plotCode" width="130" />
      <el-table-column label="温度(℃)" align="center" prop="temperature" width="95" />
      <el-table-column label="湿度(%)" align="center" prop="humidity" width="95" />
      <el-table-column label="CO2(ppm)" align="center" prop="co2Value" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="采集时间" align="center" prop="collectTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.collectTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="dataSource" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['agri:envSensor:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['agri:envSensor:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="740px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="95px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备编码" prop="deviceCode">
              <el-input v-model="form.deviceCode" placeholder="请输入设备编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地块编码" prop="plotCode">
              <el-input v-model="form.plotCode" placeholder="请输入地块编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="温度(℃)" prop="temperature">
              <el-input-number v-model="form.temperature" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="湿度(%)" prop="humidity">
              <el-input-number v-model="form.humidity" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="CO2(ppm)" prop="co2Value">
              <el-input-number v-model="form.co2Value" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="采集时间" prop="collectTime">
              <el-date-picker
                v-model="form.collectTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择采集时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据来源" prop="dataSource">
              <el-input v-model="form.dataSource" placeholder="如：mqtt-gateway-01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
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
  listEnvSensor,
  getEnvSensor,
  delEnvSensor,
  addEnvSensor,
  updateEnvSensor,
  getEnvSensorDashboard,
  diagnoseEnvSensor
} from '@/api/agri/envSensor'

export default {
  name: 'EnvSensor',
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      envSensorList: [],
      dashboardData: {},
      smartLoading: false,
      smartResult: {
        recordId: null,
        deviceCode: '',
        plotCode: '',
        riskScore: '--',
        riskLevel: '--',
        findings: [],
        summary: '',
        aiOriginalExcerpt: '',
        requestTime: ''
      },
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceCode: undefined,
        plotCode: undefined,
        status: undefined
      },
      form: {},
      rules: {
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        plotCode: [{ required: true, message: '地块编码不能为空', trigger: 'blur' }],
        collectTime: [{ required: true, message: '采集时间不能为空', trigger: 'change' }]
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
        { key: 'total', label: '监测总量', value: summary.totalCount || 0, foot: '当前筛选范围内记录数' },
        { key: 'device', label: '设备数', value: summary.deviceCount || 0, foot: '去重设备编码数量' },
        { key: 'plot', label: '地块数', value: summary.plotCount || 0, foot: '覆盖的地块范围' },
        { key: 'alert', label: '告警数', value: summary.alertCount || 0, foot: '需要优先处理的异常点位' },
        { key: 'temp', label: '平均温度', value: this.formatMetric(summary.avgTemperature, '℃'), foot: '采样均值' },
        { key: 'hum', label: '平均湿度', value: this.formatMetric(summary.avgHumidity, '%'), foot: '采样均值' },
        { key: 'co2', label: '平均CO2', value: this.formatMetric(summary.avgCo2, 'ppm'), foot: '采样均值' },
        { key: 'normal', label: '正常记录', value: summary.normalCount || 0, foot: '状态平稳记录数' }
      ]
    },
    dashboardAlerts() {
      return this.dashboardData.alerts || []
    },
    dashboardRecentRows() {
      return this.dashboardData.recentRows || []
    }
  },
  methods: {
    refreshDashboard() {
      return getEnvSensorDashboard(this.queryParams).then(response => {
        this.dashboardData = response.data || {}
      })
    },
    getList() {
      this.loading = true
      listEnvSensor(this.queryParams).then(response => {
        this.envSensorList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatMetric(value, unit) {
      const text = value === undefined || value === null || value === '' ? '0.00' : Number(value).toFixed(2)
      return `${text}${unit}`
    },
    handleDiagnose(row) {
      const target = this.resolveDiagnoseTarget(row)
      if (!target || !target.recordId) {
        this.$modal.msgWarning('请先选择一条环境监测记录')
        return
      }
      this.smartLoading = true
      this.smartResult = {
        recordId: target.recordId,
        deviceCode: target.deviceCode || '',
        plotCode: target.plotCode || '',
        riskScore: '--',
        riskLevel: '--',
        findings: ['正在获取智能诊断结果...'],
        summary: '正在生成诊断结果，请稍候。',
        aiOriginalExcerpt: '',
        requestTime: ''
      }
      diagnoseEnvSensor(target.recordId).then(response => {
        const data = response.data || {}
        this.smartResult = {
          recordId: data.recordId || target.recordId,
          deviceCode: data.deviceCode || target.deviceCode || '',
          plotCode: data.plotCode || target.plotCode || '',
          riskScore: data.riskScore != null ? data.riskScore : '--',
          riskLevel: data.riskLevel || '--',
          findings: Array.isArray(data.findings) ? data.findings : [],
          summary: data.summary || '诊断已完成',
          aiOriginalExcerpt: data.aiOriginalExcerpt || '',
          requestTime: new Date().toLocaleString()
        }
        this.$modal.msgSuccess('智能诊断已更新')
      }).catch(error => {
        const message = (error && error.msg) || '智能诊断失败，请稍后重试'
        this.$modal.msgError(message)
      }).finally(() => {
        this.smartLoading = false
      })
    },
    resolveDiagnoseTarget(row) {
      if (row && row.recordId) {
        return row
      }
      if (this.dashboardAlerts.length) {
        return this.dashboardAlerts[0]
      }
      if (this.dashboardRecentRows.length) {
        return this.dashboardRecentRows[0]
      }
      if (this.envSensorList.length) {
        return this.envSensorList[0]
      }
      return null
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        recordId: undefined,
        deviceCode: undefined,
        plotCode: undefined,
        temperature: undefined,
        humidity: undefined,
        co2Value: undefined,
        status: '0',
        dataSource: 'manual',
        collectTime: undefined,
        remark: undefined
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
      this.ids = selection.map(item => item.recordId)
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
      this.title = '新增环境传感器数据'
    },
    handleUpdate(row) {
      this.reset()
      const recordId = row.recordId || this.ids
      getEnvSensor(recordId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改环境传感器数据'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.recordId !== undefined) {
          updateEnvSensor(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        addEnvSensor(this.form).then(() => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal
        .confirm('是否确认删除环境传感器数据编号为"' + recordIds + '"的数据项？')
        .then(function () {
          return delEnvSensor(recordIds)
        })
        .then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {})
    },
    handleExport() {
      this.download(
        'agri/envSensor/export',
        {
          ...this.queryParams
        },
        `env_sensor_${new Date().getTime()}.xlsx`
      )
    }
  }
}
</script>

<style scoped>
.env-hero {
  border: 1px solid #dfe7dd;
  background: linear-gradient(135deg, #f7fbf7 0%, #eef6ef 100%);
}

.env-hero-head,
.env-panel-head,
.env-alert-item,
.env-smart-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.env-title {
  font-size: 20px;
  font-weight: 700;
  color: #173b2d;
}

.env-desc {
  margin-top: 6px;
  color: #5d6f64;
}

.env-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.env-kpi-row,
.env-overview-row {
  margin-top: 14px;
}

.env-kpi-card,
.env-panel {
  border: 1px solid #e3ece4;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}

.env-kpi-card {
  min-height: 88px;
  margin-bottom: 12px;
}

.env-kpi-card span,
.env-panel-head small,
.env-alert-item p,
.env-smart-box p,
.env-kpi-card small {
  color: #6c7d72;
}

.env-kpi-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
  color: #173b2d;
}

.env-panel-head {
  margin-bottom: 12px;
}

.env-alert-list {
  display: grid;
  gap: 10px;
}

.env-alert-item {
  padding: 10px 12px;
  border-radius: 8px;
  background: #fafdf9;
  cursor: pointer;
}

.env-alert-item p {
  margin: 4px 0 0;
}

.env-smart-box ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #4b5f55;
}

.env-smart-summary {
  margin-top: 10px;
  color: #2f4136;
  line-height: 1.8;
}

.env-smart-excerpt-box {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f7fbf7;
  border: 1px solid #e4efe5;
}

.env-smart-excerpt-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #264235;
}

.env-smart-excerpt {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #4b5f55;
}

.env-smart-footnote {
  margin-top: 10px;
  font-size: 12px;
  color: #748579;
}

.env-smart-score {
  margin-bottom: 10px;
}

.env-smart-score strong {
  font-size: 26px;
  color: #2c6b48;
}
</style>
