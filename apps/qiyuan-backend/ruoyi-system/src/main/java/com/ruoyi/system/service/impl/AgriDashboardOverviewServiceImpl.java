package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDashboardOverview;
import com.ruoyi.system.mapper.AgriDashboardOverviewMapper;
import com.ruoyi.system.service.IAgriDashboardOverviewService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据总览看板Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDashboardOverviewServiceImpl implements IAgriDashboardOverviewService
{
    @Autowired
    private AgriDashboardOverviewMapper agriDashboardOverviewMapper;

    @Override
    public AgriDashboardOverview selectAgriDashboardOverviewByOverviewId(Long overviewId)
    {
        return agriDashboardOverviewMapper.selectAgriDashboardOverviewByOverviewId(overviewId);
    }

    @Override
    public List<AgriDashboardOverview> selectAgriDashboardOverviewList(AgriDashboardOverview agriDashboardOverview)
    {
        return agriDashboardOverviewMapper.selectAgriDashboardOverviewList(agriDashboardOverview);
    }

    @Override
    public int insertAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview)
    {
        return agriDashboardOverviewMapper.insertAgriDashboardOverview(agriDashboardOverview);
    }

    @Override
    public int updateAgriDashboardOverview(AgriDashboardOverview agriDashboardOverview)
    {
        return agriDashboardOverviewMapper.updateAgriDashboardOverview(agriDashboardOverview);
    }

    @Override
    public int deleteAgriDashboardOverviewByOverviewIds(Long[] overviewIds)
    {
        return agriDashboardOverviewMapper.deleteAgriDashboardOverviewByOverviewIds(overviewIds);
    }
}
