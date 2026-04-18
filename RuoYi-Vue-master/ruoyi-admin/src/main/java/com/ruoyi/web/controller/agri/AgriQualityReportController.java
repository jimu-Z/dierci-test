package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.domain.AgriQualityReport;
import com.ruoyi.system.service.IAgriQualityInspectTaskService;
import com.ruoyi.system.service.IAgriQualityReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
 * 质检报告Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/qualityReport")
public class AgriQualityReportController extends BaseController
{
    @Autowired
    private IAgriQualityReportService agriQualityReportService;

    @Autowired
    private IAgriQualityInspectTaskService agriQualityInspectTaskService;

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriQualityReport agriQualityReport)
    {
        startPage();
        List<AgriQualityReport> list = agriQualityReportService.selectAgriQualityReportList(agriQualityReport);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriQualityReport agriQualityReport)
    {
        return success(buildDashboard(agriQualityReport));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:query')")
    @GetMapping("/smart/review/{reportId}")
    public AjaxResult review(@PathVariable("reportId") Long reportId)
    {
        AgriQualityReport report = agriQualityReportService.selectAgriQualityReportByReportId(reportId);
        if (report == null)
        {
            return error("质检报告不存在");
        }
        return success(buildReview(report));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:export')")
    @Log(title = "质检报告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriQualityReport agriQualityReport)
    {
        List<AgriQualityReport> list = agriQualityReportService.selectAgriQualityReportList(agriQualityReport);
        ExcelUtil<AgriQualityReport> util = new ExcelUtil<>(AgriQualityReport.class);
        util.exportExcel(response, list, "质检报告数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return success(agriQualityReportService.selectAgriQualityReportByReportId(reportId));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:add')")
    @Log(title = "质检报告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriQualityReport agriQualityReport)
    {
        agriQualityReport.setCreateBy(getUsername());
        return toAjax(agriQualityReportService.insertAgriQualityReport(agriQualityReport));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:edit')")
    @Log(title = "质检报告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriQualityReport agriQualityReport)
    {
        agriQualityReport.setUpdateBy(getUsername());
        return toAjax(agriQualityReportService.updateAgriQualityReport(agriQualityReport));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:remove')")
    @Log(title = "质检报告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(agriQualityReportService.deleteAgriQualityReportByReportIds(reportIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityReport:add')")
    @Log(title = "质检报告生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate/{inspectId}")
    public AjaxResult generate(@PathVariable("inspectId") Long inspectId)
    {
        AgriQualityInspectTask task = agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(inspectId);
        if (task == null)
        {
            return error("检测任务不存在");
        }

        AgriQualityReport exists = agriQualityReportService.selectAgriQualityReportByInspectId(inspectId);
        if (exists != null)
        {
            return error("该检测任务已生成报告");
        }

        AgriQualityReport report = new AgriQualityReport();
        report.setReportNo("QCR-" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + "-" + inspectId);
        report.setInspectId(inspectId);
        report.setProcessBatchNo(task.getProcessBatchNo());
        report.setQualityGrade(task.getQualityGrade());
        report.setDefectRate(task.getDefectRate() == null ? BigDecimal.ZERO : task.getDefectRate());
        report.setReportSummary("样品" + task.getSampleCode() + "品质等级为" + (task.getQualityGrade() == null ? "未知" : task.getQualityGrade())
            + "，缺陷率" + (task.getDefectRate() == null ? BigDecimal.ZERO : task.getDefectRate()) + "。"
            + (task.getInspectResult() == null ? "" : task.getInspectResult()));
        report.setReportStatus("1");
        report.setReportTime(DateUtils.getNowDate());
        report.setStatus("0");
        report.setCreateBy(getUsername());
        return toAjax(agriQualityReportService.insertAgriQualityReport(report));
    }

    private Map<String, Object> buildDashboard(AgriQualityReport query)
    {
        List<AgriQualityReport> reports = agriQualityReportService.selectAgriQualityReportList(query);
        Set<String> batchNos = new HashSet<>();
        int generatedCount = 0;
        int draftCount = 0;
        BigDecimal defectTotal = BigDecimal.ZERO;
        int defectCount = 0;
        List<AgriQualityReport> sorted = new ArrayList<>(reports);
        sorted.sort(Comparator.comparing(AgriQualityReport::getReportTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> highRiskRows = new ArrayList<>();
        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriQualityReport report : reports)
        {
            if (report == null)
            {
                continue;
            }
            if (report.getProcessBatchNo() != null)
            {
                batchNos.add(report.getProcessBatchNo());
            }
            if ("1".equals(report.getReportStatus()))
            {
                generatedCount++;
            }
            else
            {
                draftCount++;
            }
            if (report.getDefectRate() != null)
            {
                defectTotal = defectTotal.add(report.getDefectRate());
                defectCount++;
                if (report.getDefectRate().compareTo(new BigDecimal("0.05")) >= 0 && highRiskRows.size() < 5)
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("reportId", report.getReportId());
                    row.put("reportNo", report.getReportNo());
                    row.put("processBatchNo", report.getProcessBatchNo());
                    row.put("qualityGrade", report.getQualityGrade());
                    row.put("defectRate", report.getDefectRate());
                    row.put("reportStatus", report.getReportStatus());
                    highRiskRows.add(row);
                }
            }
        }

        for (AgriQualityReport report : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("reportId", report.getReportId());
            row.put("reportNo", report.getReportNo());
            row.put("processBatchNo", report.getProcessBatchNo());
            row.put("qualityGrade", report.getQualityGrade());
            row.put("defectRate", report.getDefectRate());
            row.put("reportStatus", report.getReportStatus());
            row.put("reportTime", report.getReportTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", reports.size());
        summary.put("batchCount", batchNos.size());
        summary.put("generatedCount", generatedCount);
        summary.put("draftCount", draftCount);
        summary.put("avgDefectRate", defectCount == 0 ? BigDecimal.ZERO : defectTotal.divide(BigDecimal.valueOf(defectCount), 4, BigDecimal.ROUND_HALF_UP));
        summary.put("highRiskCount", highRiskRows.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("highRiskRows", highRiskRows);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildReview(AgriQualityReport report)
    {
        int riskScore = 100;
        List<String> findings = new ArrayList<>();
        if (report.getDefectRate() != null && report.getDefectRate().compareTo(new BigDecimal("0.05")) >= 0)
        {
            findings.add("缺陷率偏高，建议重点核查原料、工艺和抽检样本");
            riskScore -= 30;
        }
        if (report.getQualityGrade() == null || report.getQualityGrade().trim().isEmpty())
        {
            findings.add("品质等级未填写，建议补齐分级结果");
            riskScore -= 15;
        }
        if ("0".equals(report.getReportStatus()))
        {
            findings.add("报告仍处草稿状态，建议尽快确认并归档");
            riskScore -= 10;
        }
        if (report.getReportSummary() == null || report.getReportSummary().trim().isEmpty())
        {
            findings.add("报告摘要为空，建议补充问题描述和复核意见");
            riskScore -= 15;
        }
        if (findings.isEmpty())
        {
            findings.add("报告整体正常，可直接进入归档或对外发布");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportId", report.getReportId());
        result.put("riskScore", Math.max(0, riskScore));
        result.put("riskLevel", riskScore >= 85 ? "低" : riskScore >= 70 ? "中" : "高");
        result.put("findings", findings);
        result.put("report", report);
        return result;
    }
}
