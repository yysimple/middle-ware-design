package com.simple.rpc.config.spring.bean;

import com.alibaba.fastjson.JSON;
import com.simple.rpc.config.ConsumerConfig;
import com.simple.rpc.entity.RpcProviderConfig;
import com.simple.rpc.network.client.ClientSocket;
import com.simple.rpc.network.msg.Request;
import com.simple.rpc.reflect.JDKProxy;
import com.simple.rpc.register.RedisRegistryCenter;
import com.simple.rpc.util.ClassLoaderUtils;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.FactoryBean;

/**
 * 功能描述: 当有客户端进行连接的时候
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:31
 **/
public class ConsumerBean<T> extends ConsumerConfig implements FactoryBean<T> {
    private ChannelFuture channelFuture;

    private RpcProviderConfig rpcProviderConfig;

    @Override
    public T getObject() throws Exception {

        //从redis获取链接
        if (null == rpcProviderConfig) {
            String infoStr = RedisRegistryCenter.obtainProvider(nozzle, alias);
            rpcProviderConfig = JSON.parseObject(infoStr, RpcProviderConfig.class);
        }
        assert null != rpcProviderConfig;

        //获取通信channel
        if (null == channelFuture) {
            ClientSocket clientSocket = new ClientSocket(rpcProviderConfig.getHost(), rpcProviderConfig.getPort());
            new Thread(clientSocket).start();
            for (int i = 0; i < 100; i++) {
                if (null != channelFuture) {
                    break;
                }
                Thread.sleep(500);
                channelFuture = clientSocket.getFuture();
            }
        }

        Request request = new Request();
        request.setChannel(channelFuture.channel());
        request.setNozzle(nozzle);
        request.setRef(rpcProviderConfig.getRef());
        request.setAlias(alias);
        return (T) JDKProxy.getProxy(ClassLoaderUtils.forName(nozzle), request);
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return ClassLoaderUtils.forName(nozzle);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
