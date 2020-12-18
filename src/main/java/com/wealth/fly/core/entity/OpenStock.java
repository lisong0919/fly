package com.wealth.fly.core.entity;

import com.wealth.fly.core.strategy.Strategy;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class OpenStock {
    private BigDecimal openPrice;

}
