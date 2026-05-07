package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriSmartContractDeploy;
import java.util.List;

/**
 * 智能合约部署Mapper接口
 *
 * @author ruoyi
 */
public interface AgriSmartContractDeployMapper
{
    public AgriSmartContractDeploy selectAgriSmartContractDeployByDeployId(Long deployId);

    public List<AgriSmartContractDeploy> selectAgriSmartContractDeployList(AgriSmartContractDeploy agriSmartContractDeploy);

    public int insertAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy);

    public int updateAgriSmartContractDeploy(AgriSmartContractDeploy agriSmartContractDeploy);

    public int deleteAgriSmartContractDeployByDeployId(Long deployId);

    public int deleteAgriSmartContractDeployByDeployIds(Long[] deployIds);
}
