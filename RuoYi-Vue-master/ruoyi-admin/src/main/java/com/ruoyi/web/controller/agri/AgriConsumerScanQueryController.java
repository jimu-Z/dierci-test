package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriBrandTracePage;
import com.ruoyi.system.domain.AgriConsumerScanQuery;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriBrandTracePageService;
import com.ruoyi.system.service.IAgriConsumerScanQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
 * 消费者扫码查询Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/consumerScan")
public class AgriConsumerScanQueryController extends BaseController
{
    @Autowired
    private IAgriConsumerScanQueryService agriConsumerScanQueryService;

    @Autowired
    private IAgriBrandTracePageService agriBrandTracePageService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriConsumerScanQuery agriConsumerScanQuery)
    {
        startPage();
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(agriConsumerScanQuery);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(new AgriConsumerScanQuery());
        int total = list.size();
        int hitCount = 0;
        int riskyCount = 0;
        Map<String, Integer> channelCount = new LinkedHashMap<>();
        for (AgriConsumerScanQuery item : list)
        {
            if ("1".equals(item.getScanResult()))
            {
                hitCount++;
            }
            if (isRiskyScan(item))
            {
                riskyCount++;
            }
            String channel = StringUtils.defaultIfBlank(item.getScanChannel(), "UNKNOWN");
            channelCount.put(channel, channelCount.getOrDefault(channel, 0) + 1);
        }

        List<AgriConsumerScanQuery> recent = list.stream()
            .sorted(Comparator.comparing(AgriConsumerScanQuery::getQueryTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("hitCount", hitCount);
        kpi.put("hitRate", total == 0 ? 0 : (hitCount * 100.0 / total));
        kpi.put("riskyCount", riskyCount);
        result.put("kpi", kpi);
        result.put("channelDistribution", channelCount);
        result.put("recent", recent);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(new AgriConsumerScanQuery());
        int total = list.size();
        int hitCount = 0;
        int suspiciousCount = 0;
        int highRiskCount = 0;
        Map<String, Integer> channelDistribution = new LinkedHashMap<>();
        Map<String, Integer> riskLevelDistribution = new LinkedHashMap<>();
        riskLevelDistribution.put("低", 0);
        riskLevelDistribution.put("中", 0);
        riskLevelDistribution.put("高", 0);

        Map<String, Integer> dayRiskCounter = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--)
        {
            dayRiskCounter.put(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, DateUtils.addDays(DateUtils.getNowDate(), -i)), 0);
        }

        Map<String, Integer> traceCounter = new LinkedHashMap<>();
        for (AgriConsumerScanQuery item : list)
        {
            if ("1".equals(item.getScanResult()))
            {
                hitCount++;
            }
            if (isRiskyScan(item))
            {
                suspiciousCount++;
            }

            int score = calcAnomalyScore(item);
            String riskLevel = score >= 80 ? "高" : (score >= 50 ? "中" : "低");
            if ("高".equals(riskLevel))
            {
                highRiskCount++;
            }
            riskLevelDistribution.put(riskLevel, riskLevelDistribution.get(riskLevel) + 1);

            String channel = StringUtils.defaultIfBlank(item.getScanChannel(), "UNKNOWN");
            channelDistribution.put(channel, channelDistribution.getOrDefault(channel, 0) + 1);

            String traceCode = StringUtils.defaultIfBlank(item.getTraceCode(), "UNKNOWN");
            traceCounter.put(traceCode, traceCounter.getOrDefault(traceCode, 0) + 1);

            if (item.getQueryTime() != null)
            {
                String day = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, item.getQueryTime());
                if (dayRiskCounter.containsKey(day) && score >= 50)
                {
                    dayRiskCounter.put(day, dayRiskCounter.get(day) + 1);
                }
            }
        }

        List<Map<String, Object>> riskTrend = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dayRiskCounter.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", entry.getKey());
            item.put("riskCount", entry.getValue());
            riskTrend.add(item);
        }

        List<Map<String, Object>> traceHotspots = traceCounter.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(6)
            .map(entry -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("traceCode", entry.getKey());
                map.put("scanCount", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());

        List<Map<String, Object>> alertStream = list.stream()
            .filter(this::isRiskyScan)
            .sorted(Comparator.comparing(AgriConsumerScanQuery::getQueryTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(10)
            .map(item -> {
                int score = calcAnomalyScore(item);
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("queryId", item.getQueryId());
                map.put("traceCode", item.getTraceCode());
                map.put("scanChannel", item.getScanChannel());
                map.put("consumerName", item.getConsumerName());
                map.put("scanIp", item.getScanIp());
                map.put("queryTime", item.getQueryTime());
                map.put("score", score);
                map.put("level", score >= 80 ? "高" : (score >= 50 ? "中" : "低"));
                return map;
            })
            .collect(Collectors.toList());

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("hitCount", hitCount);
        kpi.put("hitRate", total == 0 ? 0 : (hitCount * 100.0 / total));
        kpi.put("suspiciousCount", suspiciousCount);
        kpi.put("highRiskCount", highRiskCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("channelDistribution", channelDistribution);
        result.put("riskLevelDistribution", riskLevelDistribution);
        result.put("riskTrend", riskTrend);
        result.put("traceHotspots", traceHotspots);
        result.put("alertStream", alertStream);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:edit')")
    @GetMapping("/smart/analyze/{queryId}")
    public AjaxResult smartAnalyze(@PathVariable("queryId") Long queryId)
    {
        AgriConsumerScanQuery query = agriConsumerScanQueryService.selectAgriConsumerScanQueryByQueryId(queryId);
        if (query == null)
        {
            return error("查询记录不存在");
        }

        int anomalyScore = 20;
        List<String> flags = new ArrayList<>();
        if ("0".equals(query.getScanResult()))
        {
            anomalyScore += 35;
            flags.add("未命中溯源码");
        }
        if ("2".equals(query.getScanResult()))
        {
            anomalyScore += 25;
            flags.add("命中未发布页面");
        }
        if (StringUtils.isBlank(query.getConsumerPhone()))
        {
            anomalyScore += 10;
            flags.add("缺少手机号");
        }
        if (StringUtils.isNotBlank(query.getScanIp()) && (query.getScanIp().startsWith("10.") || query.getScanIp().startsWith("192.168.")))
        {
            anomalyScore += 10;
            flags.add("内网IP来源");
        }
        anomalyScore = Math.min(anomalyScore, 100);

        String level = anomalyScore >= 80 ? "高" : (anomalyScore >= 50 ? "中" : "低");
        List<String> suggestions = new ArrayList<>();
        if (anomalyScore >= 80)
        {
            suggestions.add("建议加入临时黑名单并触发人工复核");
        }
        else if (anomalyScore >= 50)
        {
            suggestions.add("建议对同溯源码短时重复扫码进行限流");
        }
        else
        {
            suggestions.add("风险可控，建议持续监控渠道与地区分布");
        }

        String summary = "基于扫码结果、IP与身份信息完成反欺诈评估，风险等级为" + level + "。";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "消费者扫码反欺诈智能分析");
            context.put("queryId", query.getQueryId());
            context.put("traceCode", query.getTraceCode());
            context.put("scanResult", query.getScanResult());
            context.put("scanChannel", query.getScanChannel());
            context.put("scanIp", query.getScanIp());
            context.put("consumerName", query.getConsumerName());
            context.put("consumerPhone", query.getConsumerPhone());
            context.put("queryTime", query.getQueryTime());
            context.put("ruleFlags", flags);
            context.put("ruleLevel", level);
            context.put("ruleScore", anomalyScore);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("消费者扫码反欺诈智能分析", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getInsightSummary()))
            {
                summary = aiResult.getInsightSummary();
            }
            if (StringUtils.isNotBlank(aiResult.getRiskLevel()))
            {
                level = aiResult.getRiskLevel();
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
        result.put("queryId", query.getQueryId());
        result.put("traceCode", query.getTraceCode());
        result.put("algorithm", "scan-antifraud-v1");
        result.put("anomalyScore", anomalyScore);
        result.put("riskLevel", level);
        result.put("flags", flags);
        result.put("suggestions", suggestions);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:export')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriConsumerScanQuery agriConsumerScanQuery)
    {
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(agriConsumerScanQuery);
        ExcelUtil<AgriConsumerScanQuery> util = new ExcelUtil<>(AgriConsumerScanQuery.class);
        util.exportExcel(response, list, "消费者扫码查询数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:query')")
    @GetMapping(value = "/{queryId}")
    public AjaxResult getInfo(@PathVariable("queryId") Long queryId)
    {
        return success(agriConsumerScanQueryService.selectAgriConsumerScanQueryByQueryId(queryId));
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:add')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriConsumerScanQuery agriConsumerScanQuery)
    {
        agriConsumerScanQuery.setCreateBy(getUsername());
        if (StringUtils.isEmpty(agriConsumerScanQuery.getScanResult()))
        {
            agriConsumerScanQuery.setScanResult("0");
        }
        if (agriConsumerScanQuery.getQueryTime() == null)
        {
            agriConsumerScanQuery.setQueryTime(DateUtils.getNowDate());
        }
        return toAjax(agriConsumerScanQueryService.insertAgriConsumerScanQuery(agriConsumerScanQuery));
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:edit')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriConsumerScanQuery agriConsumerScanQuery)
    {
        agriConsumerScanQuery.setUpdateBy(getUsername());
        return toAjax(agriConsumerScanQueryService.updateAgriConsumerScanQuery(agriConsumerScanQuery));
    }

    @Log(title = "消费者扫码模拟查询", businessType = BusinessType.OTHER)
    @PostMapping("/scan/{traceCode}")
    public AjaxResult scan(@PathVariable("traceCode") String traceCode,
                           @RequestBody(required = false) AgriConsumerScanQuery payload,
                           HttpServletRequest request)
    {
        AgriConsumerScanQuery scanQuery = new AgriConsumerScanQuery();
        scanQuery.setTraceCode(traceCode);
        scanQuery.setQueryTime(DateUtils.getNowDate());
        scanQuery.setScanChannel(payload == null || StringUtils.isEmpty(payload.getScanChannel()) ? "WECHAT" : payload.getScanChannel());
        if (payload != null)
        {
            scanQuery.setConsumerName(payload.getConsumerName());
            scanQuery.setConsumerPhone(payload.getConsumerPhone());
            scanQuery.setScanAddress(payload.getScanAddress());
            scanQuery.setRemark(payload.getRemark());
        }
        scanQuery.setScanIp(getRequestIp(request));
        scanQuery.setStatus("0");

        AgriBrandTracePage page = agriBrandTracePageService.selectAgriBrandTracePageByTraceCode(traceCode);
        if (page == null)
        {
            scanQuery.setScanResult("0");
            agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
            return error("未查询到对应溯源码信息");
        }

        if (!"1".equals(page.getPublishStatus()))
        {
            scanQuery.setScanResult("2");
            agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
            return error("该溯源码页面未发布");
        }

        scanQuery.setScanResult("1");
        agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
        return success(page);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:remove')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.DELETE)
    @DeleteMapping("/{queryIds}")
    public AjaxResult remove(@PathVariable Long[] queryIds)
    {
        return toAjax(agriConsumerScanQueryService.deleteAgriConsumerScanQueryByQueryIds(queryIds));
    }

    private String getRequestIp(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && ip.contains(","))
        {
            ip = ip.split(",")[0].trim();
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private boolean isRiskyScan(AgriConsumerScanQuery item)
    {
        return "0".equals(item.getScanResult())
            || "2".equals(item.getScanResult())
            || StringUtils.isBlank(item.getConsumerPhone());
    }

    private int calcAnomalyScore(AgriConsumerScanQuery query)
    {
        int anomalyScore = 20;
        if ("0".equals(query.getScanResult()))
        {
            anomalyScore += 35;
        }
        if ("2".equals(query.getScanResult()))
        {
            anomalyScore += 25;
        }
        if (StringUtils.isBlank(query.getConsumerPhone()))
        {
            anomalyScore += 10;
        }
        if (StringUtils.isNotBlank(query.getScanIp())
            && (query.getScanIp().startsWith("10.") || query.getScanIp().startsWith("192.168.")))
        {
            anomalyScore += 10;
        }
        return Math.min(anomalyScore, 100);
    }
}
