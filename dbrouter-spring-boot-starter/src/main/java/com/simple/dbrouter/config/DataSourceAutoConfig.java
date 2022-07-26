package com.simple.dbrouter.config;

import com.simple.dbrouter.DbRouterConfig;
import com.simple.dbrouter.DbRouterJoinPoint;
import com.simple.dbrouter.dynamic.DynamicDataSource;
import com.simple.dbrouter.dynamic.DynamicMybatisPlugin;
import com.simple.dbrouter.strategy.DbRouterStrategy;
import com.simple.dbrouter.strategy.impl.DbHashStrategy;
import com.simple.dbrouter.util.PropertyUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:27
 **/
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {

    Logger logger = LoggerFactory.getLogger(DataSourceAutoConfig.class);

    /**
     * 数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>(4);

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

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

    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DbRouterJoinPoint point(DbRouterConfig dbRouterConfig, DbRouterStrategy dbRouterStrategy) {
        return new DbRouterJoinPoint(dbRouterConfig, dbRouterStrategy);
    }

    @Bean
    public Interceptor plugin() {
        return new DynamicMybatisPlugin();
    }

    @Bean
    public DbRouterConfig dbRouterConfig() {
        return new DbRouterConfig(dbCount, tbCount, routerKey);
    }

    @Bean
    public DataSource dataSource() {
        // 根据配置文件创建数据源
        Map<Object, Object> targetDataSources = new HashMap<>(dbCount);
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            // 创建一个新的数据源
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString(), objMap.get("username").toString(), objMap.get("password").toString()));
        }
        // 动态设置多数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(defaultDataSourceConfig.get("url").toString(),
                defaultDataSourceConfig.get("username").toString(),
                defaultDataSourceConfig.get("password").toString()));
        return dynamicDataSource;
    }

    @Bean
    public DbRouterStrategy dbRouterStrategy(DbRouterConfig dbRouterConfig) {
        return new DbHashStrategy(dbRouterConfig);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "router.jdbc.datasource.";

        // 拿到数据库的数量和表的数量，这里会以一个map的形式进行存储，举个例子：
        /**
         * router:
         *   jdbc:
         *     datasource:
         *       dbCount: 2
         */
        // 上面这个的存储是：environment.propertySources.propertySourceList.source（这是个map）中以
        // router.jdbc.datasource.dbCount 为key， 2为值的方式存储
        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));
        routerKey = environment.getProperty(prefix + "routerKey");

        if ((dbCount * tbCount) % 2 != 0) {
            logger.error("请将您的数据库数（dbCount）* 表数量（tbCount）设置为2的倍数，否则有意外错误！！");
        }
        // 拿到数据源信息
        String dataSources = environment.getProperty(prefix + "list");
        for (String dbInfo : dataSources.split(",")) {
            // 将数据源的 url 等作为 key存入到map中，后面需要注入到spring的数据源中
            // 这里也只是拿到不同版本的 environment 然后根据上面一样的处理，拿到数据，只是最后会转成一个map返回
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }

        // 默认数据源
        String defaultData = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix + defaultData, Map.class);
    }
}
