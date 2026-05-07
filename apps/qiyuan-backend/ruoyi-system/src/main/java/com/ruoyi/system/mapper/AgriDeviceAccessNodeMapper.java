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

    /**
     * 按设备编码精确查询（Webhook 等设备标识须唯一命中，不使用模糊查询）。
     */
    public AgriDeviceAccessNode selectAgriDeviceAccessNodeByDeviceCode(String deviceCode);

    public List<AgriDeviceAccessNode> selectAgriDeviceAccessNodeList(AgriDeviceAccessNode agriDeviceAccessNode);

    public int insertAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int updateAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode);

    public int deleteAgriDeviceAccessNodeByNodeId(Long nodeId);

    public int deleteAgriDeviceAccessNodeByNodeIds(Long[] nodeIds);
}
