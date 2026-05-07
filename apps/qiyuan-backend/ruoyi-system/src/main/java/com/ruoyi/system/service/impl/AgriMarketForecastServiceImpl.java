package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriMarketForecast;
import com.ruoyi.system.mapper.AgriMarketForecastMapper;
import com.ruoyi.system.service.IAgriMarketForecastService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 市场预测分析Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriMarketForecastServiceImpl implements IAgriMarketForecastService
{
    @Autowired
    private AgriMarketForecastMapper agriMarketForecastMapper;

    @Override
    public AgriMarketForecast selectAgriMarketForecastByForecastId(Long forecastId)
    {
        return agriMarketForecastMapper.selectAgriMarketForecastByForecastId(forecastId);
    }

    @Override
    public List<AgriMarketForecast> selectAgriMarketForecastList(AgriMarketForecast agriMarketForecast)
    {
        return agriMarketForecastMapper.selectAgriMarketForecastList(agriMarketForecast);
    }

    @Override
    public int insertAgriMarketForecast(AgriMarketForecast agriMarketForecast)
    {
        return agriMarketForecastMapper.insertAgriMarketForecast(agriMarketForecast);
    }

    @Override
    public int updateAgriMarketForecast(AgriMarketForecast agriMarketForecast)
    {
        return agriMarketForecastMapper.updateAgriMarketForecast(agriMarketForecast);
    }

    @Override
    public int deleteAgriMarketForecastByForecastIds(Long[] forecastIds)
    {
        return agriMarketForecastMapper.deleteAgriMarketForecastByForecastIds(forecastIds);
    }
}
