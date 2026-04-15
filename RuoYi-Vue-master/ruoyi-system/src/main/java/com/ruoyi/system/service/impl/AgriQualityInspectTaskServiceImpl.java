package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.mapper.AgriQualityInspectTaskMapper;
import com.ruoyi.system.service.IAgriQualityInspectTaskService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 视觉品质检测任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriQualityInspectTaskServiceImpl implements IAgriQualityInspectTaskService
{
    @Autowired
    private AgriQualityInspectTaskMapper agriQualityInspectTaskMapper;

    @Override
    public AgriQualityInspectTask selectAgriQualityInspectTaskByInspectId(Long inspectId)
    {
        return agriQualityInspectTaskMapper.selectAgriQualityInspectTaskByInspectId(inspectId);
    }

    @Override
    public List<AgriQualityInspectTask> selectAgriQualityInspectTaskList(AgriQualityInspectTask agriQualityInspectTask)
    {
        return agriQualityInspectTaskMapper.selectAgriQualityInspectTaskList(agriQualityInspectTask);
    }

    @Override
    public int insertAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask)
    {
        return agriQualityInspectTaskMapper.insertAgriQualityInspectTask(agriQualityInspectTask);
    }

    @Override
    public int updateAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask)
    {
        return agriQualityInspectTaskMapper.updateAgriQualityInspectTask(agriQualityInspectTask);
    }

    @Override
    public int deleteAgriQualityInspectTaskByInspectIds(Long[] inspectIds)
    {
        return agriQualityInspectTaskMapper.deleteAgriQualityInspectTaskByInspectIds(inspectIds);
    }
}
