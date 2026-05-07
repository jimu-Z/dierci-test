package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 设备接入管理对象 agri_device_access_node
 *
 * @author ruoyi
 */
public class AgriDeviceAccessNode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long nodeId;

    @Excel(name = "设备编码")
    private String deviceCode;

    @Excel(name = "设备名称")
    private String deviceName;

    @Excel(name = "设备类型")
    private String deviceType;

    @Excel(name = "接入协议")
    private String protocolType;

    @Excel(name = "固件版本")
    private String firmwareVersion;

    @Excel(name = "绑定区域")
    private String bindArea;

    @Excel(name = "接入状态", readConverterExp = "0=待接入,1=在线,2=离线,3=异常")
    private String accessStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近在线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
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

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 64, message = "设备名称长度不能超过64个字符")
    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    @NotBlank(message = "设备类型不能为空")
    @Size(max = 32, message = "设备类型长度不能超过32个字符")
    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    @NotBlank(message = "接入协议不能为空")
    @Size(max = 32, message = "接入协议长度不能超过32个字符")
    public String getProtocolType()
    {
        return protocolType;
    }

    public void setProtocolType(String protocolType)
    {
        this.protocolType = protocolType;
    }

    @Size(max = 32, message = "固件版本长度不能超过32个字符")
    public String getFirmwareVersion()
    {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion)
    {
        this.firmwareVersion = firmwareVersion;
    }

    @Size(max = 128, message = "绑定区域长度不能超过128个字符")
    public String getBindArea()
    {
        return bindArea;
    }

    public void setBindArea(String bindArea)
    {
        this.bindArea = bindArea;
    }

    public String getAccessStatus()
    {
        return accessStatus;
    }

    public void setAccessStatus(String accessStatus)
    {
        this.accessStatus = accessStatus;
    }

    public Date getLastOnlineTime()
    {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(Date lastOnlineTime)
    {
        this.lastOnlineTime = lastOnlineTime;
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
            .append("nodeId", getNodeId())
            .append("deviceCode", getDeviceCode())
            .append("deviceName", getDeviceName())
            .append("deviceType", getDeviceType())
            .append("protocolType", getProtocolType())
            .append("firmwareVersion", getFirmwareVersion())
            .append("bindArea", getBindArea())
            .append("accessStatus", getAccessStatus())
            .append("lastOnlineTime", getLastOnlineTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
