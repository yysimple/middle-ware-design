package com.simple.rpc.network.future;

import com.simple.rpc.network.msg.Response;

import java.util.concurrent.Future;

/**
 * 功能描述: 用于异步通知的类
 *
 * @author: WuChengXing
 * @create: 2021-12-29 10:57
 **/
public interface WriteFuture<T> extends Future<T> {

    /**
     * 异常原因
     *
     * @return
     */
    Throwable cause();

    /**
     * 设置异常信息
     *
     * @param cause
     */
    void setCause(Throwable cause);

    /**
     * 是否写成功
     *
     * @return
     */
    boolean isWriteSuccess();

    /**
     * 设置写结果
     *
     * @param result
     */
    void setWriteResult(boolean result);

    /**
     * 请求id
     *
     * @return
     */
    String requestId();

    /**
     * 响应结果
     *
     * @return
     */
    T response();

    /**
     * 设置响应结果
     *
     * @param response
     */
    void setResponse(Response response);

    /**
     * 是否超时
     *
     * @return
     */
    boolean isTimeout();
}
