package com.simple.schedule.util;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 字符串操作
 *
 * @author: WuChengXing
 * @create: 2022-07-11 10:52
 **/
public class StrUtil {

    public static String joinStr(String... str) {
        StringBuilder sb = new StringBuilder();
        for (String sign : str) {
            if (null != sign) {
                sb.append(sign);
            }
        }
        return sb.toString();
    }
}
