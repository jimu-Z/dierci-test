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
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 农事记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/farmOp")
public class AgriFarmOperationRecordController extends BaseController
{
    @Autowired
    private IAgriFarmOperationRecordService agriFarmOperationRecordService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    /**
     * 查询农事记录列表
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        startPage();
        List<AgriFarmOperationRecord> list = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(agriFarmOperationRecord);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:farmOp:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        return success(buildDashboard(agriFarmOperationRecord));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmOp:query')")
    @GetMapping("/smart/advice/{operationId}")
    public AjaxResult advice(@PathVariable("operationId") Long operationId)
    {
        AgriFarmOperationRecord record = agriFarmOperationRecordService.selectAgriFarmOperationRecordByOperationId(operationId);
        if (record == null)
        {
            return error("农事记录不存在");
        }
        return success(buildAdvice(record));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmOp:query')")
    @GetMapping("/plot/options")
    public AjaxResult plotOptions(@RequestParam(value = "keyword", required = false) String keyword)
    {
        return success(agriFarmOperationRecordService.selectPlotCodeOptions(keyword));
    }

    /**
     * 导出农事记录列表
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:export')")
    @Log(title = "农事记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriFarmOperationRecord agriFarmOperationRecord)
    {
        List<AgriFarmOperationRecord> list = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(agriFarmOperationRecord);
        ExcelUtil<AgriFarmOperationRecord> util = new ExcelUtil<>(AgriFarmOperationRecord.class);
        util.exportExcel(response, list, "农事记录数据");
    }

    /**
     * 获取农事记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:query')")
    @GetMapping(value = "/{operationId}")
    public AjaxResult getInfo(@PathVariable("operationId") Long operationId)
    {
        return success(agriFarmOperationRecordService.selectAgriFarmOperationRecordByOperationId(operationId));
    }

    /**
     * 新增农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:add')")
    @Log(title = "农事记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriFarmOperationRecord agriFarmOperationRecord)
    {
        agriFarmOperationRecord.setCreateBy(getUsername());
        return toAjax(agriFarmOperationRecordService.insertAgriFarmOperationRecord(agriFarmOperationRecord));
    }

    /**
     * 修改农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:edit')")
    @Log(title = "农事记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriFarmOperationRecord agriFarmOperationRecord)
    {
        agriFarmOperationRecord.setUpdateBy(getUsername());
        return toAjax(agriFarmOperationRecordService.updateAgriFarmOperationRecord(agriFarmOperationRecord));
    }

    /**
     * 删除农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:remove')")
    @Log(title = "农事记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operationIds}")
    public AjaxResult remove(@PathVariable Long[] operationIds)
    {
        return toAjax(agriFarmOperationRecordService.deleteAgriFarmOperationRecordByOperationIds(operationIds));
    }

    private Map<String, Object> buildDashboard(AgriFarmOperationRecord query)
    {
        List<AgriFarmOperationRecord> records = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(query);
        Set<String> plots = new HashSet<>();
        Set<String> operators = new HashSet<>();
        Map<String, Integer> typeCounter = new HashMap<>();
        int activeCount = 0;
        int statusCount = 0;
        BigDecimal inputTotal = BigDecimal.ZERO;
        List<AgriFarmOperationRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AgriFarmOperationRecord::getOperationTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriFarmOperationRecord record : records)
        {
            if (record == null)
            {
                continue;
            }
            if (record.getPlotCode() != null)
            {
                plots.add(record.getPlotCode());
            }
            if (record.getOperatorName() != null)
            {
                operators.add(record.getOperatorName());
            }
            typeCounter.put(record.getOperationType(), typeCounter.getOrDefault(record.getOperationType(), 0) + 1);
            if (record.getInputAmount() != null)
            {
                inputTotal = inputTotal.add(record.getInputAmount());
            }
            if ("0".equals(record.getStatus()))
            {
                activeCount++;
            }
            else
            {
                statusCount++;
            }
        }

        for (AgriFarmOperationRecord record : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("operationId", record.getOperationId());
            row.put("plotCode", record.getPlotCode());
            row.put("operationType", record.getOperationType());
            row.put("operatorName", record.getOperatorName());
            row.put("operationTime", record.getOperationTime());
            row.put("status", record.getStatus());
            row.put("inputName", record.getInputName());
            row.put("inputAmount", record.getInputAmount());
            recentRows.add(row);
        }

        List<Map<String, Object>> typeRows = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeCounter.entrySet())
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type", entry.getKey());
            row.put("count", entry.getValue());
            typeRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", records.size());
        summary.put("plotCount", plots.size());
        summary.put("operatorCount", operators.size());
        summary.put("activeCount", activeCount);
        summary.put("inactiveCount", statusCount);
        summary.put("inputTotal", inputTotal);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("typeRows", typeRows);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildAdvice(AgriFarmOperationRecord record)
    {
        int riskScore = 100;
        List<String> suggestions = new ArrayList<>();
        if (record.getOperationContent() == null || record.getOperationContent().trim().isEmpty())
        {
            suggestions.add("作业内容为空，建议补充作业目标和执行细节");
            riskScore -= 20;
        }
        if (record.getInputAmount() == null || record.getInputAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            suggestions.add("投入品用量缺失，建议核对投放记录与台账");
            riskScore -= 15;
        }

        String advice;
        if ("SOWING".equals(record.getOperationType()))
        {
            advice = "播种作业建议先核对地块墒情与播种密度，再确认种子批次。";
        }
        else if ("FERTILIZATION".equals(record.getOperationType()))
        {
            advice = "施肥作业建议核对施用均匀度和后续追肥计划。";
        }
        else if ("IRRIGATION".equals(record.getOperationType()))
        {
            advice = "灌溉作业建议结合天气与墒情，避免过量和积水。";
        }
        else if ("PEST_CONTROL".equals(record.getOperationType()))
        {
            advice = "用药作业建议同步检查安全间隔期和回收废液处理。";
        }
        else if ("HARVEST".equals(record.getOperationType()))
        {
            advice = "采收作业建议同步准备分级、装筐和冷链转运安排。";
        }
        else
        {
            advice = "当前作业类型建议先补充标准作业卡，再进入执行复盘。";
        }

        if ("1".equals(record.getStatus()))
        {
            suggestions.add("记录处于非正常状态，建议复核执行人与审批链路");
            riskScore -= 10;
        }

        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("operationId", record.getOperationId());
            context.put("plotCode", record.getPlotCode());
            context.put("operationType", record.getOperationType());
            context.put("operationContent", record.getOperationContent());
            context.put("inputName", record.getInputName());
            context.put("inputAmount", record.getInputAmount());
            context.put("status", record.getStatus());
            context.put("operatorName", record.getOperatorName());
            context.put("ruleRiskScore", Math.max(0, riskScore));
            context.put("ruleAdvice", advice);
            context.put("ruleSuggestions", suggestions);

            AgriHttpIntegrationClient.GeneralInsightResult aiResult =
                agriHttpIntegrationClient.invokeGeneralInsight("农事作业智能建议", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getInsightSummary()))
            {
                advice = aiResult.getInsightSummary();
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
        catch (Exception ex)
        {
            suggestions.add("AI分析暂不可用，已回退本地规则：" + StringUtils.substring(ex.getMessage(), 0, 120));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operationId", record.getOperationId());
        result.put("operationType", record.getOperationType());
        result.put("riskScore", Math.max(0, riskScore));
        result.put("riskLevel", riskScore >= 85 ? "低" : riskScore >= 70 ? "中" : "高");
        result.put("advice", advice);
        result.put("suggestions", suggestions);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        result.put("record", record);
        return result;
    }
}
