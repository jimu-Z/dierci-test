package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriTraceQueryStats;
import java.util.List;

/**
 * 溯源查询统计Service接口
 *
 * @author ruoyi
 */
public interface IAgriTraceQueryStatsService
{
    public AgriTraceQueryStats selectAgriTraceQueryStatsByStatsId(Long statsId);

    public List<AgriTraceQueryStats> selectAgriTraceQueryStatsList(AgriTraceQueryStats agriTraceQueryStats);

    public int insertAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats);

    public int updateAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats);

    public int deleteAgriTraceQueryStatsByStatsIds(Long[] statsIds);
}
