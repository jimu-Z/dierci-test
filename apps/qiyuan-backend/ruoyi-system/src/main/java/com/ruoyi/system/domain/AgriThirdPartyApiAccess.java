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
 * 第三方API接入对象 agri_third_party_api_access
 *
 * @author ruoyi
 */
public class AgriThirdPartyApiAccess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long accessId;

    @Excel(name = "接入编码")
    private String accessCode;

    @Excel(name = "接入名称")
    private String accessName;

    @Excel(name = "API类型")
    private String apiType;

    @Excel(name = "供应商")
    private String provider;

    @Excel(name = "请求地址")
    private String endpointUrl;

    @Excel(name = "超时(秒)")
    private Integer timeoutSec;

    @Excel(name = "成功率(%)")
    private BigDecimal successRate;

    @Excel(name = "调用状态", readConverterExp = "0=未调用,1=成功,2=失败")
    private String callStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近调用时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastCallTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getAccessId()
    {
        return accessId;
    }

    public void setAccessId(Long accessId)
    {
        this.accessId = accessId;
    }

    @NotBlank(message = "接入编码不能为空")
    @Size(max = 64, message = "接入编码长度不能超过64个字符")
    public String getAccessCode()
    {
        return accessCode;
    }

    public void setAccessCode(String accessCode)
    {
        this.accessCode = accessCode;
    }

    @NotBlank(message = "接入名称不能为空")
    @Size(max = 100, message = "接入名称长度不能超过100个字符")
    public String getAccessName()
    {
        return accessName;
    }

    public void setAccessName(String accessName)
    {
        this.accessName = accessName;
    }

    @NotBlank(message = "API类型不能为空")
    @Size(max = 32, message = "API类型长度不能超过32个字符")
    public String getApiType()
    {
        return apiType;
    }

    public void setApiType(String apiType)
    {
        this.apiType = apiType;
    }

    @Size(max = 64, message = "供应商长度不能超过64个字符")
    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    @NotBlank(message = "请求地址不能为空")
    @Size(max = 255, message = "请求地址长度不能超过255个字符")
    public String getEndpointUrl()
    {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl)
    {
        this.endpointUrl = endpointUrl;
    }

    @NotNull(message = "超时不能为空")
    public Integer getTimeoutSec()
    {
        return timeoutSec;
    }

    public void setTimeoutSec(Integer timeoutSec)
    {
        this.timeoutSec = timeoutSec;
    }

    public BigDecimal getSuccessRate()
    {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate)
    {
        this.successRate = successRate;
    }

    public String getCallStatus()
    {
        return callStatus;
    }

    public void setCallStatus(String callStatus)
    {
        this.callStatus = callStatus;
    }

    public Date getLastCallTime()
    {
        return lastCallTime;
    }

    public void setLastCallTime(Date lastCallTime)
    {
        this.lastCallTime = lastCallTime;
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
            .append("accessId", getAccessId())
            .append("accessCode", getAccessCode())
            .append("accessName", getAccessName())
            .append("apiType", getApiType())
            .append("provider", getProvider())
            .append("endpointUrl", getEndpointUrl())
            .append("timeoutSec", getTimeoutSec())
            .append("successRate", getSuccessRate())
            .append("callStatus", getCallStatus())
            .append("lastCallTime", getLastCallTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
