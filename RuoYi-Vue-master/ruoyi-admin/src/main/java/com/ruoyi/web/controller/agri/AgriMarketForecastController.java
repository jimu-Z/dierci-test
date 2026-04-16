package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriMarketForecast;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriMarketForecastService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 市场预测分析Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/marketForecast")
public class AgriMarketForecastController extends BaseController
{
    @Autowired
    private IAgriMarketForecastService agriMarketForecastService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriMarketForecast agriMarketForecast)
    {
        startPage();
        List<AgriMarketForecast> list = agriMarketForecastService.selectAgriMarketForecastList(agriMarketForecast);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:export')")
    @Log(title = "市场预测分析", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriMarketForecast agriMarketForecast)
    {
        List<AgriMarketForecast> list = agriMarketForecastService.selectAgriMarketForecastList(agriMarketForecast);
        ExcelUtil<AgriMarketForecast> util = new ExcelUtil<>(AgriMarketForecast.class);
        util.exportExcel(response, list, "市场预测分析数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:query')")
    @GetMapping(value = "/{forecastId}")
    public AjaxResult getInfo(@PathVariable("forecastId") Long forecastId)
    {
        return success(agriMarketForecastService.selectAgriMarketForecastByForecastId(forecastId));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:add')")
    @Log(title = "市场预测分析", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriMarketForecast agriMarketForecast)
    {
        agriMarketForecast.setCreateBy(getUsername());
        if (agriMarketForecast.getForecastStatus() == null || agriMarketForecast.getForecastStatus().isEmpty())
        {
            agriMarketForecast.setForecastStatus("0");
        }
        return toAjax(agriMarketForecastService.insertAgriMarketForecast(agriMarketForecast));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:edit')")
    @Log(title = "市场预测分析", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriMarketForecast agriMarketForecast)
    {
        agriMarketForecast.setUpdateBy(getUsername());
        return toAjax(agriMarketForecastService.updateAgriMarketForecast(agriMarketForecast));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:remove')")
    @Log(title = "市场预测分析", businessType = BusinessType.DELETE)
    @DeleteMapping("/{forecastIds}")
    public AjaxResult remove(@PathVariable Long[] forecastIds)
    {
        return toAjax(agriMarketForecastService.deleteAgriMarketForecastByForecastIds(forecastIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:edit')")
    @Log(title = "市场预测回写", businessType = BusinessType.UPDATE)
    @PutMapping("/predict")
    public AjaxResult predict(@RequestBody AgriMarketForecast agriMarketForecast)
    {
        AgriMarketForecast db = agriMarketForecastService.selectAgriMarketForecastByForecastId(agriMarketForecast.getForecastId());
        if (db == null)
        {
            return error("任务不存在");
        }

        BigDecimal forecastSales = agriMarketForecast.getForecastSalesKg();
        if (forecastSales == null)
        {
            BigDecimal history = db.getHistoricalSalesKg() == null ? new BigDecimal("1000") : db.getHistoricalSalesKg();
            forecastSales = history.multiply(new BigDecimal("1.08")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal forecastPrice = agriMarketForecast.getForecastPrice();
        if (forecastPrice == null)
        {
            BigDecimal base = db.getForecastPrice() == null ? new BigDecimal("8.50") : db.getForecastPrice();
            forecastPrice = base.setScale(2, RoundingMode.HALF_UP);
        }

        db.setForecastSalesKg(forecastSales);
        db.setForecastPrice(forecastPrice);
        db.setConfidenceRate(agriMarketForecast.getConfidenceRate() == null ? new BigDecimal("0.82") : agriMarketForecast.getConfidenceRate());
        db.setModelVersion(agriMarketForecast.getModelVersion());
        db.setForecastStatus("1");
        db.setForecastTime(DateUtils.getNowDate());
        db.setUpdateBy(getUsername());
        return toAjax(agriMarketForecastService.updateAgriMarketForecast(db));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:edit')")
    @Log(title = "市场预测AI调用", businessType = BusinessType.UPDATE)
    @PostMapping("/invoke/{forecastId}")
    public AjaxResult invoke(@PathVariable("forecastId") Long forecastId)
    {
        AgriMarketForecast db = agriMarketForecastService.selectAgriMarketForecastByForecastId(forecastId);
        if (db == null)
        {
            return error("任务不存在");
        }

        try
        {
            AgriHttpIntegrationClient.MarketForecastResult result = agriHttpIntegrationClient.invokeMarketForecast(db);
            db.setForecastSalesKg(result.getForecastSalesKg());
            db.setForecastPrice(result.getForecastPrice());
            db.setConfidenceRate(result.getConfidenceRate() == null ? new BigDecimal("0.82") : result.getConfidenceRate());
            db.setModelVersion(result.getModelVersion());
            db.setForecastStatus("1");
            db.setForecastTime(DateUtils.getNowDate());
            db.setRemark(result.getForecastSummary());
            db.setUpdateBy(getUsername());
            agriMarketForecastService.updateAgriMarketForecast(db);
            return success("市场预测调用成功");
        }
        catch (Exception ex)
        {
            db.setForecastStatus("2");
            db.setRemark("调用失败: " + ex.getMessage());
            db.setForecastTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriMarketForecastService.updateAgriMarketForecast(db);
            return error("市场预测调用失败: " + ex.getMessage());
        }
    }
}
