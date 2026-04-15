package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDashboardOverview;
import com.ruoyi.system.service.IAgriDashboardOverviewService;
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
 * 数据总览看板Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/dashboardOverview")
public class AgriDashboardOverviewController extends BaseController
{
    @Autowired
    private IAgriDashboardOverviewService agriDashboardOverviewService;

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDashboardOverview agriDashboardOverview)
    {
        startPage();
        return getDataTable(agriDashboardOverviewService.selectAgriDashboardOverviewList(agriDashboardOverview));
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:export')")
    @Log(title = "数据总览看板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDashboardOverview agriDashboardOverview)
    {
        ExcelUtil<AgriDashboardOverview> util = new ExcelUtil<>(AgriDashboardOverview.class);
        util.exportExcel(response, agriDashboardOverviewService.selectAgriDashboardOverviewList(agriDashboardOverview), "数据总览看板数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:query')")
    @GetMapping(value = "/{overviewId}")
    public AjaxResult getInfo(@PathVariable("overviewId") Long overviewId)
    {
        return success(agriDashboardOverviewService.selectAgriDashboardOverviewByOverviewId(overviewId));
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:add')")
    @Log(title = "数据总览看板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriDashboardOverview agriDashboardOverview)
    {
        return toAjax(agriDashboardOverviewService.insertAgriDashboardOverview(agriDashboardOverview));
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:edit')")
    @Log(title = "数据总览看板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriDashboardOverview agriDashboardOverview)
    {
        return toAjax(agriDashboardOverviewService.updateAgriDashboardOverview(agriDashboardOverview));
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:remove')")
    @Log(title = "数据总览看板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{overviewIds}")
    public AjaxResult remove(@PathVariable Long[] overviewIds)
    {
        return toAjax(agriDashboardOverviewService.deleteAgriDashboardOverviewByOverviewIds(overviewIds));
    }
}
