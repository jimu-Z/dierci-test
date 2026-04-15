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
 * 智能合约部署对象 agri_smart_contract_deploy
 *
 * @author ruoyi
 */
public class AgriSmartContractDeploy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long deployId;

    @Excel(name = "合约名称")
    private String contractName;

    @Excel(name = "合约版本")
    private String contractVersion;

    @Excel(name = "链平台")
    private String chainPlatform;

    @Excel(name = "源码哈希")
    private String sourceHash;

    @Excel(name = "ABI")
    private String abiJson;

    @Excel(name = "合约地址")
    private String contractAddress;

    @Excel(name = "部署交易哈希")
    private String deployTxHash;

    @Excel(name = "部署状态", readConverterExp = "0=待部署,1=已部署,2=失败")
    private String deployStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "部署时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deployTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getDeployId()
    {
        return deployId;
    }

    public void setDeployId(Long deployId)
    {
        this.deployId = deployId;
    }

    @NotBlank(message = "合约名称不能为空")
    @Size(max = 64, message = "合约名称长度不能超过64个字符")
    public String getContractName()
    {
        return contractName;
    }

    public void setContractName(String contractName)
    {
        this.contractName = contractName;
    }

    @NotBlank(message = "合约版本不能为空")
    @Size(max = 32, message = "合约版本长度不能超过32个字符")
    public String getContractVersion()
    {
        return contractVersion;
    }

    public void setContractVersion(String contractVersion)
    {
        this.contractVersion = contractVersion;
    }

    @NotBlank(message = "链平台不能为空")
    @Size(max = 32, message = "链平台长度不能超过32个字符")
    public String getChainPlatform()
    {
        return chainPlatform;
    }

    public void setChainPlatform(String chainPlatform)
    {
        this.chainPlatform = chainPlatform;
    }

    @NotBlank(message = "源码哈希不能为空")
    @Size(max = 128, message = "源码哈希长度不能超过128个字符")
    public String getSourceHash()
    {
        return sourceHash;
    }

    public void setSourceHash(String sourceHash)
    {
        this.sourceHash = sourceHash;
    }

    public String getAbiJson()
    {
        return abiJson;
    }

    public void setAbiJson(String abiJson)
    {
        this.abiJson = abiJson;
    }

    @Size(max = 128, message = "合约地址长度不能超过128个字符")
    public String getContractAddress()
    {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress)
    {
        this.contractAddress = contractAddress;
    }

    @Size(max = 128, message = "部署交易哈希长度不能超过128个字符")
    public String getDeployTxHash()
    {
        return deployTxHash;
    }

    public void setDeployTxHash(String deployTxHash)
    {
        this.deployTxHash = deployTxHash;
    }

    public String getDeployStatus()
    {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus)
    {
        this.deployStatus = deployStatus;
    }

    public Date getDeployTime()
    {
        return deployTime;
    }

    public void setDeployTime(Date deployTime)
    {
        this.deployTime = deployTime;
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
            .append("deployId", getDeployId())
            .append("contractName", getContractName())
            .append("contractVersion", getContractVersion())
            .append("chainPlatform", getChainPlatform())
            .append("sourceHash", getSourceHash())
            .append("abiJson", getAbiJson())
            .append("contractAddress", getContractAddress())
            .append("deployTxHash", getDeployTxHash())
            .append("deployStatus", getDeployStatus())
            .append("deployTime", getDeployTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
