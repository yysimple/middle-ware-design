package com.simple.rpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能描述: 这个是用于Spring的方式进行注册的
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:23
 **/
@ConfigurationProperties("rpc.register")
@Component
public class ServerProperties {

    /**
     * 注册中心地址
     */
    private String host;

    /**
     * 注册中心端口
     */
    private int port;

    /**
     * 注册中心密码
     */
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
