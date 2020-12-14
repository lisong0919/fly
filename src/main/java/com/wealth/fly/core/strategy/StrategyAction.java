package com.wealth.fly.core.strategy;

import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;

public interface StrategyAction {

    void onOpenStock(Strategy strategy, KLine kLine);

    void onCloseStock(Strategy openStrategy, KLine openKLine, Strategy closeStrategy, BigDecimal closePrice, long closeDataTime);

}
