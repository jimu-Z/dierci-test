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
 * 产量与销量趋势图对象 agri_output_sales_trend
 *
 * @author ruoyi
 */
public class AgriOutputSalesTrend extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long trendId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date statDate;

    @Excel(name = "产量(吨)")
    private BigDecimal outputValue;

    @Excel(name = "销量(万元)")
    private BigDecimal salesValue;

    @Excel(name = "目标产量(吨)")
    private BigDecimal targetOutput;

    @Excel(name = "目标销量(万元)")
    private BigDecimal targetSales;

    @Excel(name = "环比产量增幅(%)")
    private BigDecimal outputMomRate;

    @Excel(name = "环比销量增幅(%)")
    private BigDecimal salesMomRate;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getTrendId()
    {
        return trendId;
    }

    public void setTrendId(Long trendId)
    {
        this.trendId = trendId;
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

    @NotNull(message = "产量不能为空")
    public BigDecimal getOutputValue()
    {
        return outputValue;
    }

    public void setOutputValue(BigDecimal outputValue)
    {
        this.outputValue = outputValue;
    }

    @NotNull(message = "销量不能为空")
    public BigDecimal getSalesValue()
    {
        return salesValue;
    }

    public void setSalesValue(BigDecimal salesValue)
    {
        this.salesValue = salesValue;
    }

    public BigDecimal getTargetOutput()
    {
        return targetOutput;
    }

    public void setTargetOutput(BigDecimal targetOutput)
    {
        this.targetOutput = targetOutput;
    }

    public BigDecimal getTargetSales()
    {
        return targetSales;
    }

    public void setTargetSales(BigDecimal targetSales)
    {
        this.targetSales = targetSales;
    }

    public BigDecimal getOutputMomRate()
    {
        return outputMomRate;
    }

    public void setOutputMomRate(BigDecimal outputMomRate)
    {
        this.outputMomRate = outputMomRate;
    }

    public BigDecimal getSalesMomRate()
    {
        return salesMomRate;
    }

    public void setSalesMomRate(BigDecimal salesMomRate)
    {
        this.salesMomRate = salesMomRate;
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
            .append("trendId", getTrendId())
            .append("statDate", getStatDate())
            .append("outputValue", getOutputValue())
            .append("salesValue", getSalesValue())
            .append("targetOutput", getTargetOutput())
            .append("targetSales", getTargetSales())
            .append("outputMomRate", getOutputMomRate())
            .append("salesMomRate", getSalesMomRate())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
