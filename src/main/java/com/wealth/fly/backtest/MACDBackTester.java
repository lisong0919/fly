package com.wealth.fly.backtest;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.MAParam;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyFactory;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.core.strategy.criteria.Sector;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MACDBackTester extends BackTester {

    private Long startTime;
    private Long endTime;

    @Override
    public List<RealTimePrice> generateRealTimePrice(KLine kLine) {
        BigDecimal price = triggerRealTimeIfPossiable(kLine);
        if (price == null) {
            return null;
        }
        RealTimePrice realTimePrice = new RealTimePrice();
        realTimePrice.setPrice(price);
        realTimePrice.setDataTime(kLine.getDataTime());

        return Arrays.asList(new RealTimePrice[]{realTimePrice});
    }

    private BigDecimal triggerRealTimeIfPossiable(KLine kLine) {

        if (strategyHandler.getHoldingStockMap().isEmpty()) {
            return null;
        }
        for (StrategyHandler.HoldingStock holdingStock : strategyHandler.getHoldingStockMap()
                .values()) {

            if (kLine.getDataTime().longValue() == holdingStock.getOpenDataTime().longValue()) {
                continue;
            }

            BigDecimal openStockPrice = holdingStock.getOpenStockPrice();

            if ("ClassicMACDLongOpenStrategy".equals(holdingStock.getOpenStrategy().getId())) {
                BigDecimal missPrice = MathUtil.addPercent(openStockPrice,
                        new BigDecimal(CommonConstants.MACD_MISS_PERCENT).multiply(new BigDecimal(-1)));
                if (kLine.getLow().compareTo(missPrice) < 0) {
                    return missPrice.subtract(new BigDecimal(2));
                }

                BigDecimal winPrice = MathUtil.addPercent(openStockPrice, CommonConstants.MACD_WIN_PERCENT);
                if (kLine.getHigh().compareTo(winPrice) > 0) {
                    return winPrice.add(new BigDecimal(2));
                }
            }
        }


        return null;
    }

    @Override
    public Long getStartTime() {
        return this.startTime;
    }

    @Override
    public Long getEndTime() {
        return this.endTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public DataGranularity getDataGranularity() {
        return CommonConstants.DEFAULT_DATA_GRANULARITY;
    }

    public List<Strategy> getStrategyList() {

        List<Strategy> strategyList = new ArrayList<>();

        Strategy strategy1 = new Strategy();
        strategy1.setId("ClassicMACDLongOpenStrategy");
        strategy1.setCriteria(StrategyFactory.getClassicMacdLongCriteria());
        strategy1.setGoingLong(true);
        strategy1.setOpenStock(true);
        strategy1.setDataGranularity(getDataGranularity());
        strategyList.add(strategy1);

        Strategy strategy2 = new Strategy();
        strategy2.setId("ClassicMACDLongCloseStrategy");
        strategy2.setCriteria(StrategyFactory.getClassicMacdLongCloseCriteria());
        strategy2.setGoingLong(true);
        strategy2.setOpenStock(false);
        strategy2.setCloseStrategyId(strategy1.getId());
        strategy2.setDataGranularity(getDataGranularity());
        strategyList.add(strategy2);


        Strategy strategy3 = new Strategy();
        strategy3.setId("ClassicMACDShortOpenStrategy");
        strategy3.setCriteria(StrategyFactory.getClassicMacdLongCriteria());
        strategy3.setGoingLong(false);
        strategy3.setOpenStock(true);
        strategy3.setDataGranularity(getDataGranularity());
        strategyList.add(strategy3);

        Strategy strategy4 = new Strategy();
        strategy4.setId("ClassicMACDShortCloseStrategy");
        strategy4.setCriteria(StrategyFactory.getClassicMacdLongCloseCriteria());
        strategy4.setGoingLong(false);
        strategy4.setOpenStock(false);
        strategy4.setCloseStrategyId(strategy3.getId());
        strategy4.setDataGranularity(getDataGranularity());
        strategyList.add(strategy4);

        System.out.println(JSONObject.toJSONString(strategyList));

        return strategyList;
    }


}
