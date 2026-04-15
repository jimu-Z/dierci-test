package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDashboardOverview;
import java.util.List;

/**
 * 数据总览看板Service接口
 *
 * @author ruoyi
 */
public interface IAgriDashboardOverviewService
{
    public AgriDashboardOverview selectAgriDashboardOverviewByOverviewId(Long overviewId);

    public List<AgriDashboardOverview> selectAgriDashboardOverviewList(AgriDashboardOverview agriDashboardOverview);

    public int insertAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview);

    public int updateAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview);

    public int deleteAgriDashboardOverviewByOverviewIds(Long[] overviewIds);
}
