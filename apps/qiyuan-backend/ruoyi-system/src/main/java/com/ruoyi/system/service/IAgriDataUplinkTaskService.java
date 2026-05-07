package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDataUplinkTask;
import java.util.List;

/**
 * 数据上链任务Service接口
 *
 * @author ruoyi
 */
public interface IAgriDataUplinkTaskService
{
    public AgriDataUplinkTask selectAgriDataUplinkTaskByUplinkId(Long uplinkId);

    public List<AgriDataUplinkTask> selectAgriDataUplinkTaskList(AgriDataUplinkTask agriDataUplinkTask);

    public int insertAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask);

    public int updateAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask);

    public int deleteAgriDataUplinkTaskByUplinkIds(Long[] uplinkIds);
}
