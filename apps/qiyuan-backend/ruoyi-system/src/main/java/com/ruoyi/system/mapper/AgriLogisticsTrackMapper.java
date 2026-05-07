package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriLogisticsTrack;
import java.util.List;

/**
 * 物流路径追踪Mapper接口
 *
 * @author ruoyi
 */
public interface AgriLogisticsTrackMapper
{
    public AgriLogisticsTrack selectAgriLogisticsTrackByTrackId(Long trackId);

    public List<AgriLogisticsTrack> selectAgriLogisticsTrackList(AgriLogisticsTrack agriLogisticsTrack);

    public int insertAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack);

    public int updateAgriLogisticsTrack(AgriLogisticsTrack agriLogisticsTrack);

    public int deleteAgriLogisticsTrackByTrackId(Long trackId);

    public int deleteAgriLogisticsTrackByTrackIds(Long[] trackIds);

    public List<AgriLogisticsTrack> selectAgriLogisticsTrackTimeline(String traceCode);
}
