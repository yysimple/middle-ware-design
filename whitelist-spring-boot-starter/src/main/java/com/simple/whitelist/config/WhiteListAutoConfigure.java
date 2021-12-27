package com.simple.whitelist.config;

import com.simple.whitelist.DoJoinPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 15:53
 **/
@Configuration
@ConditionalOnClass(WhiteListProperties.class)
@EnableConfigurationProperties(WhiteListProperties.class)
public class WhiteListAutoConfigure {

    @Bean("whiteListConfig")
    @ConditionalOnMissingBean
    public String whiteListConfig(WhiteListProperties properties) {
        return properties.getUsers();
    }

    @Bean
    @ConditionalOnMissingBean
    public DoJoinPoint point(){
        return new DoJoinPoint();
    }


}
