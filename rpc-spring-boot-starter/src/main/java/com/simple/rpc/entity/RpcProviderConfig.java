package com.simple.rpc.entity;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-29 10:24
 **/
public class RpcProviderConfig {
    /**
     * 接口
     */
    private String nozzle;

    /**
     * 映射
     */
    private String ref;

    /**
     * 别名
     */
    private String alias;

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    public String getNozzle() {
        return nozzle;
    }

    public void setNozzle(String nozzle) {
        this.nozzle = nozzle;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

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
}
