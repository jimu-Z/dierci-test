package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import java.util.List;

/**
 * 物流温湿度监控Mapper接口
 *
 * @author ruoyi
 */
public interface AgriLogisticsTempHumidityMapper
{
    public AgriLogisticsTempHumidity selectAgriLogisticsTempHumidityByRecordId(Long recordId);

    public List<AgriLogisticsTempHumidity> selectAgriLogisticsTempHumidityList(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public int insertAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public int updateAgriLogisticsTempHumidity(AgriLogisticsTempHumidity agriLogisticsTempHumidity);

    public int deleteAgriLogisticsTempHumidityByRecordId(Long recordId);

    public int deleteAgriLogisticsTempHumidityByRecordIds(Long[] recordIds);
}
