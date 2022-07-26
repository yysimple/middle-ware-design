package com.simple.dbrouter.annotation;

import java.lang.annotation.*;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 路由策略，分表标记
 *
 * @author: WuChengXing
 * @create: 2022-07-26 15:30
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DbRouterStrategy {

    boolean splitTable() default false;
}
