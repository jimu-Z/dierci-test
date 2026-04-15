package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.mapper.AgriPestIdentifyTaskMapper;
import com.ruoyi.system.service.IAgriPestIdentifyTaskService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 病虫害识别任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriPestIdentifyTaskServiceImpl implements IAgriPestIdentifyTaskService
{
    @Autowired
    private AgriPestIdentifyTaskMapper agriPestIdentifyTaskMapper;

    /**
     * 查询病虫害识别任务
     *
     * @param taskId 主键
     * @return 病虫害识别任务
     */
    @Override
    public AgriPestIdentifyTask selectAgriPestIdentifyTaskByTaskId(Long taskId)
    {
        return agriPestIdentifyTaskMapper.selectAgriPestIdentifyTaskByTaskId(taskId);
    }

    /**
     * 查询病虫害识别任务列表
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 病虫害识别任务
     */
    @Override
    public List<AgriPestIdentifyTask> selectAgriPestIdentifyTaskList(AgriPestIdentifyTask agriPestIdentifyTask)
    {
        return agriPestIdentifyTaskMapper.selectAgriPestIdentifyTaskList(agriPestIdentifyTask);
    }

    /**
     * 新增病虫害识别任务
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 结果
     */
    @Override
    public int insertAgriPestIdentifyTask(AgriPestIdentifyTask agriPestIdentifyTask)
    {
        return agriPestIdentifyTaskMapper.insertAgriPestIdentifyTask(agriPestIdentifyTask);
    }

    /**
     * 修改病虫害识别任务
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 结果
     */
    @Override
    public int updateAgriPestIdentifyTask(AgriPestIdentifyTask agriPestIdentifyTask)
    {
        return agriPestIdentifyTaskMapper.updateAgriPestIdentifyTask(agriPestIdentifyTask);
    }

    /**
     * 批量删除病虫害识别任务
     *
     * @param taskIds 需要删除的主键
     * @return 结果
     */
    @Override
    public int deleteAgriPestIdentifyTaskByTaskIds(Long[] taskIds)
    {
        return agriPestIdentifyTaskMapper.deleteAgriPestIdentifyTaskByTaskIds(taskIds);
    }
}
