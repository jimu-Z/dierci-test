package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 品牌溯源页面对象 agri_brand_trace_page
 *
 * @author ruoyi
 */
public class AgriBrandTracePage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long pageId;

    @Excel(name = "溯源码")
    private String traceCode;

    @Excel(name = "品牌名称")
    private String brandName;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产地")
    private String originPlace;

    @Excel(name = "种植批次号")
    private String plantingBatchNo;

    @Excel(name = "加工批次号")
    private String processBatchNo;

    @Excel(name = "物流运单号")
    private String logisticsTraceCode;

    @Excel(name = "封面图")
    private String coverImageUrl;

    @Excel(name = "详情页地址")
    private String pageUrl;

    @Excel(name = "二维码地址")
    private String qrCodeUrl;

    @Excel(name = "品牌故事")
    private String brandStory;

    @Excel(name = "发布状态", readConverterExp = "0=草稿,1=已发布")
    private String publishStatus;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getPageId()
    {
        return pageId;
    }

    public void setPageId(Long pageId)
    {
        this.pageId = pageId;
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

    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 64, message = "品牌名称长度不能超过64个字符")
    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    @NotBlank(message = "产品名称不能为空")
    @Size(max = 64, message = "产品名称长度不能超过64个字符")
    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    @Size(max = 64, message = "产品编码长度不能超过64个字符")
    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    @Size(max = 128, message = "产地长度不能超过128个字符")
    public String getOriginPlace()
    {
        return originPlace;
    }

    public void setOriginPlace(String originPlace)
    {
        this.originPlace = originPlace;
    }

    @Size(max = 64, message = "种植批次号长度不能超过64个字符")
    public String getPlantingBatchNo()
    {
        return plantingBatchNo;
    }

    public void setPlantingBatchNo(String plantingBatchNo)
    {
        this.plantingBatchNo = plantingBatchNo;
    }

    @Size(max = 64, message = "加工批次号长度不能超过64个字符")
    public String getProcessBatchNo()
    {
        return processBatchNo;
    }

    public void setProcessBatchNo(String processBatchNo)
    {
        this.processBatchNo = processBatchNo;
    }

    @Size(max = 64, message = "物流运单号长度不能超过64个字符")
    public String getLogisticsTraceCode()
    {
        return logisticsTraceCode;
    }

    public void setLogisticsTraceCode(String logisticsTraceCode)
    {
        this.logisticsTraceCode = logisticsTraceCode;
    }

    @Size(max = 500, message = "封面图地址长度不能超过500个字符")
    public String getCoverImageUrl()
    {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl)
    {
        this.coverImageUrl = coverImageUrl;
    }

    @Size(max = 500, message = "详情页地址长度不能超过500个字符")
    public String getPageUrl()
    {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    @Size(max = 500, message = "二维码地址长度不能超过500个字符")
    public String getQrCodeUrl()
    {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl)
    {
        this.qrCodeUrl = qrCodeUrl;
    }

    @Size(max = 2000, message = "品牌故事长度不能超过2000个字符")
    public String getBrandStory()
    {
        return brandStory;
    }

    public void setBrandStory(String brandStory)
    {
        this.brandStory = brandStory;
    }

    public String getPublishStatus()
    {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus)
    {
        this.publishStatus = publishStatus;
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
            .append("pageId", getPageId())
            .append("traceCode", getTraceCode())
            .append("brandName", getBrandName())
            .append("productName", getProductName())
            .append("productCode", getProductCode())
            .append("originPlace", getOriginPlace())
            .append("plantingBatchNo", getPlantingBatchNo())
            .append("processBatchNo", getProcessBatchNo())
            .append("logisticsTraceCode", getLogisticsTraceCode())
            .append("coverImageUrl", getCoverImageUrl())
            .append("pageUrl", getPageUrl())
            .append("qrCodeUrl", getQrCodeUrl())
            .append("brandStory", getBrandStory())
            .append("publishStatus", getPublishStatus())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
