package com.simple.probe;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-04-13 19:16
 **/
public class AgentMain {
    /**
     * JVM 首先尝试在代理类上调用以下方法
     *
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> builder
                // 拦截任意方法
                .method(ElementMatchers.named("executeInternal"))
                // 委托
                .intercept(MethodDelegation.to(SqlMonitorMethod.class));

        new AgentBuilder
                .Default()
                .type(ElementMatchers.nameStartsWith("com.mysql.jdbc.PreparedStatement"))
                .transform(transformer)
                .installOn(inst);
    }

    /**
     * 如果代理类没有实现上面的方法，那么 JVM 将尝试调用该方法
     *
     * @param agentArgs
     */
    public static void premain(String agentArgs) {
    }
}
