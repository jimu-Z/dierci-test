package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import com.ruoyi.system.service.IAgriLogisticsTempHumidityService;
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
 * 物流温湿度监控Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsTemp")
public class AgriLogisticsTempHumidityController extends BaseController
{
    @Autowired
    private IAgriLogisticsTempHumidityService agriLogisticsTempHumidityService;

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        startPage();
        List<AgriLogisticsTempHumidity> list = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityList(agriLogisticsTempHumidity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:export')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        List<AgriLogisticsTempHumidity> list = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityList(agriLogisticsTempHumidity);
        ExcelUtil<AgriLogisticsTempHumidity> util = new ExcelUtil<>(AgriLogisticsTempHumidity.class);
        util.exportExcel(response, list, "物流温湿度监控数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityByRecordId(recordId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:add')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        agriLogisticsTempHumidity.setCreateBy(getUsername());
        return toAjax(agriLogisticsTempHumidityService.insertAgriLogisticsTempHumidity(agriLogisticsTempHumidity));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:edit')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsTempHumidity agriLogisticsTempHumidity)
    {
        agriLogisticsTempHumidity.setUpdateBy(getUsername());
        return toAjax(agriLogisticsTempHumidityService.updateAgriLogisticsTempHumidity(agriLogisticsTempHumidity));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTemp:remove')")
    @Log(title = "物流温湿度监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(agriLogisticsTempHumidityService.deleteAgriLogisticsTempHumidityByRecordIds(recordIds));
    }
}
