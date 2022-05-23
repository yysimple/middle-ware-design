package com.simple.pagination.annotation;

import java.lang.annotation.*;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 18:23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface DisableCount {
}
