package com.wealth.fly.core.strategy;

import com.wealth.fly.core.entity.CloseStock;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.OpenStock;

import java.math.BigDecimal;

public interface StrategyAction {

    void onOpenStock(Strategy strategy, OpenStock openStock);

    void onCloseStock(Strategy openStrategy, Strategy closeStrategy, CloseStock closeStock);

}
