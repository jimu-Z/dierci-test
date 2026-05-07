package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriLogisticsTrack;
import com.ruoyi.system.domain.AgriLogisticsTrackSummary;
import com.ruoyi.system.mapper.AgriLogisticsTrackMapper;
import com.ruoyi.system.service.IAgriLogisticsTrackService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 物流路径追踪Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriLogisticsTrackServiceImpl implements IAgriLogisticsTrackService
{
    @Autowired
    private AgriLogisticsTrackMapper agriLogisticsTrackMapper;

    @Override
    public AgriLogisticsTrack selectAgriLogisticsTrackByTrackId(Long trackId)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackByTrackId(trackId);
    }

    @Override
    public List<AgriLogisticsTrack> selectAgriLogisticsTrackList(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackList(agriLogisticsTrack);
    }

    @Override
    public int insertAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.insertAgriLogisticsTrack(agriLogisticsTrack);
    }

    @Override
    public int updateAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.updateAgriLogisticsTrack(agriLogisticsTrack);
    }

    @Override
    public int deleteAgriLogisticsTrackByTrackIds(Long[] trackIds)
    {
        return agriLogisticsTrackMapper.deleteAgriLogisticsTrackByTrackIds(trackIds);
    }

    @Override
    public List<AgriLogisticsTrack> selectAgriLogisticsTrackTimeline(String traceCode)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackTimeline(traceCode);
    }

    @Override
    public AgriLogisticsTrackSummary selectAgriLogisticsTrackSummary(String traceCode)
    {
        List<AgriLogisticsTrack> timeline = agriLogisticsTrackMapper.selectAgriLogisticsTrackTimeline(traceCode);
        AgriLogisticsTrackSummary summary = new AgriLogisticsTrackSummary();
        summary.setTraceCode(traceCode);
        summary.setRecordCount(timeline == null ? 0 : timeline.size());

        if (timeline == null || timeline.isEmpty())
        {
            summary.setNodeCount(0);
            summary.setUniqueLocationCount(0);
            summary.setStopRiskCount(0);
            summary.setStopRiskFlag(false);
            summary.setSummaryText("当前运单暂无轨迹记录");
            return summary;
        }

        List<String> routeSteps = new ArrayList<>();
        Set<String> uniqueLocations = new LinkedHashSet<>();
        BigDecimal maxTemperature = null;
        BigDecimal minTemperature = null;
        BigDecimal humiditySum = BigDecimal.ZERO;
        int humidityCount = 0;
        int stopRiskCount = 0;

        Date firstEventTime = null;
        Date lastEventTime = null;
        AgriLogisticsTrack previous = null;

        for (AgriLogisticsTrack track : timeline)
        {
            if (firstEventTime == null)
            {
                firstEventTime = track.getEventTime();
            }
            lastEventTime = track.getEventTime();

            if (track.getCurrentLocation() != null && !track.getCurrentLocation().isBlank())
            {
                uniqueLocations.add(track.getCurrentLocation());
            }

            if (track.getTemperature() != null)
            {
                maxTemperature = maxTemperature == null ? track.getTemperature() : maxTemperature.max(track.getTemperature());
                minTemperature = minTemperature == null ? track.getTemperature() : minTemperature.min(track.getTemperature());
            }

            if (track.getHumidity() != null)
            {
                humiditySum = humiditySum.add(track.getHumidity());
                humidityCount++;
            }

            if (track.getRoutePath() != null && !track.getRoutePath().isBlank() && routeSteps.isEmpty())
            {
                routeSteps = parseRouteSteps(track.getRoutePath());
            }

            if (previous != null && isStopRisk(previous, track))
            {
                stopRiskCount++;
            }
            previous = track;
        }

        summary.setNodeCount(routeSteps.isEmpty() ? uniqueLocations.size() : routeSteps.size());
        summary.setUniqueLocationCount(uniqueLocations.size());
        summary.setStopRiskCount(stopRiskCount);
        summary.setStopRiskFlag(stopRiskCount > 0);
        summary.setFirstEventTime(firstEventTime);
        summary.setLastEventTime(lastEventTime);
        summary.setDurationMinutes(firstEventTime == null || lastEventTime == null ? 0L : Math.max(0L, (lastEventTime.getTime() - firstEventTime.getTime()) / 60000L));
        summary.setMaxTemperature(maxTemperature);
        summary.setMinTemperature(minTemperature);
        summary.setAvgHumidity(humidityCount == 0 ? null : humiditySum.divide(BigDecimal.valueOf(humidityCount), 2, RoundingMode.HALF_UP));

        AgriLogisticsTrack latest = timeline.get(timeline.size() - 1);
        summary.setLatestStatus(latest.getTrackStatus());
        summary.setLatestLocation(latest.getCurrentLocation());
        summary.setLatestEventDesc(latest.getEventDesc());
        summary.setRoutePath(firstRoutePath(timeline));
        summary.setRouteSteps(routeSteps);
        summary.setSummaryText(buildSummaryText(summary));
        return summary;
    }

    private List<String> parseRouteSteps(String routePath)
    {
        List<String> steps = new ArrayList<>();
        if (routePath == null || routePath.isBlank())
        {
            return steps;
        }
        String[] fragments = routePath.split("\\s*(?:->|→|—>|/|,|，|\\||;|>)+\\s*");
        for (String fragment : fragments)
        {
            if (fragment != null && !fragment.isBlank())
            {
                steps.add(fragment.trim());
            }
        }
        return steps;
    }

    private boolean isStopRisk(AgriLogisticsTrack previous, AgriLogisticsTrack current)
    {
        if (previous == null || current == null || previous.getEventTime() == null || current.getEventTime() == null)
        {
            return false;
        }
        if (previous.getCurrentLocation() == null || current.getCurrentLocation() == null)
        {
            return false;
        }
        if (!previous.getCurrentLocation().equals(current.getCurrentLocation()))
        {
            return false;
        }
        long intervalMinutes = Math.abs(current.getEventTime().getTime() - previous.getEventTime().getTime()) / 60000L;
        return intervalMinutes >= 30L;
    }

    private String firstRoutePath(List<AgriLogisticsTrack> timeline)
    {
        for (AgriLogisticsTrack track : timeline)
        {
            if (track.getRoutePath() != null && !track.getRoutePath().isBlank())
            {
                return track.getRoutePath();
            }
        }
        return null;
    }

    private String buildSummaryText(AgriLogisticsTrackSummary summary)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("共").append(summary.getRecordCount()).append("条记录");
        builder.append("，").append(summary.getNodeCount()).append("个节点");
        builder.append("，").append(summary.getDurationMinutes()).append("分钟");
        if (summary.getStopRiskFlag() != null && summary.getStopRiskFlag())
        {
            builder.append("，存在").append(summary.getStopRiskCount()).append("次停留风险");
        }
        else
        {
            builder.append("，未发现明显停留风险");
        }
        return builder.toString();
    }
}
