package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriTodoTaskReminder;
import com.ruoyi.system.mapper.AgriTodoTaskReminderMapper;
import com.ruoyi.system.service.IAgriTodoTaskReminderService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 任务待办提醒Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriTodoTaskReminderServiceImpl implements IAgriTodoTaskReminderService
{
    @Autowired
    private AgriTodoTaskReminderMapper agriTodoTaskReminderMapper;

    @Override
    public AgriTodoTaskReminder selectAgriTodoTaskReminderByReminderId(Long reminderId)
    {
        return agriTodoTaskReminderMapper.selectAgriTodoTaskReminderByReminderId(reminderId);
    }

    @Override
    public List<AgriTodoTaskReminder> selectAgriTodoTaskReminderList(AgriTodoTaskReminder agriTodoTaskReminder)
    {
        return agriTodoTaskReminderMapper.selectAgriTodoTaskReminderList(agriTodoTaskReminder);
    }

    @Override
    public int insertAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder)
    {
        return agriTodoTaskReminderMapper.insertAgriTodoTaskReminder(agriTodoTaskReminder);
    }

    @Override
    public int updateAgriTodoTaskReminder(AgriTodoTaskReminder agriTodoTaskReminder)
    {
        return agriTodoTaskReminderMapper.updateAgriTodoTaskReminder(agriTodoTaskReminder);
    }

    @Override
    public int deleteAgriTodoTaskReminderByReminderIds(Long[] reminderIds)
    {
        return agriTodoTaskReminderMapper.deleteAgriTodoTaskReminderByReminderIds(reminderIds);
    }
}
