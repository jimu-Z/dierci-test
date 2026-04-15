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
import java.util.List;
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
}
