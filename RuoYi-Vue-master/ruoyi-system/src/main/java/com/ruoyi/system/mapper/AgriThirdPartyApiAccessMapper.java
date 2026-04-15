package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriThirdPartyApiAccess;
import java.util.List;

/**
 * 第三方API接入Mapper接口
 *
 * @author ruoyi
 */
public interface AgriThirdPartyApiAccessMapper
{
    public AgriThirdPartyApiAccess selectAgriThirdPartyApiAccessByAccessId(Long accessId);

    public List<AgriThirdPartyApiAccess> selectAgriThirdPartyApiAccessList(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int insertAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int updateAgriThirdPartyApiAccess(AgriThirdPartyApiAccess agriThirdPartyApiAccess);

    public int deleteAgriThirdPartyApiAccessByAccessId(Long accessId);

    public int deleteAgriThirdPartyApiAccessByAccessIds(Long[] accessIds);
}
