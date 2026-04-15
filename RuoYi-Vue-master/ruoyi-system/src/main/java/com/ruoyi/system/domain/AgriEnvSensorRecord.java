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
 * 环境传感器数据对象 agri_env_sensor_record
 *
 * @author ruoyi
 */
public class AgriEnvSensorRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long recordId;

    /** 设备编码 */
    @Excel(name = "设备编码")
    private String deviceCode;

    /** 地块编码 */
    @Excel(name = "地块编码")
    private String plotCode;

    /** 温度 */
    @Excel(name = "温度(℃)")
    private BigDecimal temperature;

    /** 湿度 */
    @Excel(name = "湿度(%)")
    private BigDecimal humidity;

    /** 二氧化碳浓度 */
    @Excel(name = "CO2(ppm)")
    private BigDecimal co2Value;

    /** 采集状态（0正常 1预警） */
    @Excel(name = "采集状态", readConverterExp = "0=正常,1=预警")
    private String status;

    /** 数据来源 */
    @Excel(name = "数据来源")
    private String dataSource;

    /** 采集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "采集时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date collectTime;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
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

    public BigDecimal getCo2Value()
    {
        return co2Value;
    }

    public void setCo2Value(BigDecimal co2Value)
    {
        this.co2Value = co2Value;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Size(max = 32, message = "数据来源长度不能超过32个字符")
    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("deviceCode", getDeviceCode())
            .append("plotCode", getPlotCode())
            .append("temperature", getTemperature())
            .append("humidity", getHumidity())
            .append("co2Value", getCo2Value())
            .append("status", getStatus())
            .append("dataSource", getDataSource())
            .append("collectTime", getCollectTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
