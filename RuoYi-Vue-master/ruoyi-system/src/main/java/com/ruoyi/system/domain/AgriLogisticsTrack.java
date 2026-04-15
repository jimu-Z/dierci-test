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
 * 物流路径追踪对象 agri_logistics_track
 *
 * @author ruoyi
 */
public class AgriLogisticsTrack extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long trackId;

    @Excel(name = "运单号")
    private String traceCode;

    @Excel(name = "销售订单号")
    private String orderNo;

    @Excel(name = "产品批次号")
    private String productBatchNo;

    @Excel(name = "车辆编号")
    private String vehicleNo;

    @Excel(name = "司机姓名")
    private String driverName;

    @Excel(name = "司机电话")
    private String driverPhone;

    @Excel(name = "起始地")
    private String startLocation;

    @Excel(name = "当前位置")
    private String currentLocation;

    @Excel(name = "目的地")
    private String targetLocation;

    @Excel(name = "路线轨迹")
    private String routePath;

    @Excel(name = "运输状态", readConverterExp = "0=待发车,1=运输中,2=已签收,3=异常")
    private String trackStatus;

    @Excel(name = "轨迹描述")
    private String eventDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "事件时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    @Excel(name = "温度(°C)")
    private BigDecimal temperature;

    @Excel(name = "湿度(%)")
    private BigDecimal humidity;

    @Excel(name = "经度")
    private BigDecimal longitude;

    @Excel(name = "纬度")
    private BigDecimal latitude;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getTrackId()
    {
        return trackId;
    }

    public void setTrackId(Long trackId)
    {
        this.trackId = trackId;
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

    @NotBlank(message = "销售订单号不能为空")
    @Size(max = 64, message = "销售订单号长度不能超过64个字符")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    @NotBlank(message = "产品批次号不能为空")
    @Size(max = 64, message = "产品批次号长度不能超过64个字符")
    public String getProductBatchNo()
    {
        return productBatchNo;
    }

    public void setProductBatchNo(String productBatchNo)
    {
        this.productBatchNo = productBatchNo;
    }

    @Size(max = 64, message = "车辆编号长度不能超过64个字符")
    public String getVehicleNo()
    {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo)
    {
        this.vehicleNo = vehicleNo;
    }

    @Size(max = 64, message = "司机姓名长度不能超过64个字符")
    public String getDriverName()
    {
        return driverName;
    }

    public void setDriverName(String driverName)
    {
        this.driverName = driverName;
    }

    @Size(max = 20, message = "司机电话长度不能超过20个字符")
    public String getDriverPhone()
    {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone)
    {
        this.driverPhone = driverPhone;
    }

    @Size(max = 128, message = "起始地长度不能超过128个字符")
    public String getStartLocation()
    {
        return startLocation;
    }

    public void setStartLocation(String startLocation)
    {
        this.startLocation = startLocation;
    }

    @NotBlank(message = "当前位置不能为空")
    @Size(max = 128, message = "当前位置长度不能超过128个字符")
    public String getCurrentLocation()
    {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation)
    {
        this.currentLocation = currentLocation;
    }

    @Size(max = 128, message = "目的地长度不能超过128个字符")
    public String getTargetLocation()
    {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation)
    {
        this.targetLocation = targetLocation;
    }

    @Size(max = 1000, message = "路线轨迹长度不能超过1000个字符")
    public String getRoutePath()
    {
        return routePath;
    }

    public void setRoutePath(String routePath)
    {
        this.routePath = routePath;
    }

    @NotNull(message = "运输状态不能为空")
    public String getTrackStatus()
    {
        return trackStatus;
    }

    public void setTrackStatus(String trackStatus)
    {
        this.trackStatus = trackStatus;
    }

    @Size(max = 500, message = "轨迹描述长度不能超过500个字符")
    public String getEventDesc()
    {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc)
    {
        this.eventDesc = eventDesc;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
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

    public BigDecimal getLongitude()
    {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude()
    {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
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
            .append("trackId", getTrackId())
            .append("traceCode", getTraceCode())
            .append("orderNo", getOrderNo())
            .append("productBatchNo", getProductBatchNo())
            .append("vehicleNo", getVehicleNo())
            .append("driverName", getDriverName())
            .append("driverPhone", getDriverPhone())
            .append("startLocation", getStartLocation())
            .append("currentLocation", getCurrentLocation())
            .append("targetLocation", getTargetLocation())
            .append("routePath", getRoutePath())
            .append("trackStatus", getTrackStatus())
            .append("eventDesc", getEventDesc())
            .append("eventTime", getEventTime())
            .append("temperature", getTemperature())
            .append("humidity", getHumidity())
            .append("longitude", getLongitude())
            .append("latitude", getLatitude())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
