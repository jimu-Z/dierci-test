package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriPestIdentifyTask;
import java.util.List;

/**
 * 病虫害识别任务Mapper接口
 *
 * @author ruoyi
 */
public interface AgriPestIdentifyTaskMapper
{
    /**
     * 查询病虫害识别任务
     *
     * @param taskId 主键
     * @return 病虫害识别任务
     */
    public AgriPestIdentifyTask selectAgriPestIdentifyTaskByTaskId(Long taskId);

    /**
     * 查询病虫害识别任务列表
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 病虫害识别任务集合
     */
    public List<AgriPestIdentifyTask> selectAgriPestIdentifyTaskList(AgriPestIdentifyTask agriPestIdentifyTask);

    /**
     * 新增病虫害识别任务
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 结果
     */
    public int insertAgriPestIdentifyTask(AgriPestIdentifyTask agriPestIdentifyTask);

    /**
     * 修改病虫害识别任务
     *
     * @param agriPestIdentifyTask 病虫害识别任务
     * @return 结果
     */
    public int updateAgriPestIdentifyTask(AgriPestIdentifyTask agriPestIdentifyTask);

    /**
     * 删除病虫害识别任务
     *
     * @param taskId 主键
     * @return 结果
     */
    public int deleteAgriPestIdentifyTaskByTaskId(Long taskId);

    /**
     * 批量删除病虫害识别任务
     *
     * @param taskIds 需要删除的主键集合
     * @return 结果
     */
    public int deleteAgriPestIdentifyTaskByTaskIds(Long[] taskIds);
}
