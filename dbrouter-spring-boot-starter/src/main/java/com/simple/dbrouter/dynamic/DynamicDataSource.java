package com.simple.dbrouter.dynamic;

import com.simple.dbrouter.DbContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 功能描述: 继承spring的AbstractRoutingDataSource，会根据我们计算出来的
 * dbKey去选择指定的数据源进行操作，这里的话，是使用Map把数据源存储起来；
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:24
 **/
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DbContextHolder.getDBKey();
    }
}
