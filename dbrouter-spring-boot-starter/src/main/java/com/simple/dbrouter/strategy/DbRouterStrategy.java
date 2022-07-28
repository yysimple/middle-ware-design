package com.simple.dbrouter.strategy;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: db路由策略
 *
 * @author: WuChengXing
 * @create: 2022-07-26 14:55
 **/
public interface DbRouterStrategy {

    /**
     * 路由计算
     *
     * @param dbKeyAttr 路由字段
     */
    void doRouter(String dbKeyAttr);

    /**
     * 手动设置分库路由
     *
     * @param dbIdx 路由库，需要在配置范围内
     */
    void setDbKey(int dbIdx);

    /**
     * 手动设置分表路由
     *
     * @param tbIdx 路由表，需要在配置范围内
     */
    void setTableKey(int tbIdx);

    /**
     * 获取分库数
     *
     * @return 数量
     */
    int dbCount();

    /**
     * 获取分表数
     *
     * @return 数量
     */
    int tbCount();

    /**
     * 清除路由
     */
    void clear();
}
