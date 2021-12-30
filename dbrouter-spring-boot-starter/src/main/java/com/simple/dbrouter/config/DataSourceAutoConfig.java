package com.simple.dbrouter.config;

import com.simple.dbrouter.DBRouterConfig;
import com.simple.dbrouter.dynamic.DynamicDataSource;
import com.simple.dbrouter.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
public class DataSourceAutoConfig implements EnvironmentAware {

    Logger logger = LoggerFactory.getLogger(DataSourceAutoConfig.class);

    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>(4);

    /**
     * 分库数
     */
    private int dbCount;

    /**
     * 分表数
     */
    private int tbCount;

    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dbCount, tbCount);
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
        return dynamicDataSource;
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
    }
}
