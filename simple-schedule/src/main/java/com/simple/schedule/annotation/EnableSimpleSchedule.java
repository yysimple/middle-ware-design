package com.simple.schedule.annotation;

import com.simple.schedule.DoJoinPoint;
import com.simple.schedule.config.SchedulingConfiguration;
import com.simple.schedule.task.CronTaskRegister;
import com.simple.schedule.task.SchedulingConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 开启定时任务注解
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:33
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({SchedulingConfiguration.class})
@ImportAutoConfiguration({SchedulingConfig.class, CronTaskRegister.class, DoJoinPoint.class})
@ComponentScan("com.simple.schedule.*")
public @interface EnableSimpleSchedule {
}
