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
 * 病虫害识别任务对象 agri_pest_identify_task
 *
 * @author ruoyi
 */
public class AgriPestIdentifyTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long taskId;

    /** 地块编码 */
    @Excel(name = "地块编码")
    private String plotCode;

    /** 作物名称 */
    @Excel(name = "作物名称")
    private String cropName;

    /** 图片URL */
    @Excel(name = "图片URL")
    private String imageUrl;

    /** 识别状态（0待识别 1已识别 2失败） */
    @Excel(name = "识别状态", readConverterExp = "0=待识别,1=已识别,2=失败")
    private String identifyStatus;

    /** 识别结果 */
    @Excel(name = "识别结果")
    private String identifyResult;

    /** 置信度 */
    @Excel(name = "置信度")
    private BigDecimal confidence;

    /** 识别时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "识别时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date identifyTime;

    /** 模型版本 */
    @Excel(name = "模型版本")
    private String modelVersion;

    /** 数据状态（0正常 1停用） */
    @Excel(name = "数据状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    @NotBlank(message = "地块编码不能为空")
    @Size(max = 64, message = "地块编码长度不能超过64个字符")
    public String getPlotCode()
    {
        return plotCode;
    }

    public void setPlotCode(String plotCode)
    {
        this.plotCode = plotCode;
    }

    @NotBlank(message = "作物名称不能为空")
    @Size(max = 64, message = "作物名称长度不能超过64个字符")
    public String getCropName()
    {
        return cropName;
    }

    public void setCropName(String cropName)
    {
        this.cropName = cropName;
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

    public String getIdentifyStatus()
    {
        return identifyStatus;
    }

    public void setIdentifyStatus(String identifyStatus)
    {
        this.identifyStatus = identifyStatus;
    }

    @Size(max = 500, message = "识别结果长度不能超过500个字符")
    public String getIdentifyResult()
    {
        return identifyResult;
    }

    public void setIdentifyResult(String identifyResult)
    {
        this.identifyResult = identifyResult;
    }

    public BigDecimal getConfidence()
    {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence)
    {
        this.confidence = confidence;
    }

    public Date getIdentifyTime()
    {
        return identifyTime;
    }

    public void setIdentifyTime(Date identifyTime)
    {
        this.identifyTime = identifyTime;
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
            .append("taskId", getTaskId())
            .append("plotCode", getPlotCode())
            .append("cropName", getCropName())
            .append("imageUrl", getImageUrl())
            .append("identifyStatus", getIdentifyStatus())
            .append("identifyResult", getIdentifyResult())
            .append("confidence", getConfidence())
            .append("identifyTime", getIdentifyTime())
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
