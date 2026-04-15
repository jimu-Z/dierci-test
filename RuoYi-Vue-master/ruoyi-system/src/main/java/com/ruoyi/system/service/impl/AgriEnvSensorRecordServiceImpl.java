package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriEnvSensorRecord;
import com.ruoyi.system.mapper.AgriEnvSensorRecordMapper;
import com.ruoyi.system.service.IAgriEnvSensorRecordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 环境传感器数据Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriEnvSensorRecordServiceImpl implements IAgriEnvSensorRecordService
{
    @Autowired
    private AgriEnvSensorRecordMapper agriEnvSensorRecordMapper;

    /**
     * 查询环境传感器数据
     *
     * @param recordId 主键
     * @return 环境传感器数据
     */
    @Override
    public AgriEnvSensorRecord selectAgriEnvSensorRecordByRecordId(Long recordId)
    {
        return agriEnvSensorRecordMapper.selectAgriEnvSensorRecordByRecordId(recordId);
    }

    /**
     * 查询环境传感器数据列表
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 环境传感器数据
     */
    @Override
    public List<AgriEnvSensorRecord> selectAgriEnvSensorRecordList(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        return agriEnvSensorRecordMapper.selectAgriEnvSensorRecordList(agriEnvSensorRecord);
    }

    /**
     * 新增环境传感器数据
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 结果
     */
    @Override
    public int insertAgriEnvSensorRecord(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        return agriEnvSensorRecordMapper.insertAgriEnvSensorRecord(agriEnvSensorRecord);
    }

    /**
     * 修改环境传感器数据
     *
     * @param agriEnvSensorRecord 环境传感器数据
     * @return 结果
     */
    @Override
    public int updateAgriEnvSensorRecord(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        return agriEnvSensorRecordMapper.updateAgriEnvSensorRecord(agriEnvSensorRecord);
    }

    /**
     * 批量删除环境传感器数据
     *
     * @param recordIds 需要删除的主键
     * @return 结果
     */
    @Override
    public int deleteAgriEnvSensorRecordByRecordIds(Long[] recordIds)
    {
        return agriEnvSensorRecordMapper.deleteAgriEnvSensorRecordByRecordIds(recordIds);
    }
}
