package com.simple.backlist;

import com.alibaba.fastjson.JSON;
import com.simple.backlist.annotation.DoBackList;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 09:36
 **/
@Aspect
public class DoBackListPoint {

    Logger logger = LoggerFactory.getLogger(DoBackListPoint.class);

    @Pointcut("@annotation(com.simple.backlist.annotation.DoBackList)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object deal(ProceedingJoinPoint jp) throws Throwable {
        // 先获取方法信息
        Method method = getMethod(jp);
        // 拿到注解信息
        DoBackList annotation = method.getAnnotation(DoBackList.class);
        // 获取注解中的方法名
        String handlerMethodName = annotation.method();
        Method handlerMethod = getClass(jp).getMethod(handlerMethodName, method.getParameterTypes());
        Class<?> returnType = handlerMethod.getReturnType();
        // 只支持返回boolean类型
        if (!returnType.getName().equals("boolean")) {
            throw new RuntimeException("annotation @DoBackList set method：" + handlerMethodName + " returnType is not boolean");
        }
        // 调用该方法
        boolean handlerPass = (boolean) handlerMethod.invoke(jp.getThis(), jp.getArgs());
        logger.info("拦截方法执行完成，是否是黑名单用户：{}", (!handlerPass ? "是，" + method.getParameterTypes().toString() : "不是"));
        // 最后进行处理逻辑
        return handlerPass ? jp.proceed() : JSON.parseObject(annotation.returnJson(), method.getReturnType());
    }

    public Method getMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    private Class<? extends Object> getClass(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass();
    }
}

