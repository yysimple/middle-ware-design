package com.simple.schedule.domain;

import lombok.Data;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: schedule的执行指令
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:44
 **/
@Data
public class ScheduleInstruct {

    /**
     * 机器IP
     */
    private String ip;

    /**
     * 任务服务ID；工程名称En
     */
    private String schedulerServerId;

    /**
     * 类对象名称
     */
    private String beanName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 任务执行
     */
    private String cron;

    /**
     * Constants.InstructStatus 0关闭、1启动、2更新
     */
    private Integer status;
}
