package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriProcessBatchLink;
import com.ruoyi.system.mapper.AgriProcessBatchLinkMapper;
import com.ruoyi.system.service.IAgriProcessBatchLinkService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 加工批次关联Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriProcessBatchLinkServiceImpl implements IAgriProcessBatchLinkService
{
    @Autowired
    private AgriProcessBatchLinkMapper agriProcessBatchLinkMapper;

    @Override
    public AgriProcessBatchLink selectAgriProcessBatchLinkByLinkId(Long linkId)
    {
        return agriProcessBatchLinkMapper.selectAgriProcessBatchLinkByLinkId(linkId);
    }

    @Override
    public List<AgriProcessBatchLink> selectAgriProcessBatchLinkList(AgriProcessBatchLink agriProcessBatchLink)
    {
        return agriProcessBatchLinkMapper.selectAgriProcessBatchLinkList(agriProcessBatchLink);
    }

    @Override
    public int insertAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink)
    {
        return agriProcessBatchLinkMapper.insertAgriProcessBatchLink(agriProcessBatchLink);
    }

    @Override
    public int updateAgriProcessBatchLink(AgriProcessBatchLink agriProcessBatchLink)
    {
        return agriProcessBatchLinkMapper.updateAgriProcessBatchLink(agriProcessBatchLink);
    }

    @Override
    public int deleteAgriProcessBatchLinkByLinkIds(Long[] linkIds)
    {
        return agriProcessBatchLinkMapper.deleteAgriProcessBatchLinkByLinkIds(linkIds);
    }
}
