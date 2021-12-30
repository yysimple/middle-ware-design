package com.simple.dbrouter;

/**
 * 功能描述: 记录分库数、分表数
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:18
 **/
public class DBRouterConfig {

    /**
     * 分库数
     */
    private int dbCount;

    /**
     * 分表数
     */
    private int tbCount;

    public DBRouterConfig() {
    }

    public DBRouterConfig(int dbCount, int tbCount) {
        this.dbCount = dbCount;
        this.tbCount = tbCount;
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
}
