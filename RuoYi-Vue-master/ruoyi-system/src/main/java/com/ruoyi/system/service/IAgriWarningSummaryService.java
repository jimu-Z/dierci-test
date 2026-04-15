package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriWarningSummary;
import java.util.List;

/**
 * 预警信息汇总Service接口
 *
 * @author ruoyi
 */
public interface IAgriWarningSummaryService
{
    public AgriWarningSummary selectAgriWarningSummaryBySummaryId(Long summaryId);

    public List<AgriWarningSummary> selectAgriWarningSummaryList(AgriWarningSummary agriWarningSummary);

    public int insertAgriWarningSummary(AgriWarningSummary agriWarningSummary);

    public int updateAgriWarningSummary(AgriWarningSummary agriWarningSummary);

    public int deleteAgriWarningSummaryBySummaryIds(Long[] summaryIds);
}
