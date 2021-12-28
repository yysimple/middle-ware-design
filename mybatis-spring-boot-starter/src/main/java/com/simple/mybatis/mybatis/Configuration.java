package com.simple.mybatis.mybatis;

import java.sql.Connection;
import java.util.Map;

/**
 * 功能描述: 我们这里把所有的数据都存放在map中，mybatis中会封装一个MappedStatement对象，来处理一部分数据
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:51
 **/
public class Configuration {
    /**
     * 连接信息
     */
    protected Connection connection;

    /**
     * 存放数据源信息
     */
    protected Map<String, String> dataSource;

    /**
     * 存放sql信息
     */
    protected Map<String, XNode> mapperElement;

    /**
     * 需要扫描的Mapper包
     */
    private String baseMapperPackage;

    /**
     * 哪些xml中是否支持别名
     */
    private String typeAliasesPackage;

    /**
     * 是否支持Mysql跟javaBean的字段自动映射（不区分大小写，支持下划线）
     * == mapUnderscoreToCamelCase
     */
    private Boolean isAllowedMapper;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }

    public Connection getConnection() {
        return connection;
    }

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }

    public String getBaseMapperPackage() {
        return baseMapperPackage;
    }

    public void setBaseMapperPackage(String baseMapperPackage) {
        this.baseMapperPackage = baseMapperPackage;
    }

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    public Boolean getIsAllowedMapper() {
        return isAllowedMapper;
    }

    public void setIsAllowedMapper(Boolean isAllowedMapper) {
        this.isAllowedMapper = isAllowedMapper;
    }
}
