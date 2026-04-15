package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriYieldForecastTask;
import java.util.List;

/**
 * 产量预测任务Service接口
 *
 * @author ruoyi
 */
public interface IAgriYieldForecastTaskService
{
    public AgriYieldForecastTask selectAgriYieldForecastTaskByForecastId(Long forecastId);

    public List<AgriYieldForecastTask> selectAgriYieldForecastTaskList(AgriYieldForecastTask agriYieldForecastTask);

    public int insertAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask);

    public int updateAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask);

    public int deleteAgriYieldForecastTaskByForecastIds(Long[] forecastIds);
}
