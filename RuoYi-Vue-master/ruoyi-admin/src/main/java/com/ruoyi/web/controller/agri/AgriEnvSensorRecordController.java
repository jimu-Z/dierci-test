package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDeviceAccessNode;
import com.ruoyi.system.domain.AgriEnvSensorRecord;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.integration.AgriIntegrationProperties;
import com.ruoyi.system.service.IAgriDeviceAccessNodeService;
import com.ruoyi.system.service.IAgriEnvSensorRecordService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环境传感器数据Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/envSensor")
public class AgriEnvSensorRecordController extends BaseController
{
    @Autowired
    private IAgriEnvSensorRecordService agriEnvSensorRecordService;

    @Autowired
    private IAgriDeviceAccessNodeService agriDeviceAccessNodeService;

    @Autowired
    private AgriIntegrationProperties agriIntegrationProperties;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    /**
     * 查询环境传感器数据列表
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        startPage();
        List<AgriEnvSensorRecord> list = agriEnvSensorRecordService.selectAgriEnvSensorRecordList(agriEnvSensorRecord);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:envSensor:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        return success(buildDashboard(agriEnvSensorRecord));
    }

    @PreAuthorize("@ss.hasPermi('agri:envSensor:query')")
    @GetMapping("/smart/diagnose/{recordId}")
    public AjaxResult diagnose(@PathVariable("recordId") Long recordId)
    {
        AgriEnvSensorRecord record = agriEnvSensorRecordService.selectAgriEnvSensorRecordByRecordId(recordId);
        if (record == null)
        {
            return error("环境传感器记录不存在");
        }
        return success(buildDiagnosis(record));
    }

    /**
     * 导出环境传感器数据列表
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:export')")
    @Log(title = "环境传感器数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriEnvSensorRecord agriEnvSensorRecord)
    {
        List<AgriEnvSensorRecord> list = agriEnvSensorRecordService.selectAgriEnvSensorRecordList(agriEnvSensorRecord);
        ExcelUtil<AgriEnvSensorRecord> util = new ExcelUtil<>(AgriEnvSensorRecord.class);
        util.exportExcel(response, list, "环境传感器数据");
    }

    /**
     * 获取环境传感器数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(agriEnvSensorRecordService.selectAgriEnvSensorRecordByRecordId(recordId));
    }

    /**
     * 新增环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:add')")
    @Log(title = "环境传感器数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriEnvSensorRecord agriEnvSensorRecord)
    {
        agriEnvSensorRecord.setCreateBy(getUsername());
        return toAjax(agriEnvSensorRecordService.insertAgriEnvSensorRecord(agriEnvSensorRecord));
    }

    /**
     * 修改环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:edit')")
    @Log(title = "环境传感器数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriEnvSensorRecord agriEnvSensorRecord)
    {
        agriEnvSensorRecord.setUpdateBy(getUsername());
        return toAjax(agriEnvSensorRecordService.updateAgriEnvSensorRecord(agriEnvSensorRecord));
    }

    /**
     * 删除环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:remove')")
    @Log(title = "环境传感器数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(agriEnvSensorRecordService.deleteAgriEnvSensorRecordByRecordIds(recordIds));
    }

    @Anonymous
    @PostMapping("/ingest")
    public AjaxResult ingest(@RequestBody AgriEnvSensorRecord agriEnvSensorRecord, HttpServletRequest request)
    {
        return ingestInternal(agriEnvSensorRecord, request, agriIntegrationProperties.getSensor().getDataSource());
    }

    @Anonymous
    @PostMapping("/ingest/emqx")
    public AjaxResult ingestEmqx(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        AgriEnvSensorRecord agriEnvSensorRecord = mapFromEmqx(body);
        return ingestInternal(agriEnvSensorRecord, request, agriIntegrationProperties.getSensor().getEmqx().getDataSource());
    }

    private AjaxResult ingestInternal(AgriEnvSensorRecord agriEnvSensorRecord, HttpServletRequest request, String dataSource)
    {
        String configuredToken = agriIntegrationProperties.getSensor().getIngestToken();
        if (StringUtils.isNotBlank(configuredToken))
        {
            String headerName = agriIntegrationProperties.getSensor().getAuthHeaderName();
            String requestToken = request.getHeader(headerName);
            if (StringUtils.isBlank(requestToken))
            {
                requestToken = request.getHeader("X-Agri-Token");
            }
            if (!StringUtils.equals(configuredToken, requestToken))
            {
                return error("传感器接入令牌无效");
            }
        }

        if (StringUtils.isBlank(agriEnvSensorRecord.getDeviceCode()) || StringUtils.isBlank(agriEnvSensorRecord.getPlotCode()))
        {
            return error("设备编码和地块编码不能为空");
        }

        AgriDeviceAccessNode query = new AgriDeviceAccessNode();
        query.setDeviceCode(agriEnvSensorRecord.getDeviceCode());
        List<AgriDeviceAccessNode> nodes = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(query);
        if (nodes == null || nodes.isEmpty())
        {
            return error("设备未注册，拒绝接入");
        }

        AgriDeviceAccessNode node = nodes.get(0);
        node.setAccessStatus("1");
        node.setLastOnlineTime(DateUtils.getNowDate());
        node.setUpdateBy("gateway");
        agriDeviceAccessNodeService.updateAgriDeviceAccessNode(node);

        agriEnvSensorRecord.setDataSource(StringUtils.defaultIfBlank(dataSource, agriIntegrationProperties.getSensor().getDataSource()));
        if (agriEnvSensorRecord.getCollectTime() == null)
        {
            agriEnvSensorRecord.setCollectTime(DateUtils.getNowDate());
        }
        agriEnvSensorRecord.setStatus(isAlert(agriEnvSensorRecord) ? "1" : "0");
        agriEnvSensorRecord.setCreateBy("gateway");

        int rows = agriEnvSensorRecordService.insertAgriEnvSensorRecord(agriEnvSensorRecord);
        if (rows <= 0)
        {
            return error("上报入库失败");
        }
        return success("接收成功");
    }

    private AgriEnvSensorRecord mapFromEmqx(Map<String, Object> body)
    {
        Map<String, Object> safeBody = body == null ? Map.of() : body;
        Map<String, Object> payload = asMap(safeBody.get("payload"));
        Map<String, Object> data = asMap(safeBody.get("data"));

        AgriEnvSensorRecord record = new AgriEnvSensorRecord();
        record.setDeviceCode(firstNonBlank(
            readString(safeBody, "deviceCode", "device_code", "deviceId", "clientid", "clientId"),
            readString(payload, "deviceCode", "device_code", "deviceId", "clientid", "clientId"),
            readString(data, "deviceCode", "device_code", "deviceId", "clientid", "clientId")
        ));
        record.setPlotCode(firstNonBlank(
            readString(safeBody, "plotCode", "plot_code"),
            readString(payload, "plotCode", "plot_code"),
            readString(data, "plotCode", "plot_code")
        ));
        record.setTemperature(firstDecimal(
            readDecimal(safeBody, "temperature", "temp"),
            readDecimal(payload, "temperature", "temp"),
            readDecimal(data, "temperature", "temp")
        ));
        record.setHumidity(firstDecimal(
            readDecimal(safeBody, "humidity", "hum"),
            readDecimal(payload, "humidity", "hum"),
            readDecimal(data, "humidity", "hum")
        ));
        record.setCo2Value(firstDecimal(
            readDecimal(safeBody, "co2", "co2Value", "co2_value"),
            readDecimal(payload, "co2", "co2Value", "co2_value"),
            readDecimal(data, "co2", "co2Value", "co2_value")
        ));

        Date collectTime = firstDate(
            readDate(safeBody, "collectTime", "collect_time", "timestamp", "ts"),
            readDate(payload, "collectTime", "collect_time", "timestamp", "ts"),
            readDate(data, "collectTime", "collect_time", "timestamp", "ts")
        );
        if (collectTime != null)
        {
            record.setCollectTime(collectTime);
        }

        String topic = firstNonBlank(
            readString(safeBody, "topic"),
            readString(payload, "topic"),
            readString(data, "topic")
        );
        applyTopicFallback(record, topic);
        return record;
    }

    private void applyTopicFallback(AgriEnvSensorRecord record, String topic)
    {
        if (StringUtils.isBlank(topic))
        {
            return;
        }
        String[] segments = StringUtils.split(topic, '/');
        if (segments == null || segments.length < 2)
        {
            return;
        }
        if (StringUtils.isBlank(record.getPlotCode()))
        {
            record.setPlotCode(segments[segments.length - 2]);
        }
        if (StringUtils.isBlank(record.getDeviceCode()))
        {
            record.setDeviceCode(segments[segments.length - 1]);
        }
    }

    private String firstNonBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(BigDecimal... values)
    {
        if (values == null)
        {
            return null;
        }
        for (BigDecimal value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private Date firstDate(Date... values)
    {
        if (values == null)
        {
            return null;
        }
        for (Date value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private String readString(Map<String, Object> source, String... keys)
    {
        if (source == null || keys == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object value = source.get(key);
            if (value != null)
            {
                String text = String.valueOf(value);
                if (StringUtils.isNotBlank(text))
                {
                    return text;
                }
            }
        }
        return null;
    }

    private BigDecimal readDecimal(Map<String, Object> source, String... keys)
    {
        if (source == null || keys == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object value = source.get(key);
            if (value == null)
            {
                continue;
            }
            if (value instanceof Number)
            {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            try
            {
                return new BigDecimal(String.valueOf(value));
            }
            catch (Exception ignored)
            {
                // ignore invalid numeric field and continue fallback
            }
        }
        return null;
    }

    private Date readDate(Map<String, Object> source, String... keys)
    {
        if (source == null || keys == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object value = source.get(key);
            if (value == null)
            {
                continue;
            }
            if (value instanceof Number)
            {
                long ts = ((Number) value).longValue();
                if (ts > 0)
                {
                    if (ts < 100000000000L)
                    {
                        ts = ts * 1000;
                    }
                    return new Date(ts);
                }
            }
            try
            {
                return DateUtils.parseDate(value);
            }
            catch (Exception ignored)
            {
                // ignore invalid date field and continue fallback
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value)
    {
        if (value instanceof Map<?, ?> map)
        {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private boolean isAlert(AgriEnvSensorRecord record)
    {
        BigDecimal t = record.getTemperature();
        BigDecimal h = record.getHumidity();
        if (t != null && t.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getTempAlertHigh())) > 0)
        {
            return true;
        }
        return h != null && h.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getHumidityAlertHigh())) > 0;
    }

    private Map<String, Object> buildDashboard(AgriEnvSensorRecord query)
    {
        List<AgriEnvSensorRecord> records = agriEnvSensorRecordService.selectAgriEnvSensorRecordList(query);
        Set<String> deviceCodes = new HashSet<>();
        Set<String> plotCodes = new HashSet<>();
        int alertCount = 0;
        int normalCount = 0;
        BigDecimal tempTotal = BigDecimal.ZERO;
        BigDecimal humidityTotal = BigDecimal.ZERO;
        BigDecimal co2Total = BigDecimal.ZERO;
        int tempCount = 0;
        int humidityCount = 0;
        int co2Count = 0;
        List<AgriEnvSensorRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AgriEnvSensorRecord::getCollectTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> alertRows = new ArrayList<>();
        for (AgriEnvSensorRecord record : records)
        {
            if (record == null)
            {
                continue;
            }
            if (StringUtils.isNotBlank(record.getDeviceCode()))
            {
                deviceCodes.add(record.getDeviceCode());
            }
            if (StringUtils.isNotBlank(record.getPlotCode()))
            {
                plotCodes.add(record.getPlotCode());
            }

            BigDecimal temperature = record.getTemperature();
            if (temperature != null)
            {
                tempTotal = tempTotal.add(temperature);
                tempCount++;
            }
            BigDecimal humidity = record.getHumidity();
            if (humidity != null)
            {
                humidityTotal = humidityTotal.add(humidity);
                humidityCount++;
            }
            BigDecimal co2 = record.getCo2Value();
            if (co2 != null)
            {
                co2Total = co2Total.add(co2);
                co2Count++;
            }

            boolean alert = isAlert(record);
            if (alert)
            {
                alertCount++;
                if (alertRows.size() < 5)
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("recordId", record.getRecordId());
                    row.put("deviceCode", record.getDeviceCode());
                    row.put("plotCode", record.getPlotCode());
                    row.put("temperature", record.getTemperature());
                    row.put("humidity", record.getHumidity());
                    row.put("co2Value", record.getCo2Value());
                    row.put("collectTime", record.getCollectTime());
                    row.put("status", record.getStatus());
                    row.put("severity", buildSeverity(record));
                    alertRows.add(row);
                }
            }
            else
            {
                normalCount++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", records.size());
        summary.put("deviceCount", deviceCodes.size());
        summary.put("plotCount", plotCodes.size());
        summary.put("alertCount", alertCount);
        summary.put("normalCount", normalCount);
        summary.put("avgTemperature", scale(tempCount == 0 ? BigDecimal.ZERO : tempTotal.divide(BigDecimal.valueOf(tempCount), 2, BigDecimal.ROUND_HALF_UP)));
        summary.put("avgHumidity", scale(humidityCount == 0 ? BigDecimal.ZERO : humidityTotal.divide(BigDecimal.valueOf(humidityCount), 2, BigDecimal.ROUND_HALF_UP)));
        summary.put("avgCo2", scale(co2Count == 0 ? BigDecimal.ZERO : co2Total.divide(BigDecimal.valueOf(co2Count), 2, BigDecimal.ROUND_HALF_UP)));

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriEnvSensorRecord record : sorted)
        {
            if (recentRows.size() >= 8)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordId", record.getRecordId());
            row.put("deviceCode", record.getDeviceCode());
            row.put("plotCode", record.getPlotCode());
            row.put("temperature", record.getTemperature());
            row.put("humidity", record.getHumidity());
            row.put("co2Value", record.getCo2Value());
            row.put("status", record.getStatus());
            row.put("collectTime", record.getCollectTime());
            row.put("alert", isAlert(record));
            recentRows.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("alerts", alertRows);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildDiagnosis(AgriEnvSensorRecord record)
    {
        List<String> findings = new ArrayList<>();
        int score = 100;
        BigDecimal temperature = record.getTemperature();
        BigDecimal humidity = record.getHumidity();
        BigDecimal co2 = record.getCo2Value();

        if (temperature != null)
        {
            if (temperature.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getTempAlertHigh())) > 0)
            {
                findings.add("温度高于阈值，建议检查遮阳、灌溉或通风策略");
                score -= 25;
            }
            else if (temperature.compareTo(BigDecimal.valueOf(Math.min(18D, agriIntegrationProperties.getSensor().getTempAlertHigh() * 0.6D))) < 0)
            {
                findings.add("温度偏低，建议核对夜间保温与环境联动控制");
                score -= 15;
            }
        }
        if (humidity != null)
        {
            if (humidity.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getHumidityAlertHigh())) > 0)
            {
                findings.add("湿度偏高，存在病害扩散或凝露风险");
                score -= 20;
            }
            else if (humidity.compareTo(BigDecimal.valueOf(40D)) < 0)
            {
                findings.add("湿度偏低，建议补充喷雾或调整通风强度");
                score -= 15;
            }
        }
        if (co2 != null && co2.compareTo(BigDecimal.valueOf(1200)) > 0)
        {
            findings.add("CO2偏高，建议排查棚内通风与密闭状态");
            score -= 10;
        }
        if (isAlert(record))
        {
            findings.add("记录已触发告警标记，建议同步复核设备状态和上报链路");
            score -= 10;
        }
        if (findings.isEmpty())
        {
            findings.add("当前监测值整体平稳，可继续保持当前环境策略");
        }

        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "环境传感智能诊断");
            context.put("recordId", record.getRecordId());
            context.put("deviceCode", record.getDeviceCode());
            context.put("plotCode", record.getPlotCode());
            context.put("temperature", temperature);
            context.put("humidity", humidity);
            context.put("co2Value", co2);
            context.put("status", record.getStatus());
            context.put("collectTime", record.getCollectTime());
            context.put("ruleFindings", findings);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("环境传感智能诊断", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getInsightSummary()))
            {
                findings.add("AI结论：" + aiResult.getInsightSummary());
            }
            if (StringUtils.isNotBlank(aiResult.getSuggestion()))
            {
                findings.add("AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotBlank(aiOriginalExcerpt))
            {
                findings.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", record.getRecordId());
        result.put("deviceCode", record.getDeviceCode());
        result.put("plotCode", record.getPlotCode());
        result.put("riskScore", Math.max(0, score));
        result.put("riskLevel", score >= 85 ? "低" : score >= 70 ? "中" : "高");
        result.put("findings", findings);
        result.put("temperature", record.getTemperature());
        result.put("humidity", record.getHumidity());
        result.put("co2Value", record.getCo2Value());
        result.put("collectTime", record.getCollectTime());
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return result;
    }

    private BigDecimal scale(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String buildSeverity(AgriEnvSensorRecord record)
    {
        BigDecimal score = BigDecimal.ZERO;
        if (record.getTemperature() != null && record.getTemperature().compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getTempAlertHigh())) > 0)
        {
            score = score.add(BigDecimal.valueOf(40));
        }
        if (record.getHumidity() != null && record.getHumidity().compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getHumidityAlertHigh())) > 0)
        {
            score = score.add(BigDecimal.valueOf(35));
        }
        if (record.getCo2Value() != null && record.getCo2Value().compareTo(BigDecimal.valueOf(1200)) > 0)
        {
            score = score.add(BigDecimal.valueOf(25));
        }
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0)
        {
            return "严重";
        }
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0)
        {
            return "预警";
        }
        if (score.compareTo(BigDecimal.valueOf(20)) >= 0)
        {
            return "提示";
        }
        return "正常";
    }
}
