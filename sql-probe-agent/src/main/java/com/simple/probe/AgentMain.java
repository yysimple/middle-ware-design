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
        System.out.println("-------- agent start weave --------");
        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> builder
                // 拦截任意方法，这里是拦截对应的方法，这里是精确到方法，这里跟下面可以配合，这里也可以选择 any 如注释的代码
                .method(ElementMatchers.named("executeInternal"))
                // .method(ElementMatchers.any())
                // 委托
                .intercept(MethodDelegation.to(SqlMonitorMethod.class));

        new AgentBuilder
                .Default()
                // 这里的话可以精确到类，也可以指定包名；如果 这里指定com.mysql，上面指定是 any();那么agent则会扫码 com.mysql下所有类的所有方法进行织入
                .type(ElementMatchers.nameStartsWith("com.mysql.cj.jdbc.ClientPreparedStatement"))
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
