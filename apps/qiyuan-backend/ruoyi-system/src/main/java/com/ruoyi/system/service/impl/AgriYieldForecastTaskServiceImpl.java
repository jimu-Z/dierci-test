package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriYieldForecastTask;
import com.ruoyi.system.mapper.AgriYieldForecastTaskMapper;
import com.ruoyi.system.service.IAgriYieldForecastTaskService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 产量预测任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriYieldForecastTaskServiceImpl implements IAgriYieldForecastTaskService
{
    @Autowired
    private AgriYieldForecastTaskMapper agriYieldForecastTaskMapper;

    @Override
    public AgriYieldForecastTask selectAgriYieldForecastTaskByForecastId(Long forecastId)
    {
        return agriYieldForecastTaskMapper.selectAgriYieldForecastTaskByForecastId(forecastId);
    }

    @Override
    public List<AgriYieldForecastTask> selectAgriYieldForecastTaskList(AgriYieldForecastTask agriYieldForecastTask)
    {
        return agriYieldForecastTaskMapper.selectAgriYieldForecastTaskList(agriYieldForecastTask);
    }

    @Override
    public int insertAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask)
    {
        return agriYieldForecastTaskMapper.insertAgriYieldForecastTask(agriYieldForecastTask);
    }

    @Override
    public int updateAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask)
    {
        return agriYieldForecastTaskMapper.updateAgriYieldForecastTask(agriYieldForecastTask);
    }

    @Override
    public int deleteAgriYieldForecastTaskByForecastIds(Long[] forecastIds)
    {
        return agriYieldForecastTaskMapper.deleteAgriYieldForecastTaskByForecastIds(forecastIds);
    }
}
