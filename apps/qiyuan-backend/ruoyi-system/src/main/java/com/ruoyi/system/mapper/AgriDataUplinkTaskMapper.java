package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriDataUplinkTask;
import java.util.List;

/**
 * 数据上链任务Mapper接口
 *
 * @author ruoyi
 */
public interface AgriDataUplinkTaskMapper
{
    public AgriDataUplinkTask selectAgriDataUplinkTaskByUplinkId(Long uplinkId);

    public List<AgriDataUplinkTask> selectAgriDataUplinkTaskList(AgriDataUplinkTask agriDataUplinkTask);

    public int insertAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask);

    public int updateAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask);

    public int deleteAgriDataUplinkTaskByUplinkId(Long uplinkId);

    public int deleteAgriDataUplinkTaskByUplinkIds(Long[] uplinkIds);
}
