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

    /**
     * 新增任务
     *
     * @param task
     * @param cronExpression
     */
    public void addCronTask(SchedulingRunnable task, String cronExpression) {
        if (null != Constants.SCHEDULED_TASKS.get(task.taskId())) {
            removeCronTask(task.taskId());
        }
        // 构建需要执行的任务，和任务对应的cron表达式
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
        // 使用spring提供的定时任务，然后拿到对应的future
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
