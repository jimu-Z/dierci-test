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
 * 农事记录对象 agri_farm_operation_record
 *
 * @author ruoyi
 */
public class AgriFarmOperationRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long operationId;

    /** 地块编码 */
    @Excel(name = "地块编码")
    private String plotCode;

    /** 作业类型 */
    @Excel(name = "作业类型")
    private String operationType;

    /** 作业内容 */
    @Excel(name = "作业内容")
    private String operationContent;

    /** 作业人 */
    @Excel(name = "作业人")
    private String operatorName;

    /** 投入品名称 */
    @Excel(name = "投入品名称")
    private String inputName;

    /** 投入品用量 */
    @Excel(name = "投入品用量")
    private BigDecimal inputAmount;

    /** 用量单位 */
    @Excel(name = "单位")
    private String inputUnit;

    /** 作业时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "作业时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getOperationId()
    {
        return operationId;
    }

    public void setOperationId(Long operationId)
    {
        this.operationId = operationId;
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

    @NotBlank(message = "作业类型不能为空")
    @Size(max = 50, message = "作业类型长度不能超过50个字符")
    public String getOperationType()
    {
        return operationType;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    @NotBlank(message = "作业内容不能为空")
    @Size(max = 500, message = "作业内容长度不能超过500个字符")
    public String getOperationContent()
    {
        return operationContent;
    }

    public void setOperationContent(String operationContent)
    {
        this.operationContent = operationContent;
    }

    @NotBlank(message = "作业人不能为空")
    @Size(max = 64, message = "作业人长度不能超过64个字符")
    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    @Size(max = 100, message = "投入品名称长度不能超过100个字符")
    public String getInputName()
    {
        return inputName;
    }

    public void setInputName(String inputName)
    {
        this.inputName = inputName;
    }

    public BigDecimal getInputAmount()
    {
        return inputAmount;
    }

    public void setInputAmount(BigDecimal inputAmount)
    {
        this.inputAmount = inputAmount;
    }

    @Size(max = 20, message = "单位长度不能超过20个字符")
    public String getInputUnit()
    {
        return inputUnit;
    }

    public void setInputUnit(String inputUnit)
    {
        this.inputUnit = inputUnit;
    }

    @NotNull(message = "作业时间不能为空")
    public Date getOperationTime()
    {
        return operationTime;
    }

    public void setOperationTime(Date operationTime)
    {
        this.operationTime = operationTime;
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
            .append("operationId", getOperationId())
            .append("plotCode", getPlotCode())
            .append("operationType", getOperationType())
            .append("operationContent", getOperationContent())
            .append("operatorName", getOperatorName())
            .append("inputName", getInputName())
            .append("inputAmount", getInputAmount())
            .append("inputUnit", getInputUnit())
            .append("operationTime", getOperationTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
