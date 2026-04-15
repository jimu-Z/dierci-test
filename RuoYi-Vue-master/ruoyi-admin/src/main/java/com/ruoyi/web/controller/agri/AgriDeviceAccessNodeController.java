package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDeviceAccessNode;
import com.ruoyi.system.service.IAgriDeviceAccessNodeService;
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
 * 设备接入管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/deviceAccess")
public class AgriDeviceAccessNodeController extends BaseController
{
    @Autowired
    private IAgriDeviceAccessNodeService agriDeviceAccessNodeService;

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDeviceAccessNode agriDeviceAccessNode)
    {
        startPage();
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(agriDeviceAccessNode);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:export')")
    @Log(title = "设备接入管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDeviceAccessNode agriDeviceAccessNode)
    {
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(agriDeviceAccessNode);
        ExcelUtil<AgriDeviceAccessNode> util = new ExcelUtil<>(AgriDeviceAccessNode.class);
        util.exportExcel(response, list, "设备接入管理数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:query')")
    @GetMapping(value = "/{nodeId}")
    public AjaxResult getInfo(@PathVariable("nodeId") Long nodeId)
    {
        return success(agriDeviceAccessNodeService.selectAgriDeviceAccessNodeByNodeId(nodeId));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:add')")
    @Log(title = "设备接入管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriDeviceAccessNode agriDeviceAccessNode)
    {
        agriDeviceAccessNode.setCreateBy(getUsername());
        if (agriDeviceAccessNode.getAccessStatus() == null || agriDeviceAccessNode.getAccessStatus().isEmpty())
        {
            agriDeviceAccessNode.setAccessStatus("0");
        }
        return toAjax(agriDeviceAccessNodeService.insertAgriDeviceAccessNode(agriDeviceAccessNode));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:edit')")
    @Log(title = "设备接入管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriDeviceAccessNode agriDeviceAccessNode)
    {
        agriDeviceAccessNode.setUpdateBy(getUsername());
        return toAjax(agriDeviceAccessNodeService.updateAgriDeviceAccessNode(agriDeviceAccessNode));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:edit')")
    @Log(title = "设备激活", businessType = BusinessType.UPDATE)
    @PutMapping("/activate/{nodeId}")
    public AjaxResult activate(@PathVariable("nodeId") Long nodeId)
    {
        AgriDeviceAccessNode node = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeByNodeId(nodeId);
        if (node == null)
        {
            return error("设备节点不存在");
        }
        node.setAccessStatus("1");
        node.setLastOnlineTime(DateUtils.getNowDate());
        node.setUpdateBy(getUsername());
        return toAjax(agriDeviceAccessNodeService.updateAgriDeviceAccessNode(node));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:remove')")
    @Log(title = "设备接入管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{nodeIds}")
    public AjaxResult remove(@PathVariable Long[] nodeIds)
    {
        return toAjax(agriDeviceAccessNodeService.deleteAgriDeviceAccessNodeByNodeIds(nodeIds));
    }
}
