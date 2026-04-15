package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTempHumidity;
import com.ruoyi.system.domain.AgriLogisticsWarning;
import com.ruoyi.system.service.IAgriLogisticsTempHumidityService;
import com.ruoyi.system.service.IAgriLogisticsWarningService;
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
 * 在途异常预警Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsWarning")
public class AgriLogisticsWarningController extends BaseController
{
    @Autowired
    private IAgriLogisticsWarningService agriLogisticsWarningService;

    @Autowired
    private IAgriLogisticsTempHumidityService agriLogisticsTempHumidityService;

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsWarning agriLogisticsWarning)
    {
        startPage();
        List<AgriLogisticsWarning> list = agriLogisticsWarningService.selectAgriLogisticsWarningList(agriLogisticsWarning);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:export')")
    @Log(title = "在途异常预警", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsWarning agriLogisticsWarning)
    {
        List<AgriLogisticsWarning> list = agriLogisticsWarningService.selectAgriLogisticsWarningList(agriLogisticsWarning);
        ExcelUtil<AgriLogisticsWarning> util = new ExcelUtil<>(AgriLogisticsWarning.class);
        util.exportExcel(response, list, "在途异常预警数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:query')")
    @GetMapping(value = "/{warningId}")
    public AjaxResult getInfo(@PathVariable("warningId") Long warningId)
    {
        return success(agriLogisticsWarningService.selectAgriLogisticsWarningByWarningId(warningId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:add')")
    @Log(title = "在途异常预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsWarning agriLogisticsWarning)
    {
        agriLogisticsWarning.setCreateBy(getUsername());
        return toAjax(agriLogisticsWarningService.insertAgriLogisticsWarning(agriLogisticsWarning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:add')")
    @Log(title = "在途异常预警生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate/{recordId}")
    public AjaxResult generateByTempRecord(@PathVariable("recordId") Long recordId)
    {
        AgriLogisticsTempHumidity record = agriLogisticsTempHumidityService.selectAgriLogisticsTempHumidityByRecordId(recordId);
        if (record == null)
        {
            return error("温湿度记录不存在");
        }
        if (!"1".equals(record.getAlertFlag()))
        {
            return error("该温湿度记录未触发告警");
        }
        AgriLogisticsWarning exists = agriLogisticsWarningService.selectAgriLogisticsWarningBySourceRecordId(recordId);
        if (exists != null)
        {
            return error("该温湿度记录已生成预警");
        }

        AgriLogisticsWarning warning = new AgriLogisticsWarning();
        warning.setTraceCode(record.getTraceCode());
        warning.setOrderNo(record.getOrderNo());
        warning.setWarningType("TEMP_HUMIDITY");
        warning.setWarningLevel("2");
        warning.setWarningStatus("0");
        warning.setSourceRecordId(recordId);
        warning.setWarningTitle("温湿度在途异常");
        warning.setWarningContent(record.getAlertMessage() == null ? "温湿度超阈值" : record.getAlertMessage());
        warning.setWarningTime(record.getCollectTime() == null ? DateUtils.getNowDate() : record.getCollectTime());
        warning.setStatus("0");
        warning.setCreateBy(getUsername());
        return toAjax(agriLogisticsWarningService.insertAgriLogisticsWarning(warning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:edit')")
    @Log(title = "在途异常预警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsWarning agriLogisticsWarning)
    {
        agriLogisticsWarning.setUpdateBy(getUsername());
        return toAjax(agriLogisticsWarningService.updateAgriLogisticsWarning(agriLogisticsWarning));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsWarning:remove')")
    @Log(title = "在途异常预警", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warningIds}")
    public AjaxResult remove(@PathVariable Long[] warningIds)
    {
        return toAjax(agriLogisticsWarningService.deleteAgriLogisticsWarningByWarningIds(warningIds));
    }
}
