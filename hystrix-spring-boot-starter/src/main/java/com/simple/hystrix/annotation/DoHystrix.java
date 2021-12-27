package com.simple.hystrix.annotation;

import com.simple.hystrix.value.IValueService;
import com.simple.hystrix.value.impl.DefaultHystrixValueImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 17:53
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoHystrix {

    /**
     * 失败结果 JSON
     *
     * @return
     */
    String returnJson() default "";

    /**
     * 超时熔断
     *
     * @return
     */
    int timeoutValue() default 0;

    Class<? extends IValueService> forFallback() default DefaultHystrixValueImpl.class;
}
