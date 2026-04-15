package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriEnvSensorRecord;
import com.ruoyi.system.service.IAgriEnvSensorRecordService;
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
 * 环境传感器数据Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/envSensor")
public class AgriEnvSensorRecordController extends BaseController
{
    @Autowired
    private IAgriEnvSensorRecordService agriEnvSensorRecordService;

    /**
     * 查询环境传感器数据列表
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriEnvSensorRecord agriEnvSensorRecord)
    {
        startPage();
        List<AgriEnvSensorRecord> list = agriEnvSensorRecordService.selectAgriEnvSensorRecordList(agriEnvSensorRecord);
        return getDataTable(list);
    }

    /**
     * 导出环境传感器数据列表
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:export')")
    @Log(title = "环境传感器数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriEnvSensorRecord agriEnvSensorRecord)
    {
        List<AgriEnvSensorRecord> list = agriEnvSensorRecordService.selectAgriEnvSensorRecordList(agriEnvSensorRecord);
        ExcelUtil<AgriEnvSensorRecord> util = new ExcelUtil<>(AgriEnvSensorRecord.class);
        util.exportExcel(response, list, "环境传感器数据");
    }

    /**
     * 获取环境传感器数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(agriEnvSensorRecordService.selectAgriEnvSensorRecordByRecordId(recordId));
    }

    /**
     * 新增环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:add')")
    @Log(title = "环境传感器数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriEnvSensorRecord agriEnvSensorRecord)
    {
        agriEnvSensorRecord.setCreateBy(getUsername());
        return toAjax(agriEnvSensorRecordService.insertAgriEnvSensorRecord(agriEnvSensorRecord));
    }

    /**
     * 修改环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:edit')")
    @Log(title = "环境传感器数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriEnvSensorRecord agriEnvSensorRecord)
    {
        agriEnvSensorRecord.setUpdateBy(getUsername());
        return toAjax(agriEnvSensorRecordService.updateAgriEnvSensorRecord(agriEnvSensorRecord));
    }

    /**
     * 删除环境传感器数据
     */
    @PreAuthorize("@ss.hasPermi('agri:envSensor:remove')")
    @Log(title = "环境传感器数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(agriEnvSensorRecordService.deleteAgriEnvSensorRecordByRecordIds(recordIds));
    }
}
