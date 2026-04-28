package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import java.util.List;

/**
 * 物流温湿度监控Service接口
 *
 * @author ruoyi
 */
public interface IAgriLogisticsTempHumidityService
{
    public AgriLogisticsTempHumidity selectAgriLogisticsTempHumidityByRecordId(Long recordId);

    public List<AgriLogisticsTempHumidity> selectAgriLogisticsTempHumidityList(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public List<AgriLogisticsTempHumidity> selectByTraceCodeWithTimeRange(String traceCode, String startTime, String endTime);

    public int insertAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public int updateAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public int deleteAgriLogisticsTempHumidityByRecordIds(Long[] recordIds);
}
