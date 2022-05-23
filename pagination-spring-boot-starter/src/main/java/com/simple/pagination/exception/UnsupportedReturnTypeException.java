package com.simple.pagination.exception;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 15:12
 */
public class UnsupportedReturnTypeException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public UnsupportedReturnTypeException() {
    }

    public UnsupportedReturnTypeException(String message) {
        super(message);
    }

    public UnsupportedReturnTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedReturnTypeException(Throwable cause) {
        super(cause);
    }
}
