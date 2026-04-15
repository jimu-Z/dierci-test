package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriTraceQueryStats;
import com.ruoyi.system.service.IAgriTraceQueryStatsService;
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
 * 溯源查询统计Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/traceQueryStats")
public class AgriTraceQueryStatsController extends BaseController
{
    @Autowired
    private IAgriTraceQueryStatsService agriTraceQueryStatsService;

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriTraceQueryStats agriTraceQueryStats)
    {
        startPage();
        return getDataTable(agriTraceQueryStatsService.selectAgriTraceQueryStatsList(agriTraceQueryStats));
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:export')")
    @Log(title = "溯源查询统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriTraceQueryStats agriTraceQueryStats)
    {
        ExcelUtil<AgriTraceQueryStats> util = new ExcelUtil<>(AgriTraceQueryStats.class);
        util.exportExcel(response, agriTraceQueryStatsService.selectAgriTraceQueryStatsList(agriTraceQueryStats), "溯源查询统计数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:query')")
    @GetMapping(value = "/{statsId}")
    public AjaxResult getInfo(@PathVariable("statsId") Long statsId)
    {
        return success(agriTraceQueryStatsService.selectAgriTraceQueryStatsByStatsId(statsId));
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:add')")
    @Log(title = "溯源查询统计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriTraceQueryStats agriTraceQueryStats)
    {
        return toAjax(agriTraceQueryStatsService.insertAgriTraceQueryStats(agriTraceQueryStats));
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:edit')")
    @Log(title = "溯源查询统计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriTraceQueryStats agriTraceQueryStats)
    {
        return toAjax(agriTraceQueryStatsService.updateAgriTraceQueryStats(agriTraceQueryStats));
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:remove')")
    @Log(title = "溯源查询统计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{statsIds}")
    public AjaxResult remove(@PathVariable Long[] statsIds)
    {
        return toAjax(agriTraceQueryStatsService.deleteAgriTraceQueryStatsByStatsIds(statsIds));
    }
}
