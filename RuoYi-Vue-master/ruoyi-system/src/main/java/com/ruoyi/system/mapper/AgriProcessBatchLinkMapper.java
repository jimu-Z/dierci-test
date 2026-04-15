package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriProcessBatchLink;
import java.util.List;

/**
 * 加工批次关联Mapper接口
 *
 * @author ruoyi
 */
public interface AgriProcessBatchLinkMapper
{
    public AgriProcessBatchLink selectAgriProcessBatchLinkByLinkId(Long linkId);

    public List<AgriProcessBatchLink> selectAgriProcessBatchLinkList(AgriProcessBatchLink agriProcessBatchLink);

    public int insertAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink);

    public int updateAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink);

    public int deleteAgriProcessBatchLinkByLinkId(Long linkId);

    public int deleteAgriProcessBatchLinkByLinkIds(Long[] linkIds);
}
