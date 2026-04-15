package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriBaseRule;
import java.util.List;

/**
 * 启元农链基础规则配置Mapper接口
 *
 * @author ruoyi
 */
public interface AgriBaseRuleMapper
{
    /**
     * 查询基础规则配置
     *
     * @param ruleId 主键
     * @return 基础规则配置
     */
    public AgriBaseRule selectAgriBaseRuleByRuleId(Long ruleId);

    /**
     * 查询基础规则配置列表
     *
     * @param agriBaseRule 基础规则配置
     * @return 基础规则配置集合
     */
    public List<AgriBaseRule> selectAgriBaseRuleList(AgriBaseRule agriBaseRule);

    /**
     * 新增基础规则配置
     *
     * @param agriBaseRule 基础规则配置
     * @return 结果
     */
    public int insertAgriBaseRule(AgriBaseRule agriBaseRule);

    /**
     * 修改基础规则配置
     *
     * @param agriBaseRule 基础规则配置
     * @return 结果
     */
    public int updateAgriBaseRule(AgriBaseRule agriBaseRule);

    /**
     * 删除基础规则配置
     *
     * @param ruleId 主键
     * @return 结果
     */
    public int deleteAgriBaseRuleByRuleId(Long ruleId);

    /**
     * 批量删除基础规则配置
     *
     * @param ruleIds 需要删除的主键集合
     * @return 结果
     */
    public int deleteAgriBaseRuleByRuleIds(Long[] ruleIds);

    /**
     * 校验规则编码是否唯一
     *
     * @param ruleCode 规则编码
     * @return 结果
     */
    public AgriBaseRule checkRuleCodeUnique(String ruleCode);
}
