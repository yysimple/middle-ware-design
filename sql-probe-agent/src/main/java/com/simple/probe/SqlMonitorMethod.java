package com.simple.probe;

import cn.hutool.core.util.ReflectUtil;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-04-13 19:21
 **/
public class SqlMonitorMethod {

    @RuntimeType
    public static Object intercept(@This Object obj, @Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object... args) throws Exception {
        try {
            return callable.call();
        } finally {

            String replaceSql = ReflectUtil.invoke(obj, "asSql");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("数据库名称：Mysql");
            System.out.println("线程ID：" + Thread.currentThread().getId());
            System.out.println("时间：" + simpleDateFormat.format(new Date()));
            System.out.println("可执行的SQL：\r\n" + replaceSql);
            System.out.println("-------- agent finish weave --------");
        }
    }
}
