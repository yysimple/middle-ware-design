package com.simple.schedule.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 15:49
 **/
public class SchedulingRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingRunnable.class);

    /**
     * 类对象
     */
    private final Object bean;

    /**
     * 类名称
     */
    private final String beanName;

    /**
     * 方法名称
     */
    private final String methodName;

    public SchedulingRunnable(Object bean, String beanName, String methodName) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    /**
     * 通过反射进行具体方法调用
     */
    @Override
    public void run() {
        try {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean);
        } catch (Exception e) {
            logger.error("middleware schedule err！", e);
        }
    }

    public String taskId() {
        return beanName + "_" + methodName;
    }
}
