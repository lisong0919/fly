package com.wealth.fly.statistic;

import com.wealth.fly.common.MathUtil;
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

    @Override
    public void doAction(Strategy strategy, KLine openKLine, KLine closeKline, BigDecimal priceMA) {
        StatisticStrategyAction.StatisticItem item = new StatisticStrategyAction.StatisticItem();
        item.setStartPrice(openKLine.getClose());
        item.setStartDataTime(openKLine.getDataTime());
        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(closeKline.getClose(), openKLine.getClose()));
        item.setGoingLong(strategy.isGoingLong());
        item.setEndPrice(closeKline.getClose());
        item.setEndDataTime(closeKline.getDataTime());
        item.setProfitPercent(MathUtil.distancePercentInDecimal(item.getEndPrice(), item.getStartPrice()));

        boolean increase = item.getEndPrice().compareTo(item.getStartPrice()) > 0;
        item.setIsWin(item.getGoingLong() ? increase : !increase);

        if (!item.getIsWin()) {
            item.setProfitPercent(item.getProfitPercent().multiply(new BigDecimal(-1)));
        }

        targetKlineMap.put("" + item.getStartDataTime(), item);
    }

    public Map<String, StatisticStrategyAction.StatisticItem> getTargetKlineMap() {
        return targetKlineMap;
    }


}
