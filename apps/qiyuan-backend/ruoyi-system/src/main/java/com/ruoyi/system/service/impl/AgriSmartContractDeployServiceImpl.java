package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriSmartContractDeploy;
import com.ruoyi.system.mapper.AgriSmartContractDeployMapper;
import com.ruoyi.system.service.IAgriSmartContractDeployService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 智能合约部署Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriSmartContractDeployServiceImpl implements IAgriSmartContractDeployService
{
    @Autowired
    private AgriSmartContractDeployMapper agriSmartContractDeployMapper;

    @Override
    public AgriSmartContractDeploy selectAgriSmartContractDeployByDeployId(Long deployId)
    {
        return agriSmartContractDeployMapper.selectAgriSmartContractDeployByDeployId(deployId);
    }

    @Override
    public List<AgriSmartContractDeploy> selectAgriSmartContractDeployList(AgriSmartContractDeploy agriSmartContractDeploy)
    {
        return agriSmartContractDeployMapper.selectAgriSmartContractDeployList(agriSmartContractDeploy);
    }

    @Override
    public int insertAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy)
    {
        return agriSmartContractDeployMapper.insertAgriSmartContractDeploy(agriSmartContractDeploy);
    }

    @Override
    public int updateAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy)
    {
        return agriSmartContractDeployMapper.updateAgriSmartContractDeploy(agriSmartContractDeploy);
    }

    @Override
    public int deleteAgriSmartContractDeployByDeployIds(Long[] deployIds)
    {
        return agriSmartContractDeployMapper.deleteAgriSmartContractDeployByDeployIds(deployIds);
    }
}
