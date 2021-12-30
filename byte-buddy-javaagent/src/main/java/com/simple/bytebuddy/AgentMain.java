package com.simple.bytebuddy;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 17:54
 **/
public class AgentMain {

    /**
     * JVM 首先尝试在代理类上调用以下方法
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
            return builder
                    .method(ElementMatchers.any()) // 拦截任意方法
                    .intercept(MethodDelegation.to(MonitorMethod.class)); // 委托
        };

        new AgentBuilder
                .Default()
                // 指定需要拦截的类 "com.simple.test.AgentTest"
                .type(ElementMatchers.nameStartsWith(agentArgs))
                .transform(transformer)
                .installOn(inst);
    }

    /**
     * 如果代理类没有实现上面的方法，那么 JVM 将尝试调用该方法
     * @param agentArgs
     */
    public static void premain(String agentArgs) {
    }
}
