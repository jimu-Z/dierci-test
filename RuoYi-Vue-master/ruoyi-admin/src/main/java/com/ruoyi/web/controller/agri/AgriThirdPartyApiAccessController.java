package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriThirdPartyApiAccess;
import com.ruoyi.system.service.IAgriThirdPartyApiAccessService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方API接入Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/thirdApi")
public class AgriThirdPartyApiAccessController extends BaseController
{
    @Autowired
    private IAgriThirdPartyApiAccessService agriThirdPartyApiAccessService;

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        startPage();
        return getDataTable(agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessList(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:export')")
    @Log(title = "第三方API接入", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        ExcelUtil<AgriThirdPartyApiAccess> util = new ExcelUtil<>(AgriThirdPartyApiAccess.class);
        util.exportExcel(response, agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessList(agriThirdPartyApiAccess), "第三方API接入数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:query')")
    @GetMapping(value = "/{accessId}")
    public AjaxResult getInfo(@PathVariable("accessId") Long accessId)
    {
        return success(agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessByAccessId(accessId));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:add')")
    @Log(title = "第三方API接入", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return toAjax(agriThirdPartyApiAccessService.insertAgriThirdPartyApiAccess(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:edit')")
    @Log(title = "第三方API接入", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return toAjax(agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:remove')")
    @Log(title = "第三方API接入", businessType = BusinessType.DELETE)
    @DeleteMapping("/{accessIds}")
    public AjaxResult remove(@PathVariable Long[] accessIds)
    {
        return toAjax(agriThirdPartyApiAccessService.deleteAgriThirdPartyApiAccessByAccessIds(accessIds));
    }
}
