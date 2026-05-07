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
 * 视觉品质检测任务对象 agri_quality_inspect_task
 *
 * @author ruoyi
 */
public class AgriQualityInspectTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long inspectId;

    @Excel(name = "加工批次号")
    private String processBatchNo;

    @Excel(name = "样品编码")
    private String sampleCode;

    @Excel(name = "图片URL")
    private String imageUrl;

    @Excel(name = "检测状态", readConverterExp = "0=待检测,1=已检测,2=失败")
    private String inspectStatus;

    @Excel(name = "品质等级")
    private String qualityGrade;

    @Excel(name = "缺陷率")
    private BigDecimal defectRate;

    @Excel(name = "检测结果")
    private String inspectResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "检测时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date inspectTime;

    @Excel(name = "模型版本")
    private String modelVersion;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

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

    @NotBlank(message = "样品编码不能为空")
    @Size(max = 64, message = "样品编码长度不能超过64个字符")
    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    @NotBlank(message = "图片URL不能为空")
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getInspectStatus()
    {
        return inspectStatus;
    }

    public void setInspectStatus(String inspectStatus)
    {
        this.inspectStatus = inspectStatus;
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

    @Size(max = 500, message = "检测结果长度不能超过500个字符")
    public String getInspectResult()
    {
        return inspectResult;
    }

    public void setInspectResult(String inspectResult)
    {
        this.inspectResult = inspectResult;
    }

    public Date getInspectTime()
    {
        return inspectTime;
    }

    public void setInspectTime(Date inspectTime)
    {
        this.inspectTime = inspectTime;
    }

    @Size(max = 50, message = "模型版本长度不能超过50个字符")
    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion)
    {
        this.modelVersion = modelVersion;
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
            .append("inspectId", getInspectId())
            .append("processBatchNo", getProcessBatchNo())
            .append("sampleCode", getSampleCode())
            .append("imageUrl", getImageUrl())
            .append("inspectStatus", getInspectStatus())
            .append("qualityGrade", getQualityGrade())
            .append("defectRate", getDefectRate())
            .append("inspectResult", getInspectResult())
            .append("inspectTime", getInspectTime())
            .append("modelVersion", getModelVersion())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
