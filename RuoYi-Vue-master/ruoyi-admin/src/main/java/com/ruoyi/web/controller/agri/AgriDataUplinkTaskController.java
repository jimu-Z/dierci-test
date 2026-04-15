package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDataUplinkTask;
import com.ruoyi.system.service.IAgriDataUplinkTaskService;
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
 * 数据上链任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/dataUplink")
public class AgriDataUplinkTaskController extends BaseController
{
    @Autowired
    private IAgriDataUplinkTaskService agriDataUplinkTaskService;

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDataUplinkTask agriDataUplinkTask)
    {
        startPage();
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(agriDataUplinkTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:export')")
    @Log(title = "数据上链任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDataUplinkTask agriDataUplinkTask)
    {
        List<AgriDataUplinkTask> list = agriDataUplinkTaskService.selectAgriDataUplinkTaskList(agriDataUplinkTask);
        ExcelUtil<AgriDataUplinkTask> util = new ExcelUtil<>(AgriDataUplinkTask.class);
        util.exportExcel(response, list, "数据上链任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:query')")
    @GetMapping(value = "/{uplinkId}")
    public AjaxResult getInfo(@PathVariable("uplinkId") Long uplinkId)
    {
        return success(agriDataUplinkTaskService.selectAgriDataUplinkTaskByUplinkId(uplinkId));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:add')")
    @Log(title = "数据上链任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriDataUplinkTask agriDataUplinkTask)
    {
        agriDataUplinkTask.setCreateBy(getUsername());
        if (StringUtils.isEmpty(agriDataUplinkTask.getUplinkStatus()))
        {
            agriDataUplinkTask.setUplinkStatus("0");
        }
        return toAjax(agriDataUplinkTaskService.insertAgriDataUplinkTask(agriDataUplinkTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:edit')")
    @Log(title = "数据上链任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriDataUplinkTask agriDataUplinkTask)
    {
        agriDataUplinkTask.setUpdateBy(getUsername());
        return toAjax(agriDataUplinkTaskService.updateAgriDataUplinkTask(agriDataUplinkTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:edit')")
    @Log(title = "数据上链执行", businessType = BusinessType.UPDATE)
    @PutMapping("/uplink/{uplinkId}")
    public AjaxResult uplink(@PathVariable("uplinkId") Long uplinkId)
    {
        AgriDataUplinkTask task = agriDataUplinkTaskService.selectAgriDataUplinkTaskByUplinkId(uplinkId);
        if (task == null)
        {
            return error("上链任务不存在");
        }
        if (!"1".equals(task.getUplinkStatus()))
        {
            task.setUplinkStatus("1");
            task.setUplinkTime(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(task.getTxHash()))
        {
            task.setTxHash("0x" + Long.toHexString(System.currentTimeMillis()) + Long.toHexString(uplinkId));
        }
        task.setUpdateBy(getUsername());
        return toAjax(agriDataUplinkTaskService.updateAgriDataUplinkTask(task));
    }

    @PreAuthorize("@ss.hasPermi('agri:dataUplink:remove')")
    @Log(title = "数据上链任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{uplinkIds}")
    public AjaxResult remove(@PathVariable Long[] uplinkIds)
    {
        return toAjax(agriDataUplinkTaskService.deleteAgriDataUplinkTaskByUplinkIds(uplinkIds));
    }
}
