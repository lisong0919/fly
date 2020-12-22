package com.wealth.fly.core.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class MAParam {
    private long datatime;
    private BigDecimal value;

    public MAParam(){

    }

    public MAParam(long datatime, BigDecimal value) {
        this.datatime = datatime;
        this.value = value;
    }
}
