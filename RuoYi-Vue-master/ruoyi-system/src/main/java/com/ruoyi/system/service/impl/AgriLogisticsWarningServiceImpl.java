package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriLogisticsWarning;
import com.ruoyi.system.mapper.AgriLogisticsWarningMapper;
import com.ruoyi.system.service.IAgriLogisticsWarningService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 在途异常预警Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriLogisticsWarningServiceImpl implements IAgriLogisticsWarningService
{
    @Autowired
    private AgriLogisticsWarningMapper agriLogisticsWarningMapper;

    @Override
    public AgriLogisticsWarning selectAgriLogisticsWarningByWarningId(Long warningId)
    {
        return agriLogisticsWarningMapper.selectAgriLogisticsWarningByWarningId(warningId);
    }

    @Override
    public List<AgriLogisticsWarning> selectAgriLogisticsWarningList(AgriLogisticsWarning agriLogisticsWarning)
    {
        return agriLogisticsWarningMapper.selectAgriLogisticsWarningList(agriLogisticsWarning);
    }

    @Override
    public int insertAgriLogisticsWarning(AgriLogisticsWarning agriLogisticsWarning)
    {
        return agriLogisticsWarningMapper.insertAgriLogisticsWarning(agriLogisticsWarning);
    }

    @Override
    public int updateAgriLogisticsWarning(AgriLogisticsWarning agriLogisticsWarning)
    {
        return agriLogisticsWarningMapper.updateAgriLogisticsWarning(agriLogisticsWarning);
    }

    @Override
    public int deleteAgriLogisticsWarningByWarningIds(Long[] warningIds)
    {
        return agriLogisticsWarningMapper.deleteAgriLogisticsWarningByWarningIds(warningIds);
    }

    @Override
    public AgriLogisticsWarning selectAgriLogisticsWarningBySourceRecordId(Long sourceRecordId)
    {
        return agriLogisticsWarningMapper.selectAgriLogisticsWarningBySourceRecordId(sourceRecordId);
    }
}
