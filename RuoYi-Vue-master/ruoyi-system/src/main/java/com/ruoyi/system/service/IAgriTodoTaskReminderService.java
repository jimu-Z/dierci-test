package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriTodoTaskReminder;
import java.util.List;

/**
 * 任务待办提醒Service接口
 *
 * @author ruoyi
 */
public interface IAgriTodoTaskReminderService
{
    public AgriTodoTaskReminder selectAgriTodoTaskReminderByReminderId(Long reminderId);

    public List<AgriTodoTaskReminder> selectAgriTodoTaskReminderList(AgriTodoTaskReminder agriTodoTaskReminder);

    public int insertAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder);

    public int updateAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder);

    public int deleteAgriTodoTaskReminderByReminderIds(Long[] reminderIds);
}
