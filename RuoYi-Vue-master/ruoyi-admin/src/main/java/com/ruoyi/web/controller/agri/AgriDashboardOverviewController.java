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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriDashboardOverview> list = agriDashboardOverviewService.selectAgriDashboardOverviewList(new AgriDashboardOverview());
        int total = list.size();
        long traceQueryCount = 0L;
        long warningCount = 0L;
        long onlineDeviceCount = 0L;
        long pendingTaskCount = 0L;
        BigDecimal totalOutput = BigDecimal.ZERO;
        BigDecimal totalSales = BigDecimal.ZERO;

        for (AgriDashboardOverview item : list)
        {
            totalOutput = totalOutput.add(defaultZero(item.getTotalOutput()));
            totalSales = totalSales.add(defaultZero(item.getTotalSales()));
            traceQueryCount += defaultZero(item.getTraceQueryCount());
            warningCount += defaultZero(item.getWarningCount());
            onlineDeviceCount += defaultZero(item.getOnlineDeviceCount());
            pendingTaskCount += defaultZero(item.getPendingTaskCount());
        }

        BigDecimal outputToSalesRatio = totalOutput.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalSales.divide(totalOutput, 2, RoundingMode.HALF_UP);
        BigDecimal traceCoverage = totalOutput.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(traceQueryCount).divide(totalOutput, 2, RoundingMode.HALF_UP);

        List<AgriDashboardOverview> topList = list.stream()
            .sorted(Comparator.comparing(AgriDashboardOverview::getStatDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(6)
            .toList();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("totalOutput", totalOutput.setScale(2, RoundingMode.HALF_UP));
        kpi.put("totalSales", totalSales.setScale(2, RoundingMode.HALF_UP));
        kpi.put("traceQueryCount", traceQueryCount);
        kpi.put("warningCount", warningCount);
        kpi.put("onlineDeviceCount", onlineDeviceCount);
        kpi.put("pendingTaskCount", pendingTaskCount);
        kpi.put("outputToSalesRatio", outputToSalesRatio);
        kpi.put("traceCoverage", traceCoverage);

        List<String> recommendations = new ArrayList<>();
        if (pendingTaskCount > warningCount)
        {
            recommendations.add("待办积压高于预警量，建议先清理未闭环任务再扩展新增统计口径。");
        }
        if (traceCoverage.compareTo(new BigDecimal("3.00")) < 0)
        {
            recommendations.add("溯源查询密度偏低，建议补充消费者扫码和渠道追踪入口。");
        }
        if (onlineDeviceCount == 0)
        {
            recommendations.add("在线设备数为零，建议先核验设备接入与心跳上报链路。");
        }
        if (recommendations.isEmpty())
        {
            recommendations.add("总览指标处于可控区间，建议继续按日滚动刷新并关注周末波动。");
        }

        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("overviewSummary", "累计产销已形成稳定看板，当前输出/销量比为" + outputToSalesRatio + "，溯源覆盖率为" + traceCoverage + "。");
        insight.put("healthLevel", buildHealthLevel(warningCount, pendingTaskCount, traceCoverage));
        insight.put("confidenceRate", buildConfidence(total, warningCount, pendingTaskCount));
        insight.put("modelVersion", "overview-health-v1");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("topList", topList);
        result.put("insight", insight);
        result.put("recommendations", recommendations);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:dashboardOverview:query')")
    @GetMapping("/smart/insight/{overviewId}")
    public AjaxResult smartInsight(@PathVariable("overviewId") Long overviewId)
    {
        AgriDashboardOverview overview = agriDashboardOverviewService.selectAgriDashboardOverviewByOverviewId(overviewId);
        if (overview == null)
        {
            return error("看板记录不存在");
        }

        BigDecimal totalOutput = defaultZero(overview.getTotalOutput());
        BigDecimal totalSales = defaultZero(overview.getTotalSales());
        long traceQueryCount = defaultZero(overview.getTraceQueryCount());
        long warningCount = defaultZero(overview.getWarningCount());
        long onlineDeviceCount = defaultZero(overview.getOnlineDeviceCount());
        long pendingTaskCount = defaultZero(overview.getPendingTaskCount());

        BigDecimal outputToSalesRatio = totalOutput.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalSales.divide(totalOutput, 2, RoundingMode.HALF_UP);
        BigDecimal traceCoverage = totalOutput.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(traceQueryCount).divide(totalOutput, 2, RoundingMode.HALF_UP);

        List<String> suggestions = new ArrayList<>();
        if (warningCount > pendingTaskCount)
        {
            suggestions.add("预警量大于待办量，建议增加自动分派和分级处置规则。");
        }
        if (traceCoverage.compareTo(new BigDecimal("3.00")) < 0)
        {
            suggestions.add("溯源查询偏少，建议在销售与扫码入口中补充提示并强化追踪动作。");
        }
        if (onlineDeviceCount <= 0)
        {
            suggestions.add("在线设备为空，建议先恢复设备上报后再扩大总览统计范围。");
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前总览指标处于稳定区间，建议继续按日观测产销比和预警波动。");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overviewId", overview.getOverviewId());
        result.put("statDate", overview.getStatDate());
        result.put("algorithm", "overview-health-v1");
        result.put("outputToSalesRatio", outputToSalesRatio);
        result.put("traceCoverage", traceCoverage);
        result.put("healthLevel", buildHealthLevel(warningCount, pendingTaskCount, traceCoverage));
        result.put("confidenceRate", buildConfidence(totalOutput, warningCount, pendingTaskCount));
        result.put("recommendations", suggestions);
        result.put("summary", "基于产销、溯源、预警与待办四项指标生成总览健康诊断，当前健康等级为" + buildHealthLevel(warningCount, pendingTaskCount, traceCoverage) + "。");
        return success(result);
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

    private BigDecimal defaultZero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long defaultZero(Long value)
    {
        return value == null ? 0L : value;
    }

    private String buildHealthLevel(long warningCount, long pendingTaskCount, BigDecimal traceCoverage)
    {
        if (warningCount > pendingTaskCount || traceCoverage.compareTo(new BigDecimal("2.00")) < 0)
        {
            return "高风险";
        }
        if (warningCount > 0 || pendingTaskCount > 0)
        {
            return "关注";
        }
        return "稳定";
    }

    private BigDecimal buildConfidence(int total, long warningCount, long pendingTaskCount)
    {
        BigDecimal base = total == 0 ? new BigDecimal("0.58") : new BigDecimal("0.70");
        BigDecimal penalty = BigDecimal.valueOf(warningCount + pendingTaskCount).multiply(new BigDecimal("0.01"));
        return base.subtract(penalty).max(new BigDecimal("0.55")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal buildConfidence(BigDecimal totalOutput, long warningCount, long pendingTaskCount)
    {
        BigDecimal base = totalOutput.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("0.58") : new BigDecimal("0.70");
        BigDecimal penalty = BigDecimal.valueOf(warningCount + pendingTaskCount).multiply(new BigDecimal("0.01"));
        return base.subtract(penalty).max(new BigDecimal("0.55")).setScale(2, RoundingMode.HALF_UP);
    }
}
