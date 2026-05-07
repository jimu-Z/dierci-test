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
 * 农业金融风控指标对象 agri_finance_risk_metric
 *
 * @author ruoyi
 */
public class AgriFinanceRiskMetric extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long riskId;

    @Excel(name = "指标编码")
    private String indicatorCode;

    @Excel(name = "指标名称")
    private String indicatorName;

    @Excel(name = "风险维度")
    private String riskDimension;

    @Excel(name = "风险分值")
    private BigDecimal riskScore;

    @Excel(name = "阈值")
    private BigDecimal thresholdValue;

    @Excel(name = "风险等级", readConverterExp = "L=低,M=中,H=高,C=严重")
    private String riskLevel;

    @Excel(name = "评估状态", readConverterExp = "0=待评估,1=已评估,2=已预警")
    private String evaluateStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "评估时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date evaluateTime;

    @Excel(name = "评估人")
    private String evaluator;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getRiskId()
    {
        return riskId;
    }

    public void setRiskId(Long riskId)
    {
        this.riskId = riskId;
    }

    @NotBlank(message = "指标编码不能为空")
    @Size(max = 64, message = "指标编码长度不能超过64个字符")
    public String getIndicatorCode()
    {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode)
    {
        this.indicatorCode = indicatorCode;
    }

    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    public String getIndicatorName()
    {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName)
    {
        this.indicatorName = indicatorName;
    }

    @NotBlank(message = "风险维度不能为空")
    @Size(max = 32, message = "风险维度长度不能超过32个字符")
    public String getRiskDimension()
    {
        return riskDimension;
    }

    public void setRiskDimension(String riskDimension)
    {
        this.riskDimension = riskDimension;
    }

    @NotNull(message = "风险分值不能为空")
    public BigDecimal getRiskScore()
    {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore)
    {
        this.riskScore = riskScore;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue)
    {
        this.thresholdValue = thresholdValue;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getEvaluateStatus()
    {
        return evaluateStatus;
    }

    public void setEvaluateStatus(String evaluateStatus)
    {
        this.evaluateStatus = evaluateStatus;
    }

    public Date getEvaluateTime()
    {
        return evaluateTime;
    }

    public void setEvaluateTime(Date evaluateTime)
    {
        this.evaluateTime = evaluateTime;
    }

    @Size(max = 64, message = "评估人长度不能超过64个字符")
    public String getEvaluator()
    {
        return evaluator;
    }

    public void setEvaluator(String evaluator)
    {
        this.evaluator = evaluator;
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
            .append("riskId", getRiskId())
            .append("indicatorCode", getIndicatorCode())
            .append("indicatorName", getIndicatorName())
            .append("riskDimension", getRiskDimension())
            .append("riskScore", getRiskScore())
            .append("thresholdValue", getThresholdValue())
            .append("riskLevel", getRiskLevel())
            .append("evaluateStatus", getEvaluateStatus())
            .append("evaluateTime", getEvaluateTime())
            .append("evaluator", getEvaluator())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
