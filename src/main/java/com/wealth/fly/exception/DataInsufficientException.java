package com.wealth.fly.exception;

public class DataInsufficientException extends RuntimeException{

    public DataInsufficientException(){

    }

    public DataInsufficientException(String message){
        super(message);
    }

}
