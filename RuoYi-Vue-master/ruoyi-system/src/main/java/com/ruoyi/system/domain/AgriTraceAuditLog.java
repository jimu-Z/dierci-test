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
 * 溯源审计日志对象 agri_trace_audit_log
 *
 * @author ruoyi
 */
public class AgriTraceAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long auditId;

    @Excel(name = "业务编号")
    private String bizNo;

    @Excel(name = "模块名称")
    private String moduleName;

    @Excel(name = "操作类型")
    private String actionType;

    @Excel(name = "操作人")
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    @Excel(name = "操作结果", readConverterExp = "0=失败,1=成功")
    private String operateResult;

    @Excel(name = "交易哈希")
    private String txHash;

    @Excel(name = "IP地址")
    private String ipAddress;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getAuditId()
    {
        return auditId;
    }

    public void setAuditId(Long auditId)
    {
        this.auditId = auditId;
    }

    @NotBlank(message = "业务编号不能为空")
    @Size(max = 64, message = "业务编号长度不能超过64个字符")
    public String getBizNo()
    {
        return bizNo;
    }

    public void setBizNo(String bizNo)
    {
        this.bizNo = bizNo;
    }

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 64, message = "模块名称长度不能超过64个字符")
    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    @NotBlank(message = "操作类型不能为空")
    @Size(max = 32, message = "操作类型长度不能超过32个字符")
    public String getActionType()
    {
        return actionType;
    }

    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    @Size(max = 64, message = "操作人长度不能超过64个字符")
    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    public String getOperateResult()
    {
        return operateResult;
    }

    public void setOperateResult(String operateResult)
    {
        this.operateResult = operateResult;
    }

    @Size(max = 128, message = "交易哈希长度不能超过128个字符")
    public String getTxHash()
    {
        return txHash;
    }

    public void setTxHash(String txHash)
    {
        this.txHash = txHash;
    }

    @Size(max = 64, message = "IP地址长度不能超过64个字符")
    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
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
            .append("auditId", getAuditId())
            .append("bizNo", getBizNo())
            .append("moduleName", getModuleName())
            .append("actionType", getActionType())
            .append("operatorName", getOperatorName())
            .append("operateTime", getOperateTime())
            .append("operateResult", getOperateResult())
            .append("txHash", getTxHash())
            .append("ipAddress", getIpAddress())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
