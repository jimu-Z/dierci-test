package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriFinanceRiskMetric;
import com.ruoyi.system.mapper.AgriFinanceRiskMetricMapper;
import com.ruoyi.system.service.IAgriFinanceRiskMetricService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 农业金融风控指标Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriFinanceRiskMetricServiceImpl implements IAgriFinanceRiskMetricService
{
    @Autowired
    private AgriFinanceRiskMetricMapper agriFinanceRiskMetricMapper;

    @Override
    public AgriFinanceRiskMetric selectAgriFinanceRiskMetricByRiskId(Long riskId)
    {
        return agriFinanceRiskMetricMapper.selectAgriFinanceRiskMetricByRiskId(riskId);
    }

    @Override
    public List<AgriFinanceRiskMetric> selectAgriFinanceRiskMetricList(AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return agriFinanceRiskMetricMapper.selectAgriFinanceRiskMetricList(agriFinanceRiskMetric);
    }

    @Override
    public int insertAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return agriFinanceRiskMetricMapper.insertAgriFinanceRiskMetric(agriFinanceRiskMetric);
    }

    @Override
    public int updateAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric)
    {
        return agriFinanceRiskMetricMapper.updateAgriFinanceRiskMetric(agriFinanceRiskMetric);
    }

    @Override
    public int deleteAgriFinanceRiskMetricByRiskIds(Long[] riskIds)
    {
        return agriFinanceRiskMetricMapper.deleteAgriFinanceRiskMetricByRiskIds(riskIds);
    }
}
