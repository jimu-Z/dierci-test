package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import java.util.List;

/**
 * 设备状态监控Service接口
 *
 * @author ruoyi
 */
public interface IAgriDeviceStatusMonitorService
{
    public AgriDeviceStatusMonitor selectAgriDeviceStatusMonitorByMonitorId(Long monitorId);

    public List<AgriDeviceStatusMonitor> selectAgriDeviceStatusMonitorList(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int insertAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int updateAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int deleteAgriDeviceStatusMonitorByMonitorIds(Long[] monitorIds);
}
