package com.simple.dbrouter;

/**
 * 功能描述: 记录分库数、分表数
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:18
 **/
public class DbRouterConfig {

    /**
     * 分库数
     */
    private int dbCount;

    /**
     * 分表数
     */
    private int tbCount;

    /**
     * 路由字段
     */
    private String routerKey;

    public DbRouterConfig() {
    }

    public DbRouterConfig(int dbCount, int tbCount, String routerKey) {
        this.dbCount = dbCount;
        this.tbCount = tbCount;
        this.routerKey = routerKey;
    }

    public int getDbCount() {
        return dbCount;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public int getTbCount() {
        return tbCount;
    }

    public void setTbCount(int tbCount) {
        this.tbCount = tbCount;
    }

    public String getRouterKey() {
        return routerKey;
    }

    public void setRouterKey(String routerKey) {
        this.routerKey = routerKey;
    }
}
