package com.wealth.fly.core.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class RealTimePrice {
    private Long dataTime;
    private BigDecimal price;
    private BigDecimal maPrice;

    public RealTimePrice(){}

    public RealTimePrice(Long dataTime, BigDecimal price, BigDecimal maPrice) {
        this.dataTime = dataTime;
        this.price = price;
        this.maPrice = maPrice;
    }
}
