package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriFinanceRiskMetric;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriFinanceRiskMetricService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农业金融风控指标Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/financeRisk")
public class AgriFinanceRiskMetricController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AgriFinanceRiskMetricController.class);

    @Autowired
    private IAgriFinanceRiskMetricService agriFinanceRiskMetricService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        startPage();
        return getDataTable(agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriFinanceRiskMetric> list = agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(new AgriFinanceRiskMetric());
        int total = list.size();
        int highRiskCount = 0;
        int warnedCount = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;
        for (AgriFinanceRiskMetric item : list)
        {
            BigDecimal score = defaultZero(item.getRiskScore());
            scoreSum = scoreSum.add(score);
            if ("H".equalsIgnoreCase(item.getRiskLevel()) || "C".equalsIgnoreCase(item.getRiskLevel()))
            {
                highRiskCount++;
            }
            if ("2".equals(item.getEvaluateStatus()))
            {
                warnedCount++;
            }
        }
        BigDecimal avgScore = total == 0 ? BigDecimal.ZERO : scoreSum.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        List<AgriFinanceRiskMetric> topList = list.stream()
            .sorted(Comparator.comparing((AgriFinanceRiskMetric x) -> defaultZero(x.getRiskScore())).reversed())
            .limit(6)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("highRiskCount", highRiskCount);
        kpi.put("warnedCount", warnedCount);
        kpi.put("avgScore", avgScore);
        result.put("kpi", kpi);
        result.put("topList", topList);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriFinanceRiskMetric> list = agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(new AgriFinanceRiskMetric());
        int total = list.size();
        int highRiskCount = 0;
        int overdueCount = 0;
        int monitoredCount = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;
        Map<String, Integer> dimensionBucket = new LinkedHashMap<>();
        dimensionBucket.put("CREDIT", 0);
        dimensionBucket.put("REPAY", 0);
        dimensionBucket.put("OPERATION", 0);
        dimensionBucket.put("OTHER", 0);

        for (AgriFinanceRiskMetric item : list)
        {
            BigDecimal score = defaultZero(item.getRiskScore());
            scoreSum = scoreSum.add(score);
            if ("H".equalsIgnoreCase(item.getRiskLevel()) || "C".equalsIgnoreCase(item.getRiskLevel()))
            {
                highRiskCount++;
            }
            if ("2".equals(item.getEvaluateStatus()))
            {
                overdueCount++;
            }
            if ("1".equals(item.getEvaluateStatus()))
            {
                monitoredCount++;
            }
            String dimension = normalizeDimension(item.getRiskDimension());
            dimensionBucket.put(dimension, dimensionBucket.getOrDefault(dimension, 0) + 1);
        }

        BigDecimal avgScore = total == 0 ? BigDecimal.ZERO : scoreSum.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        List<AgriFinanceRiskMetric> pressureQueue = list.stream()
            .sorted(Comparator.comparing((AgriFinanceRiskMetric x) -> defaultZero(x.getRiskScore())).reversed())
            .limit(8)
            .toList();

        List<Map<String, Object>> alerts = list.stream()
            .filter(item -> "H".equalsIgnoreCase(item.getRiskLevel()) || "C".equalsIgnoreCase(item.getRiskLevel()) || "2".equals(item.getEvaluateStatus()))
            .sorted(Comparator.comparing((AgriFinanceRiskMetric x) -> defaultZero(x.getRiskScore())).reversed())
            .limit(10)
            .map(item -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("riskId", item.getRiskId());
                row.put("indicatorCode", item.getIndicatorCode());
                row.put("indicatorName", item.getIndicatorName());
                row.put("riskScore", defaultZero(item.getRiskScore()));
                row.put("thresholdValue", defaultZero(item.getThresholdValue()));
                row.put("riskLevel", item.getRiskLevel());
                row.put("evaluateStatus", item.getEvaluateStatus());
                row.put("evaluator", item.getEvaluator());
                row.put("evaluateTime", item.getEvaluateTime());
                return row;
            })
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("highRiskCount", highRiskCount);
        kpi.put("overdueCount", overdueCount);
        kpi.put("monitoredCount", monitoredCount);
        kpi.put("avgScore", avgScore);
        result.put("kpi", kpi);
        result.put("dimensionBucket", dimensionBucket);
        result.put("pressureQueue", pressureQueue);
        result.put("alerts", alerts);
        result.put("suggestions", buildFinanceSuggestions(avgScore, highRiskCount, overdueCount));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:edit')")
    @GetMapping("/smart/analyze/{riskId}")
    public AjaxResult smartAnalyze(@PathVariable("riskId") Long riskId)
    {
        log.info("financeRisk smartAnalyze called, riskId={}", riskId);
        AgriFinanceRiskMetric metric = agriFinanceRiskMetricService.selectAgriFinanceRiskMetricByRiskId(riskId);
        if (metric == null)
        {
            log.warn("financeRisk smartAnalyze metric not found, riskId={}", riskId);
            return error("指标不存在");
        }

        BigDecimal score = defaultZero(metric.getRiskScore());
        BigDecimal threshold = defaultZero(metric.getThresholdValue());
        BigDecimal gap = score.subtract(threshold);
        BigDecimal stressIndex = score.multiply(dimensionWeight(metric.getRiskDimension())).setScale(2, RoundingMode.HALF_UP);
        String inferredLevel = inferRiskLevel(score, threshold);

        List<String> suggestions = new ArrayList<>();
        if (gap.compareTo(BigDecimal.ZERO) > 0)
        {
            suggestions.add("风险分值已超过阈值，建议立即触发预警流程并复核授信条件");
        }
        if (stressIndex.compareTo(new BigDecimal("80")) >= 0)
        {
            suggestions.add("压力指数偏高，建议收紧放款节奏并增加担保审查");
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前指标可控，建议保持周度复盘并持续监测阈值变化");
        }

        String summary = "基于分值-阈值偏离与维度权重完成压力测算，判定风险等级为" + inferredLevel + "。";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "农业金融风控智能分析");
            context.put("riskId", metric.getRiskId());
            context.put("indicatorCode", metric.getIndicatorCode());
            context.put("indicatorName", metric.getIndicatorName());
            context.put("riskDimension", metric.getRiskDimension());
            context.put("riskScore", score);
            context.put("thresholdValue", threshold);
            context.put("riskLevel", metric.getRiskLevel());
            context.put("evaluateStatus", metric.getEvaluateStatus());
            context.put("ruleStressIndex", stressIndex);
            context.put("ruleRiskLevel", inferredLevel);
            context.put("ruleSuggestions", suggestions);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("农业金融风控智能分析", JSON.toJSONString(context));
            AgriAiResultHelper.MergeResult mergeResult = AgriAiResultHelper.mergeGeneralInsightResult(summary, inferredLevel, suggestions, aiResult);
            summary = mergeResult.getSummary();
            inferredLevel = mergeResult.getRiskLevel();
            aiOriginalExcerpt = mergeResult.getAiOriginalExcerpt();
            log.info("financeRisk smartAnalyze AI succeeded, riskId={}", riskId);
        }
        catch (Exception ex)
        {
            // keep rule-based fallback when AI is unavailable
            log.warn("financeRisk smartAnalyze AI fallback, riskId={}, reason={}", riskId, ex.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskId", metric.getRiskId());
        result.put("indicatorName", metric.getIndicatorName());
        result.put("algorithm", "credit-stress-v1");
        result.put("stressIndex", stressIndex);
        result.put("scoreGap", gap.setScale(2, RoundingMode.HALF_UP));
        result.put("riskLevel", inferredLevel);
        result.put("confidence", buildConfidence(score, threshold));
        result.put("suggestions", suggestions);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        log.info("financeRisk smartAnalyze response ready, riskId={}, riskLevel={}", riskId, inferredLevel);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:export')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        ExcelUtil<AgriFinanceRiskMetric> util = new ExcelUtil<>(AgriFinanceRiskMetric.class);
        util.exportExcel(response, agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(agriFinanceRiskMetric), "农业金融风控指标数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:query')")
    @GetMapping(value = "/{riskId}")
    public AjaxResult getInfo(@PathVariable("riskId") Long riskId)
    {
        return success(agriFinanceRiskMetricService.selectAgriFinanceRiskMetricByRiskId(riskId));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:add')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return toAjax(agriFinanceRiskMetricService.insertAgriFinanceRiskMetric(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:edit')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return toAjax(agriFinanceRiskMetricService.updateAgriFinanceRiskMetric(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:remove')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{riskIds}")
    public AjaxResult remove(@PathVariable Long[] riskIds)
    {
        return toAjax(agriFinanceRiskMetricService.deleteAgriFinanceRiskMetricByRiskIds(riskIds));
    }

    private BigDecimal defaultZero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal dimensionWeight(String dimension)
    {
        if ("CREDIT".equalsIgnoreCase(dimension))
        {
            return new BigDecimal("1.30");
        }
        if ("REPAY".equalsIgnoreCase(dimension))
        {
            return new BigDecimal("1.20");
        }
        if ("OPERATION".equalsIgnoreCase(dimension))
        {
            return new BigDecimal("1.10");
        }
        return BigDecimal.ONE;
    }

    private String inferRiskLevel(BigDecimal score, BigDecimal threshold)
    {
        BigDecimal gap = score.subtract(threshold);
        if (gap.compareTo(new BigDecimal("20")) >= 0)
        {
            return "C";
        }
        if (gap.compareTo(new BigDecimal("8")) >= 0)
        {
            return "H";
        }
        if (gap.compareTo(BigDecimal.ZERO) >= 0)
        {
            return "M";
        }
        return "L";
    }

    private BigDecimal buildConfidence(BigDecimal score, BigDecimal threshold)
    {
        BigDecimal ratio = threshold.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : score.divide(threshold, 4, RoundingMode.HALF_UP);
        BigDecimal confidence = ratio.min(new BigDecimal("1.00")).max(new BigDecimal("0.55"));
        return confidence.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeDimension(String dimension)
    {
        if ("CREDIT".equalsIgnoreCase(dimension))
        {
            return "CREDIT";
        }
        if ("REPAY".equalsIgnoreCase(dimension))
        {
            return "REPAY";
        }
        if ("OPERATION".equalsIgnoreCase(dimension))
        {
            return "OPERATION";
        }
        return "OTHER";
    }

    private List<String> buildFinanceSuggestions(BigDecimal avgScore, int highRiskCount, int overdueCount)
    {
        List<String> suggestions = new ArrayList<>();
        if (highRiskCount > 0)
        {
            suggestions.add("存在高风险指标，建议优先核查授信、回款与保证金配置");
        }
        if (overdueCount > 0)
        {
            suggestions.add("存在未完成评估项，建议补齐评估人和复核时间后再进入授信流程");
        }
        if (avgScore.compareTo(new BigDecimal("70")) >= 0)
        {
            suggestions.add("整体压力偏高，建议同步收紧授信阈值并联动销售回款策略");
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前风险态势平稳，建议维持周度巡检和月度复盘");
        }
        return suggestions;
    }
}
