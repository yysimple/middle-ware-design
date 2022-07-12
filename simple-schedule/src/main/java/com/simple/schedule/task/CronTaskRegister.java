package com.simple.schedule.task;

import com.simple.schedule.common.Constants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 15:41
 **/
@Component("com-simple-schedule-cronTaskRegister")
public class CronTaskRegister implements DisposableBean {

    @Resource(name = "com-simple-schedule-taskScheduler")
    private TaskScheduler taskScheduler;

    public TaskScheduler getScheduler() {
        return this.taskScheduler;
    }

    public void addCronTask(SchedulingRunnable task, String cronExpression) {
        if (null != Constants.SCHEDULED_TASKS.get(task.taskId())) {
            removeCronTask(task.taskId());
        }
        CronTask cronTask = new CronTask(task, cronExpression);
        Constants.SCHEDULED_TASKS.put(task.taskId(), scheduleCronTask(cronTask));
    }

    public void removeCronTask(String taskId) {
        ScheduledTask scheduledTask = Constants.SCHEDULED_TASKS.remove(taskId);
        if (scheduledTask == null) {
            return;
        }
        scheduledTask.cancel();
    }

    private ScheduledTask scheduleCronTask(CronTask cronTask) {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        return scheduledTask;
    }

    @Override
    public void destroy() {
        for (ScheduledTask task : Constants.SCHEDULED_TASKS.values()) {
            task.cancel();
        }
        Constants.SCHEDULED_TASKS.clear();
    }
}
