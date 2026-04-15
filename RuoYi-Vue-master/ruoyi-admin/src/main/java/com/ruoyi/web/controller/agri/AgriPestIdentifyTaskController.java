package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.service.IAgriPestIdentifyTaskService;
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
 * 病虫害识别任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/pestIdentify")
public class AgriPestIdentifyTaskController extends BaseController
{
    @Autowired
    private IAgriPestIdentifyTaskService agriPestIdentifyTaskService;

    /**
     * 查询病虫害识别任务列表
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriPestIdentifyTask agriPestIdentifyTask)
    {
        startPage();
        List<AgriPestIdentifyTask> list = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(agriPestIdentifyTask);
        return getDataTable(list);
    }

    /**
     * 导出病虫害识别任务列表
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:export')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriPestIdentifyTask agriPestIdentifyTask)
    {
        List<AgriPestIdentifyTask> list = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskList(agriPestIdentifyTask);
        ExcelUtil<AgriPestIdentifyTask> util = new ExcelUtil<>(AgriPestIdentifyTask.class);
        util.exportExcel(response, list, "病虫害识别任务数据");
    }

    /**
     * 获取病虫害识别任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(taskId));
    }

    /**
     * 新增病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:add')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        agriPestIdentifyTask.setCreateBy(getUsername());
        if (agriPestIdentifyTask.getIdentifyStatus() == null || agriPestIdentifyTask.getIdentifyStatus().isEmpty())
        {
            agriPestIdentifyTask.setIdentifyStatus("0");
        }
        return toAjax(agriPestIdentifyTaskService.insertAgriPestIdentifyTask(agriPestIdentifyTask));
    }

    /**
     * 修改病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:edit')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        agriPestIdentifyTask.setUpdateBy(getUsername());
        return toAjax(agriPestIdentifyTaskService.updateAgriPestIdentifyTask(agriPestIdentifyTask));
    }

    /**
     * 删除病虫害识别任务
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:remove')")
    @Log(title = "病虫害识别任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(agriPestIdentifyTaskService.deleteAgriPestIdentifyTaskByTaskIds(taskIds));
    }

    /**
     * 模型识别回写占位接口
     */
    @PreAuthorize("@ss.hasPermi('agri:pestIdentify:edit')")
    @Log(title = "病虫害识别回写", businessType = BusinessType.UPDATE)
    @PutMapping("/feedback")
    public AjaxResult feedback(@RequestBody AgriPestIdentifyTask agriPestIdentifyTask)
    {
        AgriPestIdentifyTask db = agriPestIdentifyTaskService.selectAgriPestIdentifyTaskByTaskId(agriPestIdentifyTask.getTaskId());
        if (db == null)
        {
            return error("任务不存在");
        }
        db.setIdentifyStatus(agriPestIdentifyTask.getIdentifyStatus());
        db.setIdentifyResult(agriPestIdentifyTask.getIdentifyResult());
        db.setConfidence(agriPestIdentifyTask.getConfidence() == null ? BigDecimal.ZERO : agriPestIdentifyTask.getConfidence());
        db.setIdentifyTime(DateUtils.getNowDate());
        db.setModelVersion(agriPestIdentifyTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriPestIdentifyTaskService.updateAgriPestIdentifyTask(db));
    }
}
