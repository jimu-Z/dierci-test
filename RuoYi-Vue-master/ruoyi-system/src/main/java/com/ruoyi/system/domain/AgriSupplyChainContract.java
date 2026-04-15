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
 * 供应链金融合约管理对象 agri_supply_chain_contract
 *
 * @author ruoyi
 */
public class AgriSupplyChainContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long contractId;

    @Excel(name = "合约编号")
    private String contractNo;

    @Excel(name = "合约名称")
    private String contractName;

    @Excel(name = "融资主体")
    private String financeSubject;

    @Excel(name = "融资金额")
    private BigDecimal financeAmount;

    @Excel(name = "利率(%)")
    private BigDecimal interestRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "起始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    @Excel(name = "合约状态", readConverterExp = "0=草稿,1=生效,2=到期,3=终止")
    private String contractStatus;

    @Excel(name = "风控等级", readConverterExp = "L=低,M=中,H=高,C=严重")
    private String riskLevel;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getContractId()
    {
        return contractId;
    }

    public void setContractId(Long contractId)
    {
        this.contractId = contractId;
    }

    @NotBlank(message = "合约编号不能为空")
    @Size(max = 64, message = "合约编号长度不能超过64个字符")
    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String contractNo)
    {
        this.contractNo = contractNo;
    }

    @NotBlank(message = "合约名称不能为空")
    @Size(max = 100, message = "合约名称长度不能超过100个字符")
    public String getContractName()
    {
        return contractName;
    }

    public void setContractName(String contractName)
    {
        this.contractName = contractName;
    }

    @NotBlank(message = "融资主体不能为空")
    @Size(max = 100, message = "融资主体长度不能超过100个字符")
    public String getFinanceSubject()
    {
        return financeSubject;
    }

    public void setFinanceSubject(String financeSubject)
    {
        this.financeSubject = financeSubject;
    }

    @NotNull(message = "融资金额不能为空")
    public BigDecimal getFinanceAmount()
    {
        return financeAmount;
    }

    public void setFinanceAmount(BigDecimal financeAmount)
    {
        this.financeAmount = financeAmount;
    }

    public BigDecimal getInterestRate()
    {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate)
    {
        this.interestRate = interestRate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public String getContractStatus()
    {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus)
    {
        this.contractStatus = contractStatus;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
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
            .append("contractId", getContractId())
            .append("contractNo", getContractNo())
            .append("contractName", getContractName())
            .append("financeSubject", getFinanceSubject())
            .append("financeAmount", getFinanceAmount())
            .append("interestRate", getInterestRate())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("contractStatus", getContractStatus())
            .append("riskLevel", getRiskLevel())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
