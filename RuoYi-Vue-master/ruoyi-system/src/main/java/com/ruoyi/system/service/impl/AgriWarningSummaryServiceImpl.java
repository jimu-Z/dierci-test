package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriWarningSummary;
import com.ruoyi.system.mapper.AgriWarningSummaryMapper;
import com.ruoyi.system.service.IAgriWarningSummaryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预警信息汇总Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriWarningSummaryServiceImpl implements IAgriWarningSummaryService
{
    @Autowired
    private AgriWarningSummaryMapper agriWarningSummaryMapper;

    @Override
    public AgriWarningSummary selectAgriWarningSummaryBySummaryId(Long summaryId)
    {
        return agriWarningSummaryMapper.selectAgriWarningSummaryBySummaryId(summaryId);
    }

    @Override
    public List<AgriWarningSummary> selectAgriWarningSummaryList(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.selectAgriWarningSummaryList(agriWarningSummary);
    }

    @Override
    public int insertAgriWarningSummary(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.insertAgriWarningSummary(agriWarningSummary);
    }

    @Override
    public int updateAgriWarningSummary(AgriWarningSummary agriWarningSummary)
    {
        return agriWarningSummaryMapper.updateAgriWarningSummary(agriWarningSummary);
    }

    @Override
    public int deleteAgriWarningSummaryBySummaryIds(Long[] summaryIds)
    {
        return agriWarningSummaryMapper.deleteAgriWarningSummaryBySummaryIds(summaryIds);
    }
}
