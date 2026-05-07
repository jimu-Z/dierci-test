package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriProcessBatchLink;
import java.util.List;

/**
 * 加工批次关联Service接口
 *
 * @author ruoyi
 */
public interface IAgriProcessBatchLinkService
{
    public AgriProcessBatchLink selectAgriProcessBatchLinkByLinkId(Long linkId);

    public List<AgriProcessBatchLink> selectAgriProcessBatchLinkList(AgriProcessBatchLink agriProcessBatchLink);

    public int insertAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink);

    public int updateAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink);

    public int deleteAgriProcessBatchLinkByLinkIds(Long[] linkIds);
}
