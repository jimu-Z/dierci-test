package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriTraceAuditLog;
import java.util.List;

/**
 * 溯源审计日志Mapper接口
 *
 * @author ruoyi
 */
public interface AgriTraceAuditLogMapper
{
    public AgriTraceAuditLog selectAgriTraceAuditLogByAuditId(Long auditId);

    public List<AgriTraceAuditLog> selectAgriTraceAuditLogList(AgriTraceAuditLog agriTraceAuditLog);

    public int insertAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog);

    public int updateAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog);

    public int deleteAgriTraceAuditLogByAuditId(Long auditId);

    public int deleteAgriTraceAuditLogByAuditIds(Long[] auditIds);
}
