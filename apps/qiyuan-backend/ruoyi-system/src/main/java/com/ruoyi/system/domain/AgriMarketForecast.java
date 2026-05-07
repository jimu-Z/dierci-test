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
 * 市场预测分析对象 agri_market_forecast
 *
 * @author ruoyi
 */
public class AgriMarketForecast extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long forecastId;

    @Excel(name = "市场区域")
    private String marketArea;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预测周期开始", width = 20, dateFormat = "yyyy-MM-dd")
    private Date periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预测周期结束", width = 20, dateFormat = "yyyy-MM-dd")
    private Date periodEnd;

    @Excel(name = "历史销量(kg)")
    private BigDecimal historicalSalesKg;

    @Excel(name = "预测销量(kg)")
    private BigDecimal forecastSalesKg;

    @Excel(name = "预测价格(元/kg)")
    private BigDecimal forecastPrice;

    @Excel(name = "置信度")
    private BigDecimal confidenceRate;

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

    @NotBlank(message = "市场区域不能为空")
    @Size(max = 64, message = "市场区域长度不能超过64个字符")
    public String getMarketArea()
    {
        return marketArea;
    }

    public void setMarketArea(String marketArea)
    {
        this.marketArea = marketArea;
    }

    @NotBlank(message = "产品编码不能为空")
    @Size(max = 64, message = "产品编码长度不能超过64个字符")
    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    @NotBlank(message = "产品名称不能为空")
    @Size(max = 64, message = "产品名称长度不能超过64个字符")
    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    @NotNull(message = "预测周期开始不能为空")
    public Date getPeriodStart()
    {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart)
    {
        this.periodStart = periodStart;
    }

    @NotNull(message = "预测周期结束不能为空")
    public Date getPeriodEnd()
    {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd)
    {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getHistoricalSalesKg()
    {
        return historicalSalesKg;
    }

    public void setHistoricalSalesKg(BigDecimal historicalSalesKg)
    {
        this.historicalSalesKg = historicalSalesKg;
    }

    public BigDecimal getForecastSalesKg()
    {
        return forecastSalesKg;
    }

    public void setForecastSalesKg(BigDecimal forecastSalesKg)
    {
        this.forecastSalesKg = forecastSalesKg;
    }

    public BigDecimal getForecastPrice()
    {
        return forecastPrice;
    }

    public void setForecastPrice(BigDecimal forecastPrice)
    {
        this.forecastPrice = forecastPrice;
    }

    public BigDecimal getConfidenceRate()
    {
        return confidenceRate;
    }

    public void setConfidenceRate(BigDecimal confidenceRate)
    {
        this.confidenceRate = confidenceRate;
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
            .append("marketArea", getMarketArea())
            .append("productCode", getProductCode())
            .append("productName", getProductName())
            .append("periodStart", getPeriodStart())
            .append("periodEnd", getPeriodEnd())
            .append("historicalSalesKg", getHistoricalSalesKg())
            .append("forecastSalesKg", getForecastSalesKg())
            .append("forecastPrice", getForecastPrice())
            .append("confidenceRate", getConfidenceRate())
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
