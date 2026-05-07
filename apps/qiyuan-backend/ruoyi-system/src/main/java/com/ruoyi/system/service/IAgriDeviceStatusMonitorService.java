package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import java.util.List;
import java.util.Map;

/**
 * 设备状态监控Service接口
 *
 * @author ruoyi
 */
public interface IAgriDeviceStatusMonitorService
{
    public AgriDeviceStatusMonitor selectAgriDeviceStatusMonitorByMonitorId(Long monitorId);

    public List<AgriDeviceStatusMonitor> selectAgriDeviceStatusMonitorList(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public Map<String, Object> selectAgriDeviceStatusMonitorDashboard(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public Map<String, Object> selectAgriDeviceStatusMonitorAlerts(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int insertAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int updateAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int deleteAgriDeviceStatusMonitorByMonitorIds(Long[] monitorIds);
}
