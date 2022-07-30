package com.simple.dbrouter;

import com.simple.dbrouter.annotation.DBRouter;
import com.simple.dbrouter.annotation.RouterParam;
import com.simple.dbrouter.strategy.DbRouterStrategy;
import com.simple.dbrouter.util.ClassUtils;
import com.simple.dbrouter.util.ReflectUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 功能描述: 切面
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:11
 **/
@Aspect
public class DbRouterJoinPoint {

    private final Logger logger = LoggerFactory.getLogger(DbRouterJoinPoint.class);

    private final DbRouterConfig dbRouterConfig;

    private final DbRouterStrategy dbRouterStrategy;

    public DbRouterJoinPoint(DbRouterConfig dbRouterConfig, DbRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Pointcut("@annotation(com.simple.dbrouter.annotation.DBRouter)")
    public void aopPoint() {
    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        // 拿到对应的key，可以是用户id之类的
        String dbKey = dbRouter.key();
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();
        // 如果key为空，则抛出异常
        if (StringUtils.isBlank(dbKey)) {
            throw new RuntimeException("annotation DBRouter key is null！");
        }
        // 计算路由
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs(), dealParams(jp));

        if (!StringUtils.isNotBlank(dbKeyAttr)) {
            throw new RuntimeException("DBRouter key mapping value is null！");
        }
        dbRouterStrategy.doRouter(dbKeyAttr);
        // 返回结果
        try {
            return jp.proceed();
        } finally {
            logger.info("调用的方法：{}", getMethod(jp).getName());
            dbRouterStrategy.clear();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    private String dealParams(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        Class<?> realClass = null;
        try {
            realClass = ClassUtils.getRealClass(jp.getTarget());
        } catch (Exception e) {
            logger.error("获取真实代理失败！");
            e.printStackTrace();
        }
        assert realClass != null;
        Method method = realClass.getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Object[] args = jp.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Boolean isRouterParam = containsRouterParam(parameterAnnotations[i]);
            if (isRouterParam) {
                return args[i].toString();
            }
        }
        return null;
    }

    private Boolean containsRouterParam(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof RouterParam) {
                return true;
            }
        }
        return false;
    }

    public String getAttrValue(String attr, Object[] args, String paramValue) {
        if (1 == args.length) {
            Object arg = args[0];
            if (checkIsNormal(arg)) {
                return arg.toString();
            }
        }

        if (StringUtils.isNotBlank(paramValue)) {
            return paramValue;
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = ReflectUtils.getValueOfGet(arg, attr).toString();
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 一个参数的情况下，只支持三种类型
     *
     * @param arg
     * @return
     */
    private Boolean checkIsNormal(Object arg) {
        if (arg instanceof Long || arg instanceof String || arg instanceof Integer) {
            return true;
        }
        return false;
    }
}
