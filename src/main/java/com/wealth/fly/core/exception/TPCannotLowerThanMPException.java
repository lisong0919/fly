package com.wealth.fly.core.exception;

/**
 * @author : lisong
 * @date : 2023/5/6
 */
public class TPCannotLowerThanMPException extends RuntimeException {

    private Integer code;
    private String message;

    public TPCannotLowerThanMPException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
