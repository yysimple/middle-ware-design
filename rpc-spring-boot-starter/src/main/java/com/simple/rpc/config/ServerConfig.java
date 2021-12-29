package com.simple.rpc.config;

/**
 * 功能描述: 这里可以根据注册中心的不同来选择,这里支持spring的xml的方式进行注册信息
 * 后面会标注，因为是starter所以不采取xml的形式去完成属性注入
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:25
 **/
public class ServerConfig {
    /**
     * 注册中心地址
     */
    protected String host;

    /**
     * 注册中心端口
     */
    protected int port;

    /**
     * 注册中心的密码
     */
    protected String password;

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
