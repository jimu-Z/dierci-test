package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriFinanceRiskMetric;
import com.ruoyi.system.service.IAgriFinanceRiskMetricService;
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
 * 农业金融风控指标Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/financeRisk")
public class AgriFinanceRiskMetricController extends BaseController
{
    @Autowired
    private IAgriFinanceRiskMetricService agriFinanceRiskMetricService;

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        startPage();
        return getDataTable(agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:export')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        ExcelUtil<AgriFinanceRiskMetric> util = new ExcelUtil<>(AgriFinanceRiskMetric.class);
        util.exportExcel(response, agriFinanceRiskMetricService.selectAgriFinanceRiskMetricList(agriFinanceRiskMetric), "农业金融风控指标数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:query')")
    @GetMapping(value = "/{riskId}")
    public AjaxResult getInfo(@PathVariable("riskId") Long riskId)
    {
        return success(agriFinanceRiskMetricService.selectAgriFinanceRiskMetricByRiskId(riskId));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:add')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return toAjax(agriFinanceRiskMetricService.insertAgriFinanceRiskMetric(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:edit')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return toAjax(agriFinanceRiskMetricService.updateAgriFinanceRiskMetric(agriFinanceRiskMetric));
    }

    @PreAuthorize("@ss.hasPermi('agri:financeRisk:remove')")
    @Log(title = "农业金融风控指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{riskIds}")
    public AjaxResult remove(@PathVariable Long[] riskIds)
    {
        return toAjax(agriFinanceRiskMetricService.deleteAgriFinanceRiskMetricByRiskIds(riskIds));
    }
}
