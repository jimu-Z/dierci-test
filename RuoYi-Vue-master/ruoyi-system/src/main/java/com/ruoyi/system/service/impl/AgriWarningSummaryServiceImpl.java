package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriWarningSummary;
import com.ruoyi.system.mapper.AgriWarningSummaryMapper;
import com.ruoyi.system.service.IAgriWarningSummaryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预警信息汇总Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriWarningSummaryServiceImpl implements IAgriWarningSummaryService
{
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private AgriWarningSummaryMapper agriWarningSummaryMapper;

    @Override
    public AgriWarningSummary selectAgriWarningSummaryBySummaryId(Long summaryId)
    {
        return agriWarningSummaryMapper.selectAgriWarningSummaryBySummaryId(summaryId);
    }

    @Override
    public List<AgriWarningSummary> selectAgriWarningSummaryList(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.selectAgriWarningSummaryList(agriWarningSummary);
    }

    @Override
    public Map<String, Object> selectAgriWarningSummaryDashboard(AgriWarningSummary agriWarningSummary)
    {
        List<AgriWarningSummary> rawList = agriWarningSummaryMapper.selectAgriWarningSummaryList(
            agriWarningSummary == null ? new AgriWarningSummary() : agriWarningSummary
        );
        List<AgriWarningSummary> summaryList = rawList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", buildKpi(summaryList));
        result.put("trend", buildTrend(summaryList));
        result.put("topAlerts", buildTopAlerts(summaryList));
        result.put("insight", buildInsight(summaryList));
        result.put("recommendations", buildRecommendations(summaryList));
        result.put("total", summaryList.size());
        return result;
    }

    @Override
    public Map<String, Object> selectAgriWarningSummaryAlerts(AgriWarningSummary agriWarningSummary)
    {
        List<AgriWarningSummary> rawList = agriWarningSummaryMapper.selectAgriWarningSummaryList(
            agriWarningSummary == null ? new AgriWarningSummary() : agriWarningSummary
        );
        List<AgriWarningSummary> summaryList = rawList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        AgriWarningSummary latest = summaryList.stream().findFirst().orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("latest", latest);
        result.put("topAlerts", buildTopAlerts(summaryList));
        result.put("pendingCount", sumOf(summaryList, AgriWarningSummary::getPendingCount));
        result.put("severeCount", sumOf(summaryList, AgriWarningSummary::getLevel3Count));
        result.put("totalWarningCount", sumOf(summaryList, AgriWarningSummary::getTotalWarningCount));
        return result;
    }

    @Override
    public int insertAgriWarningSummary(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.insertAgriWarningSummary(agriWarningSummary);
    }

    @Override
    public int updateAgriWarningSummary(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.updateAgriWarningSummary(agriWarningSummary);
    }

    @Override
    public int deleteAgriWarningSummaryBySummaryIds(Long[] summaryIds)
    {
        return agriWarningSummaryMapper.deleteAgriWarningSummaryBySummaryIds(summaryIds);
    }

    private Map<String, Object> buildKpi(List<AgriWarningSummary> summaryList)
    {
        long totalWarningCount = sumOf(summaryList, AgriWarningSummary::getTotalWarningCount);
        long handledCount = sumOf(summaryList, AgriWarningSummary::getHandledCount);
        long pendingCount = sumOf(summaryList, AgriWarningSummary::getPendingCount);
        long severeCount = sumOf(summaryList, AgriWarningSummary::getLevel3Count);
        long warningCount = sumOf(summaryList, AgriWarningSummary::getLevel2Count);
        BigDecimal avgHandleMinutes = weightedAvgHandle(summaryList);

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalWarningCount", totalWarningCount);
        kpi.put("handledCount", handledCount);
        kpi.put("pendingCount", pendingCount);
        kpi.put("warningCount", warningCount);
        kpi.put("severeCount", severeCount);
        kpi.put("avgHandleMinutes", avgHandleMinutes);
        kpi.put("handleRate", rate(handledCount, handledCount + pendingCount));
        kpi.put("severeRate", rate(severeCount, totalWarningCount));
        return kpi;
    }

    private List<Map<String, Object>> buildTrend(List<AgriWarningSummary> summaryList)
    {
        Map<LocalDate, Long> totalByDate = summaryList.stream()
            .filter(item -> item.getSummaryDate() != null)
            .collect(Collectors.groupingBy(item -> item.getSummaryDate().toInstant().atZone(SYSTEM_ZONE).toLocalDate(), LinkedHashMap::new,
                Collectors.summingLong(item -> safeLong(item.getTotalWarningCount()))));

        Map<LocalDate, Long> handledByDate = summaryList.stream()
            .filter(item -> item.getSummaryDate() != null)
            .collect(Collectors.groupingBy(item -> item.getSummaryDate().toInstant().atZone(SYSTEM_ZONE).toLocalDate(), LinkedHashMap::new,
                Collectors.summingLong(item -> safeLong(item.getHandledCount()))));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--)
        {
            LocalDate day = LocalDate.now().minusDays(offset);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", day.format(DATE_FORMATTER));
            item.put("total", totalByDate.getOrDefault(day, 0L));
            item.put("handled", handledByDate.getOrDefault(day, 0L));
            trend.add(item);
        }
        return trend;
    }

    private List<Map<String, Object>> buildTopAlerts(List<AgriWarningSummary> summaryList)
    {
        return summaryList.stream()
            .sorted(Comparator
                .comparingLong((AgriWarningSummary item) -> safeLong(item.getLevel3Count())).reversed()
                .thenComparing(Comparator.comparingLong((AgriWarningSummary item) -> safeLong(item.getPendingCount())).reversed())
                .thenComparing(AgriWarningSummary::getSummaryDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(6)
            .map(this::toAlertCard)
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildInsight(List<AgriWarningSummary> summaryList)
    {
        Map<String, Object> kpi = buildKpi(summaryList);
        long totalWarningCount = safeLong((Long) kpi.get("totalWarningCount"));
        BigDecimal severeRate = (BigDecimal) kpi.get("severeRate");
        BigDecimal handleRate = (BigDecimal) kpi.get("handleRate");

        String riskLevel;
        if (severeRate.compareTo(new BigDecimal("0.25")) >= 0 || handleRate.compareTo(new BigDecimal("0.55")) < 0)
        {
            riskLevel = "高";
        }
        else if (severeRate.compareTo(new BigDecimal("0.12")) >= 0 || handleRate.compareTo(new BigDecimal("0.75")) < 0)
        {
            riskLevel = "中";
        }
        else
        {
            riskLevel = "低";
        }

        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("riskLevel", riskLevel);
        insight.put("confidenceRate", confidenceRate(summaryList));
        insight.put("modelVersion", "rule-v1");
        insight.put("insightSummary", "最近统计窗口累计预警" + totalWarningCount + "条，严重级占比"
            + severeRate.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "% ，闭环率"
            + handleRate.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "% 。");
        insight.put("suggestion", "优先清理严重级与待处理告警，并按天跟踪闭环率变化，确保平均处理时长稳定下降。");
        return insight;
    }

    private List<String> buildRecommendations(List<AgriWarningSummary> summaryList)
    {
        Map<String, Object> kpi = buildKpi(summaryList);
        long pendingCount = safeLong((Long) kpi.get("pendingCount"));
        long severeCount = safeLong((Long) kpi.get("severeCount"));

        List<String> recommendations = new ArrayList<>();
        if (severeCount > 0)
        {
            recommendations.add("对严重级告警建立2小时内闭环机制，优先分派到值班责任人。");
        }
        if (pendingCount > 0)
        {
            recommendations.add("待处理告警建议按预警等级和业务影响排序，优先处置高影响项。");
        }
        recommendations.add("按周复盘平均处理时长与闭环率，持续优化处置SOP和通知策略。");
        return recommendations;
    }

    private Map<String, Object> toAlertCard(AgriWarningSummary item)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summaryId", item.getSummaryId());
        result.put("summaryDate", item.getSummaryDate());
        result.put("totalWarningCount", safeLong(item.getTotalWarningCount()));
        result.put("pendingCount", safeLong(item.getPendingCount()));
        result.put("handledCount", safeLong(item.getHandledCount()));
        result.put("level1Count", safeLong(item.getLevel1Count()));
        result.put("level2Count", safeLong(item.getLevel2Count()));
        result.put("level3Count", safeLong(item.getLevel3Count()));
        result.put("avgHandleMinutes", safeLong(item.getAvgHandleMinutes()));
        result.put("riskLevel", riskLabel(item));
        result.put("suggestion", alertSuggestion(item));
        return result;
    }

    private String riskLabel(AgriWarningSummary item)
    {
        long total = safeLong(item.getTotalWarningCount());
        long severe = safeLong(item.getLevel3Count());
        long pending = safeLong(item.getPendingCount());
        if (severe >= 5 || pending >= 12 || rate(severe, total).compareTo(new BigDecimal("0.25")) >= 0)
        {
            return "高";
        }
        if (severe > 0 || pending > 0)
        {
            return "中";
        }
        return "低";
    }

    private String alertSuggestion(AgriWarningSummary item)
    {
        String risk = riskLabel(item);
        if ("高".equals(risk))
        {
            return "建议立即安排值班负责人处理严重级和超时未闭环告警。";
        }
        if ("中".equals(risk))
        {
            return "建议在当班内完成待处理告警并复核处理结果。";
        }
        return "当前告警态势平稳，可维持例行巡检。";
    }

    private BigDecimal weightedAvgHandle(List<AgriWarningSummary> summaryList)
    {
        long total = summaryList.stream().map(AgriWarningSummary::getTotalWarningCount).mapToLong(this::safeLong).sum();
        if (total == 0)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal weighted = summaryList.stream()
            .map(item -> BigDecimal.valueOf(safeLong(item.getAvgHandleMinutes())).multiply(BigDecimal.valueOf(safeLong(item.getTotalWarningCount()))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weighted.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private long sumOf(List<AgriWarningSummary> list, java.util.function.Function<AgriWarningSummary, Long> extractor)
    {
        return list.stream().map(extractor).mapToLong(this::safeLong).sum();
    }

    private long safeLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private BigDecimal rate(long numerator, long denominator)
    {
        if (denominator <= 0)
        {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal confidenceRate(List<AgriWarningSummary> summaryList)
    {
        if (summaryList.isEmpty())
        {
            return new BigDecimal("0.50");
        }
        long validCount = summaryList.stream().filter(item -> item.getSummaryDate() != null).count();
        return BigDecimal.valueOf(validCount).divide(BigDecimal.valueOf(summaryList.size()), 2, RoundingMode.HALF_UP);
    }
}
