package com.wealth.fly.core.entity;

import com.wealth.fly.core.strategy.Strategy;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class CloseStock {
    private Long openDataTime;
    private Long closeDataTime;
    private BigDecimal openPirce;
    private BigDecimal closePrice;
}
