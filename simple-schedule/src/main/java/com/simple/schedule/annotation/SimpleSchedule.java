package com.simple.schedule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 定时任务配置
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimpleSchedule {

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "描述";

    /**
     * cron表达式
     *
     * @return
     */
    String cron() default "";

    /**
     * 是否自启动
     *
     * @return
     */
    boolean autoStartup() default true;
}
