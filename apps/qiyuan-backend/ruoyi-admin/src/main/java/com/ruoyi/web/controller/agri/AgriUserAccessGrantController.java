package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriUserAccessGrant;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriUserAccessGrantService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * 用户权限管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/userAccess")
public class AgriUserAccessGrantController extends BaseController
{
    @Autowired
    private IAgriUserAccessGrantService agriUserAccessGrantService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:userAccess:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriUserAccessGrant agriUserAccessGrant)
    {
        startPage();
        List<AgriUserAccessGrant> list = agriUserAccessGrantService.selectAgriUserAccessGrantList(agriUserAccessGrant);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriUserAccessGrant> list = agriUserAccessGrantService.selectAgriUserAccessGrantList(new AgriUserAccessGrant());
        int total = list.size();
        int granted = 0;
        int pending = 0;
        int rejected = 0;
        for (AgriUserAccessGrant item : list)
        {
            if ("1".equals(item.getGrantStatus()))
            {
                granted++;
            }
            else if ("2".equals(item.getGrantStatus()))
            {
                rejected++;
            }
            else
            {
                pending++;
            }
        }

        List<AgriUserAccessGrant> reviewList = list.stream()
            .filter(x -> "0".equals(x.getGrantStatus()))
            .sorted(Comparator.comparing(AgriUserAccessGrant::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("granted", granted);
        kpi.put("pending", pending);
        kpi.put("rejected", rejected);
        kpi.put("grantRate", total == 0 ? 0 : granted * 100.0 / total);
        result.put("kpi", kpi);
        result.put("reviewList", reviewList);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:edit')")
    @GetMapping("/smart/recommend/{grantId}")
    public AjaxResult smartRecommend(@PathVariable("grantId") Long grantId)
    {
        AgriUserAccessGrant grant = agriUserAccessGrantService.selectAgriUserAccessGrantByGrantId(grantId);
        if (grant == null)
        {
            return error("授权记录不存在");
        }

        List<String> recommendedMenus = new ArrayList<>();
        String roleKey = grant.getRoleKey() == null ? "" : grant.getRoleKey();
        if (roleKey.contains("admin"))
        {
            recommendedMenus.add("agri:dashboardOverview");
            recommendedMenus.add("agri:warningSummary");
            recommendedMenus.add("agri:auditLog");
        }
        else if (roleKey.contains("operator"))
        {
            recommendedMenus.add("agri:envSensor");
            recommendedMenus.add("agri:farmOp");
            recommendedMenus.add("agri:pestIdentify");
        }
        else
        {
            recommendedMenus.add("agri:traceQueryStats");
            recommendedMenus.add("agri:consumerScan");
        }

        String currentScope = grant.getMenuScope() == null ? "" : grant.getMenuScope();
        int overlap = 0;
        for (String menu : recommendedMenus)
        {
            if (currentScope.contains(menu))
            {
                overlap++;
            }
        }
        double fitScore = recommendedMenus.isEmpty() ? 0 : (overlap * 100.0 / recommendedMenus.size());
        String summary = "基于角色模板完成权限拟合，建议补齐最小可用菜单集。";
        String aiOriginalExcerpt = null;

        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "用户权限智能推荐");
            context.put("grantId", grant.getGrantId());
            context.put("userName", grant.getUserName());
            context.put("roleKey", grant.getRoleKey());
            context.put("grantStatus", grant.getGrantStatus());
            context.put("menuScope", grant.getMenuScope());
            context.put("ruleFitScore", Math.round(fitScore * 100.0) / 100.0);
            context.put("ruleRecommendedMenus", recommendedMenus);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("用户权限智能推荐", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (aiResult.getInsightSummary() != null && !aiResult.getInsightSummary().isEmpty())
            {
                summary = aiResult.getInsightSummary();
            }
            if (aiResult.getSuggestion() != null && !aiResult.getSuggestion().isEmpty())
            {
                recommendedMenus.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (aiOriginalExcerpt != null && !aiOriginalExcerpt.isEmpty())
            {
                recommendedMenus.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("grantId", grant.getGrantId());
        result.put("algorithm", "rbac-fit-v1");
        result.put("roleKey", grant.getRoleKey());
        result.put("fitScore", Math.round(fitScore * 100.0) / 100.0);
        result.put("recommendedMenus", recommendedMenus);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:export')")
    @Log(title = "用户权限管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriUserAccessGrant agriUserAccessGrant)
    {
        List<AgriUserAccessGrant> list = agriUserAccessGrantService.selectAgriUserAccessGrantList(agriUserAccessGrant);
        ExcelUtil<AgriUserAccessGrant> util = new ExcelUtil<>(AgriUserAccessGrant.class);
        util.exportExcel(response, list, "用户权限管理数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:query')")
    @GetMapping(value = "/{grantId}")
    public AjaxResult getInfo(@PathVariable("grantId") Long grantId)
    {
        return success(agriUserAccessGrantService.selectAgriUserAccessGrantByGrantId(grantId));
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:add')")
    @Log(title = "用户权限管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriUserAccessGrant agriUserAccessGrant)
    {
        agriUserAccessGrant.setCreateBy(getUsername());
        if (agriUserAccessGrant.getGrantStatus() == null || agriUserAccessGrant.getGrantStatus().isEmpty())
        {
            agriUserAccessGrant.setGrantStatus("0");
        }
        return toAjax(agriUserAccessGrantService.insertAgriUserAccessGrant(agriUserAccessGrant));
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:edit')")
    @Log(title = "用户权限管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriUserAccessGrant agriUserAccessGrant)
    {
        agriUserAccessGrant.setUpdateBy(getUsername());
        return toAjax(agriUserAccessGrantService.updateAgriUserAccessGrant(agriUserAccessGrant));
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:edit')")
    @Log(title = "用户权限授权", businessType = BusinessType.UPDATE)
    @PutMapping("/grant/{grantId}")
    public AjaxResult grant(@PathVariable("grantId") Long grantId)
    {
        AgriUserAccessGrant grant = agriUserAccessGrantService.selectAgriUserAccessGrantByGrantId(grantId);
        if (grant == null)
        {
            return error("授权记录不存在");
        }
        grant.setGrantStatus("1");
        grant.setUpdateBy(getUsername());
        return toAjax(agriUserAccessGrantService.updateAgriUserAccessGrant(grant));
    }

    @PreAuthorize("@ss.hasPermi('agri:userAccess:remove')")
    @Log(title = "用户权限管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{grantIds}")
    public AjaxResult remove(@PathVariable Long[] grantIds)
    {
        return toAjax(agriUserAccessGrantService.deleteAgriUserAccessGrantByGrantIds(grantIds));
    }
}
