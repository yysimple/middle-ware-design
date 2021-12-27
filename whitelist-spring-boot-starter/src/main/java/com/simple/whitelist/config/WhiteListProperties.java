package com.simple.whitelist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 15:52
 **/
@ConfigurationProperties("simple.whitelist")
@Component
public class WhiteListProperties {

    private String users;

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

}
