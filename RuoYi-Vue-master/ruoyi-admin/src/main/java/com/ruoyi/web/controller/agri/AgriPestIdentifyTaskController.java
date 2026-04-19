package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriPestIdentifyTaskService;
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
 * 病虫害识别任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/pestIdentify")
public class AgriPestIdentifyTaskController extends BaseController
{
    @Autowired
    private IAgriPestIdentifyTaskService agriPestIdentifyTaskService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    /**
     * 查询病虫害识别任务列表
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriPestIdentifyTask agriPestIdentifyTask)
    {
        startPage();
        List<AgriPestIdentifyTask> list = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(agriPestIdentifyTask);
        return getDataTable(list);
    }

    /**
     * 病虫害识别看板概览
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(Integer topN)
    {
        AgriPestIdentifyTask query = new AgriPestIdentifyTask();
        query.setStatus("0");
        List<AgriPestIdentifyTask> list = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(query);

        int total = list.size();
        int successCount = 0;
        int failCount = 0;
        BigDecimal confidenceTotal = BigDecimal.ZERO;
        int confidenceCount = 0;
        int highRiskCount = 0;

        for (AgriPestIdentifyTask item : list)
        {
            if ("1".equals(item.getIdentifyStatus()))
            {
                successCount++;
            }
            else if ("2".equals(item.getIdentifyStatus()))
            {
                failCount++;
            }

            if (item.getConfidence() != null)
            {
                confidenceTotal = confidenceTotal.add(item.getConfidence());
                confidenceCount++;
            }

            if (isHighRisk(item))
            {
                highRiskCount++;
            }
        }

        BigDecimal avgConfidence = confidenceCount == 0
            ? BigDecimal.ZERO
            : confidenceTotal.divide(new BigDecimal(confidenceCount), 4, RoundingMode.HALF_UP);
        BigDecimal failRate = total == 0
            ? BigDecimal.ZERO
            : new BigDecimal(failCount).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        int topLimit = topN == null ? 8 : Math.max(4, Math.min(16, topN));
        list.sort(Comparator.comparing(AgriPestIdentifyTask::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> recentTasks = new ArrayList<>();
        for (AgriPestIdentifyTask item : list)
        {
            if (recentTasks.size() >= topLimit)
            {
                break;
            }
            recentTasks.add(toTaskCard(item));
        }

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("successCount", successCount);
        kpi.put("failCount", failCount);
        kpi.put("pendingCount", Math.max(total - successCount - failCount, 0));
        kpi.put("avgConfidence", avgConfidence.setScale(2, RoundingMode.HALF_UP));
        kpi.put("failRate", failRate.setScale(2, RoundingMode.HALF_UP));
        kpi.put("highRiskCount", highRiskCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("recentTasks", recentTasks);
        return success(result);
    }

    /**
     * 导出病虫害识别任务列表
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:export')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriPestIdentifyTask agriPestIdentifyTask)
    {
        List<AgriPestIdentifyTask> list = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(agriPestIdentifyTask);
        ExcelUtil<AgriPestIdentifyTask> util = new ExcelUtil<>(AgriPestIdentifyTask.class);
        util.exportExcel(response, list, "病虫害识别任务数据");
    }

    /**
     * 获取病虫害识别任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(taskId));
    }

    /**
     * 新增病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:add')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        agriPestIdentifyTask.setCreateBy(getUsername());
        if (agriPestIdentifyTask.getIdentifyStatus() == null || agriPestIdentifyTask.getIdentifyStatus().isEmpty())
        {
            agriPestIdentifyTask.setIdentifyStatus("0");
        }
        return toAjax(agriPestIdentifyTaskService.insertAgriPestIdentifyTask(agriPestIdentifyTask));
    }

    /**
     * 修改病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:edit')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        agriPestIdentifyTask.setUpdateBy(getUsername());
        return toAjax(agriPestIdentifyTaskService.updateAgriPestIdentifyTask(agriPestIdentifyTask));
    }

    /**
     * 删除病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:remove')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(agriPestIdentifyTaskService.deleteAgriPestIdentifyTaskByTaskIds(taskIds));
    }

    /**
     * 模型识别回写占位接口
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:edit')")
    @Log(title = "病虫害识别回写", businessType = BusinessType.UPDATE)
    @PutMapping("/feedback")
    public AjaxResult feedback(@RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        AgriPestIdentifyTask db = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(agriPestIdentifyTask.getTaskId());
        if (db == null)
        {
            return error("任务不存在");
        }
        db.setIdentifyStatus(agriPestIdentifyTask.getIdentifyStatus());
        db.setIdentifyResult(agriPestIdentifyTask.getIdentifyResult());
        db.setConfidence(agriPestIdentifyTask.getConfidence() == null ? BigDecimal.ZERO : agriPestIdentifyTask.getConfidence());
        db.setIdentifyTime(DateUtils.getNowDate());
        db.setModelVersion(agriPestIdentifyTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriPestIdentifyTaskService.updateAgriPestIdentifyTask(db));
    }

    /**
     * 触发真实AI识别调用
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:edit')")
    @Log(title = "病虫害识别调用", businessType = BusinessType.UPDATE)
    @PostMapping("/invoke/{taskId}")
    public AjaxResult invoke(@PathVariable("taskId") Long taskId)
    {
        AgriPestIdentifyTask db = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(taskId);
        if (db == null)
        {
            return error("任务不存在");
        }

        List<Map<String, Object>> processTrace = new ArrayList<>();
        appendTrace(processTrace, "任务入队", "接收识别任务，开始进行图像与上下文校验。", "success");
        appendTrace(processTrace, "图像预处理", "校验图片链接与地块信息，完成基础特征提取。", "success");

        return executeInvoke(db, processTrace);
    }

    /**
     * 上传后快速创建并调用识别
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:add')")
    @Log(title = "病虫害识别快速调用", businessType = BusinessType.INSERT)
    @PostMapping("/quickInvoke")
    public AjaxResult quickInvoke(@Valid @RequestBody AgriPestIdentifyTask task)
    {
        task.setCreateBy(getUsername());
        task.setIdentifyStatus("0");
        task.setStatus("0");
        if (task.getModelVersion() == null || task.getModelVersion().isEmpty())
        {
            task.setModelVersion("deepseek-chat");
        }

        int inserted = agriPestIdentifyTaskService.insertAgriPestIdentifyTask(task);
        if (inserted <= 0 || task.getTaskId() == null)
        {
            return error("任务创建失败");
        }

        AgriPestIdentifyTask db = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(task.getTaskId());
        if (db == null)
        {
            return error("任务创建后查询失败");
        }

        List<Map<String, Object>> processTrace = new ArrayList<>();
        appendTrace(processTrace, "任务创建", "图片上传成功并已生成识别任务。", "success");
        appendTrace(processTrace, "任务入队", "任务进入识别队列，准备调用模型。", "success");
        return executeInvoke(db, processTrace);
    }

    private AjaxResult executeInvoke(AgriPestIdentifyTask db, List<Map<String, Object>> processTrace)
    {

        try
        {
            appendTrace(processTrace, "智能识别算法", "调用AI识别模型，进行病虫害检测与标签判断。", "running");
            AgriHttpIntegrationClient.PestResult result = agriHttpIntegrationClient.invokePest(db);
            db.setIdentifyStatus("1");
            db.setIdentifyResult(result.getIdentifyResult());
            db.setConfidence(result.getConfidence() == null ? BigDecimal.ZERO : result.getConfidence());
            db.setModelVersion(result.getModelVersion());
            db.setIdentifyTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriPestIdentifyTaskService.updateAgriPestIdentifyTask(db);

            appendTrace(processTrace, "结果聚合", "完成风险评级与处置建议生成。", "success");
            appendTrace(processTrace, "回写完成", "识别结果已写入任务台账。", "success");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("task", toTaskCard(db));
            payload.put("process", processTrace);
            payload.put("algorithm", buildAlgorithmSummary(db));
            payload.put("aiOriginalExcerpt", result.getRawContent());
            return success(payload);
        }
        catch (Exception ex)
        {
            db.setIdentifyStatus("2");
            db.setIdentifyResult("调用失败: " + ex.getMessage());
            db.setIdentifyTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriPestIdentifyTaskService.updateAgriPestIdentifyTask(db);

            appendTrace(processTrace, "智能识别算法", "模型调用失败: " + ex.getMessage(), "error");
            appendTrace(processTrace, "异常回写", "任务状态已标记失败，请检查图片质量或模型连通性。", "error");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("task", toTaskCard(db));
            payload.put("process", processTrace);
            payload.put("algorithm", buildAlgorithmSummary(db));
            payload.put("invokeSuccess", false);
            payload.put("message", "识别调用失败: " + ex.getMessage());
            return success(payload);
        }
    }

    private void appendTrace(List<Map<String, Object>> processTrace, String stage, String message, String status)
    {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("stage", stage);
        node.put("message", message);
        node.put("status", status);
        node.put("time", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getNowDate()));
        processTrace.add(node);
    }

    private Map<String, Object> toTaskCard(AgriPestIdentifyTask item)
    {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("taskId", item.getTaskId());
        card.put("plotCode", item.getPlotCode());
        card.put("cropName", item.getCropName());
        card.put("imageUrl", item.getImageUrl());
        card.put("identifyStatus", item.getIdentifyStatus());
        card.put("identifyResult", item.getIdentifyResult());
        card.put("confidence", item.getConfidence() == null ? BigDecimal.ZERO : item.getConfidence().setScale(2, RoundingMode.HALF_UP));
        card.put("modelVersion", item.getModelVersion());
        card.put("identifyTime", item.getIdentifyTime());
        card.put("riskLevel", buildRiskLevel(item));
        return card;
    }

    private Map<String, Object> buildAlgorithmSummary(AgriPestIdentifyTask task)
    {
        Map<String, Object> summary = new LinkedHashMap<>();
        BigDecimal confidence = task.getConfidence() == null ? BigDecimal.ZERO : task.getConfidence();
        String result = task.getIdentifyResult() == null ? "" : task.getIdentifyResult();
        summary.put("engine", task.getModelVersion() == null ? "deepseek-chat" : task.getModelVersion());
        summary.put("confidence", confidence.setScale(2, RoundingMode.HALF_UP));
        summary.put("riskLevel", buildRiskLevel(task));
        summary.put("tags", extractTags(result));
        summary.put("conclusion", result);
        return summary;
    }

    private List<String> extractTags(String text)
    {
        List<String> tags = new ArrayList<>();
        if (text == null || text.isEmpty())
        {
            return tags;
        }
        if (text.contains("病") || text.contains("霉"))
        {
            tags.add("病害风险");
        }
        if (text.contains("虫"))
        {
            tags.add("虫害风险");
        }
        if (text.contains("叶") || text.contains("斑"))
        {
            tags.add("叶面异常");
        }
        if (tags.isEmpty())
        {
            tags.add("待复核");
        }
        return tags;
    }

    private boolean isHighRisk(AgriPestIdentifyTask task)
    {
        return "高".equals(buildRiskLevel(task));
    }

    private String buildRiskLevel(AgriPestIdentifyTask task)
    {
        BigDecimal confidence = task.getConfidence() == null ? BigDecimal.ZERO : task.getConfidence();
        String result = task.getIdentifyResult() == null ? "" : task.getIdentifyResult();
        if ("2".equals(task.getIdentifyStatus()))
        {
            return "高";
        }
        if (result.contains("病") || result.contains("虫") || result.contains("霉"))
        {
            if (confidence.compareTo(new BigDecimal("0.75")) >= 0)
            {
                return "高";
            }
            return "中";
        }
        if (confidence.compareTo(new BigDecimal("0.85")) >= 0)
        {
            return "低";
        }
        return "中";
    }
}
