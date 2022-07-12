package com.simple.schedule.task;

import java.util.concurrent.ScheduledFuture;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 15:43
 **/
public class ScheduledTask {

    volatile ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future == null) {
            return;
        }
        future.cancel(true);
    }

    public boolean isCancelled() {
        ScheduledFuture<?> future = this.future;
        if (future == null) {
            return true;
        }
        return future.isCancelled();
    }
}
