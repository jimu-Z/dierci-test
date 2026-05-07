package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriFinanceRiskMetric;
import java.util.List;

/**
 * 农业金融风控指标Service接口
 *
 * @author ruoyi
 */
public interface IAgriFinanceRiskMetricService
{
    public AgriFinanceRiskMetric selectAgriFinanceRiskMetricByRiskId(Long riskId);

    public List<AgriFinanceRiskMetric> selectAgriFinanceRiskMetricList(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int insertAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int updateAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int deleteAgriFinanceRiskMetricByRiskIds(Long[] riskIds);
}
