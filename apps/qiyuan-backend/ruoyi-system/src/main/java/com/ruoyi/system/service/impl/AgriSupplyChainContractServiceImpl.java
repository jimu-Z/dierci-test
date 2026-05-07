package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriSupplyChainContract;
import com.ruoyi.system.mapper.AgriSupplyChainContractMapper;
import com.ruoyi.system.service.IAgriSupplyChainContractService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 供应链金融合约管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriSupplyChainContractServiceImpl implements IAgriSupplyChainContractService
{
    @Autowired
    private AgriSupplyChainContractMapper agriSupplyChainContractMapper;

    @Override
    public AgriSupplyChainContract selectAgriSupplyChainContractByContractId(Long contractId)
    {
        return agriSupplyChainContractMapper.selectAgriSupplyChainContractByContractId(contractId);
    }

    @Override
    public List<AgriSupplyChainContract> selectAgriSupplyChainContractList(AgriSupplyChainContract agriSupplyChainContract)
    {
        return agriSupplyChainContractMapper.selectAgriSupplyChainContractList(agriSupplyChainContract);
    }

    @Override
    public int insertAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract)
    {
        return agriSupplyChainContractMapper.insertAgriSupplyChainContract(agriSupplyChainContract);
    }

    @Override
    public int updateAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract)
    {
        return agriSupplyChainContractMapper.updateAgriSupplyChainContract(agriSupplyChainContract);
    }

    @Override
    public int deleteAgriSupplyChainContractByContractIds(Long[] contractIds)
    {
        return agriSupplyChainContractMapper.deleteAgriSupplyChainContractByContractIds(contractIds);
    }
}
