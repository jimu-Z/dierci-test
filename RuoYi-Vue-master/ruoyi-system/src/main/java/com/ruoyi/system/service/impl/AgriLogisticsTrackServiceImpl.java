package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriLogisticsTrack;
import com.ruoyi.system.mapper.AgriLogisticsTrackMapper;
import com.ruoyi.system.service.IAgriLogisticsTrackService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 物流路径追踪Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriLogisticsTrackServiceImpl implements IAgriLogisticsTrackService
{
    @Autowired
    private AgriLogisticsTrackMapper agriLogisticsTrackMapper;

    @Override
    public AgriLogisticsTrack selectAgriLogisticsTrackByTrackId(Long trackId)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackByTrackId(trackId);
    }

    @Override
    public List<AgriLogisticsTrack> selectAgriLogisticsTrackList(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackList(agriLogisticsTrack);
    }

    @Override
    public int insertAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.insertAgriLogisticsTrack(agriLogisticsTrack);
    }

    @Override
    public int updateAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack)
    {
        return agriLogisticsTrackMapper.updateAgriLogisticsTrack(agriLogisticsTrack);
    }

    @Override
    public int deleteAgriLogisticsTrackByTrackIds(Long[] trackIds)
    {
        return agriLogisticsTrackMapper.deleteAgriLogisticsTrackByTrackIds(trackIds);
    }

    @Override
    public List<AgriLogisticsTrack> selectAgriLogisticsTrackTimeline(String traceCode)
    {
        return agriLogisticsTrackMapper.selectAgriLogisticsTrackTimeline(traceCode);
    }
}
