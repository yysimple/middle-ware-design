package com.simple.rpc.network.msg;

import io.netty.channel.Channel;

/**
 * 功能描述: 响应的消息体
 *
 * @author: WuChengXing
 * @create: 2021-12-29 10:37
 **/
public class Response {
    /**
     * 关联通道
     */
    private transient Channel channel;

    /**
     * 此次请求id
     */
    private String requestId;

    /**
     * 返回的信息
     */
    private Object result;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
