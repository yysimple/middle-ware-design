package com.simple.dbrouter.util;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 对象相关工具类
 *
 * @author: WuChengXing
 * @create: 2022-07-28 15:31
 **/
public class ClassUtils {
    private ClassUtils() {
    }

    /**
     * 剥去springAop等代理类的外衣,获取真实的类的类型
     *
     * @param value
     * @return
     */
    public static Class<?> getRealClass(Object value) throws Exception {
        // 防止这里key是spring的aop类 或者 proxy代理
        if (!AopUtils.isAopProxy(value) && !Proxy.isProxyClass(value.getClass())) {
            return value.getClass();
        }
        Class<?> clazz = value.getClass();
        if (AopUtils.isAopProxy(value)) {
            if (AopUtils.isJdkDynamicProxy(value)) {
                clazz = getJdkDynamicProxyTargetObject(value).getClass();
            } else {
                clazz = getCglibProxyTargetObject(value).getClass();
            }
        }
        if (Proxy.isProxyClass(clazz)) {
            Class<?>[] interfaces = clazz.getInterfaces();
            clazz = interfaces[interfaces.length - 1];
        }
        return clazz;
    }


    /**
     * JDK动态代理方式被代理类的获取
     *
     * @author Monkey
     */
    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(Boolean.TRUE);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(Boolean.TRUE);
        Object advisedSupport = advised.get(aopProxy);
        return getProxyObject(proxy, (AdvisedSupport) advisedSupport);
    }

    /**
     * CGLIB方式被代理类的获取
     *
     * @author Monkey
     */
    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(Boolean.TRUE);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(Boolean.TRUE);
        Object advisedSupport = advised.get(dynamicAdvisedInterceptor);
        return getProxyObject(proxy, (AdvisedSupport) advisedSupport);
    }

    /**
     * 获取proxy代理的实际类
     *
     * @param proxy
     * @param advisedSupport
     * @return
     * @throws Exception
     */
    private static Object getProxyObject(Object proxy, AdvisedSupport advisedSupport) throws Exception {
        if (advisedSupport == null) {
            throw new RuntimeException("proxy");
        }

        Object target = advisedSupport.getTargetSource().getTarget();
        if (target == null) {
            throw new RuntimeException("proxy");
        }
        return target;
    }
}
