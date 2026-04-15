package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriLogisticsTrack;
import java.util.List;

/**
 * 物流路径追踪Service接口
 *
 * @author ruoyi
 */
public interface IAgriLogisticsTrackService
{
    public AgriLogisticsTrack selectAgriLogisticsTrackByTrackId(Long trackId);

    public List<AgriLogisticsTrack> selectAgriLogisticsTrackList(AgriLogisticsTrack agriLogisticsTrack);

    public int insertAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack);

    public int updateAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack);

    public int deleteAgriLogisticsTrackByTrackIds(Long[] trackIds);

    public List<AgriLogisticsTrack> selectAgriLogisticsTrackTimeline(String traceCode);
}
