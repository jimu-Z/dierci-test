package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriOutputSalesTrend;
import java.util.List;

/**
 * 产量与销量趋势图Mapper接口
 *
 * @author ruoyi
 */
public interface AgriOutputSalesTrendMapper
{
    public AgriOutputSalesTrend selectAgriOutputSalesTrendByTrendId(Long trendId);

    public List<AgriOutputSalesTrend> selectAgriOutputSalesTrendList(AgriOutputSalesTrend agriOutputSalesTrend);

    public int insertAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend);

    public int updateAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend);

    public int deleteAgriOutputSalesTrendByTrendId(Long trendId);

    public int deleteAgriOutputSalesTrendByTrendIds(Long[] trendIds);
}
