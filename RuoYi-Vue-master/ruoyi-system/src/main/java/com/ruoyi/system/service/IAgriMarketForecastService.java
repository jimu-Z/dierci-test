package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriMarketForecast;
import java.util.List;

/**
 * 市场预测分析Service接口
 *
 * @author ruoyi
 */
public interface IAgriMarketForecastService
{
    public AgriMarketForecast selectAgriMarketForecastByForecastId(Long forecastId);

    public List<AgriMarketForecast> selectAgriMarketForecastList(AgriMarketForecast agriMarketForecast);

    public int insertAgriMarketForecast(AgriMarketForecast agriMarketForecast);

    public int updateAgriMarketForecast(AgriMarketForecast agriMarketForecast);

    public int deleteAgriMarketForecastByForecastIds(Long[] forecastIds);
}
