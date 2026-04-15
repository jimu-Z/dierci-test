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
 * 物流温湿度监控对象 agri_logistics_temp_humidity
 *
 * @author ruoyi
 */
public class AgriLogisticsTempHumidity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long recordId;

    @Excel(name = "运单号")
    private String traceCode;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "设备编码")
    private String deviceCode;

    @Excel(name = "采集位置")
    private String collectLocation;

    @Excel(name = "温度(°C)")
    private BigDecimal temperature;

    @Excel(name = "湿度(%)")
    private BigDecimal humidity;

    @Excel(name = "温度上限")
    private BigDecimal tempUpperLimit;

    @Excel(name = "温度下限")
    private BigDecimal tempLowerLimit;

    @Excel(name = "湿度上限")
    private BigDecimal humidityUpperLimit;

    @Excel(name = "湿度下限")
    private BigDecimal humidityLowerLimit;

    @Excel(name = "告警标记", readConverterExp = "0=正常,1=超阈值")
    private String alertFlag;

    @Excel(name = "告警信息")
    private String alertMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "采集时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date collectTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    @NotBlank(message = "运单号不能为空")
    @Size(max = 64, message = "运单号长度不能超过64个字符")
    public String getTraceCode()
    {
        return traceCode;
    }

    public void setTraceCode(String traceCode)
    {
        this.traceCode = traceCode;
    }

    @NotBlank(message = "订单号不能为空")
    @Size(max = 64, message = "订单号长度不能超过64个字符")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    @NotBlank(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码长度不能超过64个字符")
    public String getDeviceCode()
    {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode)
    {
        this.deviceCode = deviceCode;
    }

    @Size(max = 128, message = "采集位置长度不能超过128个字符")
    public String getCollectLocation()
    {
        return collectLocation;
    }

    public void setCollectLocation(String collectLocation)
    {
        this.collectLocation = collectLocation;
    }

    @NotNull(message = "温度不能为空")
    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    @NotNull(message = "湿度不能为空")
    public BigDecimal getHumidity()
    {
        return humidity;
    }

    public void setHumidity(BigDecimal humidity)
    {
        this.humidity = humidity;
    }

    public BigDecimal getTempUpperLimit()
    {
        return tempUpperLimit;
    }

    public void setTempUpperLimit(BigDecimal tempUpperLimit)
    {
        this.tempUpperLimit = tempUpperLimit;
    }

    public BigDecimal getTempLowerLimit()
    {
        return tempLowerLimit;
    }

    public void setTempLowerLimit(BigDecimal tempLowerLimit)
    {
        this.tempLowerLimit = tempLowerLimit;
    }

    public BigDecimal getHumidityUpperLimit()
    {
        return humidityUpperLimit;
    }

    public void setHumidityUpperLimit(BigDecimal humidityUpperLimit)
    {
        this.humidityUpperLimit = humidityUpperLimit;
    }

    public BigDecimal getHumidityLowerLimit()
    {
        return humidityLowerLimit;
    }

    public void setHumidityLowerLimit(BigDecimal humidityLowerLimit)
    {
        this.humidityLowerLimit = humidityLowerLimit;
    }

    public String getAlertFlag()
    {
        return alertFlag;
    }

    public void setAlertFlag(String alertFlag)
    {
        this.alertFlag = alertFlag;
    }

    @Size(max = 500, message = "告警信息长度不能超过500个字符")
    public String getAlertMessage()
    {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage)
    {
        this.alertMessage = alertMessage;
    }

    @NotNull(message = "采集时间不能为空")
    public Date getCollectTime()
    {
        return collectTime;
    }

    public void setCollectTime(Date collectTime)
    {
        this.collectTime = collectTime;
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
            .append("recordId", getRecordId())
            .append("traceCode", getTraceCode())
            .append("orderNo", getOrderNo())
            .append("deviceCode", getDeviceCode())
            .append("collectLocation", getCollectLocation())
            .append("temperature", getTemperature())
            .append("humidity", getHumidity())
            .append("tempUpperLimit", getTempUpperLimit())
            .append("tempLowerLimit", getTempLowerLimit())
            .append("humidityUpperLimit", getHumidityUpperLimit())
            .append("humidityLowerLimit", getHumidityLowerLimit())
            .append("alertFlag", getAlertFlag())
            .append("alertMessage", getAlertMessage())
            .append("collectTime", getCollectTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
