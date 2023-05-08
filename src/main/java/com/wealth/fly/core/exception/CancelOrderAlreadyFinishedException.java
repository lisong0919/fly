package com.wealth.fly.core.exception;

/**
 * @author : lisong
 * @date : 2023/5/7
 */
public class CancelOrderAlreadyFinishedException extends ExchangerException {
    public CancelOrderAlreadyFinishedException(String message, Integer code) {
        super(message, code);
    }
}
