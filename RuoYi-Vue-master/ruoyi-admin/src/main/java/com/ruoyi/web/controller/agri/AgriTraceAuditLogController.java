package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriTraceAuditLog;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriTraceAuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * 溯源审计日志Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/auditLog")
public class AgriTraceAuditLogController extends BaseController
{
    @Autowired
    private IAgriTraceAuditLogService agriTraceAuditLogService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:auditLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriTraceAuditLog agriTraceAuditLog)
    {
        startPage();
        return getDataTable(agriTraceAuditLogService.selectAgriTraceAuditLogList(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriTraceAuditLog> list = agriTraceAuditLogService.selectAgriTraceAuditLogList(new AgriTraceAuditLog());
        int total = list.size();
        int success = 0;
        int failed = 0;
        for (AgriTraceAuditLog item : list)
        {
            if ("1".equals(item.getOperateResult()))
            {
                success++;
            }
            else
            {
                failed++;
            }
        }

        List<AgriTraceAuditLog> recent = list.stream()
            .sorted(Comparator.comparing(AgriTraceAuditLog::getOperateTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(10)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("success", success);
        kpi.put("failed", failed);
        kpi.put("successRate", total == 0 ? 0 : success * 100.0 / total);
        result.put("kpi", kpi);
        result.put("recent", recent);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:edit')")
    @GetMapping("/smart/detect/{auditId}")
    public AjaxResult smartDetect(@PathVariable("auditId") Long auditId)
    {
        AgriTraceAuditLog log = agriTraceAuditLogService.selectAgriTraceAuditLogByAuditId(auditId);
        if (log == null)
        {
            return error("审计记录不存在");
        }

        int score = 10;
        List<String> alerts = new ArrayList<>();
        if (!"1".equals(log.getOperateResult()))
        {
            score += 45;
            alerts.add("操作失败");
        }
        if (StringUtils.isBlank(log.getTxHash()))
        {
            score += 20;
            alerts.add("缺少交易哈希");
        }
        if (StringUtils.isBlank(log.getIpAddress()))
        {
            score += 10;
            alerts.add("缺少来源IP");
        }
        if (StringUtils.isNotBlank(log.getIpAddress()) && (log.getIpAddress().startsWith("10.") || log.getIpAddress().startsWith("192.168.")))
        {
            score += 15;
            alerts.add("内网来源IP");
        }
        score = Math.min(score, 100);
        String level = score >= 80 ? "高" : (score >= 45 ? "中" : "低");
        String summary = "完成审计异常检测，风险等级为" + level + "。";
        String aiOriginalExcerpt = null;

        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "溯源审计异常智能检测");
            context.put("auditId", log.getAuditId());
            context.put("bizNo", log.getBizNo());
            context.put("moduleName", log.getModuleName());
            context.put("actionType", log.getActionType());
            context.put("operatorName", log.getOperatorName());
            context.put("operateResult", log.getOperateResult());
            context.put("txHash", log.getTxHash());
            context.put("ipAddress", log.getIpAddress());
            context.put("ruleAnomalyScore", score);
            context.put("ruleRiskLevel", level);
            context.put("ruleAlerts", alerts);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("溯源审计异常智能检测", JSON.toJSONString(context));
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
                alerts.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotBlank(aiOriginalExcerpt))
            {
                alerts.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("auditId", log.getAuditId());
        result.put("algorithm", "audit-anomaly-v1");
        result.put("anomalyScore", score);
        result.put("riskLevel", level);
        result.put("alerts", alerts);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:export')")
    @Log(title = "溯源审计日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriTraceAuditLog agriTraceAuditLog)
    {
        ExcelUtil<AgriTraceAuditLog> util = new ExcelUtil<>(AgriTraceAuditLog.class);
        util.exportExcel(response, agriTraceAuditLogService.selectAgriTraceAuditLogList(agriTraceAuditLog), "溯源审计日志数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:query')")
    @GetMapping(value = "/{auditId}")
    public AjaxResult getInfo(@PathVariable("auditId") Long auditId)
    {
        return success(agriTraceAuditLogService.selectAgriTraceAuditLogByAuditId(auditId));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:add')")
    @Log(title = "溯源审计日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriTraceAuditLog agriTraceAuditLog)
    {
        return toAjax(agriTraceAuditLogService.insertAgriTraceAuditLog(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:edit')")
    @Log(title = "溯源审计日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriTraceAuditLog agriTraceAuditLog)
    {
        return toAjax(agriTraceAuditLogService.updateAgriTraceAuditLog(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:remove')")
    @Log(title = "溯源审计日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{auditIds}")
    public AjaxResult remove(@PathVariable Long[] auditIds)
    {
        return toAjax(agriTraceAuditLogService.deleteAgriTraceAuditLogByAuditIds(auditIds));
    }
}
