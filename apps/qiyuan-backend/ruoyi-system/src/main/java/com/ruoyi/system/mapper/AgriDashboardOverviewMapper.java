package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriDashboardOverview;
import java.util.List;

/**
 * 数据总览看板Mapper接口
 *
 * @author ruoyi
 */
public interface AgriDashboardOverviewMapper
{
    public AgriDashboardOverview selectAgriDashboardOverviewByOverviewId(Long overviewId);

    public List<AgriDashboardOverview> selectAgriDashboardOverviewList(AgriDashboardOverview agriDashboardOverview);

    public int insertAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview);

    public int updateAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview);

    public int deleteAgriDashboardOverviewByOverviewId(Long overviewId);

    public int deleteAgriDashboardOverviewByOverviewIds(Long[] overviewIds);
}
