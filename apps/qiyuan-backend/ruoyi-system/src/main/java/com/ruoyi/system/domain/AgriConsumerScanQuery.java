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
 * 消费者扫码查询对象 agri_consumer_scan_query
 *
 * @author ruoyi
 */
public class AgriConsumerScanQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long queryId;

    @Excel(name = "溯源码")
    private String traceCode;

    @Excel(name = "消费者姓名")
    private String consumerName;

    @Excel(name = "消费者手机号")
    private String consumerPhone;

    @Excel(name = "扫码渠道")
    private String scanChannel;

    @Excel(name = "扫码地址")
    private String scanAddress;

    @Excel(name = "扫码IP")
    private String scanIp;

    @Excel(name = "查询结果", readConverterExp = "0=未命中,1=命中已发布,2=命中未发布")
    private String scanResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "查询时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date queryTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Excel(name = "品牌名称")
    private String brandName;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "产地")
    private String originPlace;

    @Excel(name = "详情页地址")
    private String pageUrl;

    @Excel(name = "发布状态", readConverterExp = "0=草稿,1=已发布")
    private String publishStatus;

    public Long getQueryId()
    {
        return queryId;
    }

    public void setQueryId(Long queryId)
    {
        this.queryId = queryId;
    }

    @NotBlank(message = "溯源码不能为空")
    @Size(max = 64, message = "溯源码长度不能超过64个字符")
    public String getTraceCode()
    {
        return traceCode;
    }

    public void setTraceCode(String traceCode)
    {
        this.traceCode = traceCode;
    }

    @Size(max = 64, message = "消费者姓名长度不能超过64个字符")
    public String getConsumerName()
    {
        return consumerName;
    }

    public void setConsumerName(String consumerName)
    {
        this.consumerName = consumerName;
    }

    @Size(max = 32, message = "消费者手机号长度不能超过32个字符")
    public String getConsumerPhone()
    {
        return consumerPhone;
    }

    public void setConsumerPhone(String consumerPhone)
    {
        this.consumerPhone = consumerPhone;
    }

    @Size(max = 32, message = "扫码渠道长度不能超过32个字符")
    public String getScanChannel()
    {
        return scanChannel;
    }

    public void setScanChannel(String scanChannel)
    {
        this.scanChannel = scanChannel;
    }

    @Size(max = 128, message = "扫码地址长度不能超过128个字符")
    public String getScanAddress()
    {
        return scanAddress;
    }

    public void setScanAddress(String scanAddress)
    {
        this.scanAddress = scanAddress;
    }

    @Size(max = 64, message = "扫码IP长度不能超过64个字符")
    public String getScanIp()
    {
        return scanIp;
    }

    public void setScanIp(String scanIp)
    {
        this.scanIp = scanIp;
    }

    public String getScanResult()
    {
        return scanResult;
    }

    public void setScanResult(String scanResult)
    {
        this.scanResult = scanResult;
    }

    public Date getQueryTime()
    {
        return queryTime;
    }

    public void setQueryTime(Date queryTime)
    {
        this.queryTime = queryTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getOriginPlace()
    {
        return originPlace;
    }

    public void setOriginPlace(String originPlace)
    {
        this.originPlace = originPlace;
    }

    public String getPageUrl()
    {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    public String getPublishStatus()
    {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus)
    {
        this.publishStatus = publishStatus;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("queryId", getQueryId())
            .append("traceCode", getTraceCode())
            .append("consumerName", getConsumerName())
            .append("consumerPhone", getConsumerPhone())
            .append("scanChannel", getScanChannel())
            .append("scanAddress", getScanAddress())
            .append("scanIp", getScanIp())
            .append("scanResult", getScanResult())
            .append("queryTime", getQueryTime())
            .append("status", getStatus())
            .append("brandName", getBrandName())
            .append("productName", getProductName())
            .append("originPlace", getOriginPlace())
            .append("pageUrl", getPageUrl())
            .append("publishStatus", getPublishStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
