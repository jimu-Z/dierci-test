package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDeviceAccessNode;
import com.ruoyi.system.domain.AgriEnvSensorRecord;
import com.ruoyi.system.integration.AgriIntegrationProperties;
import com.ruoyi.system.service.IAgriDeviceAccessNodeService;
import com.ruoyi.system.service.IAgriEnvSensorRecordService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private IAgriDeviceAccessNodeService agriDeviceAccessNodeService;

    @Autowired
    private AgriIntegrationProperties agriIntegrationProperties;

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

    @Anonymous
    @PostMapping("/ingest")
    public AjaxResult ingest(@RequestBody AgriEnvSensorRecord agriEnvSensorRecord, HttpServletRequest request)
    {
        String configuredToken = agriIntegrationProperties.getSensor().getIngestToken();
        if (StringUtils.isNotBlank(configuredToken))
        {
            String headerName = agriIntegrationProperties.getSensor().getAuthHeaderName();
            String requestToken = request.getHeader(headerName);
            if (StringUtils.isBlank(requestToken))
            {
                requestToken = request.getHeader("X-Agri-Token");
            }
            if (!StringUtils.equals(configuredToken, requestToken))
            {
                return error("传感器接入令牌无效");
            }
        }

        if (StringUtils.isBlank(agriEnvSensorRecord.getDeviceCode()) || StringUtils.isBlank(agriEnvSensorRecord.getPlotCode()))
        {
            return error("设备编码和地块编码不能为空");
        }

        AgriDeviceAccessNode query = new AgriDeviceAccessNode();
        query.setDeviceCode(agriEnvSensorRecord.getDeviceCode());
        List<AgriDeviceAccessNode> nodes = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(query);
        if (nodes == null || nodes.isEmpty())
        {
            return error("设备未注册，拒绝接入");
        }

        AgriDeviceAccessNode node = nodes.get(0);
        node.setAccessStatus("1");
        node.setLastOnlineTime(DateUtils.getNowDate());
        node.setUpdateBy("gateway");
        agriDeviceAccessNodeService.updateAgriDeviceAccessNode(node);

        agriEnvSensorRecord.setDataSource(agriIntegrationProperties.getSensor().getDataSource());
        if (agriEnvSensorRecord.getCollectTime() == null)
        {
            agriEnvSensorRecord.setCollectTime(DateUtils.getNowDate());
        }
        agriEnvSensorRecord.setStatus(isAlert(agriEnvSensorRecord) ? "1" : "0");
        agriEnvSensorRecord.setCreateBy("gateway");

        int rows = agriEnvSensorRecordService.insertAgriEnvSensorRecord(agriEnvSensorRecord);
        if (rows <= 0)
        {
            return error("上报入库失败");
        }
        return success("接收成功");
    }

    private boolean isAlert(AgriEnvSensorRecord record)
    {
        BigDecimal t = record.getTemperature();
        BigDecimal h = record.getHumidity();
        if (t != null && t.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getTempAlertHigh())) > 0)
        {
            return true;
        }
        return h != null && h.compareTo(BigDecimal.valueOf(agriIntegrationProperties.getSensor().getHumidityAlertHigh())) > 0;
    }
}
