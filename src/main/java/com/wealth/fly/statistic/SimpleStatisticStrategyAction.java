package com.wealth.fly.statistic;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.Action;
import com.wealth.fly.core.strategy.Strategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SimpleStatisticStrategyAction implements Action {

    private Map<String, StatisticStrategyAction.StatisticItem> targetKlineMap = new LinkedHashMap<>();


    public Map<String, StatisticStrategyAction.StatisticItem> getTargetKlineMap() {
        return targetKlineMap;
    }


    @Override
    public void onOpenStock(Strategy strategy, KLine openKLine) {

    }

    @Override
    public void onCloseStock(Strategy openStrategy, KLine openKLine, Strategy closeStrategy, BigDecimal closePrice, long closeDataTime) {
        StatisticStrategyAction.StatisticItem item = new StatisticStrategyAction.StatisticItem();
        item.setStartPrice(openKLine.getClose());
        item.setStartDataTime(openKLine.getDataTime());
        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(openKLine.getClose(), openKLine.getOpen()));
        item.setGoingLong(openStrategy.isGoingLong());
        item.setEndPrice(closePrice);
        item.setEndDataTime(closeDataTime);
        item.setProfitPercent(MathUtil.distancePercentInDecimal(item.getEndPrice(), item.getStartPrice()));

        boolean increase = item.getEndPrice().compareTo(item.getStartPrice()) > 0;
        item.setIsWin(item.getGoingLong() ? increase : !increase);

        if (!item.getIsWin()) {
            item.setProfitPercent(item.getProfitPercent().multiply(new BigDecimal(-1)));
        }

        targetKlineMap.put("" + item.getStartDataTime(), item);
    }
}
