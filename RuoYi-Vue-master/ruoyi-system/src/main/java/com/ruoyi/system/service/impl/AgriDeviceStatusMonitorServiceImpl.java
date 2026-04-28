package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import com.ruoyi.system.mapper.AgriDeviceStatusMonitorMapper;
import com.ruoyi.system.service.IAgriDeviceStatusMonitorService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设备状态监控Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDeviceStatusMonitorServiceImpl implements IAgriDeviceStatusMonitorService
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    @Autowired
    private AgriDeviceStatusMonitorMapper agriDeviceStatusMonitorMapper;

    @Override
    public AgriDeviceStatusMonitor selectAgriDeviceStatusMonitorByMonitorId(Long monitorId)
    {
        return agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorByMonitorId(monitorId);
    }

    @Override
    public List<AgriDeviceStatusMonitor> selectAgriDeviceStatusMonitorList(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorList(agriDeviceStatusMonitor);
    }

    @Override
    public Map<String, Object> selectAgriDeviceStatusMonitorDashboard(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        List<AgriDeviceStatusMonitor> sourceList = agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorList(
            agriDeviceStatusMonitor == null ? new AgriDeviceStatusMonitor() : agriDeviceStatusMonitor
        );
        List<AgriDeviceStatusMonitor> monitorList = sourceList.stream().map(this::normalizeMonitor).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", buildKpi(monitorList));
        result.put("statusDistribution", buildStatusDistribution(monitorList));
        result.put("alertList", buildAlertList(monitorList));
        result.put("recentList", buildRecentList(monitorList));
        result.put("trend", buildTrend(monitorList));
        result.put("focus", buildFocus(monitorList));
        result.put("total", monitorList.size());
        return result;
    }

    @Override
    public Map<String, Object> selectAgriDeviceStatusMonitorAlerts(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        List<AgriDeviceStatusMonitor> sourceList = agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorList(
            agriDeviceStatusMonitor == null ? new AgriDeviceStatusMonitor() : agriDeviceStatusMonitor
        );
        List<AgriDeviceStatusMonitor> monitorList = sourceList.stream().map(this::normalizeMonitor).collect(Collectors.toList());
        List<Map<String, Object>> alertList = buildAlertList(monitorList);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("alertCount", alertList.size());
        result.put("severeCount", monitorList.stream().filter(item -> "3".equals(item.getWarningLevel())).count());
        result.put("offlineCount", monitorList.stream().filter(item -> "0".equals(item.getOnlineStatus())).count());
        result.put("staleCount", monitorList.stream().filter(this::isStale).count());
        result.put("latestAlert", alertList.isEmpty() ? null : alertList.get(0));
        result.put("alerts", alertList);
        return result;
    }

    @Override
    public int insertAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.insertAgriDeviceStatusMonitor(agriDeviceStatusMonitor);
    }

    @Override
    public int updateAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.updateAgriDeviceStatusMonitor(agriDeviceStatusMonitor);
    }

    @Override
    public int deleteAgriDeviceStatusMonitorByMonitorIds(Long[] monitorIds)
    {
        return agriDeviceStatusMonitorMapper.deleteAgriDeviceStatusMonitorByMonitorIds(monitorIds);
    }

    private AgriDeviceStatusMonitor normalizeMonitor(AgriDeviceStatusMonitor monitor)
    {
        monitor = AgriNormalizationHelper.ensureNotNull(monitor, AgriDeviceStatusMonitor::new);
        AgriNormalizationHelper.defaultIfBlank(monitor::getOnlineStatus, monitor::setOnlineStatus, "0");
        AgriNormalizationHelper.defaultIfBlank(monitor::getWarningLevel, monitor::setWarningLevel, "0");
        AgriNormalizationHelper.defaultIfNull(monitor::getBatteryLevel, monitor::setBatteryLevel, BigDecimal.ZERO);
        AgriNormalizationHelper.defaultIfNull(monitor::getSignalStrength, monitor::setSignalStrength, BigDecimal.ZERO);
        AgriNormalizationHelper.defaultIfNull(monitor::getTemperature, monitor::setTemperature, BigDecimal.ZERO);
        AgriNormalizationHelper.defaultIfNull(monitor::getHumidity, monitor::setHumidity, BigDecimal.ZERO);
        return monitor;
    }

    private Map<String, Object> buildKpi(List<AgriDeviceStatusMonitor> monitorList)
    {
        Map<String, Object> kpi = new LinkedHashMap<>();
        int total = monitorList.size();
        long onlineCount = monitorList.stream().filter(item -> "1".equals(item.getOnlineStatus())).count();
        long warningCount = monitorList.stream().filter(item -> isWarningLevel(item.getWarningLevel())).count();
        long severeCount = monitorList.stream().filter(item -> "3".equals(item.getWarningLevel())).count();
        long staleCount = monitorList.stream().filter(this::isStale).count();

        kpi.put("totalCount", total);
        kpi.put("onlineCount", onlineCount);
        kpi.put("offlineCount", total - onlineCount);
        kpi.put("warningCount", warningCount);
        kpi.put("severeCount", severeCount);
        kpi.put("staleCount", staleCount);
        kpi.put("avgBattery", averageOf(monitorList, AgriDeviceStatusMonitor::getBatteryLevel));
        kpi.put("avgSignal", averageOf(monitorList, AgriDeviceStatusMonitor::getSignalStrength));
        kpi.put("avgTemperature", averageOf(monitorList, AgriDeviceStatusMonitor::getTemperature));
        kpi.put("avgHumidity", averageOf(monitorList, AgriDeviceStatusMonitor::getHumidity));
        return kpi;
    }

    private List<Map<String, Object>> buildStatusDistribution(List<AgriDeviceStatusMonitor> monitorList)
    {
        int total = Math.max(monitorList.size(), 1);
        long onlineCount = monitorList.stream().filter(item -> "1".equals(item.getOnlineStatus())).count();
        long offlineCount = total - onlineCount;
        long warningCount = monitorList.stream().filter(item -> isWarningLevel(item.getWarningLevel())).count();
        long normalCount = Math.max(total - warningCount, 0);

        List<Map<String, Object>> distribution = new ArrayList<>();
        distribution.add(buildDistributionItem("online", "在线率", onlineCount, total));
        distribution.add(buildDistributionItem("offline", "离线率", offlineCount, total));
        distribution.add(buildDistributionItem("warning", "异常率", warningCount, total));
        distribution.add(buildDistributionItem("normal", "正常率", normalCount, total));
        return distribution;
    }

    private Map<String, Object> buildDistributionItem(String key, String label, long count, int total)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("count", count);
        item.put("rate", total == 0 ? 0 : BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP));
        return item;
    }

    private List<Map<String, Object>> buildAlertList(List<AgriDeviceStatusMonitor> monitorList)
    {
        return monitorList.stream()
            .filter(this::isAlert)
            .sorted(Comparator.comparingInt(this::alertScore).reversed()
                .thenComparing(AgriDeviceStatusMonitor::getLastReportTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(6)
            .map(this::toAlertMap)
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildRecentList(List<AgriDeviceStatusMonitor> monitorList)
    {
        return monitorList.stream()
            .sorted(Comparator.comparing(AgriDeviceStatusMonitor::getLastReportTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AgriDeviceStatusMonitor::getMonitorId, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .map(this::toDeviceCardMap)
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildFocus(List<AgriDeviceStatusMonitor> monitorList)
    {
        return monitorList.stream()
            .sorted(Comparator.comparingInt(this::alertScore).reversed()
                .thenComparing(AgriDeviceStatusMonitor::getLastReportTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .findFirst()
            .map(this::toDeviceCardMap)
            .orElse(null);
    }

    private List<Map<String, Object>> buildTrend(List<AgriDeviceStatusMonitor> monitorList)
    {
        Map<LocalDate, Long> countsByDate = monitorList.stream()
            .filter(item -> item.getLastReportTime() != null)
            .collect(Collectors.groupingBy(item -> item.getLastReportTime().toInstant().atZone(SYSTEM_ZONE).toLocalDate(), Collectors.counting()));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--)
        {
            LocalDate date = LocalDate.now().minusDays(offset);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(DATE_FORMATTER));
            item.put("count", countsByDate.getOrDefault(date, 0L));
            trend.add(item);
        }
        return trend;
    }

    private Map<String, Object> toAlertMap(AgriDeviceStatusMonitor monitor)
    {
        Map<String, Object> item = toDeviceCardMap(monitor);
        item.put("riskLabel", buildRiskLabel(monitor));
        item.put("alertScore", alertScore(monitor));
        item.put("stale", isStale(monitor));
        return item;
    }

    private Map<String, Object> toDeviceCardMap(AgriDeviceStatusMonitor monitor)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("monitorId", monitor.getMonitorId());
        item.put("deviceCode", monitor.getDeviceCode());
        item.put("deviceName", monitor.getDeviceName());
        item.put("deviceType", monitor.getDeviceType());
        item.put("onlineStatus", monitor.getOnlineStatus());
        item.put("warningLevel", monitor.getWarningLevel());
        item.put("batteryLevel", monitor.getBatteryLevel());
        item.put("signalStrength", monitor.getSignalStrength());
        item.put("temperature", monitor.getTemperature());
        item.put("humidity", monitor.getHumidity());
        item.put("lastReportTime", monitor.getLastReportTime());
        item.put("remark", monitor.getRemark());
        item.put("riskLabel", buildRiskLabel(monitor));
        item.put("freshnessMinutes", freshnessMinutes(monitor));
        item.put("stale", isStale(monitor));
        return item;
    }

    private int alertScore(AgriDeviceStatusMonitor monitor)
    {
        int score = 0;
        if ("0".equals(monitor.getOnlineStatus()))
        {
            score += 5;
        }
        if ("3".equals(monitor.getWarningLevel()))
        {
            score += 4;
        }
        else if ("2".equals(monitor.getWarningLevel()))
        {
            score += 3;
        }
        if (isStale(monitor))
        {
            score += 3;
        }
        if (safeBigDecimal(monitor.getBatteryLevel()).compareTo(new BigDecimal("20")) < 0)
        {
            score += 2;
        }
        if (safeBigDecimal(monitor.getSignalStrength()).compareTo(new BigDecimal("35")) < 0)
        {
            score += 1;
        }
        return score;
    }

    private boolean isAlert(AgriDeviceStatusMonitor monitor)
    {
        return "0".equals(monitor.getOnlineStatus())
            || isWarningLevel(monitor.getWarningLevel())
            || isStale(monitor)
            || safeBigDecimal(monitor.getBatteryLevel()).compareTo(new BigDecimal("20")) < 0
            || safeBigDecimal(monitor.getSignalStrength()).compareTo(new BigDecimal("35")) < 0;
    }

    private boolean isWarningLevel(String warningLevel)
    {
        return "2".equals(warningLevel) || "3".equals(warningLevel);
    }

    private boolean isStale(AgriDeviceStatusMonitor monitor)
    {
        if (monitor.getLastReportTime() == null)
        {
            return true;
        }
        return freshnessMinutes(monitor) >= 30;
    }

    private long freshnessMinutes(AgriDeviceStatusMonitor monitor)
    {
        if (monitor.getLastReportTime() == null)
        {
            return Long.MAX_VALUE;
        }
        return Math.max((System.currentTimeMillis() - monitor.getLastReportTime().getTime()) / 60000L, 0L);
    }

    private String buildRiskLabel(AgriDeviceStatusMonitor monitor)
    {
        if (isStale(monitor) || "3".equals(monitor.getWarningLevel()) || "0".equals(monitor.getOnlineStatus()))
        {
            return "高";
        }
        if ("2".equals(monitor.getWarningLevel())
            || safeBigDecimal(monitor.getBatteryLevel()).compareTo(new BigDecimal("40")) < 0
            || safeBigDecimal(monitor.getSignalStrength()).compareTo(new BigDecimal("50")) < 0)
        {
            return "中";
        }
        return "低";
    }

    private BigDecimal averageOf(List<AgriDeviceStatusMonitor> monitorList, java.util.function.Function<AgriDeviceStatusMonitor, BigDecimal> extractor)
    {
        if (monitorList.isEmpty())
        {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        BigDecimal sum = monitorList.stream()
            .map(extractor)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(monitorList.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal safeBigDecimal(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
