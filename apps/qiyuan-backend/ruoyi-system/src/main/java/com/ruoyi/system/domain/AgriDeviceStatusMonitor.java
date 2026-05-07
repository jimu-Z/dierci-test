package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 设备状态监控对象 agri_device_status_monitor
 *
 * @author ruoyi
 */
public class AgriDeviceStatusMonitor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long monitorId;

    @Excel(name = "设备编码")
    private String deviceCode;

    @Excel(name = "设备名称")
    private String deviceName;

    @Excel(name = "设备类型")
    private String deviceType;

    @Excel(name = "在线状态", readConverterExp = "0=离线,1=在线")
    private String onlineStatus;

    @Excel(name = "电量(%)")
    private BigDecimal batteryLevel;

    @Excel(name = "信号强度")
    private BigDecimal signalStrength;

    @Excel(name = "温度(℃)")
    private BigDecimal temperature;

    @Excel(name = "湿度(%)")
    private BigDecimal humidity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后上报时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastReportTime;

    @Excel(name = "预警等级", readConverterExp = "0=正常,1=提示,2=预警,3=严重")
    private String warningLevel;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getMonitorId()
    {
        return monitorId;
    }

    public void setMonitorId(Long monitorId)
    {
        this.monitorId = monitorId;
    }

    @NotBlank(message = "设备编码不能为空")
    public String getDeviceCode()
    {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode)
    {
        this.deviceCode = deviceCode;
    }

    @NotBlank(message = "设备名称不能为空")
    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    @NotBlank(message = "在线状态不能为空")
    public String getOnlineStatus()
    {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus)
    {
        this.onlineStatus = onlineStatus;
    }

    @NotNull(message = "电量不能为空")
    public BigDecimal getBatteryLevel()
    {
        return batteryLevel;
    }

    public void setBatteryLevel(BigDecimal batteryLevel)
    {
        this.batteryLevel = batteryLevel;
    }

    public BigDecimal getSignalStrength()
    {
        return signalStrength;
    }

    public void setSignalStrength(BigDecimal signalStrength)
    {
        this.signalStrength = signalStrength;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    public BigDecimal getHumidity()
    {
        return humidity;
    }

    public void setHumidity(BigDecimal humidity)
    {
        this.humidity = humidity;
    }

    public Date getLastReportTime()
    {
        return lastReportTime;
    }

    public void setLastReportTime(Date lastReportTime)
    {
        this.lastReportTime = lastReportTime;
    }

    public String getWarningLevel()
    {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel)
    {
        this.warningLevel = warningLevel;
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
            .append("monitorId", getMonitorId())
            .append("deviceCode", getDeviceCode())
            .append("deviceName", getDeviceName())
            .append("deviceType", getDeviceType())
            .append("onlineStatus", getOnlineStatus())
            .append("batteryLevel", getBatteryLevel())
            .append("signalStrength", getSignalStrength())
            .append("temperature", getTemperature())
            .append("humidity", getHumidity())
            .append("lastReportTime", getLastReportTime())
            .append("warningLevel", getWarningLevel())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
