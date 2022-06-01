package com.simple.asm.probe;

import com.alibaba.fastjson.JSON;
import com.simple.asm.base.MethodTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Logger;

/**
 * 功能描述: 增强切面，用来做监控逻辑
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:06
 **/
public class ProfilingAspect {
    public static final int MAX_NUM = 1024 * 32;

    private final static AtomicInteger INDEX = new AtomicInteger(0);
    private final static AtomicReferenceArray<MethodTag> METHOD_TAG_ARR = new AtomicReferenceArray<>(MAX_NUM);
    private final static Map<Integer, List<String>> METHOD_PARAMETER_GROUP = new ConcurrentHashMap<>();
    private static Logger log = Logger.getLogger(ProfilingAspect.class.toString());

    /**
     * 通过原子类生成方法ID
     *
     * @param tag
     * @return
     */
    public static int generateMethodId(MethodTag tag) {
        int methodId = INDEX.getAndIncrement();
        if (methodId > MAX_NUM) {
            return -1;
        }
        METHOD_TAG_ARR.set(methodId, tag);
        return methodId;
    }

    /**
     * 通过方法id去更新参数信息
     *
     * @param methodId
     * @param parameterName
     */
    public static synchronized void setMethodParameterGroup(final int methodId, String parameterName) {
        List<String> parameterList = METHOD_PARAMETER_GROUP.computeIfAbsent(methodId, k -> new ArrayList<>());
        parameterList.add(parameterName);
    }

    /**
     * 监控点：正常的返回值监控
     *
     * @param startNanos
     * @param methodId
     * @param requests
     * @param response
     */
    public static void point(final long startNanos, final int methodId, Object[] requests, Object response) {
        log.info("=== 正常的返回值监控 ===");
        MethodTag method = METHOD_TAG_ARR.get(methodId);
        List<String> parameters = METHOD_PARAMETER_GROUP.get(methodId);
        System.out.println("监控 - Begin");
        System.out.println("方法：" + method.getFullClassName() + "." + method.getMethodName());
        System.out.println("入参：" + JSON.toJSONString(parameters) + " 入参类型：" + JSON.toJSONString(method.getParameterTypeList()) + " 入参[值]：" + JSON.toJSONString(requests));
        System.out.println("出参：" + method.getReturnParameterType() + " 出参[值]：" + JSON.toJSONString(response));
        System.out.println("耗时：" + (System.nanoTime() - startNanos) / 1000000 + "(s)");
        System.out.println("监控 - End\r\n");
    }

    /**
     * 异常信息监控
     *
     * @param startNanos
     * @param methodId
     * @param requests
     * @param throwable
     */
    public static void point(final long startNanos, final int methodId, Object[] requests, Throwable throwable) {
        log.info("=== 异常信息监控 ===");
        MethodTag method = METHOD_TAG_ARR.get(methodId);
        List<String> parameters = METHOD_PARAMETER_GROUP.get(methodId);
        System.out.println("监控 - Begin");
        System.out.println("方法：" + method.getFullClassName() + "." + method.getMethodName());
        System.out.println("入参：" + JSON.toJSONString(parameters) + " 入参类型：" + JSON.toJSONString(method.getParameterTypeList()) + " 入数[值]：" + JSON.toJSONString(requests));
        System.out.println("异常：" + throwable.getMessage());
        System.out.println("耗时：" + (System.nanoTime() - startNanos) / 1000000 + "(s)");
        System.out.println("监控 - End\r\n");
    }

    /**
     * 无返回值监控
     *
     * @param startNanos
     * @param methodId
     * @param requests
     */
    public static void point(final long startNanos, final int methodId, Object[] requests) {
        log.info("=== 无返回值监控 ===");
        MethodTag method = METHOD_TAG_ARR.get(methodId);
        List<String> parameters = METHOD_PARAMETER_GROUP.get(methodId);
        System.out.println("监控 - Begin");
        System.out.println("方法：" + method.getFullClassName() + "." + method.getMethodName());
        System.out.println("耗时：" + (System.nanoTime() - startNanos) / 1000000 + "(s)");
        System.out.println("监控 - End\r\n");
    }
}
