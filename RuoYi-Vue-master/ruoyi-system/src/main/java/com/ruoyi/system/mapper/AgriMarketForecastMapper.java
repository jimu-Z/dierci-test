package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriMarketForecast;
import java.util.List;

/**
 * 市场预测分析Mapper接口
 *
 * @author ruoyi
 */
public interface AgriMarketForecastMapper
{
    public AgriMarketForecast selectAgriMarketForecastByForecastId(Long forecastId);

    public List<AgriMarketForecast> selectAgriMarketForecastList(AgriMarketForecast agriMarketForecast);

    public int insertAgriMarketForecast(AgriMarketForecast agriMarketForecast);

    public int updateAgriMarketForecast(AgriMarketForecast agriMarketForecast);

    public int deleteAgriMarketForecastByForecastId(Long forecastId);

    public int deleteAgriMarketForecastByForecastIds(Long[] forecastIds);
}
