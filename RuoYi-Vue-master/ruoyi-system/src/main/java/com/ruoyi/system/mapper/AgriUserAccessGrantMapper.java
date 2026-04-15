package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriUserAccessGrant;
import java.util.List;

/**
 * 用户权限管理Mapper接口
 *
 * @author ruoyi
 */
public interface AgriUserAccessGrantMapper
{
    public AgriUserAccessGrant selectAgriUserAccessGrantByGrantId(Long grantId);

    public List<AgriUserAccessGrant> selectAgriUserAccessGrantList(AgriUserAccessGrant agriUserAccessGrant);

    public int insertAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant);

    public int updateAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant);

    public int deleteAgriUserAccessGrantByGrantId(Long grantId);

    public int deleteAgriUserAccessGrantByGrantIds(Long[] grantIds);
}
