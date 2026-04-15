package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriTodoTaskReminder;
import com.ruoyi.system.service.IAgriTodoTaskReminderService;
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
 * 任务待办提醒Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/todoTaskReminder")
public class AgriTodoTaskReminderController extends BaseController
{
    @Autowired
    private IAgriTodoTaskReminderService agriTodoTaskReminderService;

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriTodoTaskReminder agriTodoTaskReminder)
    {
        startPage();
        return getDataTable(agriTodoTaskReminderService.selectAgriTodoTaskReminderList(agriTodoTaskReminder));
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:export')")
    @Log(title = "任务待办提醒", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriTodoTaskReminder agriTodoTaskReminder)
    {
        ExcelUtil<AgriTodoTaskReminder> util = new ExcelUtil<>(AgriTodoTaskReminder.class);
        util.exportExcel(response, agriTodoTaskReminderService.selectAgriTodoTaskReminderList(agriTodoTaskReminder), "任务待办提醒数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:query')")
    @GetMapping(value = "/{reminderId}")
    public AjaxResult getInfo(@PathVariable("reminderId") Long reminderId)
    {
        return success(agriTodoTaskReminderService.selectAgriTodoTaskReminderByReminderId(reminderId));
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:add')")
    @Log(title = "任务待办提醒", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriTodoTaskReminder agriTodoTaskReminder)
    {
        return toAjax(agriTodoTaskReminderService.insertAgriTodoTaskReminder(agriTodoTaskReminder));
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:edit')")
    @Log(title = "任务待办提醒", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriTodoTaskReminder agriTodoTaskReminder)
    {
        return toAjax(agriTodoTaskReminderService.updateAgriTodoTaskReminder(agriTodoTaskReminder));
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:remove')")
    @Log(title = "任务待办提醒", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reminderIds}")
    public AjaxResult remove(@PathVariable Long[] reminderIds)
    {
        return toAjax(agriTodoTaskReminderService.deleteAgriTodoTaskReminderByReminderIds(reminderIds));
    }
}
