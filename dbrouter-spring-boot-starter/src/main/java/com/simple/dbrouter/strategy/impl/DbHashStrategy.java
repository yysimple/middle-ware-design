package com.simple.dbrouter.strategy.impl;

import com.simple.dbrouter.DbContextHolder;
import com.simple.dbrouter.DbRouterConfig;
import com.simple.dbrouter.strategy.DbRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: hash策略
 *
 * @author: WuChengXing
 * @create: 2022-07-26 14:59
 **/
public class DbHashStrategy implements DbRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DbHashStrategy.class);

    private DbRouterConfig dbRouterConfig;

    public DbHashStrategy(DbRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        // 这里的size需要是2的整数倍
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();
        // 扰动函数参考HashMap（p = tab[i = (n - 1) & hash])）此处操作也是相当于
        // (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16)) % size，最后定位到指定索引位置
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));
        // 库表索引为了方便计算，假设 dbCount = 2 / tbCount = 4 / idx = 7
        // 那么这里数据库索引就是 1 ~ 2
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        // 这里表索引就是 7 -（0 ~ 4） = （3 ~ 7） % 4 + 1 = （1 ~ 4）
        int tbIdx = (idx - dbRouterConfig.getTbCount() * (dbIdx - 1)) % dbRouterConfig.getTbCount() + 1;
        // 设置到 ThreadLocal,这里就是将 1，2 这样的 设置成 01 02 ，所以在配置文件中，我们需要注意我们的设置要以 01 这样的结尾或者开头
        DbContextHolder.setDBKey(String.format("%02d", dbIdx));
        DbContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.info("数据库路由 dbIdx：{} tbIdx：{}", dbIdx, tbIdx);
    }

    @Override
    public void setDbKey(int dbIdx) {
        DbContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTableKey(int tbIdx) {
        DbContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DbContextHolder.clearDBKey();
        DbContextHolder.clearTBKey();
    }
}
