package com.simple.rpc.annotation;

import com.simple.rpc.config.ServerAutoConfiguration;
import com.simple.rpc.config.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 开启RPC的注解
 * 这里开启RPC功能的入口，会去加载标准的 xsd文件通过RpcNamespaceHandler
 *
 * @author: WuChengXing
 * @create: 2021-12-29 10:20
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({ServerAutoConfiguration.class})
@EnableConfigurationProperties(ServerProperties.class)
@ComponentScan("com.simple.rpc.*")
public @interface EnableRpc {

}
