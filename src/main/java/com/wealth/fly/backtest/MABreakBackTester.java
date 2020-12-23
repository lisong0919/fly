package com.wealth.fly.backtest;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.data.DataBroker;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.MAParam;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyFactory;
import com.wealth.fly.core.strategy.StrategyHandler;
import com.wealth.fly.core.strategy.criteria.Sector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MABreakBackTester extends BackTester {


    private Long startTime;
    private Long endTime;

    @Autowired
    private DataBroker dataBroker;


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

    @Override
    public Long getStartTime() {
        return this.startTime;
    }

    @Override
    public Long getEndTime() {
        return this.endTime;
    }


    @Override
    public DataGranularity getDataGranularity() {
        return CommonConstants.DEFAULT_DATA_GRANULARITY;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    private BigDecimal triggerRealTimeIfPossiable(KLine kLine) {

        if (!strategyHandler.getHoldingStockMap().isEmpty()) {
            for (StrategyHandler.HoldingStock holdingStock : strategyHandler.getHoldingStockMap()
                    .values()) {

                if (kLine.getDataTime().longValue() == holdingStock.getOpenDataTime().longValue()) {
                    continue;
                }

                BigDecimal openStockPrice = holdingStock.getOpenStockPrice();

                Sector sector = new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM);
                BigDecimal priceMA = dataBroker.getMaValue(sector, getDataGranularity(), new MAParam(kLine.getDataTime(), kLine.getClose()));

                if ("ClassicMALongOpenStrategy".equals(holdingStock.getOpenStrategy().getId())) {
                    BigDecimal missPrice = MathUtil.addPercent(priceMA,
                            new BigDecimal(CommonConstants.MISS_PERCENT).multiply(new BigDecimal(-1)));
                    if (kLine.getLow().compareTo(missPrice) < 0) {
                        return missPrice.subtract(new BigDecimal(2));
                    }

//                    BigDecimal missPriceOpen = MathUtil.addPercent(openStockPrice, new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(-1)));
//                    if (kLine.getLow().compareTo(missPriceOpen) < 0) {
//                        return missPriceOpen.subtract(new BigDecimal(2));
//                    }

                    BigDecimal winPrice = MathUtil.addPercent(openStockPrice, CommonConstants.WIN_PERCENT);
                    if (kLine.getHigh().compareTo(winPrice) > 0) {
                        return winPrice.add(new BigDecimal(2));
                    }

                }
                if ("ClassicMAShortOpenStrategy".equals(holdingStock.getOpenStrategy().getId())) {
                    BigDecimal missPrice = MathUtil.addPercent(priceMA, CommonConstants.MISS_PERCENT);
                    if (kLine.getHigh().compareTo(missPrice) > 0) {
                        return missPrice.add(new BigDecimal(2));
                    }

//                    BigDecimal missPriceOpen = MathUtil.addPercent(openStockPrice, CommonConstants.WIN_PERCENT);
//                    if (kLine.getHigh().compareTo(missPriceOpen) > 0) {
//                        return missPriceOpen.add(new BigDecimal(2));
//                    }

                    BigDecimal winPrice = MathUtil.addPercent(openStockPrice,
                            new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(-1)));
                    if (kLine.getLow().compareTo(winPrice) < 0) {
                        return winPrice.subtract(new BigDecimal(2));
                    }
                }
            }
        }

        return null;
    }


    public List<Strategy> getStrategyList() {

        List<Strategy> strategyList = new ArrayList<>();

        Strategy strategy1 = new Strategy();
        strategy1.setId("ClassicMALongOpenStrategy");
        strategy1.setCriteria(StrategyFactory.getClassicMALongCriteria());
        strategy1.setGoingLong(true);
        strategy1.setOpenStock(true);
        strategy1.setDataGranularity(getDataGranularity());
        strategyList.add(strategy1);

        Strategy strategy2 = new Strategy();
        strategy2.setId("ClassicMAShortOpenStrategy");
        strategy2.setCriteria(StrategyFactory.getClassicMAShortCriteria());
        strategy2.setGoingLong(false);
        strategy2.setOpenStock(true);
        strategy2.setDataGranularity(getDataGranularity());
        strategyList.add(strategy2);


        Strategy strategy3 = new Strategy();
        strategy3.setId("ClassicMALongCloseStrategy");
        strategy3.setCriteria(StrategyFactory.getClassicMALongCloseCriteria());
        strategy3.setGoingLong(true);
        strategy3.setOpenStock(false);
        strategy3.setCloseStrategyId(strategy1.getId());
        strategy3.setDataGranularity(getDataGranularity());
        strategyList.add(strategy3);


        Strategy strategy4 = new Strategy();
        strategy4.setId("ClassicMAShortCloseStrategy");
        strategy4.setCriteria(StrategyFactory.getClassicMAShortCloseCriteria());
        strategy4.setGoingLong(false);
        strategy4.setOpenStock(false);
        strategy4.setCloseStrategyId(strategy2.getId());
        strategy4.setDataGranularity(getDataGranularity());
        strategyList.add(strategy4);

        System.out.println(JSONObject.toJSONString(strategyList));

        return strategyList;
    }
}
