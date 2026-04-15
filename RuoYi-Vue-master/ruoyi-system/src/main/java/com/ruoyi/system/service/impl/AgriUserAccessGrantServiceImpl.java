package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriUserAccessGrant;
import com.ruoyi.system.mapper.AgriUserAccessGrantMapper;
import com.ruoyi.system.service.IAgriUserAccessGrantService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户权限管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriUserAccessGrantServiceImpl implements IAgriUserAccessGrantService
{
    @Autowired
    private AgriUserAccessGrantMapper agriUserAccessGrantMapper;

    @Override
    public AgriUserAccessGrant selectAgriUserAccessGrantByGrantId(Long grantId)
    {
        return agriUserAccessGrantMapper.selectAgriUserAccessGrantByGrantId(grantId);
    }

    @Override
    public List<AgriUserAccessGrant> selectAgriUserAccessGrantList(AgriUserAccessGrant agriUserAccessGrant)
    {
        return agriUserAccessGrantMapper.selectAgriUserAccessGrantList(agriUserAccessGrant);
    }

    @Override
    public int insertAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant)
    {
        return agriUserAccessGrantMapper.insertAgriUserAccessGrant(agriUserAccessGrant);
    }

    @Override
    public int updateAgriUserAccessGrant(AgriUserAccessGrant agriUserAccessGrant)
    {
        return agriUserAccessGrantMapper.updateAgriUserAccessGrant(agriUserAccessGrant);
    }

    @Override
    public int deleteAgriUserAccessGrantByGrantIds(Long[] grantIds)
    {
        return agriUserAccessGrantMapper.deleteAgriUserAccessGrantByGrantIds(grantIds);
    }
}
