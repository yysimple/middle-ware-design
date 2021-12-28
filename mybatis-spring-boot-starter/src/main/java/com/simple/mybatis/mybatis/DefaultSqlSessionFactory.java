package com.simple.mybatis.mybatis;

/**
 * 功能描述: 默认的工厂实现，可以拿到sql会话
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:53
 **/
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        // 传递配置信息下去，这里Mybatis是交由 他自己写的Executor去执行sql的
        return new DefaultSqlSession(configuration);
    }
}
