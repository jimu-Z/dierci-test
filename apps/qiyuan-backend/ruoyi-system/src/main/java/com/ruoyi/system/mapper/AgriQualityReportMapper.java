package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriQualityReport;
import java.util.List;

/**
 * 质检报告Mapper接口
 *
 * @author ruoyi
 */
public interface AgriQualityReportMapper
{
    public AgriQualityReport selectAgriQualityReportByReportId(Long reportId);

    public List<AgriQualityReport> selectAgriQualityReportList(AgriQualityReport agriQualityReport);

    public int insertAgriQualityReport(AgriQualityReport agriQualityReport);

    public int updateAgriQualityReport(AgriQualityReport agriQualityReport);

    public int deleteAgriQualityReportByReportId(Long reportId);

    public int deleteAgriQualityReportByReportIds(Long[] reportIds);

    public AgriQualityReport selectAgriQualityReportByInspectId(Long inspectId);
}
