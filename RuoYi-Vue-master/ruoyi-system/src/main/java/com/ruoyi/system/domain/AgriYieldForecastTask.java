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
 * 产量预测任务对象 agri_yield_forecast_task
 *
 * @author ruoyi
 */
public class AgriYieldForecastTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long forecastId;

    @Excel(name = "地块编码")
    private String plotCode;

    @Excel(name = "作物名称")
    private String cropName;

    @Excel(name = "季节")
    private String season;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "播种日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date sowDate;

    @Excel(name = "种植面积(亩)")
    private BigDecimal areaMu;

    @Excel(name = "预测产量(kg)")
    private BigDecimal forecastYieldKg;

    @Excel(name = "模型版本")
    private String modelVersion;

    @Excel(name = "预测状态", readConverterExp = "0=待预测,1=已预测,2=失败")
    private String forecastStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预测时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date forecastTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getForecastId()
    {
        return forecastId;
    }

    public void setForecastId(Long forecastId)
    {
        this.forecastId = forecastId;
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

    @NotBlank(message = "季节不能为空")
    @Size(max = 30, message = "季节长度不能超过30个字符")
    public String getSeason()
    {
        return season;
    }

    public void setSeason(String season)
    {
        this.season = season;
    }

    @NotNull(message = "播种日期不能为空")
    public Date getSowDate()
    {
        return sowDate;
    }

    public void setSowDate(Date sowDate)
    {
        this.sowDate = sowDate;
    }

    @NotNull(message = "种植面积不能为空")
    public BigDecimal getAreaMu()
    {
        return areaMu;
    }

    public void setAreaMu(BigDecimal areaMu)
    {
        this.areaMu = areaMu;
    }

    public BigDecimal getForecastYieldKg()
    {
        return forecastYieldKg;
    }

    public void setForecastYieldKg(BigDecimal forecastYieldKg)
    {
        this.forecastYieldKg = forecastYieldKg;
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

    public String getForecastStatus()
    {
        return forecastStatus;
    }

    public void setForecastStatus(String forecastStatus)
    {
        this.forecastStatus = forecastStatus;
    }

    public Date getForecastTime()
    {
        return forecastTime;
    }

    public void setForecastTime(Date forecastTime)
    {
        this.forecastTime = forecastTime;
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
            .append("forecastId", getForecastId())
            .append("plotCode", getPlotCode())
            .append("cropName", getCropName())
            .append("season", getSeason())
            .append("sowDate", getSowDate())
            .append("areaMu", getAreaMu())
            .append("forecastYieldKg", getForecastYieldKg())
            .append("modelVersion", getModelVersion())
            .append("forecastStatus", getForecastStatus())
            .append("forecastTime", getForecastTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
