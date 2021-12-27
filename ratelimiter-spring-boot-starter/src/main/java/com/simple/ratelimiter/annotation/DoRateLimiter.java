package com.simple.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 21:32
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoRateLimiter {
    double permitsPerSecond() default 0D;   // 限流许可量
    String returnJson() default "";         // 失败结果 JSON

}
