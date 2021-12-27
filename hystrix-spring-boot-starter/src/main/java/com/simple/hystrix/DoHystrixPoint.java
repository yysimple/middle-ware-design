package com.simple.hystrix;

import com.simple.hystrix.annotation.DoHystrix;
import com.simple.hystrix.value.IValueService;
import com.simple.hystrix.value.impl.DefaultHystrixValueImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 功能描述: 处理熔断的切面类
 *
 * @author: WuChengXing
 * @create: 2021-12-27 17:52
 **/
@Aspect
@Component
public class DoHystrixPoint {

    /**
     * 织入点
     */
    @Pointcut("@annotation(com.simple.hystrix.annotation.DoHystrix)")
    public void aopPoint() {
    }

    /**
     * 拿到方法的代理
     *
     * @param jp
     * @param doGovern
     * @return
     * @throws Throwable
     */
    @Around("aopPoint() && @annotation(doGovern)")
    public Object doRouter(ProceedingJoinPoint jp, DoHystrix doGovern) throws Throwable {
        IValueService valveService = new DefaultHystrixValueImpl(doGovern.timeoutValue());
        return valveService.access(jp, getMethod(jp), doGovern, jp.getArgs());

    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

}
