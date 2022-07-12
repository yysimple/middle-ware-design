package com.simple.schedule.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 16:20
 **/
@Configuration("com-simple-schedule-simpleScheduleAutoConfig")
@EnableConfigurationProperties(ScheduleServerProperties.class)
@Data
public class SimpleScheduleAutoConfig {

    @Autowired
    private ScheduleServerProperties properties;

}
