package com.ruoyi.system.service.impl;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AgriBaseRule;
import com.ruoyi.system.mapper.AgriBaseRuleMapper;
import com.ruoyi.system.service.IAgriBaseRuleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 启元农链基础规则配置Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriBaseRuleServiceImpl implements IAgriBaseRuleService
{
    @Autowired
    private AgriBaseRuleMapper agriBaseRuleMapper;

    /**
     * 查询基础规则配置
     *
     * @param ruleId 主键
     * @return 基础规则配置
     */
    @Override
    public AgriBaseRule selectAgriBaseRuleByRuleId(Long ruleId)
    {
        return agriBaseRuleMapper.selectAgriBaseRuleByRuleId(ruleId);
    }

    /**
     * 查询基础规则配置列表
     *
     * @param agriBaseRule 基础规则配置
     * @return 基础规则配置
     */
    @Override
    public List<AgriBaseRule> selectAgriBaseRuleList(AgriBaseRule agriBaseRule)
    {
        return agriBaseRuleMapper.selectAgriBaseRuleList(agriBaseRule);
    }

    /**
     * 新增基础规则配置
     *
     * @param agriBaseRule 基础规则配置
     * @return 结果
     */
    @Override
    public int insertAgriBaseRule(AgriBaseRule agriBaseRule)
    {
        return agriBaseRuleMapper.insertAgriBaseRule(agriBaseRule);
    }

    /**
     * 修改基础规则配置
     *
     * @param agriBaseRule 基础规则配置
     * @return 结果
     */
    @Override
    public int updateAgriBaseRule(AgriBaseRule agriBaseRule)
    {
        return agriBaseRuleMapper.updateAgriBaseRule(agriBaseRule);
    }

    /**
     * 批量删除基础规则配置
     *
     * @param ruleIds 需要删除的主键
     * @return 结果
     */
    @Override
    public int deleteAgriBaseRuleByRuleIds(Long[] ruleIds)
    {
        return agriBaseRuleMapper.deleteAgriBaseRuleByRuleIds(ruleIds);
    }

    /**
     * 校验规则编码是否唯一
     *
     * @param agriBaseRule 基础规则配置
     * @return true 唯一，false 不唯一
     */
    @Override
    public boolean checkRuleCodeUnique(AgriBaseRule agriBaseRule)
    {
        Long ruleId = StringUtils.isNull(agriBaseRule.getRuleId()) ? -1L : agriBaseRule.getRuleId();
        AgriBaseRule info = agriBaseRuleMapper.checkRuleCodeUnique(agriBaseRule.getRuleCode());
        if (StringUtils.isNotNull(info) && info.getRuleId().longValue() != ruleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
