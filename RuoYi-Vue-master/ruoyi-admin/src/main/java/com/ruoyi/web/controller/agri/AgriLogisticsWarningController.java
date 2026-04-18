package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import com.ruoyi.system.domain.AgriLogisticsWarning;
import com.ruoyi.system.service.IAgriLogisticsTempHumidityService;
import com.ruoyi.system.service.IAgriLogisticsWarningService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
 * 在途异常预警Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsWarning")
public class AgriLogisticsWarningController extends BaseController
{
    @Autowired
    private IAgriLogisticsWarningService agriLogisticsWarningService;

    @Autowired
    private IAgriLogisticsTempHumidityService agriLogisticsTempHumidityService;

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsWarning agriLogisticsWarning)
    {
        startPage();
        List<AgriLogisticsWarning> list = agriLogisticsWarningService.selectAgriLogisticsWarningList(agriLogisticsWarning);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriLogisticsWarning agriLogisticsWarning)
    {
        return success(buildDashboard(agriLogisticsWarning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:query')")
    @GetMapping("/smart/triage/{warningId}")
    public AjaxResult triage(@PathVariable("warningId") Long warningId)
    {
        AgriLogisticsWarning warning = agriLogisticsWarningService.selectAgriLogisticsWarningByWarningId(warningId);
        if (warning == null)
        {
            return error("预警记录不存在");
        }
        return success(buildTriage(warning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:export')")
    @Log(title = "在途异常预警", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsWarning agriLogisticsWarning)
    {
        List<AgriLogisticsWarning> list = agriLogisticsWarningService.selectAgriLogisticsWarningList(agriLogisticsWarning);
        ExcelUtil<AgriLogisticsWarning> util = new ExcelUtil<>(AgriLogisticsWarning.class);
        util.exportExcel(response, list, "在途异常预警数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:query')")
    @GetMapping(value = "/{warningId}")
    public AjaxResult getInfo(@PathVariable("warningId") Long warningId)
    {
        return success(agriLogisticsWarningService.selectAgriLogisticsWarningByWarningId(warningId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:add')")
    @Log(title = "在途异常预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsWarning agriLogisticsWarning)
    {
        agriLogisticsWarning.setCreateBy(getUsername());
        return toAjax(agriLogisticsWarningService.insertAgriLogisticsWarning(agriLogisticsWarning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:add')")
    @Log(title = "在途异常预警生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate/{recordId}")
    public AjaxResult generateByTempRecord(@PathVariable("recordId") Long recordId)
    {
        AgriLogisticsTempHumidity record = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityByRecordId(recordId);
        if (record == null)
        {
            return error("温湿度记录不存在");
        }
        if (!"1".equals(record.getAlertFlag()))
        {
            return error("该温湿度记录未触发告警");
        }
        AgriLogisticsWarning exists = agriLogisticsWarningService.selectAgriLogisticsWarningBySourceRecordId(recordId);
        if (exists != null)
        {
            return error("该温湿度记录已生成预警");
        }

        AgriLogisticsWarning warning = new AgriLogisticsWarning();
        warning.setTraceCode(record.getTraceCode());
        warning.setOrderNo(record.getOrderNo());
        warning.setWarningType("TEMP_HUMIDITY");
        warning.setWarningLevel("2");
        warning.setWarningStatus("0");
        warning.setSourceRecordId(recordId);
        warning.setWarningTitle("温湿度在途异常");
        warning.setWarningContent(record.getAlertMessage() == null ? "温湿度超阈值" : record.getAlertMessage());
        warning.setWarningTime(record.getCollectTime() == null ? DateUtils.getNowDate() : record.getCollectTime());
        warning.setStatus("0");
        warning.setCreateBy(getUsername());
        return toAjax(agriLogisticsWarningService.insertAgriLogisticsWarning(warning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:edit')")
    @Log(title = "在途异常预警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsWarning agriLogisticsWarning)
    {
        agriLogisticsWarning.setUpdateBy(getUsername());
        return toAjax(agriLogisticsWarningService.updateAgriLogisticsWarning(agriLogisticsWarning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:remove')")
    @Log(title = "在途异常预警", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warningIds}")
    public AjaxResult remove(@PathVariable Long[] warningIds)
    {
        return toAjax(agriLogisticsWarningService.deleteAgriLogisticsWarningByWarningIds(warningIds));
    }

    private Map<String, Object> buildDashboard(AgriLogisticsWarning query)
    {
        List<AgriLogisticsWarning> warnings = agriLogisticsWarningService.selectAgriLogisticsWarningList(query);
        int pendingCount = 0;
        int processingCount = 0;
        int closedCount = 0;
        int urgentCount = 0;
        List<AgriLogisticsWarning> sorted = new ArrayList<>(warnings);
        sorted.sort(Comparator.comparing(AgriLogisticsWarning::getWarningTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriLogisticsWarning warning : warnings)
        {
            if (warning == null)
            {
                continue;
            }
            if ("0".equals(warning.getWarningStatus()))
            {
                pendingCount++;
            }
            else if ("1".equals(warning.getWarningStatus()))
            {
                processingCount++;
            }
            else
            {
                closedCount++;
            }
            if ("3".equals(warning.getWarningLevel()))
            {
                urgentCount++;
            }
        }

        for (AgriLogisticsWarning warning : sorted)
        {
            if (recentRows.size() >= 8)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("warningId", warning.getWarningId());
            row.put("traceCode", warning.getTraceCode());
            row.put("warningType", warning.getWarningType());
            row.put("warningLevel", warning.getWarningLevel());
            row.put("warningStatus", warning.getWarningStatus());
            row.put("warningTitle", warning.getWarningTitle());
            row.put("warningContent", warning.getWarningContent());
            row.put("warningTime", warning.getWarningTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", warnings.size());
        summary.put("pendingCount", pendingCount);
        summary.put("processingCount", processingCount);
        summary.put("closedCount", closedCount);
        summary.put("urgentCount", urgentCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildTriage(AgriLogisticsWarning warning)
    {
        int score = 100;
        List<String> suggestions = new ArrayList<>();
        if ("3".equals(warning.getWarningLevel()))
        {
            suggestions.add("紧急预警，建议立即电话通知承运人与调度人员");
            score -= 35;
        }
        else if ("2".equals(warning.getWarningLevel()))
        {
            suggestions.add("严重预警，建议优先核查运单轨迹和温湿度记录");
            score -= 20;
        }
        if ("0".equals(warning.getWarningStatus()))
        {
            suggestions.add("当前仍待处理，建议尽快分派处理人并设置时限");
            score -= 15;
        }
        if (warning.getWarningContent() != null && warning.getWarningContent().contains("温湿度"))
        {
            suggestions.add("该预警源自温湿度异常，建议联动查看对应监控记录");
            score -= 10;
        }
        if (warning.getHandler() == null || warning.getHandler().trim().isEmpty())
        {
            suggestions.add("尚未指定处理人，建议补齐责任人分配");
            score -= 10;
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前预警已进入稳定处置阶段，可继续保持常规跟踪");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("warningId", warning.getWarningId());
        result.put("riskScore", Math.max(0, score));
        result.put("riskLevel", score >= 85 ? "低" : score >= 70 ? "中" : "高");
        result.put("suggestions", suggestions);
        result.put("warning", warning);
        return result;
    }
}
