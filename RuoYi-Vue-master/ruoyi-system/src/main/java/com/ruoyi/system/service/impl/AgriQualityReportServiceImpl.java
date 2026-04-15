package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriQualityReport;
import com.ruoyi.system.mapper.AgriQualityReportMapper;
import com.ruoyi.system.service.IAgriQualityReportService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 质检报告Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriQualityReportServiceImpl implements IAgriQualityReportService
{
    @Autowired
    private AgriQualityReportMapper agriQualityReportMapper;

    @Override
    public AgriQualityReport selectAgriQualityReportByReportId(Long reportId)
    {
        return agriQualityReportMapper.selectAgriQualityReportByReportId(reportId);
    }

    @Override
    public List<AgriQualityReport> selectAgriQualityReportList(AgriQualityReport agriQualityReport)
    {
        return agriQualityReportMapper.selectAgriQualityReportList(agriQualityReport);
    }

    @Override
    public int insertAgriQualityReport(AgriQualityReport agriQualityReport)
    {
        return agriQualityReportMapper.insertAgriQualityReport(agriQualityReport);
    }

    @Override
    public int updateAgriQualityReport(AgriQualityReport agriQualityReport)
    {
        return agriQualityReportMapper.updateAgriQualityReport(agriQualityReport);
    }

    @Override
    public int deleteAgriQualityReportByReportIds(Long[] reportIds)
    {
        return agriQualityReportMapper.deleteAgriQualityReportByReportIds(reportIds);
    }

    @Override
    public AgriQualityReport selectAgriQualityReportByInspectId(Long inspectId)
    {
        return agriQualityReportMapper.selectAgriQualityReportByInspectId(inspectId);
    }
}
