package com.simple.rpc.config;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:27
 **/
public class ConsumerConfig {

    /**
     * 接口
     */
    protected String nozzle;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
