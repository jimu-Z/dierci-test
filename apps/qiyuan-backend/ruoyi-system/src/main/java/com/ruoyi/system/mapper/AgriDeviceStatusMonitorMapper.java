package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import java.util.List;

/**
 * 设备状态监控Mapper接口
 *
 * @author ruoyi
 */
public interface AgriDeviceStatusMonitorMapper
{
    public AgriDeviceStatusMonitor selectAgriDeviceStatusMonitorByMonitorId(Long monitorId);

    public List<AgriDeviceStatusMonitor> selectAgriDeviceStatusMonitorList(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int insertAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int updateAgriDeviceStatusMonitor(AgriDeviceStatusMonitor agriDeviceStatusMonitor);

    public int deleteAgriDeviceStatusMonitorByMonitorId(Long monitorId);

    public int deleteAgriDeviceStatusMonitorByMonitorIds(Long[] monitorIds);
}
