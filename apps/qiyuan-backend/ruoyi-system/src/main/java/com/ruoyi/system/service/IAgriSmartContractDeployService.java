package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriSmartContractDeploy;
import java.util.List;

/**
 * 智能合约部署Service接口
 *
 * @author ruoyi
 */
public interface IAgriSmartContractDeployService
{
    public AgriSmartContractDeploy selectAgriSmartContractDeployByDeployId(Long deployId);

    public List<AgriSmartContractDeploy> selectAgriSmartContractDeployList(AgriSmartContractDeploy agriSmartContractDeploy);

    public int insertAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy);

    public int updateAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy);

    public int deleteAgriSmartContractDeployByDeployIds(Long[] deployIds);
}
