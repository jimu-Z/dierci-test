package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriQualityInspectTask;
import java.util.List;

/**
 * 视觉品质检测任务Mapper接口
 *
 * @author ruoyi
 */
public interface AgriQualityInspectTaskMapper
{
    public AgriQualityInspectTask selectAgriQualityInspectTaskByInspectId(Long inspectId);

    public List<AgriQualityInspectTask> selectAgriQualityInspectTaskList(AgriQualityInspectTask agriQualityInspectTask);

    public int insertAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask);

    public int updateAgriQualityInspectTask(AgriQualityInspectTask agriQualityInspectTask);

    public int deleteAgriQualityInspectTaskByInspectId(Long inspectId);

    public int deleteAgriQualityInspectTaskByInspectIds(Long[] inspectIds);
}
