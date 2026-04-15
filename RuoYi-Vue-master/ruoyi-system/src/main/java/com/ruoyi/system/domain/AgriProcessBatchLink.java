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
 * 加工批次关联对象 agri_process_batch_link
 *
 * @author ruoyi
 */
public class AgriProcessBatchLink extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long linkId;

    @Excel(name = "种植批次号")
    private String plantingBatchNo;

    @Excel(name = "加工批次号")
    private String processBatchNo;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "加工重量(kg)")
    private BigDecimal processWeightKg;

    @Excel(name = "加工状态", readConverterExp = "0=待加工,1=加工中,2=已完成")
    private String processStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "加工时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date processTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getLinkId()
    {
        return linkId;
    }

    public void setLinkId(Long linkId)
    {
        this.linkId = linkId;
    }

    @NotBlank(message = "种植批次号不能为空")
    @Size(max = 64, message = "种植批次号长度不能超过64个字符")
    public String getPlantingBatchNo()
    {
        return plantingBatchNo;
    }

    public void setPlantingBatchNo(String plantingBatchNo)
    {
        this.plantingBatchNo = plantingBatchNo;
    }

    @NotBlank(message = "加工批次号不能为空")
    @Size(max = 64, message = "加工批次号长度不能超过64个字符")
    public String getProcessBatchNo()
    {
        return processBatchNo;
    }

    public void setProcessBatchNo(String processBatchNo)
    {
        this.processBatchNo = processBatchNo;
    }

    @NotBlank(message = "产品编码不能为空")
    @Size(max = 64, message = "产品编码长度不能超过64个字符")
    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    @NotNull(message = "加工重量不能为空")
    public BigDecimal getProcessWeightKg()
    {
        return processWeightKg;
    }

    public void setProcessWeightKg(BigDecimal processWeightKg)
    {
        this.processWeightKg = processWeightKg;
    }

    public String getProcessStatus()
    {
        return processStatus;
    }

    public void setProcessStatus(String processStatus)
    {
        this.processStatus = processStatus;
    }

    public Date getProcessTime()
    {
        return processTime;
    }

    public void setProcessTime(Date processTime)
    {
        this.processTime = processTime;
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
            .append("linkId", getLinkId())
            .append("plantingBatchNo", getPlantingBatchNo())
            .append("processBatchNo", getProcessBatchNo())
            .append("productCode", getProductCode())
            .append("processWeightKg", getProcessWeightKg())
            .append("processStatus", getProcessStatus())
            .append("processTime", getProcessTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
