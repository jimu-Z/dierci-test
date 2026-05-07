package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 物流路径追踪摘要对象。
 */
public class AgriLogisticsTrackSummary
{
    private String traceCode;

    private Integer recordCount;

    private Integer nodeCount;

    private Integer uniqueLocationCount;

    private Integer stopRiskCount;

    private Boolean stopRiskFlag;

    private Long durationMinutes;

    private String latestStatus;

    private String latestLocation;

    private String latestEventDesc;

    private String routePath;

    private List<String> routeSteps;

    private Date firstEventTime;

    private Date lastEventTime;

    private BigDecimal maxTemperature;

    private BigDecimal minTemperature;

    private BigDecimal avgHumidity;

    private String summaryText;

    public String getTraceCode()
    {
        return traceCode;
    }

    public void setTraceCode(String traceCode)
    {
        this.traceCode = traceCode;
    }

    public Integer getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount)
    {
        this.recordCount = recordCount;
    }

    public Integer getNodeCount()
    {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount)
    {
        this.nodeCount = nodeCount;
    }

    public Integer getUniqueLocationCount()
    {
        return uniqueLocationCount;
    }

    public void setUniqueLocationCount(Integer uniqueLocationCount)
    {
        this.uniqueLocationCount = uniqueLocationCount;
    }

    public Integer getStopRiskCount()
    {
        return stopRiskCount;
    }

    public void setStopRiskCount(Integer stopRiskCount)
    {
        this.stopRiskCount = stopRiskCount;
    }

    public Boolean getStopRiskFlag()
    {
        return stopRiskFlag;
    }

    public void setStopRiskFlag(Boolean stopRiskFlag)
    {
        this.stopRiskFlag = stopRiskFlag;
    }

    public Long getDurationMinutes()
    {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes)
    {
        this.durationMinutes = durationMinutes;
    }

    public String getLatestStatus()
    {
        return latestStatus;
    }

    public void setLatestStatus(String latestStatus)
    {
        this.latestStatus = latestStatus;
    }

    public String getLatestLocation()
    {
        return latestLocation;
    }

    public void setLatestLocation(String latestLocation)
    {
        this.latestLocation = latestLocation;
    }

    public String getLatestEventDesc()
    {
        return latestEventDesc;
    }

    public void setLatestEventDesc(String latestEventDesc)
    {
        this.latestEventDesc = latestEventDesc;
    }

    public String getRoutePath()
    {
        return routePath;
    }

    public void setRoutePath(String routePath)
    {
        this.routePath = routePath;
    }

    public List<String> getRouteSteps()
    {
        return routeSteps;
    }

    public void setRouteSteps(List<String> routeSteps)
    {
        this.routeSteps = routeSteps;
    }

    public Date getFirstEventTime()
    {
        return firstEventTime;
    }

    public void setFirstEventTime(Date firstEventTime)
    {
        this.firstEventTime = firstEventTime;
    }

    public Date getLastEventTime()
    {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime)
    {
        this.lastEventTime = lastEventTime;
    }

    public BigDecimal getMaxTemperature()
    {
        return maxTemperature;
    }

    public void setMaxTemperature(BigDecimal maxTemperature)
    {
        this.maxTemperature = maxTemperature;
    }

    public BigDecimal getMinTemperature()
    {
        return minTemperature;
    }

    public void setMinTemperature(BigDecimal minTemperature)
    {
        this.minTemperature = minTemperature;
    }

    public BigDecimal getAvgHumidity()
    {
        return avgHumidity;
    }

    public void setAvgHumidity(BigDecimal avgHumidity)
    {
        this.avgHumidity = avgHumidity;
    }

    public String getSummaryText()
    {
        return summaryText;
    }

    public void setSummaryText(String summaryText)
    {
        this.summaryText = summaryText;
    }
}