package com.simple.schedule.domain;

import lombok.Data;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: schedule应用实例
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:49
 **/
@Data
public class ScheduleInstance {

    /**
     * 机器IP
     */
    private String ip;

    /**
     * 任务服务ID；  工程名称En
     */
    private String schedulerServerId;

    /**
     * 任务服务名称；工程名称Ch
     */
    private String schedulerServerName;

    /**
     * 类对象名称
     */
    private String beanName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 任务描述
     */
    private String desc;

    /**
     * 任务执行
     */
    private String cron;

    /**
     * 任务状态；0关闭、1开启、2宕机
     */
    private Integer status;

}
