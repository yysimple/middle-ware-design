package com.simple.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 17:54
 **/
public class MonitorMethod {

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] args) throws Exception {
        long start = System.currentTimeMillis();
        Object resObj = null;
        try {
            resObj = callable.call();
            return resObj;
        } finally {
            System.out.println("========== start =========");
            System.out.println("代理-方法名称：" + "[" + method.getName() + "]");
            System.out.print("代理-入参个数：" + "[" + method.getParameterCount() + "]" + " ==> 入参内容：[");
            for (int i = 0; i < method.getParameterCount(); i++) {
                System.out.print("参数" + (i + 1) + " 的类型为：" + method.getParameterTypes()[i].getTypeName() + " 内容为：" + args[i] + "; ");
            }
            System.out.println("]");
            System.out.println("代理-出参类型：" + "[" + method.getReturnType().getName() + "]" + " ==> 代理-出参结果：" + "[" + resObj + "]");
            System.out.println("代理-方法耗时：" + "[" + (System.currentTimeMillis() - start) + "ms" + "]");
            System.out.println("========== end =========");
        }
    }
}
