package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriConsumerScanQuery;
import java.util.List;

/**
 * 消费者扫码查询Mapper接口
 *
 * @author ruoyi
 */
public interface AgriConsumerScanQueryMapper
{
    public AgriConsumerScanQuery selectAgriConsumerScanQueryByQueryId(Long queryId);

    public List<AgriConsumerScanQuery> selectAgriConsumerScanQueryList(AgriConsumerScanQuery agriConsumerScanQuery);

    public int insertAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery);

    public int updateAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery);

    public int deleteAgriConsumerScanQueryByQueryId(Long queryId);

    public int deleteAgriConsumerScanQueryByQueryIds(Long[] queryIds);
}
