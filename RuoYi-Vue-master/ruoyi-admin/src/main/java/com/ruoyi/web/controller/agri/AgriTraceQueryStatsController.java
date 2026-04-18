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

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriTraceQueryStats agriTraceQueryStats)
    {
        return success(buildDashboard(agriTraceQueryStats));
    }

    @PreAuthorize("@ss.hasPermi('agri:traceQueryStats:query')")
    @GetMapping("/smart/insight/{statsId}")
    public AjaxResult insight(@PathVariable("statsId") Long statsId)
    {
        AgriTraceQueryStats stats = agriTraceQueryStatsService.selectAgriTraceQueryStatsByStatsId(statsId);
        if (stats == null)
        {
            return error("溯源查询统计不存在");
        }
        return success(buildInsight(stats));
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

    private Map<String, Object> buildDashboard(AgriTraceQueryStats query)
    {
        List<AgriTraceQueryStats> records = agriTraceQueryStatsService.selectAgriTraceQueryStatsList(query);
        long totalQueryCount = 0L;
        long uniqueUserCount = 0L;
        long successCount = 0L;
        long failCount = 0L;
        double peakQps = 0D;
        double durationSum = 0D;
        List<AgriTraceQueryStats> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AgriTraceQueryStats::getStatDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriTraceQueryStats stats : records)
        {
            if (stats == null)
            {
                continue;
            }
            totalQueryCount += safeLong(stats.getTotalQueryCount());
            uniqueUserCount += safeLong(stats.getUniqueUserCount());
            successCount += safeLong(stats.getSuccessCount());
            failCount += safeLong(stats.getFailCount());
            peakQps = Math.max(peakQps, safeDouble(stats.getPeakQps()));
            durationSum += safeDouble(stats.getAvgDurationMs());
        }

        for (AgriTraceQueryStats stats : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("statsId", stats.getStatsId());
            row.put("statDate", stats.getStatDate());
            row.put("totalQueryCount", stats.getTotalQueryCount());
            row.put("uniqueUserCount", stats.getUniqueUserCount());
            row.put("avgDurationMs", stats.getAvgDurationMs());
            row.put("successCount", stats.getSuccessCount());
            row.put("failCount", stats.getFailCount());
            row.put("peakQps", stats.getPeakQps());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalQueryCount", totalQueryCount);
        summary.put("uniqueUserCount", uniqueUserCount);
        summary.put("successCount", successCount);
        summary.put("failCount", failCount);
        summary.put("peakQps", peakQps);
        summary.put("avgDurationMs", records.isEmpty() ? 0 : Math.round(durationSum / records.size()));
        summary.put("successRate", totalQueryCount == 0 ? 0 : Math.round((successCount * 10000D) / totalQueryCount) / 100D);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildInsight(AgriTraceQueryStats stats)
    {
        int riskScore = 100;
        List<String> findings = new ArrayList<>();
        double avgDuration = safeDouble(stats.getAvgDurationMs());
        long failCount = safeLong(stats.getFailCount());
        long successCount = safeLong(stats.getSuccessCount());
        double peakQps = safeDouble(stats.getPeakQps());

        if (avgDuration > 500)
        {
            findings.add("平均响应时间偏高，建议检查查询链路、索引和缓存命中");
            riskScore -= 25;
        }
        if (failCount > successCount)
        {
            findings.add("失败次数高于成功次数，建议排查接口稳定性和上游参数质量");
            riskScore -= 30;
        }
        if (peakQps > 200)
        {
            findings.add("峰值QPS较高，建议错峰发布或增加限流与缓存策略");
            riskScore -= 10;
        }
        if (safeLong(stats.getUniqueUserCount()) > 0 && safeLong(stats.getTotalQueryCount()) / Math.max(1L, safeLong(stats.getUniqueUserCount())) > 20)
        {
            findings.add("单用户平均查询量较高，建议优化常用查询入口和默认条件");
            riskScore -= 10;
        }
        if (findings.isEmpty())
        {
            findings.add("当前查询统计较平稳，可继续保持现有发布策略");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statsId", stats.getStatsId());
        result.put("riskScore", Math.max(0, riskScore));
        result.put("riskLevel", riskScore >= 85 ? "低" : riskScore >= 70 ? "中" : "高");
        result.put("findings", findings);
        result.put("stats", stats);
        return result;
    }

    private long safeLong(Number value)
    {
        return value == null ? 0L : value.longValue();
    }

    private double safeDouble(Object value)
    {
        if (value == null)
        {
            return 0D;
        }
        if (value instanceof Number number)
        {
            return number.doubleValue();
        }
        try
        {
            return Double.parseDouble(String.valueOf(value));
        }
        catch (Exception ignored)
        {
            return 0D;
        }
    }
}
