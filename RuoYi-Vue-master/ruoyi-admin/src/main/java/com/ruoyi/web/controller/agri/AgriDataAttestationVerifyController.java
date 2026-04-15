package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDataAttestationVerify;
import com.ruoyi.system.service.IAgriDataAttestationVerifyService;
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
 * 数据存证与校验Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/attestationVerify")
public class AgriDataAttestationVerifyController extends BaseController
{
    @Autowired
    private IAgriDataAttestationVerifyService agriDataAttestationVerifyService;

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDataAttestationVerify agriDataAttestationVerify)
    {
        startPage();
        return getDataTable(agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:export')")
    @Log(title = "数据存证与校验", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDataAttestationVerify agriDataAttestationVerify)
    {
        ExcelUtil<AgriDataAttestationVerify> util = new ExcelUtil<>(AgriDataAttestationVerify.class);
        util.exportExcel(response, agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(agriDataAttestationVerify), "数据存证与校验数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:query')")
    @GetMapping(value = "/{verifyId}")
    public AjaxResult getInfo(@PathVariable("verifyId") Long verifyId)
    {
        return success(agriDataAttestationVerifyService.selectAgriDataAttestationVerifyByVerifyId(verifyId));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:add')")
    @Log(title = "数据存证与校验", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return toAjax(agriDataAttestationVerifyService.insertAgriDataAttestationVerify(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:edit')")
    @Log(title = "数据存证与校验", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return toAjax(agriDataAttestationVerifyService.updateAgriDataAttestationVerify(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:remove')")
    @Log(title = "数据存证与校验", businessType = BusinessType.DELETE)
    @DeleteMapping("/{verifyIds}")
    public AjaxResult remove(@PathVariable Long[] verifyIds)
    {
        return toAjax(agriDataAttestationVerifyService.deleteAgriDataAttestationVerifyByVerifyIds(verifyIds));
    }
}
