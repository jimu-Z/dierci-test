package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriDataAttestationVerify;
import java.util.List;

/**
 * 数据存证与校验Service接口
 *
 * @author ruoyi
 */
public interface IAgriDataAttestationVerifyService
{
    public AgriDataAttestationVerify selectAgriDataAttestationVerifyByVerifyId(Long verifyId);

    public List<AgriDataAttestationVerify> selectAgriDataAttestationVerifyList(AgriDataAttestationVerify agriDataAttestationVerify);

    public int insertAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify);

    public int updateAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify);

    public int deleteAgriDataAttestationVerifyByVerifyIds(Long[] verifyIds);
}
