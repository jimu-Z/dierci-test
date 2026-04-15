package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 溯源查询统计对象 agri_trace_query_stats
 *
 * @author ruoyi
 */
public class AgriTraceQueryStats extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long statsId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date statDate;

    @Excel(name = "查询总次数")
    private Long totalQueryCount;

    @Excel(name = "独立用户数")
    private Long uniqueUserCount;

    @Excel(name = "平均响应时长(ms)")
    private Long avgDurationMs;

    @Excel(name = "成功次数")
    private Long successCount;

    @Excel(name = "失败次数")
    private Long failCount;

    @Excel(name = "峰值QPS")
    private BigDecimal peakQps;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getStatsId()
    {
        return statsId;
    }

    public void setStatsId(Long statsId)
    {
        this.statsId = statsId;
    }

    @NotNull(message = "统计日期不能为空")
    public Date getStatDate()
    {
        return statDate;
    }

    public void setStatDate(Date statDate)
    {
        this.statDate = statDate;
    }

    @NotNull(message = "查询总次数不能为空")
    public Long getTotalQueryCount()
    {
        return totalQueryCount;
    }

    public void setTotalQueryCount(Long totalQueryCount)
    {
        this.totalQueryCount = totalQueryCount;
    }

    public Long getUniqueUserCount()
    {
        return uniqueUserCount;
    }

    public void setUniqueUserCount(Long uniqueUserCount)
    {
        this.uniqueUserCount = uniqueUserCount;
    }

    public Long getAvgDurationMs()
    {
        return avgDurationMs;
    }

    public void setAvgDurationMs(Long avgDurationMs)
    {
        this.avgDurationMs = avgDurationMs;
    }

    public Long getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(Long successCount)
    {
        this.successCount = successCount;
    }

    public Long getFailCount()
    {
        return failCount;
    }

    public void setFailCount(Long failCount)
    {
        this.failCount = failCount;
    }

    public BigDecimal getPeakQps()
    {
        return peakQps;
    }

    public void setPeakQps(BigDecimal peakQps)
    {
        this.peakQps = peakQps;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("statsId", getStatsId())
            .append("statDate", getStatDate())
            .append("totalQueryCount", getTotalQueryCount())
            .append("uniqueUserCount", getUniqueUserCount())
            .append("avgDurationMs", getAvgDurationMs())
            .append("successCount", getSuccessCount())
            .append("failCount", getFailCount())
            .append("peakQps", getPeakQps())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
