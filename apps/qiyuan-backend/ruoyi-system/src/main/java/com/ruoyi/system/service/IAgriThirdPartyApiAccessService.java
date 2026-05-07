package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriThirdPartyApiAccess;
import java.util.List;

/**
 * 第三方API接入Service接口
 *
 * @author ruoyi
 */
public interface IAgriThirdPartyApiAccessService
{
    public AgriThirdPartyApiAccess selectAgriThirdPartyApiAccessByAccessId(Long accessId);

    public List<AgriThirdPartyApiAccess> selectAgriThirdPartyApiAccessList(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int insertAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int updateAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int deleteAgriThirdPartyApiAccessByAccessIds(Long[] accessIds);
}
