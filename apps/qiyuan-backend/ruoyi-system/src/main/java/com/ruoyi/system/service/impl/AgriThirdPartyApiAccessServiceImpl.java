package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriThirdPartyApiAccess;
import com.ruoyi.system.mapper.AgriThirdPartyApiAccessMapper;
import com.ruoyi.system.service.IAgriThirdPartyApiAccessService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 第三方API接入Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriThirdPartyApiAccessServiceImpl implements IAgriThirdPartyApiAccessService
{
    @Autowired
    private AgriThirdPartyApiAccessMapper agriThirdPartyApiAccessMapper;

    @Override
    public AgriThirdPartyApiAccess selectAgriThirdPartyApiAccessByAccessId(Long accessId)
    {
        return agriThirdPartyApiAccessMapper.selectAgriThirdPartyApiAccessByAccessId(accessId);
    }

    @Override
    public List<AgriThirdPartyApiAccess> selectAgriThirdPartyApiAccessList(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return agriThirdPartyApiAccessMapper.selectAgriThirdPartyApiAccessList(agriThirdPartyApiAccess);
    }

    @Override
    public int insertAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return agriThirdPartyApiAccessMapper.insertAgriThirdPartyApiAccess(agriThirdPartyApiAccess);
    }

    @Override
    public int updateAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return agriThirdPartyApiAccessMapper.updateAgriThirdPartyApiAccess(agriThirdPartyApiAccess);
    }

    @Override
    public int deleteAgriThirdPartyApiAccessByAccessIds(Long[] accessIds)
    {
        return agriThirdPartyApiAccessMapper.deleteAgriThirdPartyApiAccessByAccessIds(accessIds);
    }
}
