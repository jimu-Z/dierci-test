package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriOutputSalesTrend;
import com.ruoyi.system.mapper.AgriOutputSalesTrendMapper;
import com.ruoyi.system.service.IAgriOutputSalesTrendService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 产量与销量趋势图Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriOutputSalesTrendServiceImpl implements IAgriOutputSalesTrendService
{
    @Autowired
    private AgriOutputSalesTrendMapper agriOutputSalesTrendMapper;

    @Override
    public AgriOutputSalesTrend selectAgriOutputSalesTrendByTrendId(Long trendId)
    {
        return agriOutputSalesTrendMapper.selectAgriOutputSalesTrendByTrendId(trendId);
    }

    @Override
    public List<AgriOutputSalesTrend> selectAgriOutputSalesTrendList(AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return agriOutputSalesTrendMapper.selectAgriOutputSalesTrendList(agriOutputSalesTrend);
    }

    @Override
    public int insertAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return agriOutputSalesTrendMapper.insertAgriOutputSalesTrend(agriOutputSalesTrend);
    }

    @Override
    public int updateAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend)
    {
        return agriOutputSalesTrendMapper.updateAgriOutputSalesTrend(agriOutputSalesTrend);
    }

    @Override
    public int deleteAgriOutputSalesTrendByTrendIds(Long[] trendIds)
    {
        return agriOutputSalesTrendMapper.deleteAgriOutputSalesTrendByTrendIds(trendIds);
    }
}
