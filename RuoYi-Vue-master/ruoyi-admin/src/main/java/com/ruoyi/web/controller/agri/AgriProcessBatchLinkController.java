package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriProcessBatchLink;
import com.ruoyi.system.service.IAgriProcessBatchLinkService;
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

/**
 * 加工批次关联Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/processBatch")
public class AgriProcessBatchLinkController extends BaseController
{
    @Autowired
    private IAgriProcessBatchLinkService agriProcessBatchLinkService;

    @PreAuthorize("@ss.hasPermi('agri:processBatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriProcessBatchLink agriProcessBatchLink)
    {
        startPage();
        List<AgriProcessBatchLink> list = agriProcessBatchLinkService.selectAgriProcessBatchLinkList(agriProcessBatchLink);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriProcessBatchLink agriProcessBatchLink)
    {
        return success(buildDashboard(agriProcessBatchLink));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:query')")
    @GetMapping("/smart/check/{linkId}")
    public AjaxResult check(@PathVariable("linkId") Long linkId)
    {
        AgriProcessBatchLink record = agriProcessBatchLinkService.selectAgriProcessBatchLinkByLinkId(linkId);
        if (record == null)
        {
            return error("加工批次关联不存在");
        }
        return success(buildCheck(record));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:export')")
    @Log(title = "加工批次关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriProcessBatchLink agriProcessBatchLink)
    {
        List<AgriProcessBatchLink> list = agriProcessBatchLinkService.selectAgriProcessBatchLinkList(agriProcessBatchLink);
        ExcelUtil<AgriProcessBatchLink> util = new ExcelUtil<>(AgriProcessBatchLink.class);
        util.exportExcel(response, list, "加工批次关联数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:query')")
    @GetMapping(value = "/{linkId}")
    public AjaxResult getInfo(@PathVariable("linkId") Long linkId)
    {
        return success(agriProcessBatchLinkService.selectAgriProcessBatchLinkByLinkId(linkId));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:add')")
    @Log(title = "加工批次关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriProcessBatchLink agriProcessBatchLink)
    {
        agriProcessBatchLink.setCreateBy(getUsername());
        return toAjax(agriProcessBatchLinkService.insertAgriProcessBatchLink(agriProcessBatchLink));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:edit')")
    @Log(title = "加工批次关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriProcessBatchLink agriProcessBatchLink)
    {
        agriProcessBatchLink.setUpdateBy(getUsername());
        return toAjax(agriProcessBatchLinkService.updateAgriProcessBatchLink(agriProcessBatchLink));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:remove')")
    @Log(title = "加工批次关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{linkIds}")
    public AjaxResult remove(@PathVariable Long[] linkIds)
    {
        return toAjax(agriProcessBatchLinkService.deleteAgriProcessBatchLinkByLinkIds(linkIds));
    }

    private Map<String, Object> buildDashboard(AgriProcessBatchLink query)
    {
        List<AgriProcessBatchLink> records = agriProcessBatchLinkService.selectAgriProcessBatchLinkList(query);
        Set<String> plantingBatchNos = new HashSet<>();
        Set<String> processBatchNos = new HashSet<>();
        int pendingCount = 0;
        int processingCount = 0;
        int finishedCount = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;
        List<AgriProcessBatchLink> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AgriProcessBatchLink::getProcessTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriProcessBatchLink record : records)
        {
            if (record == null)
            {
                continue;
            }
            if (record.getPlantingBatchNo() != null)
            {
                plantingBatchNos.add(record.getPlantingBatchNo());
            }
            if (record.getProcessBatchNo() != null)
            {
                processBatchNos.add(record.getProcessBatchNo());
            }
            if (record.getProcessWeightKg() != null)
            {
                totalWeight = totalWeight.add(record.getProcessWeightKg());
            }
            if ("0".equals(record.getProcessStatus()))
            {
                pendingCount++;
            }
            else if ("1".equals(record.getProcessStatus()))
            {
                processingCount++;
            }
            else
            {
                finishedCount++;
            }
        }

        for (AgriProcessBatchLink record : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("linkId", record.getLinkId());
            row.put("plantingBatchNo", record.getPlantingBatchNo());
            row.put("processBatchNo", record.getProcessBatchNo());
            row.put("productCode", record.getProductCode());
            row.put("processWeightKg", record.getProcessWeightKg());
            row.put("processStatus", record.getProcessStatus());
            row.put("processTime", record.getProcessTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", records.size());
        summary.put("plantingBatchCount", plantingBatchNos.size());
        summary.put("processBatchCount", processBatchNos.size());
        summary.put("pendingCount", pendingCount);
        summary.put("processingCount", processingCount);
        summary.put("finishedCount", finishedCount);
        summary.put("totalWeight", totalWeight);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildCheck(AgriProcessBatchLink record)
    {
        int riskScore = 100;
        List<String> suggestions = new ArrayList<>();
        if (record.getProductCode() == null || record.getProductCode().trim().isEmpty())
        {
            suggestions.add("产品编码缺失，建议补齐追溯载体标识");
            riskScore -= 20;
        }
        if (record.getProcessWeightKg() == null || record.getProcessWeightKg().compareTo(BigDecimal.ZERO) <= 0)
        {
            suggestions.add("加工重量异常，建议复核称重和出入库记录");
            riskScore -= 25;
        }

        String statusText;
        if ("0".equals(record.getProcessStatus()))
        {
            statusText = "待加工";
            suggestions.add("当前批次尚未进入加工环节，建议安排排产");
            riskScore -= 10;
        }
        else if ("1".equals(record.getProcessStatus()))
        {
            statusText = "加工中";
            suggestions.add("批次正在加工，建议关注工艺节点与出料损耗");
            riskScore -= 5;
        }
        else
        {
            statusText = "已完成";
        }

        if (record.getProcessBatchNo() == null || record.getProcessBatchNo().trim().isEmpty())
        {
            suggestions.add("加工批次号缺失，建议同步生产工单编码");
            riskScore -= 15;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("linkId", record.getLinkId());
        result.put("statusText", statusText);
        result.put("riskScore", Math.max(0, riskScore));
        result.put("riskLevel", riskScore >= 85 ? "低" : riskScore >= 70 ? "中" : "高");
        result.put("suggestions", suggestions);
        result.put("record", record);
        return result;
    }
}
