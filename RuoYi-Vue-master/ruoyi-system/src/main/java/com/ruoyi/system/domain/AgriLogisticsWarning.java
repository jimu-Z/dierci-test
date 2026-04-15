package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 在途异常预警对象 agri_logistics_warning
 *
 * @author ruoyi
 */
public class AgriLogisticsWarning extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long warningId;

    @Excel(name = "运单号")
    private String traceCode;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "预警类型")
    private String warningType;

    @Excel(name = "预警级别", readConverterExp = "1=一般,2=严重,3=紧急")
    private String warningLevel;

    @Excel(name = "预警状态", readConverterExp = "0=待处理,1=处理中,2=已关闭")
    private String warningStatus;

    @Excel(name = "来源记录ID", cellType = ColumnType.NUMERIC)
    private Long sourceRecordId;

    @Excel(name = "预警标题")
    private String warningTitle;

    @Excel(name = "预警内容")
    private String warningContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预警时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date warningTime;

    @Excel(name = "处理人")
    private String handler;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    @Excel(name = "处理备注")
    private String handleRemark;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getWarningId()
    {
        return warningId;
    }

    public void setWarningId(Long warningId)
    {
        this.warningId = warningId;
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

    @NotBlank(message = "预警类型不能为空")
    @Size(max = 50, message = "预警类型长度不能超过50个字符")
    public String getWarningType()
    {
        return warningType;
    }

    public void setWarningType(String warningType)
    {
        this.warningType = warningType;
    }

    @NotNull(message = "预警级别不能为空")
    public String getWarningLevel()
    {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel)
    {
        this.warningLevel = warningLevel;
    }

    public String getWarningStatus()
    {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus)
    {
        this.warningStatus = warningStatus;
    }

    public Long getSourceRecordId()
    {
        return sourceRecordId;
    }

    public void setSourceRecordId(Long sourceRecordId)
    {
        this.sourceRecordId = sourceRecordId;
    }

    @NotBlank(message = "预警标题不能为空")
    @Size(max = 100, message = "预警标题长度不能超过100个字符")
    public String getWarningTitle()
    {
        return warningTitle;
    }

    public void setWarningTitle(String warningTitle)
    {
        this.warningTitle = warningTitle;
    }

    @NotBlank(message = "预警内容不能为空")
    @Size(max = 1000, message = "预警内容长度不能超过1000个字符")
    public String getWarningContent()
    {
        return warningContent;
    }

    public void setWarningContent(String warningContent)
    {
        this.warningContent = warningContent;
    }

    @NotNull(message = "预警时间不能为空")
    public Date getWarningTime()
    {
        return warningTime;
    }

    public void setWarningTime(Date warningTime)
    {
        this.warningTime = warningTime;
    }

    @Size(max = 64, message = "处理人长度不能超过64个字符")
    public String getHandler()
    {
        return handler;
    }

    public void setHandler(String handler)
    {
        this.handler = handler;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    public String getHandleRemark()
    {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark)
    {
        this.handleRemark = handleRemark;
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
            .append("warningId", getWarningId())
            .append("traceCode", getTraceCode())
            .append("orderNo", getOrderNo())
            .append("warningType", getWarningType())
            .append("warningLevel", getWarningLevel())
            .append("warningStatus", getWarningStatus())
            .append("sourceRecordId", getSourceRecordId())
            .append("warningTitle", getWarningTitle())
            .append("warningContent", getWarningContent())
            .append("warningTime", getWarningTime())
            .append("handler", getHandler())
            .append("handleTime", getHandleTime())
            .append("handleRemark", getHandleRemark())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
