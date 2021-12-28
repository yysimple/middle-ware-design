package com.simple.mybatis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 18:13
 **/
@ConfigurationProperties(prefix = DataSourceProperties.SPRING_DATASOURCE_PREFIX)
@Component
public class DataSourceProperties {

    public static final String SPRING_DATASOURCE_PREFIX = "spring.datasource";

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    public static String getSpringDatasourcePrefix() {
        return SPRING_DATASOURCE_PREFIX;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
