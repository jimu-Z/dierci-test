package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriQualityInspectTask;
import java.util.List;

/**
 * 视觉品质检测任务Service接口
 *
 * @author ruoyi
 */
public interface IAgriQualityInspectTaskService
{
    public AgriQualityInspectTask selectAgriQualityInspectTaskByInspectId(Long inspectId);

    public List<AgriQualityInspectTask> selectAgriQualityInspectTaskList(AgriQualityInspectTask agriQualityInspectTask);

    public int insertAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask);

    public int updateAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask);

    public int deleteAgriQualityInspectTaskByInspectIds(Long[] inspectIds);
}
