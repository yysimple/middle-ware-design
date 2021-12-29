package com.simple.rpc.reflect;

import com.simple.rpc.network.msg.Request;
import com.simple.rpc.util.ClassLoaderUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:18
 **/
public class JDKProxy {
    public static <T> T getProxy(Class<T> interfaceClass, Request request) throws Exception {
        InvocationHandler handler = new JDKInvocationHandler(request);
        ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
        T result = (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, handler);
        return result;
    }
}
