package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriUserAccessGrant;
import com.ruoyi.system.service.IAgriUserAccessGrantService;
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

    @PreAuthorize("@ss.hasPermi('agri:userAccess:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriUserAccessGrant agriUserAccessGrant)
    {
        startPage();
        List<AgriUserAccessGrant> list = agriUserAccessGrantService.selectAgriUserAccessGrantList(agriUserAccessGrant);
        return getDataTable(list);
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
