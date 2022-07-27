package com.simple.dbrouter.annotation;

import java.lang.annotation.*;

/**
 * 项目: middle
 * <p>
 * 功能描述: 路由键对应的字段
 *
 * @author: WuChengXing
 * @create: 2022-07-27 21:48
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RouterParam {

}
