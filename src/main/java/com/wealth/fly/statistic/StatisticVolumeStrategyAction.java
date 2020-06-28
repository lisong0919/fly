package com.wealth.fly.statistic;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.Action;
import com.wealth.fly.core.strategy.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatisticVolumeStrategyAction implements Action {

    private Map<String, StatisticStrategyAction.StatisticItem> targetKlineMap = new LinkedHashMap<>();

    @Autowired
    private KLineDao kLineDao;


    @Override
    public void doAction(Strategy strategy, KLine kLine, BigDecimal priceMA, BigDecimal firstOpenPrice) {
        StatisticStrategyAction.StatisticItem item = new StatisticStrategyAction.StatisticItem();
        item.setStartPrice(kLine.getClose());
        item.setStartDataTime(kLine.getDataTime());
        item.setAmplitudeFromMAPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), priceMA));
//        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), firstOpenPrice));


        Long nextDataTime = kLine.getDataTime();
        while (true) {
            List<KLine> kLineList = kLineDao.getLastKLineGTDataTime(DataGranularity.ONE_MINUTE.name(), nextDataTime, 10);
            if (kLineList == null || kLineList.isEmpty()) {
                return;
            }

            for (KLine nextKline : kLineList) {
                nextDataTime = nextKline.getDataTime();

                if (lossOrGain(item, nextKline, strategy.isGoingLong())) {
                    targetKlineMap.put(String.valueOf(kLine.getDataTime()), item);
                    return;
                }

            }
        }
    }

    private boolean lossOrGain(StatisticStrategyAction.StatisticItem item, KLine nextKline, boolean goingLong) {
        if (goingLong && nextKline.getClose().compareTo(nextKline.getOpen()) < 0) {
            item.setIsWin(nextKline.getClose().compareTo(item.getStartPrice()) > 0);
            item.setEndDataTime(nextKline.getDataTime());
            item.setEndPrice(nextKline.getClose());
            item.setProfitPercent(MathUtil.distancePercentInDecimal(item.getStartPrice(), nextKline.getClose()));
            return true;
        }
        if (!goingLong && nextKline.getClose().compareTo(nextKline.getOpen()) > 0) {
            item.setIsWin(nextKline.getClose().compareTo(item.getStartPrice()) < 0);
            item.setEndDataTime(nextKline.getDataTime());
            item.setEndPrice(nextKline.getClose());
            item.setProfitPercent(MathUtil.distancePercentInDecimal(item.getStartPrice(), nextKline.getClose()));

            return true;
        }

        return false;
    }

    public Map<String, StatisticStrategyAction.StatisticItem> getTargetKlineMap() {
        return targetKlineMap;
    }
}
