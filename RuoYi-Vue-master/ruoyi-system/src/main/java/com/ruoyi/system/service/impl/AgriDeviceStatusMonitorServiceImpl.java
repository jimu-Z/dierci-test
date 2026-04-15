package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import com.ruoyi.system.mapper.AgriDeviceStatusMonitorMapper;
import com.ruoyi.system.service.IAgriDeviceStatusMonitorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设备状态监控Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDeviceStatusMonitorServiceImpl implements IAgriDeviceStatusMonitorService
{
    @Autowired
    private AgriDeviceStatusMonitorMapper agriDeviceStatusMonitorMapper;

    @Override
    public AgriDeviceStatusMonitor selectAgriDeviceStatusMonitorByMonitorId(Long monitorId)
    {
        return agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorByMonitorId(monitorId);
    }

    @Override
    public List<AgriDeviceStatusMonitor> selectAgriDeviceStatusMonitorList(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.selectAgriDeviceStatusMonitorList(agriDeviceStatusMonitor);
    }

    @Override
    public int insertAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.insertAgriDeviceStatusMonitor(agriDeviceStatusMonitor);
    }

    @Override
    public int updateAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return agriDeviceStatusMonitorMapper.updateAgriDeviceStatusMonitor(agriDeviceStatusMonitor);
    }

    @Override
    public int deleteAgriDeviceStatusMonitorByMonitorIds(Long[] monitorIds)
    {
        return agriDeviceStatusMonitorMapper.deleteAgriDeviceStatusMonitorByMonitorIds(monitorIds);
    }
}
