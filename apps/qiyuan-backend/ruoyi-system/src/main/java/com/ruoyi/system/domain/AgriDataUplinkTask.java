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
 * 数据上链任务对象 agri_data_uplink_task
 *
 * @author ruoyi
 */
public class AgriDataUplinkTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long uplinkId;

    @Excel(name = "业务批次号")
    private String batchNo;

    @Excel(name = "数据类型")
    private String dataType;

    @Excel(name = "数据哈希")
    private String dataHash;

    @Excel(name = "链平台")
    private String chainPlatform;

    @Excel(name = "合约地址")
    private String contractAddress;

    @Excel(name = "交易哈希")
    private String txHash;

    @Excel(name = "上链状态", readConverterExp = "0=待上链,1=已上链,2=失败")
    private String uplinkStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上链时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date uplinkTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getUplinkId()
    {
        return uplinkId;
    }

    public void setUplinkId(Long uplinkId)
    {
        this.uplinkId = uplinkId;
    }

    @NotBlank(message = "业务批次号不能为空")
    @Size(max = 64, message = "业务批次号长度不能超过64个字符")
    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    @NotBlank(message = "数据类型不能为空")
    @Size(max = 32, message = "数据类型长度不能超过32个字符")
    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    @NotBlank(message = "数据哈希不能为空")
    @Size(max = 128, message = "数据哈希长度不能超过128个字符")
    public String getDataHash()
    {
        return dataHash;
    }

    public void setDataHash(String dataHash)
    {
        this.dataHash = dataHash;
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

    @Size(max = 128, message = "合约地址长度不能超过128个字符")
    public String getContractAddress()
    {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress)
    {
        this.contractAddress = contractAddress;
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

    public String getUplinkStatus()
    {
        return uplinkStatus;
    }

    public void setUplinkStatus(String uplinkStatus)
    {
        this.uplinkStatus = uplinkStatus;
    }

    public Date getUplinkTime()
    {
        return uplinkTime;
    }

    public void setUplinkTime(Date uplinkTime)
    {
        this.uplinkTime = uplinkTime;
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
            .append("uplinkId", getUplinkId())
            .append("batchNo", getBatchNo())
            .append("dataType", getDataType())
            .append("dataHash", getDataHash())
            .append("chainPlatform", getChainPlatform())
            .append("contractAddress", getContractAddress())
            .append("txHash", getTxHash())
            .append("uplinkStatus", getUplinkStatus())
            .append("uplinkTime", getUplinkTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
