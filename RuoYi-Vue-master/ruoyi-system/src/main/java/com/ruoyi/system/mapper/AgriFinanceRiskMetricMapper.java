package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriFinanceRiskMetric;
import java.util.List;

/**
 * 农业金融风控指标Mapper接口
 *
 * @author ruoyi
 */
public interface AgriFinanceRiskMetricMapper
{
    public AgriFinanceRiskMetric selectAgriFinanceRiskMetricByRiskId(Long riskId);

    public List<AgriFinanceRiskMetric> selectAgriFinanceRiskMetricList(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int insertAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int updateAgriFinanceRiskMetric(AgriFinanceRiskMetric agriFinanceRiskMetric);

    public int deleteAgriFinanceRiskMetricByRiskId(Long riskId);

    public int deleteAgriFinanceRiskMetricByRiskIds(Long[] riskIds);
}
