package com.wealth.fly.statistic;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.Action;
import com.wealth.fly.core.strategy.Strategy;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatisticStrategyAction implements Action {

    private Map<String, StatisticItem> targetKlineMap = new LinkedHashMap<>();

    @Autowired
    private KLineDao kLineDao;

    @Override
    public void doAction(Strategy strategy, KLine kLine, BigDecimal priceMA, BigDecimal firstOpenPrice) {
        StatisticItem item = new StatisticItem();
        item.setStartPrice(kLine.getClose());
        item.setStartDataTime(kLine.getDataTime());
        item.setAmplitudeFromMAPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), priceMA));
        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), firstOpenPrice));


        Long nextDataTime = kLine.getDataTime();
        while (true) {
            List<KLine> kLineList = kLineDao.getLastKLineGTDataTime(DataGranularity.FIVE_MINUTES.name(), nextDataTime, 10);
            if (kLineList == null || kLineList.isEmpty()) {
                return;
            }

            for (KLine nextKline : kLineList) {
                if (lossOrGain(item, nextKline, strategy.isGoingLong())) {
                    targetKlineMap.put(String.valueOf(kLine.getDataTime()), item);
                    return;
                }
                nextDataTime = nextKline.getDataTime();
            }
        }


    }


    private boolean lossOrGain(StatisticItem item, KLine nextKline, boolean goingLong) {
        BigDecimal declinePrice = MathUtil.addPercent(item.getStartPrice(), "-0.003");
        BigDecimal increasePrice = MathUtil.addPercent(item.getStartPrice(), "0.003");

        if (goingLong) {
            //是否止损
            boolean loss = nextKline.getLow().compareTo(declinePrice) < 0;
            if (loss) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(declinePrice);
                item.setIsWin(false);
                item.setProfitPercent(new BigDecimal("-0.003"));
                return true;
            }
            //是否止盈
            boolean gain = nextKline.getHigh().compareTo(increasePrice) > 0;
            if (gain) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(increasePrice);
                item.setIsWin(true);
                item.setProfitPercent(new BigDecimal("0.003"));
                return true;
            }
        } else {
            //是否止损
            boolean loss = nextKline.getHigh().compareTo(increasePrice) > 0;
            if (loss) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(increasePrice);
                item.setIsWin(false);
                item.setProfitPercent(new BigDecimal("-0.003"));
                return true;
            }
            //是否止盈
            boolean gain = nextKline.getLow().compareTo(declinePrice) < 0;
            if (gain) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(declinePrice);
                item.setIsWin(true);
                item.setProfitPercent(new BigDecimal("0.003"));
                return true;
            }
        }

        return false;
    }

    public Map<String, StatisticItem> getTargetKlineMap() {
        return targetKlineMap;
    }

    @Data
    public static final class StatisticItem {
        /**
         * 开仓时间
         */
        private Long startDataTime;

        /**
         * 平仓时间
         */
        private Long endDataTime;
        /**
         * 开仓价格
         */
        private BigDecimal startPrice;
        /**
         * 平仓价格
         */
        private BigDecimal endPrice;
        /**
         * 盈利百分比
         */
        private BigDecimal profitPercent;
        /**
         * 距离开仓振幅
         */
        private BigDecimal amplitudeFromOpenPrice;

        /**
         * 距离均线的振幅
         */
        private BigDecimal amplitudeFromMAPrice;

        /**
         * 是否盈利
         */
        private Boolean isWin;
    }
}
