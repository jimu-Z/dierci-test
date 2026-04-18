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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriTodoTaskReminder> list = agriTodoTaskReminderService.selectAgriTodoTaskReminderList(new AgriTodoTaskReminder());
        int total = list.size();
        int pendingCount = 0;
        int remindedCount = 0;
        int completedCount = 0;
        int overdueCount = 0;
        int urgentCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (AgriTodoTaskReminder item : list)
        {
            if ("0".equals(item.getReminderStatus()))
            {
                pendingCount++;
            }
            else if ("1".equals(item.getReminderStatus()))
            {
                remindedCount++;
            }
            else if ("2".equals(item.getReminderStatus()))
            {
                completedCount++;
            }

            if ("4".equals(item.getPriorityLevel()) || "3".equals(item.getPriorityLevel()))
            {
                urgentCount++;
            }
            if (item.getDeadlineTime() != null && item.getDeadlineTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isBefore(now)
                && !"2".equals(item.getReminderStatus()))
            {
                overdueCount++;
            }
        }

        List<AgriTodoTaskReminder> topList = list.stream()
            .sorted(Comparator.comparing((AgriTodoTaskReminder item) -> priorityWeight(item.getPriorityLevel())).reversed()
                .thenComparing(AgriTodoTaskReminder::getDeadlineTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .limit(6)
            .toList();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("pendingCount", pendingCount);
        kpi.put("remindedCount", remindedCount);
        kpi.put("completedCount", completedCount);
        kpi.put("overdueCount", overdueCount);
        kpi.put("urgentCount", urgentCount);

        List<String> recommendations = new ArrayList<>();
        if (overdueCount > 0)
        {
            recommendations.add("存在超期待办，建议优先向紧急与高优先级任务发起催办。");
        }
        if (pendingCount > completedCount)
        {
            recommendations.add("待提醒数量高于已完成数量，建议先集中清理低时效任务。");
        }
        if (urgentCount > 0 && pendingCount == urgentCount)
        {
            recommendations.add("高优先级任务较集中，建议单独建立当日处置清单。");
        }
        if (recommendations.isEmpty())
        {
            recommendations.add("当前待办队列平稳，可按截止时间滚动巡检。");
        }

        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("summary", "当前待办队列中待提醒" + pendingCount + "条，超期" + overdueCount + "条，优先级较高任务" + urgentCount + "条。");
        insight.put("dispatchLevel", buildDispatchLevel(overdueCount, urgentCount, pendingCount));
        insight.put("confidenceRate", buildConfidence(total, overdueCount, urgentCount));
        insight.put("modelVersion", "todo-dispatch-v1");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("topList", topList);
        result.put("insight", insight);
        result.put("recommendations", recommendations);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:todoTaskReminder:query')")
    @GetMapping("/smart/dispatch/{reminderId}")
    public AjaxResult smartDispatch(@PathVariable("reminderId") Long reminderId)
    {
        AgriTodoTaskReminder reminder = agriTodoTaskReminderService.selectAgriTodoTaskReminderByReminderId(reminderId);
        if (reminder == null)
        {
            return error("待办记录不存在");
        }

        long overdueDays = 0L;
        if (reminder.getDeadlineTime() != null)
        {
            overdueDays = ChronoUnit.DAYS.between(reminder.getDeadlineTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDateTime.now().toLocalDate());
        }

        List<String> actions = new ArrayList<>();
        if ("4".equals(reminder.getPriorityLevel()) || overdueDays > 0)
        {
            actions.add("立即电话或消息催办，并同步抄送负责人。");
        }
        if ("0".equals(reminder.getReminderStatus()))
        {
            actions.add("先发送首次提醒，再根据反馈结果决定是否升级。");
        }
        if (actions.isEmpty())
        {
            actions.add("继续按当前节奏跟踪即可，无需额外升级动作。");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reminderId", reminder.getReminderId());
        result.put("taskCode", reminder.getTaskCode());
        result.put("algorithm", "todo-dispatch-v1");
        result.put("dispatchLevel", buildDispatchLevel(overdueDays > 0 ? 1 : 0, priorityWeight(reminder.getPriorityLevel()), "0".equals(reminder.getReminderStatus()) ? 1 : 0));
        result.put("overdueDays", Math.max(overdueDays, 0L));
        result.put("actions", actions);
        result.put("summary", "根据优先级、截止时间与提醒状态生成待办处置建议，建议尽快关注该任务。 ");
        return success(result);
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

    private int priorityWeight(String level)
    {
        if ("4".equals(level))
        {
            return 4;
        }
        if ("3".equals(level))
        {
            return 3;
        }
        if ("2".equals(level))
        {
            return 2;
        }
        return 1;
    }

    private String buildDispatchLevel(int overdueCount, int urgentCount, int pendingCount)
    {
        if (overdueCount > 0 || urgentCount > 2)
        {
            return "紧急";
        }
        if (pendingCount > 3)
        {
            return "关注";
        }
        return "稳定";
    }

    private String buildConfidence(int total, int overdueCount, int urgentCount)
    {
        int base = total == 0 ? 58 : 72;
        int penalty = overdueCount * 5 + urgentCount * 2;
        int confidence = Math.max(base - penalty, 55);
        return confidence + "%";
    }
}
