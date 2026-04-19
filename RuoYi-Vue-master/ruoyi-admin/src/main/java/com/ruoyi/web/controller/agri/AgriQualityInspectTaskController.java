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
import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriQualityInspectTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 视觉品质检测任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/qualityInspect")
public class AgriQualityInspectTaskController extends BaseController
{
    @Autowired
    private IAgriQualityInspectTaskService agriQualityInspectTaskService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriQualityInspectTask agriQualityInspectTask)
    {
        startPage();
        List<AgriQualityInspectTask> list = agriQualityInspectTaskService.selectAgriQualityInspectTaskList(agriQualityInspectTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriQualityInspectTask> list = agriQualityInspectTaskService.selectAgriQualityInspectTaskList(new AgriQualityInspectTask());
        List<AgriQualityInspectTask> sortedList = list.stream()
            .sorted(Comparator.comparing(AgriQualityInspectTask::getInspectTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        int total = sortedList.size();
        int success = 0;
        int pending = 0;
        int failed = 0;
        int highRiskCount = 0;
        BigDecimal defectRateSum = BigDecimal.ZERO;
        int defectRateCount = 0;

        Map<String, Integer> gradeDistribution = new LinkedHashMap<>();
        Map<String, Integer> statusDistribution = new LinkedHashMap<>();

        for (AgriQualityInspectTask item : sortedList)
        {
            if ("1".equals(item.getInspectStatus()))
            {
                success++;
            }
            else if ("2".equals(item.getInspectStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }

            if (item.getDefectRate() != null)
            {
                defectRateSum = defectRateSum.add(item.getDefectRate());
                defectRateCount++;
                if (item.getDefectRate().compareTo(new BigDecimal("0.08")) >= 0)
                {
                    highRiskCount++;
                }
            }
            if ("2".equals(item.getInspectStatus()))
            {
                highRiskCount++;
            }

            gradeDistribution.merge(StringUtils.isEmpty(item.getQualityGrade()) ? "待判级" : item.getQualityGrade(), 1, Integer::sum);
            statusDistribution.merge(statusLabel(item.getInspectStatus()), 1, Integer::sum);
        }

        BigDecimal avgDefectRate = defectRateCount == 0 ? BigDecimal.ZERO
            : defectRateSum.divide(new BigDecimal(defectRateCount), 4, RoundingMode.HALF_UP);

        List<Map<String, Object>> pressureQueue = sortedList.stream()
            .map(this::toPressureItem)
            .sorted(Comparator.comparingInt((Map<String, Object> o) -> (Integer) o.get("riskScore")).reversed())
            .limit(10)
            .collect(Collectors.toList());

        Map<String, Object> focus = pressureQueue.isEmpty() ? new LinkedHashMap<>() : pressureQueue.get(0);
        List<Map<String, Object>> suggestions = buildSuggestions(pending, failed, highRiskCount, total);

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("success", success);
        kpi.put("pending", pending);
        kpi.put("failed", failed);
        kpi.put("successRate", total == 0 ? 0 : success * 100.0 / total);
        kpi.put("avgDefectRate", avgDefectRate.multiply(new BigDecimal("100")));
        kpi.put("highRiskCount", highRiskCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("pressureQueue", pressureQueue);
        result.put("focus", focus);
        result.put("suggestions", suggestions);
        result.put("gradeDistribution", toDistributionList(gradeDistribution));
        result.put("statusDistribution", toDistributionList(statusDistribution));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:query')")
    @GetMapping("/smart/review/{inspectId}")
    public AjaxResult smartReview(@PathVariable("inspectId") Long inspectId)
    {
        AgriQualityInspectTask task = agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(inspectId);
        if (task == null)
        {
            return error("任务不存在");
        }

        int score = 100;
        List<String> checks = new ArrayList<>();
        if (StringUtils.isEmpty(task.getImageUrl()))
        {
            score -= 35;
            checks.add("未配置样本图片URL，无法追溯视觉证据");
        }
        if (!"1".equals(task.getInspectStatus()))
        {
            score -= 30;
            checks.add("任务尚未完成检测，建议优先触发模型识别");
        }
        if (task.getDefectRate() == null)
        {
            score -= 20;
            checks.add("缺陷率为空，无法用于批次风险分级");
        }
        else if (task.getDefectRate().compareTo(new BigDecimal("0.10")) >= 0)
        {
            score -= 20;
            checks.add("缺陷率高于10%，建议复检并隔离不合格样品");
        }
        if (StringUtils.isEmpty(task.getQualityGrade()))
        {
            score -= 10;
            checks.add("品质等级缺失，建议补齐A/B/C等级用于放行判断");
        }
        score = Math.max(score, 0);

        String summary = checks.isEmpty() ? "样本检测链路完整，可直接进入批次放行审批。" : "检测链路存在待补齐项，请按建议执行复核。";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "视觉品质智能复核");
            context.put("inspectId", task.getInspectId());
            context.put("processBatchNo", task.getProcessBatchNo());
            context.put("sampleCode", task.getSampleCode());
            context.put("imageUrl", task.getImageUrl());
            context.put("inspectStatus", task.getInspectStatus());
            context.put("qualityGrade", task.getQualityGrade());
            context.put("defectRate", task.getDefectRate());
            context.put("inspectResult", task.getInspectResult());
            context.put("ruleChecks", checks);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("视觉品质智能复核", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotEmpty(aiResult.getInsightSummary()))
            {
                summary = aiResult.getInsightSummary();
            }
            if (StringUtils.isNotEmpty(aiResult.getSuggestion()))
            {
                checks.add("AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotEmpty(aiOriginalExcerpt))
            {
                checks.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inspectId", task.getInspectId());
        result.put("algorithm", "vision-quality-review-v1");
        result.put("healthScore", score);
        result.put("riskLevel", riskLevel(score));
        result.put("checks", checks);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:export')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriQualityInspectTask agriQualityInspectTask)
    {
        List<AgriQualityInspectTask> list = agriQualityInspectTaskService.selectAgriQualityInspectTaskList(agriQualityInspectTask);
        ExcelUtil<AgriQualityInspectTask> util = new ExcelUtil<>(AgriQualityInspectTask.class);
        util.exportExcel(response, list, "视觉品质检测任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:query')")
    @GetMapping(value = "/{inspectId}")
    public AjaxResult getInfo(@PathVariable("inspectId") Long inspectId)
    {
        return success(agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(inspectId));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:add')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        agriQualityInspectTask.setCreateBy(getUsername());
        if (agriQualityInspectTask.getInspectStatus() == null || agriQualityInspectTask.getInspectStatus().isEmpty())
        {
            agriQualityInspectTask.setInspectStatus("0");
        }
        return toAjax(agriQualityInspectTaskService.insertAgriQualityInspectTask(agriQualityInspectTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:edit')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        agriQualityInspectTask.setUpdateBy(getUsername());
        return toAjax(agriQualityInspectTaskService.updateAgriQualityInspectTask(agriQualityInspectTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:remove')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inspectIds}")
    public AjaxResult remove(@PathVariable Long[] inspectIds)
    {
        return toAjax(agriQualityInspectTaskService.deleteAgriQualityInspectTaskByInspectIds(inspectIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:edit')")
    @Log(title = "视觉品质检测回写", businessType = BusinessType.UPDATE)
    @PutMapping("/feedback")
    public AjaxResult feedback(@RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        AgriQualityInspectTask db = agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(agriQualityInspectTask.getInspectId());
        if (db == null)
        {
            return error("任务不存在");
        }

        db.setInspectStatus(agriQualityInspectTask.getInspectStatus());
        db.setQualityGrade(agriQualityInspectTask.getQualityGrade());
        db.setDefectRate(agriQualityInspectTask.getDefectRate() == null ? BigDecimal.ZERO : agriQualityInspectTask.getDefectRate());
        db.setInspectResult(agriQualityInspectTask.getInspectResult());
        db.setInspectTime(DateUtils.getNowDate());
        db.setModelVersion(agriQualityInspectTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriQualityInspectTaskService.updateAgriQualityInspectTask(db));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:edit')")
    @Log(title = "视觉品质检测调用", businessType = BusinessType.UPDATE)
    @PostMapping("/invoke/{inspectId}")
    public AjaxResult invoke(@PathVariable("inspectId") Long inspectId)
    {
        AgriQualityInspectTask db = agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(inspectId);
        if (db == null)
        {
            return error("任务不存在");
        }

        try
        {
            AgriHttpIntegrationClient.QualityResult result = agriHttpIntegrationClient.invokeQuality(db);
            db.setInspectStatus("1");
            db.setQualityGrade(result.getQualityGrade());
            db.setDefectRate(result.getDefectRate() == null ? BigDecimal.ZERO : result.getDefectRate());
            db.setInspectResult(result.getInspectResult());
            db.setModelVersion(result.getModelVersion());
            db.setInspectTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriQualityInspectTaskService.updateAgriQualityInspectTask(db);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", "质检调用成功");
            payload.put("aiOriginalExcerpt", result.getRawContent());
            return success(payload);
        }
        catch (Exception ex)
        {
            db.setInspectStatus("2");
            db.setInspectResult("调用失败: " + ex.getMessage());
            db.setInspectTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriQualityInspectTaskService.updateAgriQualityInspectTask(db);
            return error("质检调用失败: " + ex.getMessage());
        }
    }

    private Map<String, Object> toPressureItem(AgriQualityInspectTask task)
    {
        int riskScore = 10;
        List<String> reasons = new ArrayList<>();
        if ("2".equals(task.getInspectStatus()))
        {
            riskScore += 45;
            reasons.add("检测失败");
        }
        if (!"1".equals(task.getInspectStatus()))
        {
            riskScore += 25;
            reasons.add("尚未完成检测");
        }
        if (task.getDefectRate() != null)
        {
            if (task.getDefectRate().compareTo(new BigDecimal("0.08")) >= 0)
            {
                riskScore += 25;
                reasons.add("缺陷率偏高");
            }
        }
        else
        {
            riskScore += 20;
            reasons.add("缺陷率未回写");
        }
        if (StringUtils.isEmpty(task.getImageUrl()))
        {
            riskScore += 10;
            reasons.add("图片证据缺失");
        }
        riskScore = Math.min(riskScore, 100);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("inspectId", task.getInspectId());
        item.put("processBatchNo", task.getProcessBatchNo());
        item.put("sampleCode", task.getSampleCode());
        item.put("imageUrl", task.getImageUrl());
        item.put("inspectStatus", task.getInspectStatus());
        item.put("statusLabel", statusLabel(task.getInspectStatus()));
        item.put("qualityGrade", task.getQualityGrade());
        item.put("defectRate", task.getDefectRate());
        item.put("inspectResult", task.getInspectResult());
        item.put("modelVersion", task.getModelVersion());
        item.put("inspectTime", task.getInspectTime());
        item.put("riskScore", riskScore);
        item.put("riskLevel", riskLevel(riskScore));
        item.put("riskReason", reasons.isEmpty() ? "检测状态健康" : String.join("；", reasons));
        return item;
    }

    private List<Map<String, Object>> buildSuggestions(int pending, int failed, int highRiskCount, int total)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (pending > 0)
        {
            suggestions.add(suggestion("压缩待检积压", "高", "当前待检测任务 " + pending + " 条，建议按批次优先级分片并行执行。"));
        }
        if (failed > 0)
        {
            suggestions.add(suggestion("失败样本复检", "高", "存在失败任务 " + failed + " 条，请优先复核图片URL可达性与模型调用日志。"));
        }
        if (highRiskCount > 0)
        {
            suggestions.add(suggestion("高风险样本隔离", "中", "当前高风险样本 " + highRiskCount + " 条，建议进入人工复核通道后再放行。"));
        }
        if (suggestions.isEmpty())
        {
            suggestions.add(suggestion("维持抽检节奏", "低", "当前 " + total + " 条任务总体稳定，建议按班次持续抽样验证。"));
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

    private String riskLevel(int score)
    {
        if (score >= 80)
        {
            return "高";
        }
        if (score >= 50)
        {
            return "中";
        }
        return "低";
    }

    private String statusLabel(String status)
    {
        if ("1".equals(status))
        {
            return "已检测";
        }
        if ("2".equals(status))
        {
            return "失败";
        }
        return "待检测";
    }
}
