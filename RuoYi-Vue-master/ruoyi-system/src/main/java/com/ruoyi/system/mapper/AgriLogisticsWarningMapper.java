package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriLogisticsWarning;
import java.util.List;

/**
 * 在途异常预警Mapper接口
 *
 * @author ruoyi
 */
public interface AgriLogisticsWarningMapper
{
    public AgriLogisticsWarning selectAgriLogisticsWarningByWarningId(Long warningId);

    public List<AgriLogisticsWarning> selectAgriLogisticsWarningList(AgriLogisticsWarning agriLogisticsWarning);

    public int insertAgriLogisticsWarning(AgriLogisticsWarning agriLogisticsWarning);

    public int updateAgriLogisticsWarning(AgriLogisticsWarning agriLogisticsWarning);

    public int deleteAgriLogisticsWarningByWarningId(Long warningId);

    public int deleteAgriLogisticsWarningByWarningIds(Long[] warningIds);

    public AgriLogisticsWarning selectAgriLogisticsWarningBySourceRecordId(Long sourceRecordId);
}
