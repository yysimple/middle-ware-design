package com.simple.whitelist.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 15:50
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoWhiteList {

    String key() default "";

    String returnJson() default "";

}
