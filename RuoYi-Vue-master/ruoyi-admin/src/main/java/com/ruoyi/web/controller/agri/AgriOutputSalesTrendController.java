package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriOutputSalesTrend;
import com.ruoyi.system.service.IAgriOutputSalesTrendService;
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
 * 产量与销量趋势图Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/outputSalesTrend")
public class AgriOutputSalesTrendController extends BaseController
{
    @Autowired
    private IAgriOutputSalesTrendService agriOutputSalesTrendService;

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriOutputSalesTrend agriOutputSalesTrend)
    {
        startPage();
        return getDataTable(agriOutputSalesTrendService.selectAgriOutputSalesTrendList(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:export')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriOutputSalesTrend agriOutputSalesTrend)
    {
        ExcelUtil<AgriOutputSalesTrend> util = new ExcelUtil<>(AgriOutputSalesTrend.class);
        util.exportExcel(response, agriOutputSalesTrendService.selectAgriOutputSalesTrendList(agriOutputSalesTrend), "产量与销量趋势图数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:query')")
    @GetMapping(value = "/{trendId}")
    public AjaxResult getInfo(@PathVariable("trendId") Long trendId)
    {
        return success(agriOutputSalesTrendService.selectAgriOutputSalesTrendByTrendId(trendId));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:add')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return toAjax(agriOutputSalesTrendService.insertAgriOutputSalesTrend(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:edit')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return toAjax(agriOutputSalesTrendService.updateAgriOutputSalesTrend(agriOutputSalesTrend));
    }

    @PreAuthorize("@ss.hasPermi('agri:outputSalesTrend:remove')")
    @Log(title = "产量与销量趋势图", businessType = BusinessType.DELETE)
    @DeleteMapping("/{trendIds}")
    public AjaxResult remove(@PathVariable Long[] trendIds)
    {
        return toAjax(agriOutputSalesTrendService.deleteAgriOutputSalesTrendByTrendIds(trendIds));
    }
}
