package com.wealth.fly.core.exception;

/**
 * @author : lisong
 * @date : 2023/5/6
 */
public class TPCannotLowerThanMPException extends ExchangerException {


    public TPCannotLowerThanMPException(String message, Integer code) {
        super(message, code);
    }
}
