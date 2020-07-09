package com.wealth.fly.core.strategy;

import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;

public interface Action {
    void doAction(Strategy strategy, KLine kLine, KLine closeKline, BigDecimal priceMA);
}
