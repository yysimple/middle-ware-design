package com.simple.rpc.config.spring;

import com.simple.rpc.config.spring.bean.ConsumerBean;
import com.simple.rpc.config.spring.bean.ProviderBean;
import com.simple.rpc.config.spring.bean.ServerBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:30
 **/
public class RpcNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("server", new RpcBeanDefinitionParser(ServerBean.class));
        registerBeanDefinitionParser("provider", new RpcBeanDefinitionParser(ProviderBean.class));
        registerBeanDefinitionParser("consumer", new RpcBeanDefinitionParser(ConsumerBean.class));
    }
}
