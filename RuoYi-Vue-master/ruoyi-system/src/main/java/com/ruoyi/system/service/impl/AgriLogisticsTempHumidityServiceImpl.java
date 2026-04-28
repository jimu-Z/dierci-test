package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import com.ruoyi.system.mapper.AgriLogisticsTempHumidityMapper;
import com.ruoyi.system.service.IAgriLogisticsTempHumidityService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 物流温湿度监控Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriLogisticsTempHumidityServiceImpl implements IAgriLogisticsTempHumidityService
{
    @Autowired
    private AgriLogisticsTempHumidityMapper agriLogisticsTempHumidityMapper;

    @Override
    public AgriLogisticsTempHumidity selectAgriLogisticsTempHumidityByRecordId(Long recordId)
    {
        return agriLogisticsTempHumidityMapper.selectAgriLogisticsTempHumidityByRecordId(recordId);
    }

    @Override
    public List<AgriLogisticsTempHumidity> selectAgriLogisticsTempHumidityList(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        return agriLogisticsTempHumidityMapper.selectAgriLogisticsTempHumidityList(agriLogisticsTempHumidity);
    }

    @Override
    public List<AgriLogisticsTempHumidity> selectByTraceCodeWithTimeRange(String traceCode, String startTime, String endTime)
    {
        return agriLogisticsTempHumidityMapper.selectByTraceCodeWithTimeRange(traceCode, startTime, endTime);
    }

    @Override
    public int insertAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        applyAlert(agriLogisticsTempHumidity);
        return agriLogisticsTempHumidityMapper.insertAgriLogisticsTempHumidity(agriLogisticsTempHumidity);
    }

    @Override
    public int updateAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        applyAlert(agriLogisticsTempHumidity);
        return agriLogisticsTempHumidityMapper.updateAgriLogisticsTempHumidity(agriLogisticsTempHumidity);
    }

    @Override
    public int deleteAgriLogisticsTempHumidityByRecordIds(Long[] recordIds)
    {
        return agriLogisticsTempHumidityMapper.deleteAgriLogisticsTempHumidityByRecordIds(recordIds);
    }

    private void applyAlert(AgriLogisticsTempHumidity record)
    {
        List<String> reasons = new ArrayList<>();
        if (record.getTemperature() != null)
        {
            if (record.getTempUpperLimit() != null && record.getTemperature().compareTo(record.getTempUpperLimit()) > 0)
            {
                reasons.add("温度超过上限");
            }
            if (record.getTempLowerLimit() != null && record.getTemperature().compareTo(record.getTempLowerLimit()) < 0)
            {
                reasons.add("温度低于下限");
            }
        }
        if (record.getHumidity() != null)
        {
            if (record.getHumidityUpperLimit() != null && record.getHumidity().compareTo(record.getHumidityUpperLimit()) > 0)
            {
                reasons.add("湿度超过上限");
            }
            if (record.getHumidityLowerLimit() != null && record.getHumidity().compareTo(record.getHumidityLowerLimit()) < 0)
            {
                reasons.add("湿度低于下限");
            }
        }
        if (reasons.isEmpty())
        {
            record.setAlertFlag("0");
            if (record.getAlertMessage() == null || record.getAlertMessage().isBlank())
            {
                record.setAlertMessage("正常");
            }
            return;
        }
        record.setAlertFlag("1");
        record.setAlertMessage(String.join(";", reasons));
    }
}
