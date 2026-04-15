package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 质检报告对象 agri_quality_report
 *
 * @author ruoyi
 */
public class AgriQualityReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long reportId;

    @Excel(name = "报告编号")
    private String reportNo;

    @Excel(name = "检测任务ID")
    private Long inspectId;

    @Excel(name = "加工批次号")
    private String processBatchNo;

    @Excel(name = "品质等级")
    private String qualityGrade;

    @Excel(name = "缺陷率")
    private BigDecimal defectRate;

    @Excel(name = "报告摘要")
    private String reportSummary;

    @Excel(name = "报告状态", readConverterExp = "0=草稿,1=已生成")
    private String reportStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报告时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getReportId()
    {
        return reportId;
    }

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
    }

    @NotBlank(message = "报告编号不能为空")
    @Size(max = 64, message = "报告编号长度不能超过64个字符")
    public String getReportNo()
    {
        return reportNo;
    }

    public void setReportNo(String reportNo)
    {
        this.reportNo = reportNo;
    }

    @NotNull(message = "检测任务ID不能为空")
    public Long getInspectId()
    {
        return inspectId;
    }

    public void setInspectId(Long inspectId)
    {
        this.inspectId = inspectId;
    }

    @NotBlank(message = "加工批次号不能为空")
    @Size(max = 64, message = "加工批次号长度不能超过64个字符")
    public String getProcessBatchNo()
    {
        return processBatchNo;
    }

    public void setProcessBatchNo(String processBatchNo)
    {
        this.processBatchNo = processBatchNo;
    }

    @Size(max = 20, message = "品质等级长度不能超过20个字符")
    public String getQualityGrade()
    {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade)
    {
        this.qualityGrade = qualityGrade;
    }

    public BigDecimal getDefectRate()
    {
        return defectRate;
    }

    public void setDefectRate(BigDecimal defectRate)
    {
        this.defectRate = defectRate;
    }

    @Size(max = 1000, message = "报告摘要长度不能超过1000个字符")
    public String getReportSummary()
    {
        return reportSummary;
    }

    public void setReportSummary(String reportSummary)
    {
        this.reportSummary = reportSummary;
    }

    public String getReportStatus()
    {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus)
    {
        this.reportStatus = reportStatus;
    }

    public Date getReportTime()
    {
        return reportTime;
    }

    public void setReportTime(Date reportTime)
    {
        this.reportTime = reportTime;
    }

    @NotNull(message = "状态不能为空")
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
            .append("reportId", getReportId())
            .append("reportNo", getReportNo())
            .append("inspectId", getInspectId())
            .append("processBatchNo", getProcessBatchNo())
            .append("qualityGrade", getQualityGrade())
            .append("defectRate", getDefectRate())
            .append("reportSummary", getReportSummary())
            .append("reportStatus", getReportStatus())
            .append("reportTime", getReportTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
