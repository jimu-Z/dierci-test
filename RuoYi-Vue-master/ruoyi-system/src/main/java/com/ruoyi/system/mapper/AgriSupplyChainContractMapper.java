package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriSupplyChainContract;
import java.util.List;

/**
 * 供应链金融合约管理Mapper接口
 *
 * @author ruoyi
 */
public interface AgriSupplyChainContractMapper
{
    public AgriSupplyChainContract selectAgriSupplyChainContractByContractId(Long contractId);

    public List<AgriSupplyChainContract> selectAgriSupplyChainContractList(AgriSupplyChainContract agriSupplyChainContract);

    public int insertAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract);

    public int updateAgriSupplyChainContract(AgriSupplyChainContract agriSupplyChainContract);

    public int deleteAgriSupplyChainContractByContractId(Long contractId);

    public int deleteAgriSupplyChainContractByContractIds(Long[] contractIds);
}
