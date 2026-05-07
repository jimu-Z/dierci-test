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
 * 碳足迹核算模型对象 agri_carbon_footprint_model
 *
 * @author ruoyi
 */
public class AgriCarbonFootprintModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long modelId;

    @Excel(name = "模型编码")
    private String modelCode;

    @Excel(name = "模型名称")
    private String modelName;

    @Excel(name = "产品类型")
    private String productType;

    @Excel(name = "核算边界")
    private String boundaryScope;

    @Excel(name = "排放因子")
    private BigDecimal emissionFactor;

    @Excel(name = "碳排放量(kgCO2e)")
    private BigDecimal carbonEmission;

    @Excel(name = "核算状态", readConverterExp = "0=待计算,1=已计算,2=已复核")
    private String calcStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "核算时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;

    @Excel(name = "复核人")
    private String verifier;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    @NotBlank(message = "模型编码不能为空")
    @Size(max = 64, message = "模型编码长度不能超过64个字符")
    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    @NotBlank(message = "产品类型不能为空")
    @Size(max = 32, message = "产品类型长度不能超过32个字符")
    public String getProductType()
    {
        return productType;
    }

    public void setProductType(String productType)
    {
        this.productType = productType;
    }

    @Size(max = 255, message = "核算边界长度不能超过255个字符")
    public String getBoundaryScope()
    {
        return boundaryScope;
    }

    public void setBoundaryScope(String boundaryScope)
    {
        this.boundaryScope = boundaryScope;
    }

    @NotNull(message = "排放因子不能为空")
    public BigDecimal getEmissionFactor()
    {
        return emissionFactor;
    }

    public void setEmissionFactor(BigDecimal emissionFactor)
    {
        this.emissionFactor = emissionFactor;
    }

    public BigDecimal getCarbonEmission()
    {
        return carbonEmission;
    }

    public void setCarbonEmission(BigDecimal carbonEmission)
    {
        this.carbonEmission = carbonEmission;
    }

    public String getCalcStatus()
    {
        return calcStatus;
    }

    public void setCalcStatus(String calcStatus)
    {
        this.calcStatus = calcStatus;
    }

    public Date getCalcTime()
    {
        return calcTime;
    }

    public void setCalcTime(Date calcTime)
    {
        this.calcTime = calcTime;
    }

    @Size(max = 64, message = "复核人长度不能超过64个字符")
    public String getVerifier()
    {
        return verifier;
    }

    public void setVerifier(String verifier)
    {
        this.verifier = verifier;
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
            .append("modelId", getModelId())
            .append("modelCode", getModelCode())
            .append("modelName", getModelName())
            .append("productType", getProductType())
            .append("boundaryScope", getBoundaryScope())
            .append("emissionFactor", getEmissionFactor())
            .append("carbonEmission", getCarbonEmission())
            .append("calcStatus", getCalcStatus())
            .append("calcTime", getCalcTime())
            .append("verifier", getVerifier())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
