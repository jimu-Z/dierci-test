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
 * 数据存证与校验对象 agri_data_attestation_verify
 *
 * @author ruoyi
 */
public class AgriDataAttestationVerify extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long verifyId;

    @Excel(name = "存证编号")
    private String attestationNo;

    @Excel(name = "业务批次号")
    private String batchNo;

    @Excel(name = "数据类型")
    private String dataType;

    @Excel(name = "原始哈希")
    private String originHash;

    @Excel(name = "链上哈希")
    private String chainHash;

    @Excel(name = "校验状态", readConverterExp = "0=待校验,1=一致,2=不一致")
    private String verifyStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "校验时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    @Excel(name = "校验人")
    private String verifyByUser;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getVerifyId()
    {
        return verifyId;
    }

    public void setVerifyId(Long verifyId)
    {
        this.verifyId = verifyId;
    }

    @NotBlank(message = "存证编号不能为空")
    @Size(max = 64, message = "存证编号长度不能超过64个字符")
    public String getAttestationNo()
    {
        return attestationNo;
    }

    public void setAttestationNo(String attestationNo)
    {
        this.attestationNo = attestationNo;
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

    @NotBlank(message = "原始哈希不能为空")
    @Size(max = 128, message = "原始哈希长度不能超过128个字符")
    public String getOriginHash()
    {
        return originHash;
    }

    public void setOriginHash(String originHash)
    {
        this.originHash = originHash;
    }

    @Size(max = 128, message = "链上哈希长度不能超过128个字符")
    public String getChainHash()
    {
        return chainHash;
    }

    public void setChainHash(String chainHash)
    {
        this.chainHash = chainHash;
    }

    public String getVerifyStatus()
    {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus)
    {
        this.verifyStatus = verifyStatus;
    }

    public Date getVerifyTime()
    {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime)
    {
        this.verifyTime = verifyTime;
    }

    @Size(max = 64, message = "校验人长度不能超过64个字符")
    public String getVerifyByUser()
    {
        return verifyByUser;
    }

    public void setVerifyByUser(String verifyByUser)
    {
        this.verifyByUser = verifyByUser;
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
            .append("verifyId", getVerifyId())
            .append("attestationNo", getAttestationNo())
            .append("batchNo", getBatchNo())
            .append("dataType", getDataType())
            .append("originHash", getOriginHash())
            .append("chainHash", getChainHash())
            .append("verifyStatus", getVerifyStatus())
            .append("verifyTime", getVerifyTime())
            .append("verifyByUser", getVerifyByUser())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
