package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriOutputSalesTrend;
import java.util.List;

/**
 * 产量与销量趋势图Service接口
 *
 * @author ruoyi
 */
public interface IAgriOutputSalesTrendService
{
    public AgriOutputSalesTrend selectAgriOutputSalesTrendByTrendId(Long trendId);

    public List<AgriOutputSalesTrend> selectAgriOutputSalesTrendList(AgriOutputSalesTrend agriOutputSalesTrend);

    public int insertAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend);

    public int updateAgriOutputSalesTrend(AgriOutputSalesTrend agriOutputSalesTrend);

    public int deleteAgriOutputSalesTrendByTrendIds(Long[] trendIds);
}
