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
import com.ruoyi.system.domain.AgriEnvSensorRecord;
import com.ruoyi.system.domain.AgriFarmOperationRecord;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.domain.AgriYieldForecastTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriEnvSensorRecordService;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
import com.ruoyi.system.service.IAgriPestIdentifyTaskService;
import com.ruoyi.system.service.IAgriYieldForecastTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
 * 产量预测任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/yieldForecast")
public class AgriYieldForecastTaskController extends BaseController
{
    @Autowired
    private IAgriYieldForecastTaskService agriYieldForecastTaskService;

    @Autowired
    private IAgriEnvSensorRecordService agriEnvSensorRecordService;

    @Autowired
    private IAgriPestIdentifyTaskService agriPestIdentifyTaskService;

    @Autowired
    private IAgriFarmOperationRecordService agriFarmOperationRecordService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriYieldForecastTask agriYieldForecastTask)
    {
        startPage();
        List<AgriYieldForecastTask> list = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(agriYieldForecastTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriYieldForecastTask> list = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(new AgriYieldForecastTask());
        List<AgriYieldForecastTask> sortedList = list.stream()
            .sorted(Comparator.comparing(this::resolveTaskSortTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        int total = sortedList.size();
        int predicted = 0;
        int pending = 0;
        int failed = 0;
        int highRiskCount = 0;
        BigDecimal totalArea = BigDecimal.ZERO;
        BigDecimal totalForecastYield = BigDecimal.ZERO;

        Map<String, Integer> seasonDistribution = new LinkedHashMap<>();
        Map<String, Integer> statusDistribution = new LinkedHashMap<>();

        for (AgriYieldForecastTask item : sortedList)
        {
            if ("1".equals(item.getForecastStatus()))
            {
                predicted++;
            }
            else if ("2".equals(item.getForecastStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }

            if (item.getAreaMu() != null)
            {
                totalArea = totalArea.add(item.getAreaMu());
            }
            if (item.getForecastYieldKg() != null)
            {
                totalForecastYield = totalForecastYield.add(item.getForecastYieldKg());
            }
            if (isHighRisk(item))
            {
                highRiskCount++;
            }

            seasonDistribution.merge(StringUtils.isEmpty(item.getSeason()) ? "未知季节" : item.getSeason(), 1, Integer::sum);
            statusDistribution.merge(statusLabel(item.getForecastStatus()), 1, Integer::sum);
        }

        BigDecimal avgYieldPerMu = BigDecimal.ZERO;
        if (totalArea.compareTo(BigDecimal.ZERO) > 0 && totalForecastYield.compareTo(BigDecimal.ZERO) > 0)
        {
            avgYieldPerMu = totalForecastYield.divide(totalArea, 2, RoundingMode.HALF_UP);
        }

        List<Map<String, Object>> pressureQueue = sortedList.stream()
            .map(this::toPressureItem)
            .sorted(Comparator.comparingInt((Map<String, Object> o) -> (Integer) o.get("riskScore")).reversed())
            .limit(10)
            .collect(Collectors.toList());

        Map<String, Object> focus = pressureQueue.isEmpty() ? new LinkedHashMap<>() : pressureQueue.get(0);
        List<Map<String, Object>> suggestions = buildSuggestions(pending, failed, highRiskCount, total);

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("predicted", predicted);
        kpi.put("pending", pending);
        kpi.put("failed", failed);
        kpi.put("predictRate", total == 0 ? 0 : predicted * 100.0 / total);
        kpi.put("totalArea", totalArea.setScale(2, RoundingMode.HALF_UP));
        kpi.put("avgYieldPerMu", avgYieldPerMu);
        kpi.put("highRiskCount", highRiskCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("pressureQueue", pressureQueue);
        result.put("focus", focus);
        result.put("suggestions", suggestions);
        result.put("seasonDistribution", toDistributionList(seasonDistribution));
        result.put("statusDistribution", toDistributionList(statusDistribution));
        result.put("trendSeries", buildTrendSeries(sortedList));
        result.put("plotBarSeries", buildPlotBarSeries(sortedList));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:query')")
    @GetMapping("/smart/assess/{forecastId}")
    public AjaxResult smartAssess(@PathVariable("forecastId") Long forecastId)
    {
        AgriYieldForecastTask task = agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(forecastId);
        if (task == null)
        {
            return error("任务不存在");
        }

        List<AgriEnvSensorRecord> sensorRecords = querySensorRecords(task.getPlotCode());
        List<AgriPestIdentifyTask> pestTasks = queryPestTasks(task.getPlotCode());
        List<AgriFarmOperationRecord> farmOperations = queryFarmOperations(task.getPlotCode());

        RulePredictResult ruleResult = buildRulePrediction(task, sensorRecords, pestTasks, farmOperations);
        Map<String, Object> context = buildYieldAiContext(task, sensorRecords, pestTasks, farmOperations, ruleResult);

        BigDecimal aiYield = null;
        String aiSuggestion = "";
        String aiSummary = "";
        String aiRiskLevel = "";
        String aiModel = "";
        BigDecimal aiConfidence = BigDecimal.ZERO;
        String aiRawExcerpt = "";
        String analysisEngine = "rule-local";
        String deepseekMessage = "未调用DeepSeek，使用本地规则结果";

        try
        {
            AgriHttpIntegrationClient.YieldInsightResult aiResult = agriHttpIntegrationClient.invokeYieldInsight(JSON.toJSONString(context));
            aiYield = aiResult.getForecastYieldKg();
            aiSuggestion = StringUtils.defaultString(aiResult.getSuggestion());
            aiSummary = StringUtils.defaultString(aiResult.getInsightSummary());
            aiRiskLevel = StringUtils.defaultString(aiResult.getRiskLevel());
            aiModel = StringUtils.defaultIfBlank(aiResult.getModelVersion(), "deepseek-chat");
            aiConfidence = aiResult.getConfidenceRate() == null ? BigDecimal.ZERO : aiResult.getConfidenceRate();
            aiRawExcerpt = trimText(aiResult.getRawContent(), 220);
            analysisEngine = "deepseek";
            deepseekMessage = "DeepSeek调用成功";
        }
        catch (Exception ex)
        {
            deepseekMessage = "DeepSeek调用失败，已回退本地规则：" + trimText(ex.getMessage(), 120);
        }

        BigDecimal finalYield = aiYield != null && aiYield.compareTo(BigDecimal.ZERO) > 0
            ? aiYield.setScale(2, RoundingMode.HALF_UP)
            : ruleResult.getPredictedYieldKg();
        BigDecimal finalYieldPerMu = calcYieldPerMu(finalYield, task.getAreaMu());

        int score = scoreByFactors(ruleResult.getFactors(), task.getForecastStatus());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forecastId", task.getForecastId());
        result.put("algorithm", "yield-multi-source-assess-v2");
        result.put("analysisEngine", analysisEngine);
        result.put("deepseekMessage", deepseekMessage);
        result.put("reliabilityScore", score);
        result.put("riskLevel", StringUtils.defaultIfBlank(aiRiskLevel, riskLevel(score)));
        result.put("yieldPerMu", finalYieldPerMu);
        result.put("benchmarkPerMu", benchmarkBySeason(task.getSeason()));
        result.put("factors", ruleResult.getFactors());
        result.put("summary", StringUtils.defaultIfBlank(aiSummary, ruleResult.getSummary()));
        result.put("suggestion", StringUtils.defaultIfBlank(aiSuggestion, ruleResult.getSuggestion()));
        result.put("suggestionList", splitSuggestion(StringUtils.defaultIfBlank(aiSuggestion, ruleResult.getSuggestion())));
        result.put("forecastYieldKg", finalYield);
        result.put("confidenceRate", aiConfidence);
        result.put("modelVersion", StringUtils.defaultIfBlank(aiModel, "rule-local-v2"));
        result.put("aiOriginalExcerpt", aiRawExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:export')")
    @Log(title = "产量预测任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriYieldForecastTask agriYieldForecastTask)
    {
        List<AgriYieldForecastTask> list = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(agriYieldForecastTask);
        ExcelUtil<AgriYieldForecastTask> util = new ExcelUtil<>(AgriYieldForecastTask.class);
        util.exportExcel(response, list, "产量预测任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:query')")
    @GetMapping(value = "/{forecastId}")
    public AjaxResult getInfo(@PathVariable("forecastId") Long forecastId)
    {
        return success(agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(forecastId));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:add')")
    @Log(title = "产量预测任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        agriYieldForecastTask.setCreateBy(getUsername());
        if (agriYieldForecastTask.getForecastStatus() == null || agriYieldForecastTask.getForecastStatus().isEmpty())
        {
            agriYieldForecastTask.setForecastStatus("0");
        }
        return toAjax(agriYieldForecastTaskService.insertAgriYieldForecastTask(agriYieldForecastTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        agriYieldForecastTask.setUpdateBy(getUsername());
        return toAjax(agriYieldForecastTaskService.updateAgriYieldForecastTask(agriYieldForecastTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:remove')")
    @Log(title = "产量预测任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{forecastIds}")
    public AjaxResult remove(@PathVariable Long[] forecastIds)
    {
        return toAjax(agriYieldForecastTaskService.deleteAgriYieldForecastTaskByForecastIds(forecastIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测回写", businessType = BusinessType.UPDATE)
    @PutMapping("/predict")
    public AjaxResult predict(@RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        AgriYieldForecastTask db = agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(agriYieldForecastTask.getForecastId());
        if (db == null)
        {
            return error("任务不存在");
        }

        BigDecimal predicted = agriYieldForecastTask.getForecastYieldKg();
        if (predicted == null)
        {
            BigDecimal area = db.getAreaMu() == null ? BigDecimal.ONE : db.getAreaMu();
            predicted = area.multiply(new BigDecimal("450")).setScale(2, RoundingMode.HALF_UP);
        }

        db.setForecastYieldKg(predicted);
        db.setForecastStatus("1");
        db.setForecastTime(DateUtils.getNowDate());
        db.setModelVersion(agriYieldForecastTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriYieldForecastTaskService.updateAgriYieldForecastTask(db));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测调用", businessType = BusinessType.UPDATE)
    @PostMapping("/invoke/{forecastId}")
    public AjaxResult invoke(@PathVariable("forecastId") Long forecastId)
    {
        AgriYieldForecastTask db = agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(forecastId);
        if (db == null)
        {
            return error("任务不存在");
        }

        List<AgriEnvSensorRecord> sensorRecords = querySensorRecords(db.getPlotCode());
        List<AgriPestIdentifyTask> pestTasks = queryPestTasks(db.getPlotCode());
        List<AgriFarmOperationRecord> farmOperations = queryFarmOperations(db.getPlotCode());
        RulePredictResult ruleResult = buildRulePrediction(db, sensorRecords, pestTasks, farmOperations);
        Map<String, Object> context = buildYieldAiContext(db, sensorRecords, pestTasks, farmOperations, ruleResult);

        BigDecimal predictedYield = ruleResult.getPredictedYieldKg();
        String modelVersion = "rule-local-v2";
        String summary = ruleResult.getSummary();
        String suggestion = ruleResult.getSuggestion();
        String rawExcerpt = "";
        String analysisEngine = "rule-local";

        try
        {
            AgriHttpIntegrationClient.YieldInsightResult aiResult = agriHttpIntegrationClient.invokeYieldInsight(JSON.toJSONString(context));
            if (aiResult.getForecastYieldKg() != null && aiResult.getForecastYieldKg().compareTo(BigDecimal.ZERO) > 0)
            {
                predictedYield = aiResult.getForecastYieldKg().setScale(2, RoundingMode.HALF_UP);
            }
            modelVersion = StringUtils.defaultIfBlank(aiResult.getModelVersion(), "deepseek-chat");
            summary = StringUtils.defaultIfBlank(aiResult.getInsightSummary(), summary);
            suggestion = StringUtils.defaultIfBlank(aiResult.getSuggestion(), suggestion);
            rawExcerpt = trimText(aiResult.getRawContent(), 180);
            analysisEngine = "deepseek";
        }
        catch (Exception ex)
        {
            rawExcerpt = "DeepSeek异常: " + trimText(ex.getMessage(), 120);
        }

        db.setForecastYieldKg(predictedYield);
        db.setModelVersion(modelVersion);
        db.setForecastStatus("1");
        db.setForecastTime(DateUtils.getNowDate());
        db.setUpdateBy(getUsername());
        db.setRemark(trimText("结论: " + summary + "；建议: " + suggestion + "；原文摘录: " + rawExcerpt, 500));
        agriYieldForecastTaskService.updateAgriYieldForecastTask(db);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forecastYieldKg", predictedYield);
        result.put("modelVersion", modelVersion);
        result.put("analysisEngine", analysisEngine);
        result.put("summary", summary);
        result.put("suggestion", suggestion);
        result.put("aiOriginalExcerpt", rawExcerpt);
        return success(result);
    }

    private List<AgriEnvSensorRecord> querySensorRecords(String plotCode)
    {
        AgriEnvSensorRecord query = new AgriEnvSensorRecord();
        query.setPlotCode(plotCode);
        return agriEnvSensorRecordService.selectAgriEnvSensorRecordList(query).stream()
            .sorted(Comparator.comparing(AgriEnvSensorRecord::getCollectTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(20)
            .collect(Collectors.toList());
    }

    private List<AgriPestIdentifyTask> queryPestTasks(String plotCode)
    {
        AgriPestIdentifyTask query = new AgriPestIdentifyTask();
        query.setPlotCode(plotCode);
        return agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(query).stream()
            .sorted(Comparator.comparing(AgriPestIdentifyTask::getIdentifyTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(20)
            .collect(Collectors.toList());
    }

    private List<AgriFarmOperationRecord> queryFarmOperations(String plotCode)
    {
        AgriFarmOperationRecord query = new AgriFarmOperationRecord();
        query.setPlotCode(plotCode);
        return agriFarmOperationRecordService.selectAgriFarmOperationRecordList(query).stream()
            .sorted(Comparator.comparing(AgriFarmOperationRecord::getOperationTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(20)
            .collect(Collectors.toList());
    }

    private RulePredictResult buildRulePrediction(
        AgriYieldForecastTask task,
        List<AgriEnvSensorRecord> sensorRecords,
        List<AgriPestIdentifyTask> pestTasks,
        List<AgriFarmOperationRecord> farmOperations)
    {
        List<String> factors = new ArrayList<>();
        BigDecimal benchmark = benchmarkBySeason(task.getSeason());
        BigDecimal adjust = BigDecimal.ONE;

        BigDecimal avgTemp = averageDecimal(sensorRecords.stream().map(AgriEnvSensorRecord::getTemperature).collect(Collectors.toList()));
        BigDecimal avgHumidity = averageDecimal(sensorRecords.stream().map(AgriEnvSensorRecord::getHumidity).collect(Collectors.toList()));
        if (avgTemp != null)
        {
            if (avgTemp.compareTo(new BigDecimal("10")) < 0 || avgTemp.compareTo(new BigDecimal("35")) > 0)
            {
                adjust = adjust.subtract(new BigDecimal("0.12"));
                factors.add("近期待温异常，影响产量稳定性");
            }
            else
            {
                factors.add("温度区间总体正常");
            }
        }
        if (avgHumidity != null && avgHumidity.compareTo(new BigDecimal("85")) > 0)
        {
            adjust = adjust.subtract(new BigDecimal("0.08"));
            factors.add("湿度偏高，病害风险上升");
        }

        long pestRiskCount = pestTasks.stream()
            .filter(item -> "1".equals(item.getIdentifyStatus()) && containsRiskWord(item.getIdentifyResult()))
            .count();
        if (pestRiskCount > 0)
        {
            BigDecimal penalty = new BigDecimal("0.10").add(new BigDecimal(Math.min(pestRiskCount, 5)).multiply(new BigDecimal("0.02")));
            adjust = adjust.subtract(penalty);
            factors.add("识别到病虫害风险 " + pestRiskCount + " 条");
        }

        Date threshold = DateUtils.addDays(new Date(), -30);
        long recentOps = farmOperations.stream().filter(item -> item.getOperationTime() != null && item.getOperationTime().after(threshold)).count();
        if (recentOps == 0)
        {
            adjust = adjust.subtract(new BigDecimal("0.05"));
            factors.add("近30天缺少农事记录，建议复核投入计划");
        }
        else
        {
            factors.add("近30天农事记录 " + recentOps + " 条");
        }

        if (adjust.compareTo(new BigDecimal("0.55")) < 0)
        {
            adjust = new BigDecimal("0.55");
        }
        if (adjust.compareTo(new BigDecimal("1.25")) > 0)
        {
            adjust = new BigDecimal("1.25");
        }

        BigDecimal area = task.getAreaMu() == null ? BigDecimal.ONE : task.getAreaMu();
        BigDecimal perMu = benchmark.multiply(adjust).setScale(2, RoundingMode.HALF_UP);
        BigDecimal predicted = area.multiply(perMu).setScale(2, RoundingMode.HALF_UP);

        BigDecimal confidence = new BigDecimal("0.82");
        if (sensorRecords.isEmpty())
        {
            confidence = confidence.subtract(new BigDecimal("0.12"));
            factors.add("传感器样本不足");
        }
        if (pestTasks.isEmpty())
        {
            confidence = confidence.subtract(new BigDecimal("0.08"));
            factors.add("病虫害样本不足");
        }
        if (confidence.compareTo(new BigDecimal("0.30")) < 0)
        {
            confidence = new BigDecimal("0.30");
        }

        RulePredictResult result = new RulePredictResult();
        result.setPredictedYieldKg(predicted);
        result.setConfidenceRate(confidence.setScale(2, RoundingMode.HALF_UP));
        result.setFactors(factors);
        result.setSummary("多源规则评估完成，预测亩产约 " + perMu + " kg/亩，总产量约 " + predicted + " kg。");
        result.setSuggestion("建议结合传感器趋势与病虫害识别结果，优先复核高风险地块的施肥与防治方案。"
            + (pestRiskCount > 0 ? "当前病虫害风险较高，建议加密巡检。" : "当前病虫害风险可控，维持周度巡检。"));
        return result;
    }

    private Map<String, Object> buildYieldAiContext(
        AgriYieldForecastTask task,
        List<AgriEnvSensorRecord> sensorRecords,
        List<AgriPestIdentifyTask> pestTasks,
        List<AgriFarmOperationRecord> farmOperations,
        RulePredictResult ruleResult)
    {
        Map<String, Object> context = new LinkedHashMap<>();
        Map<String, Object> taskInfo = new LinkedHashMap<>();
        taskInfo.put("forecastId", task.getForecastId());
        taskInfo.put("plotCode", task.getPlotCode());
        taskInfo.put("cropName", task.getCropName());
        taskInfo.put("season", task.getSeason());
        taskInfo.put("sowDate", task.getSowDate());
        taskInfo.put("areaMu", task.getAreaMu());
        taskInfo.put("forecastStatus", task.getForecastStatus());
        context.put("task", taskInfo);

        Map<String, Object> sensorSummary = new LinkedHashMap<>();
        sensorSummary.put("sampleCount", sensorRecords.size());
        sensorSummary.put("avgTemperature", averageDecimal(sensorRecords.stream().map(AgriEnvSensorRecord::getTemperature).collect(Collectors.toList())));
        sensorSummary.put("avgHumidity", averageDecimal(sensorRecords.stream().map(AgriEnvSensorRecord::getHumidity).collect(Collectors.toList())));
        sensorSummary.put("avgCo2", averageDecimal(sensorRecords.stream().map(AgriEnvSensorRecord::getCo2Value).collect(Collectors.toList())));
        sensorSummary.put("warningCount", sensorRecords.stream().filter(item -> "1".equals(item.getStatus())).count());
        context.put("sensorSummary", sensorSummary);

        Map<String, Object> pestSummary = new LinkedHashMap<>();
        pestSummary.put("sampleCount", pestTasks.size());
        pestSummary.put("identifiedCount", pestTasks.stream().filter(item -> "1".equals(item.getIdentifyStatus())).count());
        pestSummary.put("riskCount", pestTasks.stream().filter(item -> containsRiskWord(item.getIdentifyResult())).count());
        context.put("pestSummary", pestSummary);

        Map<String, Object> farmSummary = new LinkedHashMap<>();
        farmSummary.put("operationCount", farmOperations.size());
        farmSummary.put("recentOperations", farmOperations.stream().filter(item -> item.getOperationTime() != null && item.getOperationTime().after(DateUtils.addDays(new Date(), -30))).count());
        context.put("farmSummary", farmSummary);

        AgriYieldForecastTask peerQuery = new AgriYieldForecastTask();
        peerQuery.setCropName(task.getCropName());
        peerQuery.setSeason(task.getSeason());
        List<AgriYieldForecastTask> peerForecasts = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(peerQuery).stream()
            .filter(item -> item.getForecastYieldKg() != null && item.getAreaMu() != null && item.getAreaMu().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(this::resolveTaskSortTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(15)
            .collect(Collectors.toList());
        context.put("peerForecasts", peerForecasts.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("plotCode", item.getPlotCode());
            m.put("yieldPerMu", calcYieldPerMu(item.getForecastYieldKg(), item.getAreaMu()));
            m.put("forecastTime", item.getForecastTime());
            return m;
        }).collect(Collectors.toList()));

        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("predictedYieldKg", ruleResult.getPredictedYieldKg());
        rule.put("confidenceRate", ruleResult.getConfidenceRate());
        rule.put("factors", ruleResult.getFactors());
        context.put("ruleBaseline", rule);
        return context;
    }

    private Date resolveTaskSortTime(AgriYieldForecastTask task)
    {
        return task.getForecastTime() != null ? task.getForecastTime() : task.getCreateTime();
    }

    private List<Map<String, Object>> buildTrendSeries(List<AgriYieldForecastTask> sortedList)
    {
        return sortedList.stream()
            .filter(item -> item.getForecastYieldKg() != null)
            .sorted(Comparator.comparing(this::resolveTaskSortTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .skip(Math.max(0, sortedList.size() - 10))
            .map(item -> {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("name", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, resolveTaskSortTime(item)));
                point.put("value", item.getForecastYieldKg());
                return point;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildPlotBarSeries(List<AgriYieldForecastTask> sortedList)
    {
        return sortedList.stream()
            .filter(item -> item.getForecastYieldKg() != null && item.getAreaMu() != null && item.getAreaMu().compareTo(BigDecimal.ZERO) > 0)
            .map(item -> {
                Map<String, Object> bar = new LinkedHashMap<>();
                bar.put("name", item.getPlotCode());
                bar.put("value", calcYieldPerMu(item.getForecastYieldKg(), item.getAreaMu()));
                return bar;
            })
            .sorted(Comparator.comparing(o -> (BigDecimal) o.get("value"), Comparator.reverseOrder()))
            .limit(8)
            .collect(Collectors.toList());
    }

    private Map<String, Object> toPressureItem(AgriYieldForecastTask task)
    {
        int riskScore = 10;
        List<String> reasons = new ArrayList<>();
        if ("2".equals(task.getForecastStatus()))
        {
            riskScore += 45;
            reasons.add("预测失败");
        }
        if (!"1".equals(task.getForecastStatus()))
        {
            riskScore += 20;
            reasons.add("待预测");
        }
        if (task.getAreaMu() != null && task.getAreaMu().compareTo(new BigDecimal("120")) >= 0)
        {
            riskScore += 15;
            reasons.add("大面积地块优先保障");
        }

        BigDecimal yieldPerMu = calcYieldPerMu(task.getForecastYieldKg(), task.getAreaMu());
        if (yieldPerMu.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal benchmark = benchmarkBySeason(task.getSeason());
            if (yieldPerMu.compareTo(benchmark.multiply(new BigDecimal("0.70"))) < 0)
            {
                riskScore += 20;
                reasons.add("亩产显著偏低");
            }
        }
        else
        {
            riskScore += 15;
            reasons.add("缺少可用亩产数据");
        }

        riskScore = Math.min(riskScore, 100);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("forecastId", task.getForecastId());
        item.put("plotCode", task.getPlotCode());
        item.put("cropName", task.getCropName());
        item.put("season", task.getSeason());
        item.put("areaMu", task.getAreaMu());
        item.put("forecastYieldKg", task.getForecastYieldKg());
        item.put("yieldPerMu", yieldPerMu);
        item.put("forecastStatus", task.getForecastStatus());
        item.put("statusLabel", statusLabel(task.getForecastStatus()));
        item.put("modelVersion", task.getModelVersion());
        item.put("riskScore", riskScore);
        item.put("riskLevel", riskLevel(riskScore));
        item.put("riskReason", reasons.isEmpty() ? "预测状态健康" : String.join("；", reasons));
        return item;
    }

    private List<Map<String, Object>> buildSuggestions(int pending, int failed, int highRiskCount, int total)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (pending > 0)
        {
            suggestions.add(suggestion("压缩待预测积压", "高", "当前待预测任务 " + pending + " 条，建议按地块面积与采收窗口优先排程。"));
        }
        if (failed > 0)
        {
            suggestions.add(suggestion("失败任务复盘", "高", "存在失败任务 " + failed + " 条，建议复核模型参数与样本数据完整性。"));
        }
        if (highRiskCount > 0)
        {
            suggestions.add(suggestion("高风险地块跟踪", "中", "高风险地块 " + highRiskCount + " 条，建议联动传感器与病虫害数据开展复核。"));
        }
        if (suggestions.isEmpty())
        {
            suggestions.add(suggestion("维持周度复盘", "低", "当前 " + total + " 条预测任务稳定，建议按周更新参数并持续校准。"));
        }
        return suggestions;
    }

    private List<Map<String, Object>> toDistributionList(Map<String, Integer> source)
    {
        return source.entrySet().stream().map(entry -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> suggestion(String title, String priority, String content)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("priority", priority);
        item.put("content", content);
        return item;
    }

    private boolean isHighRisk(AgriYieldForecastTask task)
    {
        if ("2".equals(task.getForecastStatus()))
        {
            return true;
        }
        BigDecimal yieldPerMu = calcYieldPerMu(task.getForecastYieldKg(), task.getAreaMu());
        if (yieldPerMu.compareTo(BigDecimal.ZERO) <= 0)
        {
            return false;
        }
        BigDecimal lower = benchmarkBySeason(task.getSeason()).multiply(new BigDecimal("0.70"));
        return yieldPerMu.compareTo(lower) < 0;
    }

    private BigDecimal benchmarkBySeason(String season)
    {
        String seasonText = StringUtils.isEmpty(season) ? "" : season;
        if (seasonText.contains("夏"))
        {
            return new BigDecimal("500");
        }
        if (seasonText.contains("秋"))
        {
            return new BigDecimal("420");
        }
        if (seasonText.contains("冬"))
        {
            return new BigDecimal("350");
        }
        return new BigDecimal("450");
    }

    private BigDecimal calcYieldPerMu(BigDecimal yieldKg, BigDecimal areaMu)
    {
        if (yieldKg == null || areaMu == null || areaMu.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        return yieldKg.divide(areaMu, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageDecimal(List<BigDecimal> values)
    {
        List<BigDecimal> available = values.stream().filter(v -> v != null).collect(Collectors.toList());
        if (available.isEmpty())
        {
            return null;
        }
        BigDecimal sum = available.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(available.size()), 2, RoundingMode.HALF_UP);
    }

    private int scoreByFactors(List<String> factors, String status)
    {
        int score = 90;
        if (!"1".equals(status))
        {
            score -= 15;
        }
        score -= Math.min(factors.size() * 5, 35);
        return Math.max(score, 30);
    }

    private List<String> splitSuggestion(String suggestion)
    {
        if (StringUtils.isBlank(suggestion))
        {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(suggestion.split("[；。\\n]"))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .limit(4)
            .collect(Collectors.toList());
    }

    private boolean containsRiskWord(String text)
    {
        if (StringUtils.isBlank(text))
        {
            return false;
        }
        return text.contains("病") || text.contains("虫") || text.contains("霉") || text.contains("斑") || text.contains("枯");
    }

    private String trimText(String text, int max)
    {
        if (StringUtils.isBlank(text))
        {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
    }

    private String riskLevel(int score)
    {
        if (score >= 80)
        {
            return "低";
        }
        if (score >= 60)
        {
            return "中";
        }
        return "高";
    }

    private String statusLabel(String status)
    {
        if ("1".equals(status))
        {
            return "已预测";
        }
        if ("2".equals(status))
        {
            return "失败";
        }
        return "待预测";
    }

    private static class RulePredictResult
    {
        private BigDecimal predictedYieldKg;

        private BigDecimal confidenceRate;

        private List<String> factors;

        private String summary;

        private String suggestion;

        public BigDecimal getPredictedYieldKg()
        {
            return predictedYieldKg;
        }

        public void setPredictedYieldKg(BigDecimal predictedYieldKg)
        {
            this.predictedYieldKg = predictedYieldKg;
        }

        public BigDecimal getConfidenceRate()
        {
            return confidenceRate;
        }

        public void setConfidenceRate(BigDecimal confidenceRate)
        {
            this.confidenceRate = confidenceRate;
        }

        public List<String> getFactors()
        {
            return factors;
        }

        public void setFactors(List<String> factors)
        {
            this.factors = factors;
        }

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getSuggestion()
        {
            return suggestion;
        }

        public void setSuggestion(String suggestion)
        {
            this.suggestion = suggestion;
        }
    }
}
