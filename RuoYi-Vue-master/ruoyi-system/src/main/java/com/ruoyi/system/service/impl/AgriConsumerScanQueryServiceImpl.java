package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriConsumerScanQuery;
import com.ruoyi.system.mapper.AgriConsumerScanQueryMapper;
import com.ruoyi.system.service.IAgriConsumerScanQueryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消费者扫码查询Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriConsumerScanQueryServiceImpl implements IAgriConsumerScanQueryService
{
    @Autowired
    private AgriConsumerScanQueryMapper agriConsumerScanQueryMapper;

    @Override
    public AgriConsumerScanQuery selectAgriConsumerScanQueryByQueryId(Long queryId)
    {
        return agriConsumerScanQueryMapper.selectAgriConsumerScanQueryByQueryId(queryId);
    }

    @Override
    public List<AgriConsumerScanQuery> selectAgriConsumerScanQueryList(AgriConsumerScanQuery agriConsumerScanQuery)
    {
        return agriConsumerScanQueryMapper.selectAgriConsumerScanQueryList(agriConsumerScanQuery);
    }

    @Override
    public int insertAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery)
    {
        return agriConsumerScanQueryMapper.insertAgriConsumerScanQuery(agriConsumerScanQuery);
    }

    @Override
    public int updateAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery)
    {
        return agriConsumerScanQueryMapper.updateAgriConsumerScanQuery(agriConsumerScanQuery);
    }

    @Override
    public int deleteAgriConsumerScanQueryByQueryIds(Long[] queryIds)
    {
        return agriConsumerScanQueryMapper.deleteAgriConsumerScanQueryByQueryIds(queryIds);
    }
}
