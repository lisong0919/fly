package com.wealth.fly.statistic;


import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.entity.CloseStock;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.StrategyAction;
import com.wealth.fly.core.strategy.Strategy;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;


public class SimpleStatisticStrategyAction implements StrategyAction {

    private Map<String, StatisticItem> targetKlineMap = new LinkedHashMap<>();


    public Map<String, StatisticItem> getTargetKlineMap() {
        return targetKlineMap;
    }


    @Override
    public void onOpenStock(Strategy strategy, OpenStock openStock) {

    }

    @Override
    public void onCloseStock(Strategy openStrategy, Strategy closeStrategy, CloseStock closeStock) {
        StatisticItem item = new StatisticItem();
        item.setStartPrice(closeStock.getOpenPirce());
        item.setStartDataTime(closeStock.getOpenDataTime());
//        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(openKLine.getClose(), openKLine.getOpen()));
        item.setGoingLong(openStrategy.isGoingLong());
        item.setEndPrice(closeStock.getClosePrice());
        item.setEndDataTime(closeStock.getCloseDataTime());
        item.setProfitPercent(MathUtil.distancePercentInDecimal(item.getEndPrice(), item.getStartPrice()));

        boolean increase = item.getEndPrice().compareTo(item.getStartPrice()) > 0;
        item.setIsWin(item.getGoingLong() ? increase : !increase);

        if (!item.getIsWin()) {
            item.setProfitPercent(item.getProfitPercent().multiply(new BigDecimal(-1)));
        }

        targetKlineMap.put("" + item.getStartDataTime(), item);
    }
}
