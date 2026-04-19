package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriMarketForecast;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriMarketForecastService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriMarketForecast agriMarketForecast)
    {
        return success(buildDashboard(agriMarketForecast));
    }

    @PreAuthorize("@ss.hasPermi('agri:marketForecast:query')")
    @GetMapping("/smart/review/{forecastId}")
    public AjaxResult review(@PathVariable("forecastId") Long forecastId)
    {
        AgriMarketForecast forecast = agriMarketForecastService.selectAgriMarketForecastByForecastId(forecastId);
        if (forecast == null)
        {
            return error("预测任务不存在");
        }
        return success(buildReview(forecast));
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

    private Map<String, Object> buildDashboard(AgriMarketForecast query)
    {
        List<AgriMarketForecast> forecasts = agriMarketForecastService.selectAgriMarketForecastList(query);
        int predictedCount = 0;
        int pendingCount = 0;
        int failedCount = 0;
        BigDecimal salesSum = BigDecimal.ZERO;
        BigDecimal priceSum = BigDecimal.ZERO;
        int salesCount = 0;
        int priceCount = 0;
        List<AgriMarketForecast> sorted = new ArrayList<>(forecasts);
        sorted.sort(Comparator.comparing(AgriMarketForecast::getForecastTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        List<Map<String, Object>> hotRows = new ArrayList<>();
        for (AgriMarketForecast forecast : forecasts)
        {
            if (forecast == null)
            {
                continue;
            }
            if ("1".equals(forecast.getForecastStatus()))
            {
                predictedCount++;
            }
            else if ("2".equals(forecast.getForecastStatus()))
            {
                failedCount++;
            }
            else
            {
                pendingCount++;
            }
            if (forecast.getForecastSalesKg() != null)
            {
                salesSum = salesSum.add(forecast.getForecastSalesKg());
                salesCount++;
            }
            if (forecast.getForecastPrice() != null)
            {
                priceSum = priceSum.add(forecast.getForecastPrice());
                priceCount++;
            }
            if (forecast.getConfidenceRate() != null && forecast.getConfidenceRate().compareTo(new BigDecimal("0.75")) < 0 && hotRows.size() < 5)
            {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("forecastId", forecast.getForecastId());
                row.put("marketArea", forecast.getMarketArea());
                row.put("productCode", forecast.getProductCode());
                row.put("productName", forecast.getProductName());
                row.put("confidenceRate", forecast.getConfidenceRate());
                row.put("forecastStatus", forecast.getForecastStatus());
                hotRows.add(row);
            }
        }

        for (AgriMarketForecast forecast : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("forecastId", forecast.getForecastId());
            row.put("marketArea", forecast.getMarketArea());
            row.put("productCode", forecast.getProductCode());
            row.put("productName", forecast.getProductName());
            row.put("forecastSalesKg", forecast.getForecastSalesKg());
            row.put("forecastPrice", forecast.getForecastPrice());
            row.put("confidenceRate", forecast.getConfidenceRate());
            row.put("forecastStatus", forecast.getForecastStatus());
            row.put("forecastTime", forecast.getForecastTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", forecasts.size());
        summary.put("predictedCount", predictedCount);
        summary.put("pendingCount", pendingCount);
        summary.put("failedCount", failedCount);
        summary.put("avgSales", salesCount == 0 ? BigDecimal.ZERO : salesSum.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP));
        summary.put("avgPrice", priceCount == 0 ? BigDecimal.ZERO : priceSum.divide(BigDecimal.valueOf(priceCount), 2, RoundingMode.HALF_UP));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("hotRows", hotRows);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildReview(AgriMarketForecast forecast)
    {
        int score = 100;
        List<String> suggestions = new ArrayList<>();
        if (forecast.getConfidenceRate() != null && forecast.getConfidenceRate().compareTo(new BigDecimal("0.8")) < 0)
        {
            suggestions.add("置信度偏低，建议优先复核样本质量和特征输入");
            score -= 25;
        }
        if (forecast.getForecastStatus() == null || forecast.getForecastStatus().equals("0"))
        {
            suggestions.add("当前仍是待预测状态，建议先执行模型调用再输出业务建议");
            score -= 10;
        }
        if (forecast.getForecastSalesKg() != null && forecast.getHistoricalSalesKg() != null && forecast.getForecastSalesKg().compareTo(forecast.getHistoricalSalesKg().multiply(new BigDecimal("1.5"))) > 0)
        {
            suggestions.add("预测销量显著高于历史基线，建议结合节庆和渠道活动再复核一次");
            score -= 20;
        }
        if (forecast.getForecastPrice() != null && forecast.getForecastPrice().compareTo(new BigDecimal("0")) <= 0)
        {
            suggestions.add("预测价格异常，建议检查单位和模型回写值");
            score -= 20;
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前预测结果可直接用于销售排产或渠道沟通");
        }

        int safeScore = Math.max(0, score);
        String riskLevel = safeScore >= 85 ? "低" : safeScore >= 70 ? "中" : "高";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "市场预测智能复核");
            context.put("forecastId", forecast.getForecastId());
            context.put("marketArea", forecast.getMarketArea());
            context.put("productCode", forecast.getProductCode());
            context.put("productName", forecast.getProductName());
            context.put("forecastStatus", forecast.getForecastStatus());
            context.put("historicalSalesKg", forecast.getHistoricalSalesKg());
            context.put("forecastSalesKg", forecast.getForecastSalesKg());
            context.put("forecastPrice", forecast.getForecastPrice());
            context.put("confidenceRate", forecast.getConfidenceRate());
            context.put("modelVersion", forecast.getModelVersion());
            context.put("ruleRiskScore", safeScore);
            context.put("ruleRiskLevel", riskLevel);
            context.put("ruleSuggestions", suggestions);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("市场预测智能复核", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (StringUtils.isNotBlank(aiResult.getRiskLevel()))
            {
                riskLevel = aiResult.getRiskLevel();
            }
            if (StringUtils.isNotBlank(aiResult.getSuggestion()))
            {
                suggestions.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (StringUtils.isNotBlank(aiOriginalExcerpt))
            {
                suggestions.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forecastId", forecast.getForecastId());
        result.put("riskScore", safeScore);
        result.put("riskLevel", riskLevel);
        result.put("suggestions", suggestions);
        result.put("forecast", forecast);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return result;
    }
}
