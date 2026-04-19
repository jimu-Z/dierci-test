package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriLogisticsTempHumidityService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
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
 * 物流温湿度监控Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsTemp")
public class AgriLogisticsTempHumidityController extends BaseController
{
    @Autowired
    private IAgriLogisticsTempHumidityService agriLogisticsTempHumidityService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        startPage();
        List<AgriLogisticsTempHumidity> list = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityList(agriLogisticsTempHumidity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        return success(buildDashboard(agriLogisticsTempHumidity));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:query')")
    @GetMapping("/smart/diagnose/{recordId}")
    public AjaxResult diagnose(@PathVariable("recordId") Long recordId)
    {
        AgriLogisticsTempHumidity record = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityByRecordId(recordId);
        if (record == null)
        {
            return error("温湿度记录不存在");
        }
        return success(buildDiagnosis(record));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:export')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        List<AgriLogisticsTempHumidity> list = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityList(agriLogisticsTempHumidity);
        ExcelUtil<AgriLogisticsTempHumidity> util = new ExcelUtil<>(AgriLogisticsTempHumidity.class);
        util.exportExcel(response, list, "物流温湿度监控数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityByRecordId(recordId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:add')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        agriLogisticsTempHumidity.setCreateBy(getUsername());
        return toAjax(agriLogisticsTempHumidityService.insertAgriLogisticsTempHumidity(agriLogisticsTempHumidity));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:edit')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        agriLogisticsTempHumidity.setUpdateBy(getUsername());
        return toAjax(agriLogisticsTempHumidityService.updateAgriLogisticsTempHumidity(agriLogisticsTempHumidity));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:remove')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(agriLogisticsTempHumidityService.deleteAgriLogisticsTempHumidityByRecordIds(recordIds));
    }

    private Map<String, Object> buildDashboard(AgriLogisticsTempHumidity query)
    {
        List<AgriLogisticsTempHumidity> records = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityList(query);
        int alertCount = 0;
        BigDecimal tempSum = BigDecimal.ZERO;
        BigDecimal humiditySum = BigDecimal.ZERO;
        int tempCount = 0;
        int humidityCount = 0;
        List<AgriLogisticsTempHumidity> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AgriLogisticsTempHumidity::getCollectTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> alertRows = new ArrayList<>();
        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriLogisticsTempHumidity record : records)
        {
            if (record == null)
            {
                continue;
            }
            if (record.getTemperature() != null)
            {
                tempSum = tempSum.add(record.getTemperature());
                tempCount++;
            }
            if (record.getHumidity() != null)
            {
                humiditySum = humiditySum.add(record.getHumidity());
                humidityCount++;
            }
            if ("1".equals(record.getAlertFlag()))
            {
                alertCount++;
                if (alertRows.size() < 5)
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("recordId", record.getRecordId());
                    row.put("traceCode", record.getTraceCode());
                    row.put("deviceCode", record.getDeviceCode());
                    row.put("collectLocation", record.getCollectLocation());
                    row.put("temperature", record.getTemperature());
                    row.put("humidity", record.getHumidity());
                    row.put("alertMessage", record.getAlertMessage());
                    row.put("collectTime", record.getCollectTime());
                    alertRows.add(row);
                }
            }
        }

        for (AgriLogisticsTempHumidity record : sorted)
        {
            if (recentRows.size() >= 8)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordId", record.getRecordId());
            row.put("traceCode", record.getTraceCode());
            row.put("deviceCode", record.getDeviceCode());
            row.put("collectLocation", record.getCollectLocation());
            row.put("temperature", record.getTemperature());
            row.put("humidity", record.getHumidity());
            row.put("alertFlag", record.getAlertFlag());
            row.put("alertMessage", record.getAlertMessage());
            row.put("collectTime", record.getCollectTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", records.size());
        summary.put("alertCount", alertCount);
        summary.put("normalCount", Math.max(0, records.size() - alertCount));
        summary.put("avgTemperature", tempCount == 0 ? BigDecimal.ZERO : tempSum.divide(BigDecimal.valueOf(tempCount), 2, BigDecimal.ROUND_HALF_UP));
        summary.put("avgHumidity", humidityCount == 0 ? BigDecimal.ZERO : humiditySum.divide(BigDecimal.valueOf(humidityCount), 2, BigDecimal.ROUND_HALF_UP));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("alerts", alertRows);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildDiagnosis(AgriLogisticsTempHumidity record)
    {
        int score = 100;
        List<String> suggestions = new ArrayList<>();
        if (record.getTemperature() != null)
        {
            if (record.getTempUpperLimit() != null && record.getTemperature().compareTo(record.getTempUpperLimit()) > 0)
            {
                suggestions.add("温度高于上限，建议检查冷链、通风或保温设置");
                score -= 30;
            }
            else if (record.getTempLowerLimit() != null && record.getTemperature().compareTo(record.getTempLowerLimit()) < 0)
            {
                suggestions.add("温度低于下限，建议检查保温和装卸效率");
                score -= 20;
            }
        }
        if (record.getHumidity() != null)
        {
            if (record.getHumidityUpperLimit() != null && record.getHumidity().compareTo(record.getHumidityUpperLimit()) > 0)
            {
                suggestions.add("湿度偏高，建议检查通风与密封状态");
                score -= 25;
            }
            else if (record.getHumidityLowerLimit() != null && record.getHumidity().compareTo(record.getHumidityLowerLimit()) < 0)
            {
                suggestions.add("湿度偏低，建议检查防失水措施");
                score -= 15;
            }
        }
        if ("1".equals(record.getAlertFlag()))
        {
            suggestions.add("记录已触发系统告警，应同步生成运输处置单");
            score -= 10;
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前温湿度处于合理区间，可继续按现有冷链策略运行");
        }

        int safeScore = Math.max(0, score);
        String riskLevel = safeScore >= 85 ? "低" : safeScore >= 70 ? "中" : "高";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "物流温湿度智能诊断");
            context.put("recordId", record.getRecordId());
            context.put("traceCode", record.getTraceCode());
            context.put("deviceCode", record.getDeviceCode());
            context.put("collectLocation", record.getCollectLocation());
            context.put("temperature", record.getTemperature());
            context.put("humidity", record.getHumidity());
            context.put("tempUpperLimit", record.getTempUpperLimit());
            context.put("tempLowerLimit", record.getTempLowerLimit());
            context.put("humidityUpperLimit", record.getHumidityUpperLimit());
            context.put("humidityLowerLimit", record.getHumidityLowerLimit());
            context.put("alertFlag", record.getAlertFlag());
            context.put("alertMessage", record.getAlertMessage());
            context.put("ruleRiskScore", safeScore);
            context.put("ruleRiskLevel", riskLevel);
            context.put("ruleSuggestions", suggestions);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("物流温湿度智能诊断", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getRiskLevel()))
            {
                riskLevel = aiResult.getRiskLevel();
            }
            if (StringUtils.isNotBlank(aiResult.getSuggestion()))
            {
                suggestions.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotBlank(aiOriginalExcerpt))
            {
                suggestions.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", record.getRecordId());
        result.put("riskScore", safeScore);
        result.put("riskLevel", riskLevel);
        result.put("suggestions", suggestions);
        result.put("record", record);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return result;
    }
}
