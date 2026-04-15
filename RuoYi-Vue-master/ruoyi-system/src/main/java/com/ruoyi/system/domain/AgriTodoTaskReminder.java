package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 任务待办提醒对象 agri_todo_task_reminder
 *
 * @author ruoyi
 */
public class AgriTodoTaskReminder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long reminderId;

    @Excel(name = "任务编码")
    private String taskCode;

    @Excel(name = "任务标题")
    private String taskTitle;

    @Excel(name = "任务类型")
    private String taskType;

    @Excel(name = "优先级", readConverterExp = "1=低,2=中,3=高,4=紧急")
    private String priorityLevel;

    @Excel(name = "处理人")
    private String assignee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deadlineTime;

    @Excel(name = "提醒状态", readConverterExp = "0=待提醒,1=已提醒,2=已完成")
    private String reminderStatus;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getReminderId()
    {
        return reminderId;
    }

    public void setReminderId(Long reminderId)
    {
        this.reminderId = reminderId;
    }

    @NotBlank(message = "任务编码不能为空")
    public String getTaskCode()
    {
        return taskCode;
    }

    public void setTaskCode(String taskCode)
    {
        this.taskCode = taskCode;
    }

    @NotBlank(message = "任务标题不能为空")
    public String getTaskTitle()
    {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle)
    {
        this.taskTitle = taskTitle;
    }

    public String getTaskType()
    {
        return taskType;
    }

    public void setTaskType(String taskType)
    {
        this.taskType = taskType;
    }

    public String getPriorityLevel()
    {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel)
    {
        this.priorityLevel = priorityLevel;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(String assignee)
    {
        this.assignee = assignee;
    }

    @NotNull(message = "截止时间不能为空")
    public Date getDeadlineTime()
    {
        return deadlineTime;
    }

    public void setDeadlineTime(Date deadlineTime)
    {
        this.deadlineTime = deadlineTime;
    }

    public String getReminderStatus()
    {
        return reminderStatus;
    }

    public void setReminderStatus(String reminderStatus)
    {
        this.reminderStatus = reminderStatus;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("reminderId", getReminderId())
            .append("taskCode", getTaskCode())
            .append("taskTitle", getTaskTitle())
            .append("taskType", getTaskType())
            .append("priorityLevel", getPriorityLevel())
            .append("assignee", getAssignee())
            .append("deadlineTime", getDeadlineTime())
            .append("reminderStatus", getReminderStatus())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
