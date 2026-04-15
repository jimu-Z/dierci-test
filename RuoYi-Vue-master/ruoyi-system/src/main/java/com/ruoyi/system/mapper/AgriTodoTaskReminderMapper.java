package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriTodoTaskReminder;
import java.util.List;

/**
 * 任务待办提醒Mapper接口
 *
 * @author ruoyi
 */
public interface AgriTodoTaskReminderMapper
{
    public AgriTodoTaskReminder selectAgriTodoTaskReminderByReminderId(Long reminderId);

    public List<AgriTodoTaskReminder> selectAgriTodoTaskReminderList(AgriTodoTaskReminder agriTodoTaskReminder);

    public int insertAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder);

    public int updateAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder);

    public int deleteAgriTodoTaskReminderByReminderId(Long reminderId);

    public int deleteAgriTodoTaskReminderByReminderIds(Long[] reminderIds);
}
