package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriDataAttestationVerify;
import java.util.List;

/**
 * 数据存证与校验Mapper接口
 *
 * @author ruoyi
 */
public interface AgriDataAttestationVerifyMapper
{
    public AgriDataAttestationVerify selectAgriDataAttestationVerifyByVerifyId(Long verifyId);

    public List<AgriDataAttestationVerify> selectAgriDataAttestationVerifyList(AgriDataAttestationVerify agriDataAttestationVerify);

    public int insertAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify);

    public int updateAgriDataAttestationVerify(AgriDataAttestationVerify agriDataAttestationVerify);

    public int deleteAgriDataAttestationVerifyByVerifyId(Long verifyId);

    public int deleteAgriDataAttestationVerifyByVerifyIds(Long[] verifyIds);
}
