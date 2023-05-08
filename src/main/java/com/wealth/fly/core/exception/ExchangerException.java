package com.wealth.fly.core.exception;

/**
 * @author : lisong
 * @date : 2023/5/8
 */
public class ExchangerException extends RuntimeException{
    private Integer code;
    private String message;

    public ExchangerException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
