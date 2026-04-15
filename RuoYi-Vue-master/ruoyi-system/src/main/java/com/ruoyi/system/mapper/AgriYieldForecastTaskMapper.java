package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriYieldForecastTask;
import java.util.List;

/**
 * 产量预测任务Mapper接口
 *
 * @author ruoyi
 */
public interface AgriYieldForecastTaskMapper
{
    public AgriYieldForecastTask selectAgriYieldForecastTaskByForecastId(Long forecastId);

    public List<AgriYieldForecastTask> selectAgriYieldForecastTaskList(AgriYieldForecastTask agriYieldForecastTask);

    public int insertAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask);

    public int updateAgriYieldForecastTask(AgriYieldForecastTask agriYieldForecastTask);

    public int deleteAgriYieldForecastTaskByForecastId(Long forecastId);

    public int deleteAgriYieldForecastTaskByForecastIds(Long[] forecastIds);
}
