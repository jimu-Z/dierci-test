package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriQualityReport;
import java.util.List;

/**
 * 质检报告Service接口
 *
 * @author ruoyi
 */
public interface IAgriQualityReportService
{
    public AgriQualityReport selectAgriQualityReportByReportId(Long reportId);

    public List<AgriQualityReport> selectAgriQualityReportList(AgriQualityReport agriQualityReport);

    public int insertAgriQualityReport(AgriQualityReport agriQualityReport);

    public int updateAgriQualityReport(AgriQualityReport agriQualityReport);

    public int deleteAgriQualityReportByReportIds(Long[] reportIds);

    public AgriQualityReport selectAgriQualityReportByInspectId(Long inspectId);
}
