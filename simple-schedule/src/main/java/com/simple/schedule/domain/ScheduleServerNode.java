package com.simple.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 应用节点
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:47
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleServerNode {

    /**
     * 任务服务ID；  工程名称En
     */
    private String schedulerServerId;

    /**
     * 任务服务名称；工程名称Ch
     */
    private String schedulerServerName;
}

