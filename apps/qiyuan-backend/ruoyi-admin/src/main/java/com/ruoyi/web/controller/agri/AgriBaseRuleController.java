package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriBaseRule;
import com.ruoyi.system.service.IAgriBaseRuleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 启元农链基础规则配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/baseRule")
public class AgriBaseRuleController extends BaseController
{
    @Autowired
    private IAgriBaseRuleService agriBaseRuleService;

    /**
     * 查询基础规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriBaseRule agriBaseRule)
    {
        startPage();
        List<AgriBaseRule> list = agriBaseRuleService.selectAgriBaseRuleList(agriBaseRule);
        return getDataTable(list);
    }

    /**
     * 导出基础规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:export')")
    @Log(title = "基础规则配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriBaseRule agriBaseRule)
    {
        List<AgriBaseRule> list = agriBaseRuleService.selectAgriBaseRuleList(agriBaseRule);
        ExcelUtil<AgriBaseRule> util = new ExcelUtil<>(AgriBaseRule.class);
        util.exportExcel(response, list, "基础规则配置数据");
    }

    /**
     * 获取基础规则配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId)
    {
        return success(agriBaseRuleService.selectAgriBaseRuleByRuleId(ruleId));
    }

    /**
     * 新增基础规则配置
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:add')")
    @Log(title = "基础规则配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriBaseRule agriBaseRule)
    {
        if (!agriBaseRuleService.checkRuleCodeUnique(agriBaseRule))
        {
            return error("新增规则'" + agriBaseRule.getRuleName() + "'失败，规则编码已存在");
        }
        agriBaseRule.setCreateBy(getUsername());
        return toAjax(agriBaseRuleService.insertAgriBaseRule(agriBaseRule));
    }

    /**
     * 修改基础规则配置
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:edit')")
    @Log(title = "基础规则配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriBaseRule agriBaseRule)
    {
        if (!agriBaseRuleService.checkRuleCodeUnique(agriBaseRule))
        {
            return error("修改规则'" + agriBaseRule.getRuleName() + "'失败，规则编码已存在");
        }
        agriBaseRule.setUpdateBy(getUsername());
        return toAjax(agriBaseRuleService.updateAgriBaseRule(agriBaseRule));
    }

    /**
     * 删除基础规则配置
     */
    @PreAuthorize("@ss.hasPermi('agri:baseRule:remove')")
    @Log(title = "基础规则配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(agriBaseRuleService.deleteAgriBaseRuleByRuleIds(ruleIds));
    }
}
