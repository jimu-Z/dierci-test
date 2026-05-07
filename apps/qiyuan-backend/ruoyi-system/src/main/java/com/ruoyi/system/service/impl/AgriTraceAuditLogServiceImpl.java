package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriTraceAuditLog;
import com.ruoyi.system.mapper.AgriTraceAuditLogMapper;
import com.ruoyi.system.service.IAgriTraceAuditLogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 溯源审计日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriTraceAuditLogServiceImpl implements IAgriTraceAuditLogService
{
    @Autowired
    private AgriTraceAuditLogMapper agriTraceAuditLogMapper;

    @Override
    public AgriTraceAuditLog selectAgriTraceAuditLogByAuditId(Long auditId)
    {
        return agriTraceAuditLogMapper.selectAgriTraceAuditLogByAuditId(auditId);
    }

    @Override
    public List<AgriTraceAuditLog> selectAgriTraceAuditLogList(AgriTraceAuditLog agriTraceAuditLog)
    {
        return agriTraceAuditLogMapper.selectAgriTraceAuditLogList(agriTraceAuditLog);
    }

    @Override
    public int insertAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog)
    {
        return agriTraceAuditLogMapper.insertAgriTraceAuditLog(agriTraceAuditLog);
    }

    @Override
    public int updateAgriTraceAuditLog(AgriTraceAuditLog agriTraceAuditLog)
    {
        return agriTraceAuditLogMapper.updateAgriTraceAuditLog(agriTraceAuditLog);
    }

    @Override
    public int deleteAgriTraceAuditLogByAuditIds(Long[] auditIds)
    {
        return agriTraceAuditLogMapper.deleteAgriTraceAuditLogByAuditIds(auditIds);
    }
}
