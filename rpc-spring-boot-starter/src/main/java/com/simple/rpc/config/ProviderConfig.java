package com.simple.rpc.config;

/**
 * 功能描述: 服务提供者的配置信息
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:26
 **/
public class ProviderConfig {

    /**
     * 接口
     */
    protected String nozzle;

    /**
     * 映射
     */
    protected String ref;

    /**
     * 别名
     */
    protected String alias;

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
}
