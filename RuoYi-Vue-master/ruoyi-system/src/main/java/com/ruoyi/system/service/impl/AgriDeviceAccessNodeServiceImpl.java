package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDeviceAccessNode;
import com.ruoyi.system.mapper.AgriDeviceAccessNodeMapper;
import com.ruoyi.system.service.IAgriDeviceAccessNodeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设备接入管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDeviceAccessNodeServiceImpl implements IAgriDeviceAccessNodeService
{
    @Autowired
    private AgriDeviceAccessNodeMapper agriDeviceAccessNodeMapper;

    @Override
    public AgriDeviceAccessNode selectAgriDeviceAccessNodeByNodeId(Long nodeId)
    {
        return agriDeviceAccessNodeMapper.selectAgriDeviceAccessNodeByNodeId(nodeId);
    }

    @Override
    public List<AgriDeviceAccessNode> selectAgriDeviceAccessNodeList(AgriDeviceAccessNode agriDeviceAccessNode)
    {
        return agriDeviceAccessNodeMapper.selectAgriDeviceAccessNodeList(agriDeviceAccessNode);
    }

    @Override
    public int insertAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode)
    {
        return agriDeviceAccessNodeMapper.insertAgriDeviceAccessNode(agriDeviceAccessNode);
    }

    @Override
    public int updateAgriDeviceAccessNode(AgriDeviceAccessNode agriDeviceAccessNode)
    {
        return agriDeviceAccessNodeMapper.updateAgriDeviceAccessNode(agriDeviceAccessNode);
    }

    @Override
    public int deleteAgriDeviceAccessNodeByNodeIds(Long[] nodeIds)
    {
        return agriDeviceAccessNodeMapper.deleteAgriDeviceAccessNodeByNodeIds(nodeIds);
    }
}
