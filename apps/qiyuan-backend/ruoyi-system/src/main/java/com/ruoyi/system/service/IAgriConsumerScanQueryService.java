package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriConsumerScanQuery;
import java.util.List;

/**
 * 消费者扫码查询Service接口
 *
 * @author ruoyi
 */
public interface IAgriConsumerScanQueryService
{
    public AgriConsumerScanQuery selectAgriConsumerScanQueryByQueryId(Long queryId);

    public List<AgriConsumerScanQuery> selectAgriConsumerScanQueryList(AgriConsumerScanQuery agriConsumerScanQuery);

    public int insertAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery);

    public int updateAgriConsumerScanQuery(AgriConsumerScanQuery agriConsumerScanQuery);

    public int deleteAgriConsumerScanQueryByQueryIds(Long[] queryIds);
}
