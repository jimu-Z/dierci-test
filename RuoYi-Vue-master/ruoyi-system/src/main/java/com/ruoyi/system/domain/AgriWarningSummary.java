package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 预警信息汇总对象 agri_warning_summary
 *
 * @author ruoyi
 */
public class AgriWarningSummary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long summaryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date summaryDate;

    @Excel(name = "预警总数")
    private Long totalWarningCount;

    @Excel(name = "提示级")
    private Long level1Count;

    @Excel(name = "预警级")
    private Long level2Count;

    @Excel(name = "严重级")
    private Long level3Count;

    @Excel(name = "已处理")
    private Long handledCount;

    @Excel(name = "待处理")
    private Long pendingCount;

    @Excel(name = "平均处理时长(分钟)")
    private Long avgHandleMinutes;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getSummaryId()
    {
        return summaryId;
    }

    public void setSummaryId(Long summaryId)
    {
        this.summaryId = summaryId;
    }

    @NotNull(message = "统计日期不能为空")
    public Date getSummaryDate()
    {
        return summaryDate;
    }

    public void setSummaryDate(Date summaryDate)
    {
        this.summaryDate = summaryDate;
    }

    @NotNull(message = "预警总数不能为空")
    public Long getTotalWarningCount()
    {
        return totalWarningCount;
    }

    public void setTotalWarningCount(Long totalWarningCount)
    {
        this.totalWarningCount = totalWarningCount;
    }

    public Long getLevel1Count()
    {
        return level1Count;
    }

    public void setLevel1Count(Long level1Count)
    {
        this.level1Count = level1Count;
    }

    public Long getLevel2Count()
    {
        return level2Count;
    }

    public void setLevel2Count(Long level2Count)
    {
        this.level2Count = level2Count;
    }

    public Long getLevel3Count()
    {
        return level3Count;
    }

    public void setLevel3Count(Long level3Count)
    {
        this.level3Count = level3Count;
    }

    public Long getHandledCount()
    {
        return handledCount;
    }

    public void setHandledCount(Long handledCount)
    {
        this.handledCount = handledCount;
    }

    public Long getPendingCount()
    {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount)
    {
        this.pendingCount = pendingCount;
    }

    public Long getAvgHandleMinutes()
    {
        return avgHandleMinutes;
    }

    public void setAvgHandleMinutes(Long avgHandleMinutes)
    {
        this.avgHandleMinutes = avgHandleMinutes;
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
            .append("summaryId", getSummaryId())
            .append("summaryDate", getSummaryDate())
            .append("totalWarningCount", getTotalWarningCount())
            .append("level1Count", getLevel1Count())
            .append("level2Count", getLevel2Count())
            .append("level3Count", getLevel3Count())
            .append("handledCount", getHandledCount())
            .append("pendingCount", getPendingCount())
            .append("avgHandleMinutes", getAvgHandleMinutes())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
