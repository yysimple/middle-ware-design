package com.simple.schedule.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 执行实例
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:46
 **/
@Data
public class ExecuteInstance {

    /**
     * 类对象
     */
    @JSONField(serialize = false)
    private Object bean;

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
     * 任务状态
     */
    private Boolean autoStartup;
}

