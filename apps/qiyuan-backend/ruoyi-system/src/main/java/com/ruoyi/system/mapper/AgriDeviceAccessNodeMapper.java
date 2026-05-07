package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriDeviceAccessNode;
import java.util.List;

/**
 * 设备接入管理Mapper接口
 *
 * @author ruoyi
 */
public interface AgriDeviceAccessNodeMapper
{
    public AgriDeviceAccessNode selectAgriDeviceAccessNodeByNodeId(Long nodeId);

    public List<AgriDeviceAccessNode> selectAgriDeviceAccessNodeList(AgriDeviceAccessNode agriDeviceAccessNode);

    public int insertAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int updateAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int deleteAgriDeviceAccessNodeByNodeId(Long nodeId);

    public int deleteAgriDeviceAccessNodeByNodeIds(Long[] nodeIds);
}
