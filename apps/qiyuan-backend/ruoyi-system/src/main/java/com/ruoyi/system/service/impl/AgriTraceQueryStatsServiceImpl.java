package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriTraceQueryStats;
import com.ruoyi.system.mapper.AgriTraceQueryStatsMapper;
import com.ruoyi.system.service.IAgriTraceQueryStatsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 溯源查询统计Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriTraceQueryStatsServiceImpl implements IAgriTraceQueryStatsService
{
    @Autowired
    private AgriTraceQueryStatsMapper agriTraceQueryStatsMapper;

    @Override
    public AgriTraceQueryStats selectAgriTraceQueryStatsByStatsId(Long statsId)
    {
        return agriTraceQueryStatsMapper.selectAgriTraceQueryStatsByStatsId(statsId);
    }

    @Override
    public List<AgriTraceQueryStats> selectAgriTraceQueryStatsList(AgriTraceQueryStats agriTraceQueryStats)
    {
        return agriTraceQueryStatsMapper.selectAgriTraceQueryStatsList(agriTraceQueryStats);
    }

    @Override
    public int insertAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats)
    {
        return agriTraceQueryStatsMapper.insertAgriTraceQueryStats(agriTraceQueryStats);
    }

    @Override
    public int updateAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats)
    {
        return agriTraceQueryStatsMapper.updateAgriTraceQueryStats(agriTraceQueryStats);
    }

    @Override
    public int deleteAgriTraceQueryStatsByStatsIds(Long[] statsIds)
    {
        return agriTraceQueryStatsMapper.deleteAgriTraceQueryStatsByStatsIds(statsIds);
    }
}
