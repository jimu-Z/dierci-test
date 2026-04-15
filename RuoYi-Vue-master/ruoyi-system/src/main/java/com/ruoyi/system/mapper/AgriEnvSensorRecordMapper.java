package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriEnvSensorRecord;
import java.util.List;

/**
 * 环境传感器数据Mapper接口
 *
 * @author ruoyi
 */
public interface AgriEnvSensorRecordMapper
{
    /**
     * 查询环境传感器数据
     *
     * @param recordId 主键
     * @return 环境传感器数据
     */
    public AgriEnvSensorRecord selectAgriEnvSensorRecordByRecordId(Long recordId);

    /**
     * 查询环境传感器数据列表
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 环境传感器数据集合
     */
    public List<AgriEnvSensorRecord> selectAgriEnvSensorRecordList(AgriEnvSensorRecord agriEnvSensorRecord);

    /**
     * 新增环境传感器数据
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 结果
     */
    public int insertAgriEnvSensorRecord(AgriEnvSensorRecord agriEnvSensorRecord);

    /**
     * 修改环境传感器数据
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 结果
     */
    public int updateAgriEnvSensorRecord(AgriEnvSensorRecord agriEnvSensorRecord);

    /**
     * 删除环境传感器数据
     *
     * @param recordId 主键
     * @return 结果
     */
    public int deleteAgriEnvSensorRecordByRecordId(Long recordId);

    /**
     * 批量删除环境传感器数据
     *
     * @param recordIds 需要删除的主键集合
     * @return 结果
     */
    public int deleteAgriEnvSensorRecordByRecordIds(Long[] recordIds);
}
