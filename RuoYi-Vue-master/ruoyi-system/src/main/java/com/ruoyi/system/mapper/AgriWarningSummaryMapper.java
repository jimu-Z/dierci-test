package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriWarningSummary;
import java.util.List;

/**
 * 预警信息汇总Mapper接口
 *
 * @author ruoyi
 */
public interface AgriWarningSummaryMapper
{
    public AgriWarningSummary selectAgriWarningSummaryBySummaryId(Long summaryId);

    public List<AgriWarningSummary> selectAgriWarningSummaryList(AgriWarningSummary agriWarningSummary);

    public int insertAgriWarningSummary(AgriWarningSummary agriWarningSummary);

    public int updateAgriWarningSummary(AgriWarningSummary agriWarningSummary);

    public int deleteAgriWarningSummaryBySummaryId(Long summaryId);

    public int deleteAgriWarningSummaryBySummaryIds(Long[] summaryIds);
}
