package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriSupplyChainContract;
import java.util.List;

/**
 * 供应链金融合约管理Service接口
 *
 * @author ruoyi
 */
public interface IAgriSupplyChainContractService
{
    public AgriSupplyChainContract selectAgriSupplyChainContractByContractId(Long contractId);

    public List<AgriSupplyChainContract> selectAgriSupplyChainContractList(AgriSupplyChainContract agriSupplyChainContract);

    public int insertAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract);

    public int updateAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract);

    public int deleteAgriSupplyChainContractByContractIds(Long[] contractIds);
}
