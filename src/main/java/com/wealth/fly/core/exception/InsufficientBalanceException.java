package com.wealth.fly.core.exception;

/**
 * @author : lisong
 * @date : 2023/5/8
 */
public class InsufficientBalanceException extends ExchangerException {
    public InsufficientBalanceException(String message, Integer code) {
        super(message, code);
    }
}
