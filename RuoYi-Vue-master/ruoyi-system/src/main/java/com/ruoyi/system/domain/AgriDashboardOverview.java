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
 * 数据总览看板对象 agri_dashboard_overview
 *
 * @author ruoyi
 */
public class AgriDashboardOverview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long overviewId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date statDate;

    @Excel(name = "总产量(吨)")
    private BigDecimal totalOutput;

    @Excel(name = "总销量(万元)")
    private BigDecimal totalSales;

    @Excel(name = "溯源查询次数")
    private Long traceQueryCount;

    @Excel(name = "预警数量")
    private Long warningCount;

    @Excel(name = "在线设备数")
    private Long onlineDeviceCount;

    @Excel(name = "待办数量")
    private Long pendingTaskCount;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getOverviewId()
    {
        return overviewId;
    }

    public void setOverviewId(Long overviewId)
    {
        this.overviewId = overviewId;
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

    @NotNull(message = "总产量不能为空")
    public BigDecimal getTotalOutput()
    {
        return totalOutput;
    }

    public void setTotalOutput(BigDecimal totalOutput)
    {
        this.totalOutput = totalOutput;
    }

    @NotNull(message = "总销量不能为空")
    public BigDecimal getTotalSales()
    {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales)
    {
        this.totalSales = totalSales;
    }

    public Long getTraceQueryCount()
    {
        return traceQueryCount;
    }

    public void setTraceQueryCount(Long traceQueryCount)
    {
        this.traceQueryCount = traceQueryCount;
    }

    public Long getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(Long warningCount)
    {
        this.warningCount = warningCount;
    }

    public Long getOnlineDeviceCount()
    {
        return onlineDeviceCount;
    }

    public void setOnlineDeviceCount(Long onlineDeviceCount)
    {
        this.onlineDeviceCount = onlineDeviceCount;
    }

    public Long getPendingTaskCount()
    {
        return pendingTaskCount;
    }

    public void setPendingTaskCount(Long pendingTaskCount)
    {
        this.pendingTaskCount = pendingTaskCount;
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
            .append("overviewId", getOverviewId())
            .append("statDate", getStatDate())
            .append("totalOutput", getTotalOutput())
            .append("totalSales", getTotalSales())
            .append("traceQueryCount", getTraceQueryCount())
            .append("warningCount", getWarningCount())
            .append("onlineDeviceCount", getOnlineDeviceCount())
            .append("pendingTaskCount", getPendingTaskCount())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
