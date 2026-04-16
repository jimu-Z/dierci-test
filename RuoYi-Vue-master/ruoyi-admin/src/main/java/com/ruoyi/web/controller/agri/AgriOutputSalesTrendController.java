package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriFarmOperationRecord;
import com.ruoyi.system.domain.AgriOutputSalesTrend;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.domain.AgriYieldForecastTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.integration.AgriHttpIntegrationClient.OutputSalesInsightResult;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
import com.ruoyi.system.service.IAgriOutputSalesTrendService;
import com.ruoyi.system.service.IAgriPestIdentifyTaskService;
import com.ruoyi.system.service.IAgriYieldForecastTaskService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * 产量与销量趋势图Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/outputSalesTrend")
public class AgriOutputSalesTrendController extends BaseController
{
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private IAgriOutputSalesTrendService agriOutputSalesTrendService;

    @Autowired
    private IAgriPestIdentifyTaskService agriPestIdentifyTaskService;

    @Autowired
    private IAgriYieldForecastTaskService agriYieldForecastTaskService;

    @Autowired
    private IAgriFarmOperationRecordService agriFarmOperationRecordService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriOutputSalesTrend agriOutputSalesTrend)
    {
        startPage();
        return getDataTable(agriOutputSalesTrendService.selectAgriOutputSalesTrendList(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:export')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriOutputSalesTrend agriOutputSalesTrend)
    {
        ExcelUtil<AgriOutputSalesTrend> util = new ExcelUtil<>(AgriOutputSalesTrend.class);
        util.exportExcel(response, agriOutputSalesTrendService.selectAgriOutputSalesTrendList(agriOutputSalesTrend), "产量与销量趋势图数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:query')")
    @GetMapping(value = "/{trendId}")
    public AjaxResult getInfo(@PathVariable("trendId") Long trendId)
    {
        return success(agriOutputSalesTrendService.selectAgriOutputSalesTrendByTrendId(trendId));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(String beginStatDate, String endStatDate, Integer futureMonths)
    {
        AgriOutputSalesTrend query = new AgriOutputSalesTrend();
        query.setStatus("0");
        if (StringUtils.isNotEmpty(beginStatDate))
        {
            query.getParams().put("beginStatDate", beginStatDate);
        }
        if (StringUtils.isNotEmpty(endStatDate))
        {
            query.getParams().put("endStatDate", endStatDate);
        }

        List<AgriOutputSalesTrend> history = agriOutputSalesTrendService.selectAgriOutputSalesTrendList(query);
        history.sort(Comparator.comparing(AgriOutputSalesTrend::getStatDate));
        if (history.isEmpty())
        {
            return success(emptyDashboard());
        }

        int monthCount = futureMonths == null ? 3 : Math.max(1, Math.min(6, futureMonths));
        List<AgriPestIdentifyTask> pestTasks = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(new AgriPestIdentifyTask());
        List<AgriYieldForecastTask> yieldTasks = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(new AgriYieldForecastTask());
        List<AgriFarmOperationRecord> farmOperations = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(new AgriFarmOperationRecord());

        Map<String, Object> kpi = buildKpi(history, pestTasks, yieldTasks, farmOperations);
        List<Map<String, Object>> forecast = buildForecast(history, kpi, monthCount);
        List<Map<String, Object>> pestRisk = buildPestRisk(pestTasks);

        String aiSummary = buildLocalSummary(kpi, forecast);
        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("summary", aiSummary);
        ai.put("riskLevel", kpi.get("riskLevel"));
        ai.put("confidenceRate", kpi.get("confidenceRate"));
        ai.put("modelVersion", "rule-v1");

        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("history", toChartPoints(history));
            context.put("forecast", forecast);
            context.put("kpi", kpi);
            context.put("pestRisk", pestRisk);
            OutputSalesInsightResult aiResult = agriHttpIntegrationClient.invokeOutputSalesInsight(JSON.toJSONString(context));
            if (aiResult != null)
            {
                ai.put("summary", emptyToDefault(aiResult.getInsightSummary(), aiSummary));
                ai.put("riskLevel", emptyToDefault(aiResult.getRiskLevel(), String.valueOf(kpi.get("riskLevel"))));
                ai.put("suggestion", aiResult.getSuggestion());
                ai.put("confidenceRate", aiResult.getConfidenceRate());
                ai.put("modelVersion", emptyToDefault(aiResult.getModelVersion(), "deepseek-chat"));
            }
        }
        catch (Exception ignore)
        {
            // AI调用失败时回退到本地规则预测，确保图表可用。
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("history", toChartPoints(history));
        result.put("forecast", forecast);
        result.put("kpi", kpi);
        result.put("pestRisk", pestRisk);
        result.put("ai", ai);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:add')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return toAjax(agriOutputSalesTrendService.insertAgriOutputSalesTrend(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:edit')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return toAjax(agriOutputSalesTrendService.updateAgriOutputSalesTrend(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:remove')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.DELETE)
    @DeleteMapping("/{trendIds}")
    public AjaxResult remove(@PathVariable Long[] trendIds)
    {
        return toAjax(agriOutputSalesTrendService.deleteAgriOutputSalesTrendByTrendIds(trendIds));
    }

    private Map<String, Object> emptyDashboard()
    {
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("avgOutput", BigDecimal.ZERO);
        kpi.put("avgSales", BigDecimal.ZERO);
        kpi.put("outputGrowth", BigDecimal.ZERO);
        kpi.put("salesGrowth", BigDecimal.ZERO);
        kpi.put("plotCount", 0);
        kpi.put("pestCount", 0);
        kpi.put("farmOpsCount", 0);
        kpi.put("riskLevel", "中");
        kpi.put("confidenceRate", new BigDecimal("0.50"));

        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("summary", "暂无足够数据，建议先补充近3个月产销与病虫害数据后再进行预测。");
        ai.put("riskLevel", "中");
        ai.put("confidenceRate", new BigDecimal("0.50"));
        ai.put("modelVersion", "rule-v1");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("history", new ArrayList<>());
        result.put("forecast", new ArrayList<>());
        result.put("pestRisk", new ArrayList<>());
        result.put("kpi", kpi);
        result.put("ai", ai);
        return result;
    }

    private Map<String, Object> buildKpi(List<AgriOutputSalesTrend> history,
                                         List<AgriPestIdentifyTask> pestTasks,
                                         List<AgriYieldForecastTask> yieldTasks,
                                         List<AgriFarmOperationRecord> farmOperations)
    {
        BigDecimal totalOutput = BigDecimal.ZERO;
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal lastOutput = safeDecimal(history.get(history.size() - 1).getOutputValue());

        for (AgriOutputSalesTrend item : history)
        {
            totalOutput = totalOutput.add(safeDecimal(item.getOutputValue()));
            totalSales = totalSales.add(safeDecimal(item.getSalesValue()));
        }

        BigDecimal avgOutput = divide(totalOutput, history.size());
        BigDecimal avgSales = divide(totalSales, history.size());
        BigDecimal outputGrowth = computeGrowth(history, true);
        BigDecimal salesGrowth = computeGrowth(history, false);

        int pestCount = 0;
        Map<String, Integer> pestByPlot = new HashMap<>();
        Set<String> plotSet = new HashSet<>();
        for (AgriPestIdentifyTask task : pestTasks)
        {
            if (StringUtils.isNotEmpty(task.getPlotCode()))
            {
                plotSet.add(task.getPlotCode());
            }
            if (isPestRiskTask(task))
            {
                pestCount++;
                String plotCode = emptyToDefault(task.getPlotCode(), "未知地块");
                pestByPlot.merge(plotCode, 1, Integer::sum);
            }
        }

        int farmOpsCount = 0;
        for (AgriFarmOperationRecord operation : farmOperations)
        {
            if (StringUtils.isNotEmpty(operation.getPlotCode()))
            {
                plotSet.add(operation.getPlotCode());
            }
            if ("0".equals(emptyToDefault(operation.getStatus(), "0")))
            {
                farmOpsCount++;
            }
        }

        BigDecimal avgYieldTon = BigDecimal.ZERO;
        int validYield = 0;
        for (AgriYieldForecastTask task : yieldTasks)
        {
            if (StringUtils.isNotEmpty(task.getPlotCode()))
            {
                plotSet.add(task.getPlotCode());
            }
            if ("1".equals(task.getForecastStatus()) && task.getForecastYieldKg() != null)
            {
                avgYieldTon = avgYieldTon.add(task.getForecastYieldKg().divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP));
                validYield++;
            }
        }
        avgYieldTon = validYield == 0 ? BigDecimal.ZERO : divide(avgYieldTon, validYield);

        BigDecimal pestPressure = divide(new BigDecimal(pestCount), Math.max(history.size(), 1));
        String riskLevel = "低";
        if (pestPressure.compareTo(new BigDecimal("1.20")) > 0)
        {
            riskLevel = "高";
        }
        else if (pestPressure.compareTo(new BigDecimal("0.50")) > 0)
        {
            riskLevel = "中";
        }

        BigDecimal confidence = new BigDecimal("0.78");
        if (history.size() < 4)
        {
            confidence = new BigDecimal("0.60");
        }
        if (pestPressure.compareTo(new BigDecimal("1.20")) > 0)
        {
            confidence = confidence.subtract(new BigDecimal("0.12"));
        }
        if (avgYieldTon.compareTo(lastOutput) > 0)
        {
            confidence = confidence.add(new BigDecimal("0.05"));
        }

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("avgOutput", avgOutput.setScale(2, RoundingMode.HALF_UP));
        kpi.put("avgSales", avgSales.setScale(2, RoundingMode.HALF_UP));
        kpi.put("outputGrowth", outputGrowth.setScale(2, RoundingMode.HALF_UP));
        kpi.put("salesGrowth", salesGrowth.setScale(2, RoundingMode.HALF_UP));
        kpi.put("plotCount", plotSet.size());
        kpi.put("pestCount", pestCount);
        kpi.put("farmOpsCount", farmOpsCount);
        kpi.put("riskLevel", riskLevel);
        kpi.put("pestPressure", pestPressure.setScale(2, RoundingMode.HALF_UP));
        kpi.put("avgYieldTon", avgYieldTon.setScale(2, RoundingMode.HALF_UP));
        kpi.put("confidenceRate", clamp(confidence, new BigDecimal("0.35"), new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP));
        return kpi;
    }

    private List<Map<String, Object>> buildForecast(List<AgriOutputSalesTrend> history, Map<String, Object> kpi, int months)
    {
        AgriOutputSalesTrend last = history.get(history.size() - 1);
        LocalDate baseDate = toLocalDate(last.getStatDate());
        BigDecimal currentOutput = safeDecimal(last.getOutputValue());
        BigDecimal currentSales = safeDecimal(last.getSalesValue());

        BigDecimal outputGrowth = safeDecimal(kpi.get("outputGrowth")).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal salesGrowth = safeDecimal(kpi.get("salesGrowth")).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal pestPenalty = safeDecimal(kpi.get("pestPressure")).multiply(new BigDecimal("0.03"));
        BigDecimal farmBoost = divide(safeDecimal(kpi.get("farmOpsCount")), 100).multiply(new BigDecimal("0.015"));
        BigDecimal yieldBoost = currentOutput.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : safeDecimal(kpi.get("avgYieldTon")).divide(currentOutput, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("0.08"));

        BigDecimal finalOutputGrowth = clamp(outputGrowth.add(farmBoost).add(yieldBoost).subtract(pestPenalty), new BigDecimal("-0.20"), new BigDecimal("0.30"));
        BigDecimal finalSalesGrowth = clamp(salesGrowth.add(farmBoost).subtract(pestPenalty.multiply(new BigDecimal("0.8"))), new BigDecimal("-0.18"), new BigDecimal("0.28"));

        List<Map<String, Object>> forecast = new ArrayList<>();
        for (int i = 1; i <= months; i++)
        {
            LocalDate month = baseDate.plusMonths(i);
            currentOutput = currentOutput.multiply(BigDecimal.ONE.add(finalOutputGrowth)).setScale(2, RoundingMode.HALF_UP);
            currentSales = currentSales.multiply(BigDecimal.ONE.add(finalSalesGrowth)).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("statMonth", month.format(MONTH_FORMATTER));
            item.put("outputValue", currentOutput);
            item.put("salesValue", currentSales);
            forecast.add(item);
        }
        return forecast;
    }

    private List<Map<String, Object>> buildPestRisk(List<AgriPestIdentifyTask> pestTasks)
    {
        Map<String, Integer> counter = new HashMap<>();
        for (AgriPestIdentifyTask task : pestTasks)
        {
            if (!isPestRiskTask(task))
            {
                continue;
            }
            String plotCode = emptyToDefault(task.getPlotCode(), "未知地块");
            counter.merge(plotCode, 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counter.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<Map<String, Object>> result = new ArrayList<>();
        int max = Math.min(entries.size(), 8);
        for (int i = 0; i < max; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("plotCode", entries.get(i).getKey());
            item.put("count", entries.get(i).getValue());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> toChartPoints(List<AgriOutputSalesTrend> history)
    {
        List<Map<String, Object>> points = new ArrayList<>();
        for (AgriOutputSalesTrend item : history)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("statMonth", toLocalDate(item.getStatDate()).format(MONTH_FORMATTER));
            row.put("outputValue", safeDecimal(item.getOutputValue()).setScale(2, RoundingMode.HALF_UP));
            row.put("salesValue", safeDecimal(item.getSalesValue()).setScale(2, RoundingMode.HALF_UP));
            row.put("targetOutput", safeDecimal(item.getTargetOutput()).setScale(2, RoundingMode.HALF_UP));
            row.put("targetSales", safeDecimal(item.getTargetSales()).setScale(2, RoundingMode.HALF_UP));
            points.add(row);
        }
        return points;
    }

    private String buildLocalSummary(Map<String, Object> kpi, List<Map<String, Object>> forecast)
    {
        if (forecast.isEmpty())
        {
            return "暂无可用于预测的历史样本，建议补充产销记录。";
        }
        Map<String, Object> last = forecast.get(forecast.size() - 1);
        return "基于历史产销、地块作业和病虫害记录，系统预计未来"
            + forecast.size() + "个月产量将趋于"
            + (safeDecimal(kpi.get("outputGrowth")).compareTo(BigDecimal.ZERO) >= 0 ? "温和增长" : "小幅回落")
            + "，期末预测产量约" + safeDecimal(last.get("outputValue")).setScale(2, RoundingMode.HALF_UP) + "吨，"
            + "预测销量约" + safeDecimal(last.get("salesValue")).setScale(2, RoundingMode.HALF_UP) + "万元；"
            + "当前病虫害风险等级为" + kpi.get("riskLevel") + "，建议优先对高风险地块进行防治与农事干预。";
    }

    private boolean isPestRiskTask(AgriPestIdentifyTask task)
    {
        if (task == null)
        {
            return false;
        }
        if ("2".equals(task.getIdentifyStatus()))
        {
            return true;
        }
        String result = task.getIdentifyResult() == null ? "" : task.getIdentifyResult();
        return result.contains("病") || result.contains("虫") || result.contains("霉");
    }

    private String emptyToDefault(String value, String defaultValue)
    {
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    private BigDecimal computeGrowth(List<AgriOutputSalesTrend> history, boolean output)
    {
        if (history.size() < 2)
        {
            return BigDecimal.ZERO;
        }
        BigDecimal first = output ? safeDecimal(history.get(0).getOutputValue()) : safeDecimal(history.get(0).getSalesValue());
        BigDecimal last = output ? safeDecimal(history.get(history.size() - 1).getOutputValue()) : safeDecimal(history.get(history.size() - 1).getSalesValue());
        if (first.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        return last.subtract(first)
            .divide(first, 6, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    private BigDecimal safeDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal)
        {
            return decimal;
        }
        try
        {
            return new BigDecimal(String.valueOf(value));
        }
        catch (Exception ignore)
        {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal divide(BigDecimal value, int divisor)
    {
        if (value == null || divisor <= 0)
        {
            return BigDecimal.ZERO;
        }
        return value.divide(new BigDecimal(divisor), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max)
    {
        if (value.compareTo(min) < 0)
        {
            return min;
        }
        if (value.compareTo(max) > 0)
        {
            return max;
        }
        return value;
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
