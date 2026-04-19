package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTrackSummary;
import com.ruoyi.system.domain.AgriLogisticsTrack;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriLogisticsTrackService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * 物流路径追踪Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsTrack")
public class AgriLogisticsTrackController extends BaseController
{
    @Autowired
    private IAgriLogisticsTrackService agriLogisticsTrackService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsTrack agriLogisticsTrack)
    {
        startPage();
        List<AgriLogisticsTrack> list = agriLogisticsTrackService.selectAgriLogisticsTrackList(agriLogisticsTrack);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriLogisticsTrack> list = agriLogisticsTrackService.selectAgriLogisticsTrackList(new AgriLogisticsTrack());
        return success(buildOpsResult(list));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping("/smart/analyze/{trackId}")
    public AjaxResult smartAnalyze(@PathVariable("trackId") Long trackId)
    {
        AgriLogisticsTrack track = agriLogisticsTrackService.selectAgriLogisticsTrackByTrackId(trackId);
        if (track == null)
        {
            return error("物流轨迹不存在");
        }

        AgriLogisticsTrackSummary summary = agriLogisticsTrackService.selectAgriLogisticsTrackSummary(track.getTraceCode());
        int riskScore = calculateRiskScore(track);
        String riskLevel = resolveRiskLevel(riskScore);
        List<String> factors = buildRiskFactors(track);
        List<Map<String, Object>> suggestions = buildSmartSuggestions(track, summary, riskLevel);
        String aiOriginalExcerpt = null;

        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "物流路径智能研判");
            context.put("trackId", track.getTrackId());
            context.put("traceCode", track.getTraceCode());
            context.put("trackStatus", track.getTrackStatus());
            context.put("currentLocation", track.getCurrentLocation());
            context.put("temperature", track.getTemperature());
            context.put("humidity", track.getHumidity());
            context.put("eventDesc", track.getEventDesc());
            context.put("eventTime", track.getEventTime());
            context.put("routePath", track.getRoutePath());
            context.put("ruleRiskScore", riskScore);
            context.put("ruleRiskLevel", riskLevel);
            context.put("ruleFactors", factors);
            context.put("summary", summary);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("物流路径智能研判", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getRiskLevel()))
            {
                riskLevel = aiResult.getRiskLevel();
            }
            if (StringUtils.isNotBlank(aiResult.getSuggestion()))
            {
                Map<String, Object> aiSuggestion = new LinkedHashMap<>();
                aiSuggestion.put("title", "AI建议");
                aiSuggestion.put("priority", "高");
                aiSuggestion.put("desc", aiResult.getSuggestion());
                suggestions.add(0, aiSuggestion);
            }
            if (StringUtils.isNotBlank(aiOriginalExcerpt))
            {
                Map<String, Object> excerptSuggestion = new LinkedHashMap<>();
                excerptSuggestion.put("title", "AI原文摘录");
                excerptSuggestion.put("priority", "中");
                excerptSuggestion.put("desc", aiOriginalExcerpt);
                suggestions.add(excerptSuggestion);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trackId", track.getTrackId());
        result.put("traceCode", track.getTraceCode());
        result.put("algorithm", "logistics-route-ops-v1");
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("factors", factors);
        result.put("summary", summary);
        result.put("suggestions", suggestions);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    private Map<String, Object> buildOpsResult(List<AgriLogisticsTrack> list)
    {
        int total = list == null ? 0 : list.size();
        int pending = 0;
        int transit = 0;
        int signed = 0;
        int abnormal = 0;
        int routeLinked = 0;
        BigDecimal tempSum = BigDecimal.ZERO;
        int tempCount = 0;
        BigDecimal humiditySum = BigDecimal.ZERO;
        int humidityCount = 0;
        List<Map<String, Object>> queue = new ArrayList<>();

        if (list != null)
        {
            for (AgriLogisticsTrack item : list)
            {
                if (item == null)
                {
                    continue;
                }
                if ("0".equals(item.getTrackStatus()))
                {
                    pending++;
                }
                else if ("1".equals(item.getTrackStatus()))
                {
                    transit++;
                }
                else if ("2".equals(item.getTrackStatus()))
                {
                    signed++;
                }
                else if ("3".equals(item.getTrackStatus()))
                {
                    abnormal++;
                }

                if (item.getRoutePath() != null && !item.getRoutePath().isBlank())
                {
                    routeLinked++;
                }

                if (item.getTemperature() != null)
                {
                    tempSum = tempSum.add(item.getTemperature());
                    tempCount++;
                }

                if (item.getHumidity() != null)
                {
                    humiditySum = humiditySum.add(item.getHumidity());
                    humidityCount++;
                }

                queue.add(buildPressureTrackView(item));
            }
        }

        queue.sort(Comparator.comparingInt((Map<String, Object> item) -> ((Number) item.get("riskScore")).intValue()).reversed()
            .thenComparing(item -> item.get("eventTime") == null ? "" : item.get("eventTime").toString(), Comparator.reverseOrder()));

        List<Map<String, Object>> pressureQueue = queue.stream().limit(8).toList();
        List<Map<String, Object>> warningList = queue.stream()
            .filter(item -> ((Number) item.get("riskScore")).intValue() >= 70)
            .limit(6)
            .toList();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("pending", pending);
        kpi.put("transit", transit);
        kpi.put("signed", signed);
        kpi.put("abnormal", abnormal);
        kpi.put("routeCoverage", total == 0 ? 0 : roundPercent(routeLinked * 100.0 / total));
        kpi.put("avgTemperature", tempCount == 0 ? null : tempSum.divide(BigDecimal.valueOf(tempCount), 2, RoundingMode.HALF_UP));
        kpi.put("avgHumidity", humidityCount == 0 ? null : humiditySum.divide(BigDecimal.valueOf(humidityCount), 2, RoundingMode.HALF_UP));
        kpi.put("highRiskCount", warningList.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("pressureQueue", pressureQueue);
        result.put("warningList", warningList);
        result.put("suggestions", buildOpsSuggestions(total, pending, transit, signed, abnormal, routeLinked, warningList.size()));
        result.put("focusList", pressureQueue);
        return result;
    }

    private Map<String, Object> buildPressureTrackView(AgriLogisticsTrack item)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        int riskScore = calculateRiskScore(item);
        view.put("trackId", item.getTrackId());
        view.put("traceCode", item.getTraceCode());
        view.put("orderNo", item.getOrderNo());
        view.put("productBatchNo", item.getProductBatchNo());
        view.put("currentLocation", item.getCurrentLocation());
        view.put("trackStatus", item.getTrackStatus());
        view.put("statusLabel", resolveTrackStatusLabel(item.getTrackStatus()));
        view.put("temperature", item.getTemperature());
        view.put("humidity", item.getHumidity());
        view.put("eventTime", item.getEventTime());
        view.put("routePath", item.getRoutePath());
        view.put("riskScore", riskScore);
        view.put("riskLevel", resolveRiskLevel(riskScore));
        view.put("riskReason", buildRiskReason(item));
        view.put("summary", buildTrackBrief(item));
        return view;
    }

    private int calculateRiskScore(AgriLogisticsTrack item)
    {
        int score = 18;
        if (item == null)
        {
            return score;
        }
        if ("3".equals(item.getTrackStatus()))
        {
            score += 48;
        }
        else if ("1".equals(item.getTrackStatus()))
        {
            score += 18;
        }
        else if ("0".equals(item.getTrackStatus()))
        {
            score += 10;
        }

        if (item.getTemperature() != null)
        {
            BigDecimal temperature = item.getTemperature();
            if (temperature.compareTo(BigDecimal.valueOf(0)) < 0 || temperature.compareTo(BigDecimal.valueOf(8)) > 0)
            {
                score += 14;
            }
        }

        if (item.getHumidity() != null)
        {
            BigDecimal humidity = item.getHumidity();
            if (humidity.compareTo(BigDecimal.valueOf(30)) < 0 || humidity.compareTo(BigDecimal.valueOf(85)) > 0)
            {
                score += 12;
            }
        }

        if (item.getRoutePath() == null || item.getRoutePath().isBlank())
        {
            score += 10;
        }

        if (item.getCurrentLocation() == null || item.getCurrentLocation().isBlank())
        {
            score += 8;
        }

        if (item.getEventDesc() != null && item.getEventDesc().contains("异常"))
        {
            score += 8;
        }

        return Math.min(score, 100);
    }

    private List<String> buildRiskFactors(AgriLogisticsTrack item)
    {
        List<String> factors = new ArrayList<>();
        if (item == null)
        {
            return factors;
        }
        if ("3".equals(item.getTrackStatus()))
        {
            factors.add("运输状态为异常");
        }
        if (item.getTemperature() != null && (item.getTemperature().compareTo(BigDecimal.valueOf(0)) < 0 || item.getTemperature().compareTo(BigDecimal.valueOf(8)) > 0))
        {
            factors.add("温度超出冷链建议区间");
        }
        if (item.getHumidity() != null && (item.getHumidity().compareTo(BigDecimal.valueOf(30)) < 0 || item.getHumidity().compareTo(BigDecimal.valueOf(85)) > 0))
        {
            factors.add("湿度偏离建议控制范围");
        }
        if (item.getRoutePath() == null || item.getRoutePath().isBlank())
        {
            factors.add("缺少可回放路线轨迹");
        }
        if (item.getCurrentLocation() == null || item.getCurrentLocation().isBlank())
        {
            factors.add("当前位置缺失");
        }
        if (item.getEventDesc() != null && item.getEventDesc().contains("异常"))
        {
            factors.add("轨迹描述中存在异常告警字样");
        }
        return factors;
    }

    private List<Map<String, Object>> buildSmartSuggestions(AgriLogisticsTrack track, AgriLogisticsTrackSummary summary, String riskLevel)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.add(buildSuggestion("核对节点连续性", "优先确认运单 " + track.getTraceCode() + " 的路线节点是否连续，避免中途丢点。", "P0"));
        if (summary != null && Boolean.TRUE.equals(summary.getStopRiskFlag()))
        {
            suggestions.add(buildSuggestion("处置停留风险", "当前轨迹存在停留风险，建议联系司机和仓配负责人复核滞留原因。", "P0"));
        }
        suggestions.add(buildSuggestion("同步温控记录", "结合温度与湿度波动，优先检查冷链设备上报间隔是否正常。", "P1"));
        if ("高".equals(riskLevel))
        {
            suggestions.add(buildSuggestion("升级为人工复核", "建议将该运单升级为人工复核并同步调度处置。", "P0"));
        }
        return suggestions;
    }

    private List<Map<String, Object>> buildOpsSuggestions(int total, int pending, int transit, int signed, int abnormal, int routeLinked, int highRiskCount)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.add(buildSuggestion("优先盯防异常件", "当前共有" + abnormal + "条异常轨迹，建议先处理高风险记录并回溯原因。", "P0"));
        suggestions.add(buildSuggestion("补全路线轨迹", "仅" + routeLinked + "条记录带有路线轨迹，建议补齐路线节点以提升回放可读性。", "P1"));
        suggestions.add(buildSuggestion("关注在途节点", "运输中记录有" + transit + "条，建议结合司机、仓储和温控信息持续巡检。", "P1"));
        if (pending > 0)
        {
            suggestions.add(buildSuggestion("清理待发车队列", "仍有" + pending + "条待发车记录，可结合装车计划按批处理。", "P2"));
        }
        if (highRiskCount > 0)
        {
            suggestions.add(buildSuggestion("锁定高压告警", "当前识别到" + highRiskCount + "条高压告警，建议在运营台优先分配处置责任人。", "P0"));
        }
        if (total == 0)
        {
            suggestions.add(buildSuggestion("先补基础数据", "当前暂无物流轨迹数据，请先完成运单采集或导入样例数据。", "P1"));
        }
        return suggestions;
    }

    private Map<String, Object> buildSuggestion(String title, String content, String priority)
    {
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("title", title);
        suggestion.put("content", content);
        suggestion.put("priority", priority);
        return suggestion;
    }

    private String buildRiskReason(AgriLogisticsTrack item)
    {
        List<String> reasons = new ArrayList<>();
        if ("3".equals(item.getTrackStatus()))
        {
            reasons.add("异常状态");
        }
        if (item.getTemperature() != null && (item.getTemperature().compareTo(BigDecimal.valueOf(0)) < 0 || item.getTemperature().compareTo(BigDecimal.valueOf(8)) > 0))
        {
            reasons.add("温度越界");
        }
        if (item.getHumidity() != null && (item.getHumidity().compareTo(BigDecimal.valueOf(30)) < 0 || item.getHumidity().compareTo(BigDecimal.valueOf(85)) > 0))
        {
            reasons.add("湿度异常");
        }
        if (item.getRoutePath() == null || item.getRoutePath().isBlank())
        {
            reasons.add("缺少轨迹路线");
        }
        if (reasons.isEmpty())
        {
            reasons.add("运输状态需持续观察");
        }
        return String.join("、", reasons);
    }

    private String buildTrackBrief(AgriLogisticsTrack item)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(resolveTrackStatusLabel(item.getTrackStatus()));
        if (item.getCurrentLocation() != null && !item.getCurrentLocation().isBlank())
        {
            builder.append(" · ").append(item.getCurrentLocation());
        }
        if (item.getEventDesc() != null && !item.getEventDesc().isBlank())
        {
            builder.append(" · ").append(item.getEventDesc());
        }
        return builder.toString();
    }

    private String resolveTrackStatusLabel(String value)
    {
        if ("0".equals(value))
        {
            return "待发车";
        }
        if ("1".equals(value))
        {
            return "运输中";
        }
        if ("2".equals(value))
        {
            return "已签收";
        }
        if ("3".equals(value))
        {
            return "异常";
        }
        return value;
    }

    private String resolveRiskLevel(int riskScore)
    {
        if (riskScore >= 75)
        {
            return "高";
        }
        if (riskScore >= 45)
        {
            return "中";
        }
        return "低";
    }

    private double roundPercent(double value)
    {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:export')")
    @Log(title = "物流路径追踪", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsTrack agriLogisticsTrack)
    {
        List<AgriLogisticsTrack> list = agriLogisticsTrackService.selectAgriLogisticsTrackList(agriLogisticsTrack);
        ExcelUtil<AgriLogisticsTrack> util = new ExcelUtil<>(AgriLogisticsTrack.class);
        util.exportExcel(response, list, "物流路径追踪数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping(value = "/{trackId}")
    public AjaxResult getInfo(@PathVariable("trackId") Long trackId)
    {
        return success(agriLogisticsTrackService.selectAgriLogisticsTrackByTrackId(trackId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping("/timeline/{traceCode}")
    public AjaxResult timeline(@PathVariable("traceCode") String traceCode)
    {
        return success(agriLogisticsTrackService.selectAgriLogisticsTrackTimeline(traceCode));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping("/summary/{traceCode}")
    public AjaxResult summary(@PathVariable("traceCode") String traceCode)
    {
        return success(agriLogisticsTrackService.selectAgriLogisticsTrackSummary(traceCode));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:add')")
    @Log(title = "物流路径追踪", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsTrack agriLogisticsTrack)
    {
        agriLogisticsTrack.setCreateBy(getUsername());
        return toAjax(agriLogisticsTrackService.insertAgriLogisticsTrack(agriLogisticsTrack));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:edit')")
    @Log(title = "物流路径追踪", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsTrack agriLogisticsTrack)
    {
        agriLogisticsTrack.setUpdateBy(getUsername());
        return toAjax(agriLogisticsTrackService.updateAgriLogisticsTrack(agriLogisticsTrack));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:remove')")
    @Log(title = "物流路径追踪", businessType = BusinessType.DELETE)
    @DeleteMapping("/{trackIds}")
    public AjaxResult remove(@PathVariable Long[] trackIds)
    {
        return toAjax(agriLogisticsTrackService.deleteAgriLogisticsTrackByTrackIds(trackIds));
    }
}
