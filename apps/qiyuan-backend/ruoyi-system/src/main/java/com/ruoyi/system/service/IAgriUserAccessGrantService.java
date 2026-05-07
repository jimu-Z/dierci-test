package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriUserAccessGrant;
import java.util.List;

/**
 * 用户权限管理Service接口
 *
 * @author ruoyi
 */
public interface IAgriUserAccessGrantService
{
    public AgriUserAccessGrant selectAgriUserAccessGrantByGrantId(Long grantId);

    public List<AgriUserAccessGrant> selectAgriUserAccessGrantList(AgriUserAccessGrant agriUserAccessGrant);

    public int insertAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant);

    public int updateAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant);

    public int deleteAgriUserAccessGrantByGrantIds(Long[] grantIds);
}
