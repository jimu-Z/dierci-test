package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDeviceStatusMonitor;
import com.ruoyi.system.service.IAgriDeviceStatusMonitorService;
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
 * 设备状态监控Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/deviceStatusMonitor")
public class AgriDeviceStatusMonitorController extends BaseController
{
    @Autowired
    private IAgriDeviceStatusMonitorService agriDeviceStatusMonitorService;

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        startPage();
        return getDataTable(agriDeviceStatusMonitorService.selectAgriDeviceStatusMonitorList(agriDeviceStatusMonitor));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:export')")
    @Log(title = "设备状态监控", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        ExcelUtil<AgriDeviceStatusMonitor> util = new ExcelUtil<>(AgriDeviceStatusMonitor.class);
        util.exportExcel(response, agriDeviceStatusMonitorService.selectAgriDeviceStatusMonitorList(agriDeviceStatusMonitor), "设备状态监控数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:query')")
    @GetMapping(value = "/{monitorId}")
    public AjaxResult getInfo(@PathVariable("monitorId") Long monitorId)
    {
        return success(agriDeviceStatusMonitorService.selectAgriDeviceStatusMonitorByMonitorId(monitorId));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:add')")
    @Log(title = "设备状态监控", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return toAjax(agriDeviceStatusMonitorService.insertAgriDeviceStatusMonitor(agriDeviceStatusMonitor));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:edit')")
    @Log(title = "设备状态监控", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriDeviceStatusMonitor agriDeviceStatusMonitor)
    {
        return toAjax(agriDeviceStatusMonitorService.updateAgriDeviceStatusMonitor(agriDeviceStatusMonitor));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceStatusMonitor:remove')")
    @Log(title = "设备状态监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{monitorIds}")
    public AjaxResult remove(@PathVariable Long[] monitorIds)
    {
        return toAjax(agriDeviceStatusMonitorService.deleteAgriDeviceStatusMonitorByMonitorIds(monitorIds));
    }
}
