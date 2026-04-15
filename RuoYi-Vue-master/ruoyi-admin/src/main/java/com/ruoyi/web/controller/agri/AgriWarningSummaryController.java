package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriWarningSummary;
import com.ruoyi.system.service.IAgriWarningSummaryService;
import jakarta.servlet.http.HttpServletResponse;
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
 * 预警信息汇总Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/warningSummary")
public class AgriWarningSummaryController extends BaseController
{
    @Autowired
    private IAgriWarningSummaryService agriWarningSummaryService;

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriWarningSummary agriWarningSummary)
    {
        startPage();
        return getDataTable(agriWarningSummaryService.selectAgriWarningSummaryList(agriWarningSummary));
    }

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:export')")
    @Log(title = "预警信息汇总", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriWarningSummary agriWarningSummary)
    {
        ExcelUtil<AgriWarningSummary> util = new ExcelUtil<>(AgriWarningSummary.class);
        util.exportExcel(response, agriWarningSummaryService.selectAgriWarningSummaryList(agriWarningSummary), "预警信息汇总数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:query')")
    @GetMapping(value = "/{summaryId}")
    public AjaxResult getInfo(@PathVariable("summaryId") Long summaryId)
    {
        return success(agriWarningSummaryService.selectAgriWarningSummaryBySummaryId(summaryId));
    }

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:add')")
    @Log(title = "预警信息汇总", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriWarningSummary agriWarningSummary)
    {
        return toAjax(agriWarningSummaryService.insertAgriWarningSummary(agriWarningSummary));
    }

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:edit')")
    @Log(title = "预警信息汇总", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriWarningSummary agriWarningSummary)
    {
        return toAjax(agriWarningSummaryService.updateAgriWarningSummary(agriWarningSummary));
    }

    @PreAuthorize("@ss.hasPermi('agri:warningSummary:remove')")
    @Log(title = "预警信息汇总", businessType = BusinessType.DELETE)
    @DeleteMapping("/{summaryIds}")
    public AjaxResult remove(@PathVariable Long[] summaryIds)
    {
        return toAjax(agriWarningSummaryService.deleteAgriWarningSummaryBySummaryIds(summaryIds));
    }
}
