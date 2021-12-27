package com.simple.ratelimiter.value;

import com.simple.ratelimiter.annotation.DoRateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 22:48
 **/
public interface IValveService {

    Object access(ProceedingJoinPoint jp, Method method, DoRateLimiter doRateLimiter, Object[] args) throws Throwable;
}
