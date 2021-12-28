package com.simple.mybatis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 17:34
 **/
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
@Component
public class MybatisProperties {

    public static final String MYBATIS_PREFIX = "mybatis";

    /**
     * classpath*:mapper/*.xml
     */
    private String mapperLocations;

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

    public static String getMybatisPrefix() {
        return MYBATIS_PREFIX;
    }

    public String getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String mapperLocations) {
        this.mapperLocations = mapperLocations;
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
