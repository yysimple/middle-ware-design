package com.simple.pagination.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 项目: question-study-improve
 * <p>
 * 功能描述: 数据源配置
 *
 * @author: WuChengXing
 * @create: 2021-06-29 15:02
 **/
@ConfigurationProperties("spring.datasource")
public class DataSourceProperties {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
