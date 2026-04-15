package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.service.IAgriQualityInspectTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
 * 视觉品质检测任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/qualityInspect")
public class AgriQualityInspectTaskController extends BaseController
{
    @Autowired
    private IAgriQualityInspectTaskService agriQualityInspectTaskService;

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriQualityInspectTask agriQualityInspectTask)
    {
        startPage();
        List<AgriQualityInspectTask> list = agriQualityInspectTaskService.selectAgriQualityInspectTaskList(agriQualityInspectTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:export')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriQualityInspectTask agriQualityInspectTask)
    {
        List<AgriQualityInspectTask> list = agriQualityInspectTaskService.selectAgriQualityInspectTaskList(agriQualityInspectTask);
        ExcelUtil<AgriQualityInspectTask> util = new ExcelUtil<>(AgriQualityInspectTask.class);
        util.exportExcel(response, list, "视觉品质检测任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:query')")
    @GetMapping(value = "/{inspectId}")
    public AjaxResult getInfo(@PathVariable("inspectId") Long inspectId)
    {
        return success(agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(inspectId));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:add')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        agriQualityInspectTask.setCreateBy(getUsername());
        if (agriQualityInspectTask.getInspectStatus() == null || agriQualityInspectTask.getInspectStatus().isEmpty())
        {
            agriQualityInspectTask.setInspectStatus("0");
        }
        return toAjax(agriQualityInspectTaskService.insertAgriQualityInspectTask(agriQualityInspectTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:edit')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        agriQualityInspectTask.setUpdateBy(getUsername());
        return toAjax(agriQualityInspectTaskService.updateAgriQualityInspectTask(agriQualityInspectTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:remove')")
    @Log(title = "视觉品质检测任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inspectIds}")
    public AjaxResult remove(@PathVariable Long[] inspectIds)
    {
        return toAjax(agriQualityInspectTaskService.deleteAgriQualityInspectTaskByInspectIds(inspectIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:qualityInspect:edit')")
    @Log(title = "视觉品质检测回写", businessType = BusinessType.UPDATE)
    @PutMapping("/feedback")
    public AjaxResult feedback(@RequestBody AgriQualityInspectTask agriQualityInspectTask)
    {
        AgriQualityInspectTask db = agriQualityInspectTaskService.selectAgriQualityInspectTaskByInspectId(agriQualityInspectTask.getInspectId());
        if (db == null)
        {
            return error("任务不存在");
        }

        db.setInspectStatus(agriQualityInspectTask.getInspectStatus());
        db.setQualityGrade(agriQualityInspectTask.getQualityGrade());
        db.setDefectRate(agriQualityInspectTask.getDefectRate() == null ? BigDecimal.ZERO : agriQualityInspectTask.getDefectRate());
        db.setInspectResult(agriQualityInspectTask.getInspectResult());
        db.setInspectTime(DateUtils.getNowDate());
        db.setModelVersion(agriQualityInspectTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriQualityInspectTaskService.updateAgriQualityInspectTask(db));
    }
}
