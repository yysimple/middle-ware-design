package com.simple.schedule.domain;

import lombok.Data;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 数据收集模块
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:43
 **/
@Data
public class DataCollect {

    private int ipCount;
    private int serverCount;
    private int beanCount;
    private int methodCount;

    public DataCollect(int ipCount, int serverCount, int beanCount, int methodCount) {
        this.ipCount = ipCount;
        this.serverCount = serverCount;
        this.beanCount = beanCount;
        this.methodCount = methodCount;
    }
}
