package com.simple.asm;

import com.simple.asm.probe.ProfilingTransformer;

import java.lang.instrument.Instrumentation;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:04
 **/
public class AgentMain {
    /**
     * JVM 首先尝试在代理类上调用以下方法
     *
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ProfilingTransformer());
    }

    /**
     * 如果代理类没有实现上面的方法，那么 JVM 将尝试调用该方法
     *
     * @param agentArgs
     */
    public static void premain(String agentArgs) {
    }
}
