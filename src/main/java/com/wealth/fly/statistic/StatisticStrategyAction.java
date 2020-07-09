package com.wealth.fly.statistic;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
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
    public void doAction(Strategy strategy, KLine kLine, KLine closeKline, BigDecimal priceMA) {
        StatisticItem item = new StatisticItem();
        item.setStartPrice(kLine.getClose());
        item.setStartDataTime(kLine.getDataTime());
        item.setAmplitudeFromMAPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), priceMA));
        item.setGoingLong(strategy.isGoingLong());
//        item.setAmplitudeFromOpenPrice(MathUtil.distancePercentInDecimal(kLine.getClose(), firstOpenPrice));


        Long nextDataTime = kLine.getDataTime();
        while (true) {
            List<KLine> kLineList = kLineDao.getLastKLineGTDataTime(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), nextDataTime, 10);
            if (kLineList == null || kLineList.isEmpty()) {
                return;
            }

            for (KLine nextKline : kLineList) {
                nextDataTime = nextKline.getDataTime();

                if (lossOrGain(item, nextKline, strategy.isGoingLong())) {
//                    if (isFloatOpenSuccess(item, strategy)) {
                        targetKlineMap.put(String.valueOf(kLine.getDataTime()), item);
//                    }
                    return;
                }
            }
        }
    }

    private boolean isFloatOpenSuccess(StatisticItem item, Strategy strategy) {
        List<KLine> kLineList = kLineDao.getLastKLineByDataTimeRange(CommonConstants.DEFAULT_DATA_GRANULARITY.name(), item.getStartDataTime(), item.getEndDataTime());
        for (KLine kLine : kLineList) {
            if (strategy.isGoingLong()) {
                //期望回调后的价格
                BigDecimal floatPrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.FLOAT_PERCENT.multiply(new BigDecimal(-1)));
                if (kLine.getLow().compareTo(floatPrice) < 0) {
                    item.setStartPrice(floatPrice);
                    item.setStartDataTime(kLine.getDataTime());
                    return true;
                }
            } else {
                BigDecimal floatPrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.FLOAT_PERCENT);
                if (kLine.getHigh().compareTo(floatPrice) > 0) {
                    item.setStartPrice(floatPrice);
                    item.setStartDataTime(kLine.getDataTime());
                    return true;
                }
            }
        }
        return false;
    }


    private boolean lossOrGain(StatisticItem item, KLine nextKline, boolean goingLong) {


        if (goingLong) {

//            BigDecimal declinePrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.PROFIT_PERCENT.add(CommonConstants.FLOAT_PERCENT).multiply(new BigDecimal("-1")));
//            BigDecimal increasePrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.PROFIT_PERCENT.subtract(CommonConstants.FLOAT_PERCENT));


            BigDecimal declinePrice = MathUtil.addPercent(item.getStartPrice(), "-0.04");
            BigDecimal increasePrice = MathUtil.addPercent(item.getStartPrice(), "0.04");

            //是否止损
            boolean loss = nextKline.getLow().compareTo(declinePrice) < 0;
            if (loss) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(declinePrice);
                item.setIsWin(false);
                item.setProfitPercent(new BigDecimal("-0.04"));
                return true;
            }
            //是否止盈
            boolean gain = nextKline.getHigh().compareTo(increasePrice) > 0;
            if (gain) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(increasePrice);
                item.setIsWin(true);
                item.setProfitPercent(new BigDecimal("0.03"));
                return true;
            }
        } else {
//            BigDecimal declinePrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.PROFIT_PERCENT.subtract(CommonConstants.FLOAT_PERCENT).multiply(new BigDecimal("-1")));
//            BigDecimal increasePrice = MathUtil.addPercent(item.getStartPrice(), CommonConstants.PROFIT_PERCENT.add(CommonConstants.FLOAT_PERCENT));

            BigDecimal declinePrice = MathUtil.addPercent(item.getStartPrice(), "-0.04");
            BigDecimal increasePrice = MathUtil.addPercent(item.getStartPrice(), "0.04");

            //是否止损
            boolean loss = nextKline.getHigh().compareTo(increasePrice) > 0;
            if (loss) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(increasePrice);
                item.setIsWin(false);
                item.setProfitPercent(new BigDecimal("-0.04"));
                return true;
            }
            //是否止盈
            boolean gain = nextKline.getLow().compareTo(declinePrice) < 0;
            if (gain) {
                item.setEndDataTime(nextKline.getDataTime());
                item.setEndPrice(declinePrice);
                item.setIsWin(true);
                item.setProfitPercent(new BigDecimal("0.03"));
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

        private Boolean goingLong;
    }
}
