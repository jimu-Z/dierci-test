package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDataUplinkTask;
import com.ruoyi.system.mapper.AgriDataUplinkTaskMapper;
import com.ruoyi.system.service.IAgriDataUplinkTaskService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据上链任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDataUplinkTaskServiceImpl implements IAgriDataUplinkTaskService
{
    @Autowired
    private AgriDataUplinkTaskMapper agriDataUplinkTaskMapper;

    @Override
    public AgriDataUplinkTask selectAgriDataUplinkTaskByUplinkId(Long uplinkId)
    {
        return agriDataUplinkTaskMapper.selectAgriDataUplinkTaskByUplinkId(uplinkId);
    }

    @Override
    public List<AgriDataUplinkTask> selectAgriDataUplinkTaskList(AgriDataUplinkTask agriDataUplinkTask)
    {
        return agriDataUplinkTaskMapper.selectAgriDataUplinkTaskList(agriDataUplinkTask);
    }

    @Override
    public int insertAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask)
    {
        return agriDataUplinkTaskMapper.insertAgriDataUplinkTask(agriDataUplinkTask);
    }

    @Override
    public int updateAgriDataUplinkTask(AgriDataUplinkTask agriDataUplinkTask)
    {
        return agriDataUplinkTaskMapper.updateAgriDataUplinkTask(agriDataUplinkTask);
    }

    @Override
    public int deleteAgriDataUplinkTaskByUplinkIds(Long[] uplinkIds)
    {
        return agriDataUplinkTaskMapper.deleteAgriDataUplinkTaskByUplinkIds(uplinkIds);
    }
}
