package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriTraceQueryStats;
import java.util.List;

/**
 * 溯源查询统计Mapper接口
 *
 * @author ruoyi
 */
public interface AgriTraceQueryStatsMapper
{
    public AgriTraceQueryStats selectAgriTraceQueryStatsByStatsId(Long statsId);

    public List<AgriTraceQueryStats> selectAgriTraceQueryStatsList(AgriTraceQueryStats agriTraceQueryStats);

    public int insertAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats);

    public int updateAgriTraceQueryStats(AgriTraceQueryStats agriTraceQueryStats);

    public int deleteAgriTraceQueryStatsByStatsId(Long statsId);

    public int deleteAgriTraceQueryStatsByStatsIds(Long[] statsIds);
}
