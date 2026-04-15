package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriDataAttestationVerify;
import com.ruoyi.system.mapper.AgriDataAttestationVerifyMapper;
import com.ruoyi.system.service.IAgriDataAttestationVerifyService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据存证与校验Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriDataAttestationVerifyServiceImpl implements IAgriDataAttestationVerifyService
{
    @Autowired
    private AgriDataAttestationVerifyMapper agriDataAttestationVerifyMapper;

    @Override
    public AgriDataAttestationVerify selectAgriDataAttestationVerifyByVerifyId(Long verifyId)
    {
        return agriDataAttestationVerifyMapper.selectAgriDataAttestationVerifyByVerifyId(verifyId);
    }

    @Override
    public List<AgriDataAttestationVerify> selectAgriDataAttestationVerifyList(AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return agriDataAttestationVerifyMapper.selectAgriDataAttestationVerifyList(agriDataAttestationVerify);
    }

    @Override
    public int insertAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return agriDataAttestationVerifyMapper.insertAgriDataAttestationVerify(agriDataAttestationVerify);
    }

    @Override
    public int updateAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return agriDataAttestationVerifyMapper.updateAgriDataAttestationVerify(agriDataAttestationVerify);
    }

    @Override
    public int deleteAgriDataAttestationVerifyByVerifyIds(Long[] verifyIds)
    {
        return agriDataAttestationVerifyMapper.deleteAgriDataAttestationVerifyByVerifyIds(verifyIds);
    }
}
