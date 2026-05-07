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
import com.ruoyi.system.domain.AgriDataUplinkTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriDataUplinkTaskService;
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
 * 数据上链任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/dataUplink")
public class AgriDataUplinkTaskController extends BaseController
{
    @Autowired
    private IAgriDataUplinkTaskService agriDataUplinkTaskService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDataUplinkTask agriDataUplinkTask)
    {
        startPage();
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(agriDataUplinkTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(new AgriDataUplinkTask());
        int total = list.size();
        int success = 0;
        int failed = 0;
        int pending = 0;
        for (AgriDataUplinkTask item : list)
        {
            if ("1".equals(item.getUplinkStatus()))
            {
                success++;
            }
            else if ("2".equals(item.getUplinkStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }
        }

        List<AgriDataUplinkTask> recent = list.stream()
            .sorted(Comparator.comparing(AgriDataUplinkTask::getUplinkTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("success", success);
        kpi.put("failed", failed);
        kpi.put("pending", pending);
        kpi.put("successRate", total == 0 ? 0 : success * 100.0 / total);
        result.put("kpi", kpi);
        result.put("recent", recent);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(new AgriDataUplinkTask());
        List<AgriDataUplinkTask> sortedList = list.stream()
            .sorted(Comparator.comparing(AgriDataUplinkTask::getUplinkTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        int total = sortedList.size();
        int success = 0;
        int failed = 0;
        int pending = 0;
        int missingTx = 0;
        int missingContractAddress = 0;

        Map<String, Integer> dataTypeDistribution = new LinkedHashMap<>();
        Map<String, Integer> platformDistribution = new LinkedHashMap<>();

        for (AgriDataUplinkTask item : sortedList)
        {
            if ("1".equals(item.getUplinkStatus()))
            {
                success++;
            }
            else if ("2".equals(item.getUplinkStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }

            if (StringUtils.isEmpty(item.getTxHash()))
            {
                missingTx++;
            }
            if (StringUtils.isEmpty(item.getContractAddress()))
            {
                missingContractAddress++;
            }

            dataTypeDistribution.merge(StringUtils.defaultIfEmpty(item.getDataType(), "UNKNOWN"), 1, Integer::sum);
            platformDistribution.merge(StringUtils.defaultIfEmpty(item.getChainPlatform(), "UNKNOWN"), 1, Integer::sum);
        }

        List<Map<String, Object>> pressureQueue = sortedList.stream()
            .map(this::toPressureItem)
            .sorted(Comparator.comparingInt((Map<String, Object> o) -> (Integer) o.get("riskScore")).reversed())
            .limit(8)
            .collect(Collectors.toList());

        Map<String, Object> focus = pressureQueue.isEmpty() ? new LinkedHashMap<>() : pressureQueue.get(0);

        List<Map<String, Object>> suggestions = buildOpsSuggestions(pending, failed, missingTx, missingContractAddress, total);

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("success", success);
        kpi.put("failed", failed);
        kpi.put("pending", pending);
        kpi.put("successRate", total == 0 ? 0 : success * 100.0 / total);
        kpi.put("missingTx", missingTx);
        kpi.put("missingContractAddress", missingContractAddress);

        result.put("kpi", kpi);
        result.put("pressureQueue", pressureQueue);
        result.put("focus", focus);
        result.put("suggestions", suggestions);
        result.put("dataTypeDistribution", toDistributionList(dataTypeDistribution));
        result.put("platformDistribution", toDistributionList(platformDistribution));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:edit')")
    @GetMapping("/smart/verify/{uplinkId}")
    public AjaxResult smartVerify(@PathVariable("uplinkId") Long uplinkId)
    {
        AgriDataUplinkTask task = agriDataUplinkTaskService.selectAgriDataUplinkTaskByUplinkId(uplinkId);
        if (task == null)
        {
            return error("上链任务不存在");
        }

        int integrity = 100;
        List<String> issues = new ArrayList<>();
        if (StringUtils.isEmpty(task.getDataHash()) || task.getDataHash().length() < 16)
        {
            integrity -= 45;
            issues.add("数据哈希长度异常");
        }
        if (StringUtils.isEmpty(task.getTxHash()))
        {
            integrity -= 30;
            issues.add("缺少交易哈希");
        }
        if (!"1".equals(task.getUplinkStatus()))
        {
            integrity -= 20;
            issues.add("任务尚未成功上链");
        }
        integrity = Math.max(integrity, 0);
        String level = integrity < 50 ? "高" : (integrity < 80 ? "中" : "低");
        String summary = "完成上链完整性核验，当前风险等级为" + level + "。";
        String aiOriginalExcerpt = null;

        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "数据上链完整性智能核验");
            context.put("uplinkId", task.getUplinkId());
            context.put("batchNo", task.getBatchNo());
            context.put("dataType", task.getDataType());
            context.put("chainPlatform", task.getChainPlatform());
            context.put("uplinkStatus", task.getUplinkStatus());
            context.put("dataHashLength", task.getDataHash() == null ? 0 : task.getDataHash().length());
            context.put("hasTxHash", StringUtils.isNotEmpty(task.getTxHash()));
            context.put("hasContractAddress", StringUtils.isNotEmpty(task.getContractAddress()));
            context.put("ruleIntegrity", integrity);
            context.put("ruleRiskLevel", level);
            context.put("ruleIssues", issues);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("数据上链完整性智能核验", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotEmpty(aiResult.getInsightSummary()))
            {
                summary = aiResult.getInsightSummary();
            }
            if (StringUtils.isNotEmpty(aiResult.getRiskLevel()))
            {
                level = aiResult.getRiskLevel();
            }
            if (StringUtils.isNotEmpty(aiResult.getSuggestion()))
            {
                issues.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotEmpty(aiOriginalExcerpt))
            {
                issues.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uplinkId", task.getUplinkId());
        result.put("algorithm", "onchain-integrity-v1");
        result.put("integrity", integrity);
        result.put("riskLevel", level);
        result.put("issues", issues);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    private Map<String, Object> toPressureItem(AgriDataUplinkTask task)
    {
        int riskScore = 10;
        List<String> reasons = new ArrayList<>();
        if ("2".equals(task.getUplinkStatus()))
        {
            riskScore += 45;
            reasons.add("上链失败");
        }
        if (!"1".equals(task.getUplinkStatus()))
        {
            riskScore += 25;
            reasons.add("尚未成功上链");
        }
        if (StringUtils.isEmpty(task.getTxHash()))
        {
            riskScore += 20;
            reasons.add("缺少交易哈希");
        }
        if (StringUtils.isEmpty(task.getContractAddress()))
        {
            riskScore += 15;
            reasons.add("缺少合约地址");
        }
        riskScore = Math.min(riskScore, 100);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uplinkId", task.getUplinkId());
        item.put("batchNo", task.getBatchNo());
        item.put("dataType", task.getDataType());
        item.put("chainPlatform", task.getChainPlatform());
        item.put("uplinkStatus", task.getUplinkStatus());
        item.put("statusLabel", statusLabel(task.getUplinkStatus()));
        item.put("txHash", task.getTxHash());
        item.put("contractAddress", task.getContractAddress());
        item.put("riskScore", riskScore);
        item.put("riskLevel", riskLevel(riskScore));
        item.put("riskReason", reasons.isEmpty() ? "状态健康" : String.join("；", reasons));
        item.put("summary", StringUtils.defaultIfEmpty(task.getRemark(), "请优先核对哈希与链上回执一致性。"));
        item.put("uplinkTime", task.getUplinkTime());
        return item;
    }

    private List<Map<String, Object>> buildOpsSuggestions(int pending, int failed, int missingTx, int missingContractAddress, int total)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (pending > 0)
        {
            suggestions.add(suggestion("压缩待上链堆积", "高", "当前待上链任务 " + pending + " 条，建议按数据类型分批执行并优先处理高价值批次。"));
        }
        if (failed > 0)
        {
            suggestions.add(suggestion("失败任务复盘", "高", "检测到失败任务 " + failed + " 条，建议复核哈希格式、链平台连接和合约地址可用性。"));
        }
        if (missingTx > 0 || missingContractAddress > 0)
        {
            suggestions.add(suggestion("补齐链上凭证", "中", "缺失交易哈希 " + missingTx + " 条，缺失合约地址 " + missingContractAddress + " 条，建议补录后再导出归档。"));
        }
        if (suggestions.isEmpty())
        {
            suggestions.add(suggestion("保持巡检节奏", "低", "当前 " + total + " 条任务整体健康，建议继续每日核对链上回执与业务台账一致性。"));
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
            return "已上链";
        }
        if ("2".equals(status))
        {
            return "失败";
        }
        return "待上链";
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:export')")
    @Log(title = "数据上链任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDataUplinkTask agriDataUplinkTask)
    {
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(agriDataUplinkTask);
        ExcelUtil<AgriDataUplinkTask> util = new ExcelUtil<>(AgriDataUplinkTask.class);
        util.exportExcel(response, list, "数据上链任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:query')")
    @GetMapping(value = "/{uplinkId}")
    public AjaxResult getInfo(@PathVariable("uplinkId") Long uplinkId)
    {
        return success(agriDataUplinkTaskService.selectAgriDataUplinkTaskByUplinkId(uplinkId));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:add')")
    @Log(title = "数据上链任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriDataUplinkTask agriDataUplinkTask)
    {
        agriDataUplinkTask.setCreateBy(getUsername());
        if (StringUtils.isEmpty(agriDataUplinkTask.getUplinkStatus()))
        {
            agriDataUplinkTask.setUplinkStatus("0");
        }
        return toAjax(agriDataUplinkTaskService.insertAgriDataUplinkTask(agriDataUplinkTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:edit')")
    @Log(title = "数据上链任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriDataUplinkTask agriDataUplinkTask)
    {
        agriDataUplinkTask.setUpdateBy(getUsername());
        return toAjax(agriDataUplinkTaskService.updateAgriDataUplinkTask(agriDataUplinkTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:edit')")
    @Log(title = "数据上链执行", businessType = BusinessType.UPDATE)
    @PutMapping("/uplink/{uplinkId}")
    public AjaxResult uplink(@PathVariable("uplinkId") Long uplinkId)
    {
        AgriDataUplinkTask task = agriDataUplinkTaskService.selectAgriDataUplinkTaskByUplinkId(uplinkId);
        if (task == null)
        {
            return error("上链任务不存在");
        }
        if (!"1".equals(task.getUplinkStatus()))
        {
            task.setUplinkStatus("1");
            task.setUplinkTime(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(task.getTxHash()))
        {
            task.setTxHash("0x" + Long.toHexString(System.currentTimeMillis()) + Long.toHexString(uplinkId));
        }
        task.setUpdateBy(getUsername());
        return toAjax(agriDataUplinkTaskService.updateAgriDataUplinkTask(task));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:remove')")
    @Log(title = "数据上链任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{uplinkIds}")
    public AjaxResult remove(@PathVariable Long[] uplinkIds)
    {
        return toAjax(agriDataUplinkTaskService.deleteAgriDataUplinkTaskByUplinkIds(uplinkIds));
    }
}
