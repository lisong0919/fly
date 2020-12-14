package com.wealth.fly.core.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class RealTimePrice {
    private Long dataTime;
    private BigDecimal price;
}
