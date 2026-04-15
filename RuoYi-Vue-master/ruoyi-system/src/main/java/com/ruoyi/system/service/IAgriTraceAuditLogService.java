package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriTraceAuditLog;
import java.util.List;

/**
 * 溯源审计日志Service接口
 *
 * @author ruoyi
 */
public interface IAgriTraceAuditLogService
{
    public AgriTraceAuditLog selectAgriTraceAuditLogByAuditId(Long auditId);

    public List<AgriTraceAuditLog> selectAgriTraceAuditLogList(AgriTraceAuditLog agriTraceAuditLog);

    public int insertAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog);

    public int updateAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog);

    public int deleteAgriTraceAuditLogByAuditIds(Long[] auditIds);
}
