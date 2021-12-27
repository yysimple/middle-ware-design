package com.simple.hystrix.value;

import com.simple.hystrix.annotation.DoHystrix;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 功能描述: 这里使用接口的方式，可以自己提供实现类去完成不同的降级策略
 *
 * @author: WuChengXing
 * @create: 2021-12-27 17:55
 **/
public interface IValueService {

    /**
     * 访问降级策略
     *
     * @param jp
     * @param method
     * @param doHystrix
     * @param args
     * @return
     * @throws Throwable
     */
    Object access(ProceedingJoinPoint jp, Method method, DoHystrix doHystrix, Object[] args) throws Throwable;
}
