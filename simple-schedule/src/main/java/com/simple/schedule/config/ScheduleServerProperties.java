package com.simple.schedule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 应用连接zk的配置
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:40
 **/
@ConfigurationProperties("simple.schedule")
@Data
public class ScheduleServerProperties {

    /**
     * zookeeper服务地址；x.x.x.x:2181
     */
    private String zkAddress;

    /**
     * 任务服务ID；工程名称En
     */
    private String schedulerServerId;

    /**
     * 任务服务名称；工程名称Ch
     */
    private String schedulerServerName;

}
