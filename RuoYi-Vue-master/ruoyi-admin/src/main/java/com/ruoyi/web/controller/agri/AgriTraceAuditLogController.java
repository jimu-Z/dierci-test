package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriTraceAuditLog;
import com.ruoyi.system.service.IAgriTraceAuditLogService;
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
 * 溯源审计日志Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/auditLog")
public class AgriTraceAuditLogController extends BaseController
{
    @Autowired
    private IAgriTraceAuditLogService agriTraceAuditLogService;

    @PreAuthorize("@ss.hasPermi('agri:auditLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriTraceAuditLog agriTraceAuditLog)
    {
        startPage();
        return getDataTable(agriTraceAuditLogService.selectAgriTraceAuditLogList(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:export')")
    @Log(title = "溯源审计日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriTraceAuditLog agriTraceAuditLog)
    {
        ExcelUtil<AgriTraceAuditLog> util = new ExcelUtil<>(AgriTraceAuditLog.class);
        util.exportExcel(response, agriTraceAuditLogService.selectAgriTraceAuditLogList(agriTraceAuditLog), "溯源审计日志数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:query')")
    @GetMapping(value = "/{auditId}")
    public AjaxResult getInfo(@PathVariable("auditId") Long auditId)
    {
        return success(agriTraceAuditLogService.selectAgriTraceAuditLogByAuditId(auditId));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:add')")
    @Log(title = "溯源审计日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriTraceAuditLog agriTraceAuditLog)
    {
        return toAjax(agriTraceAuditLogService.insertAgriTraceAuditLog(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:edit')")
    @Log(title = "溯源审计日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriTraceAuditLog agriTraceAuditLog)
    {
        return toAjax(agriTraceAuditLogService.updateAgriTraceAuditLog(agriTraceAuditLog));
    }

    @PreAuthorize("@ss.hasPermi('agri:auditLog:remove')")
    @Log(title = "溯源审计日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{auditIds}")
    public AjaxResult remove(@PathVariable Long[] auditIds)
    {
        return toAjax(agriTraceAuditLogService.deleteAgriTraceAuditLogByAuditIds(auditIds));
    }
}
