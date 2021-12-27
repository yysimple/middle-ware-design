package com.simple.ratelimiter.value.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.simple.ratelimiter.Constants;
import com.simple.ratelimiter.annotation.DoRateLimiter;
import com.simple.ratelimiter.value.IValveService;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 22:48
 **/
public class RateLimiterValveImpl implements IValveService {

    @Override
    public Object access(ProceedingJoinPoint jp, Method method, DoRateLimiter doRateLimiter, Object[] args) throws Throwable {
        // 判断是否开启,0表示未开启
        if (0 == doRateLimiter.permitsPerSecond()) {
            return jp.proceed();
        }

        String clazzName = jp.getTarget().getClass().getName();
        String methodName = method.getName();

        String key = clazzName + "." + methodName;

        if (null == Constants.rateLimiterMap.get(key)) {
            // 每秒访问的次数
            Constants.rateLimiterMap.put(key, RateLimiter.create(doRateLimiter.permitsPerSecond()));
        }

        RateLimiter rateLimiter = Constants.rateLimiterMap.get(key);
        // 限制访问的次数
        if (rateLimiter.tryAcquire()) {
            return jp.proceed();
        }

        return JSON.parseObject(doRateLimiter.returnJson(), method.getReturnType());
    }
}
