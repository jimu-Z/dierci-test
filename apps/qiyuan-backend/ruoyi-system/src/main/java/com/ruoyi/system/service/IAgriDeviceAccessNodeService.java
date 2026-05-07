package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDeviceAccessNode;
import java.util.List;

/**
 * 设备接入管理Service接口
 *
 * @author ruoyi
 */
public interface IAgriDeviceAccessNodeService
{
    public AgriDeviceAccessNode selectAgriDeviceAccessNodeByNodeId(Long nodeId);

    public List<AgriDeviceAccessNode> selectAgriDeviceAccessNodeList(AgriDeviceAccessNode agriDeviceAccessNode);

    public int insertAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int updateAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int deleteAgriDeviceAccessNodeByNodeIds(Long[] nodeIds);
}
