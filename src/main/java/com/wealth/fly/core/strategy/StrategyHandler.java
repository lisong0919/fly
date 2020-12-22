package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.data.DataBroker;
import com.wealth.fly.core.listener.KLineListener;
import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.data.manufacturer.*;
import com.wealth.fly.core.data.manufacturer.interf.DataManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrategyHandler implements KLineListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyHandler.class);

    @Autowired
    private KLineDao kLineDao;
    @Autowired
    private MAHandler maHandler;
    @Autowired
    private DataFetcher dataFetcher;
    @Autowired
    private DataBroker dataBroker;
    //策略
    private List<Strategy> strategyList;
    private List<StrategyAction> strategyActionList = new ArrayList<>();

    //当前持仓列表
    private Map<String, HoldingStock> holdingStockMap = new HashMap<>();

    public void setStrategyList(List<Strategy> strategyList) {
        this.strategyList = strategyList;
    }

    public void addStrategyAction(StrategyAction strategyAction) {
        this.strategyActionList.add(strategyAction);
    }

    @PostConstruct
    public void init() {
        dataFetcher.registerKLineListener(this);
    }

    @Override
    public void onNewKLine(KLine kLine) {

        for (Strategy strategy : strategyList) {

            if (strategy.isOpenStock() && holdingStockMap.containsKey(strategy.getId())) {
                LOGGER.info("exists holding stock for strategy {}, holding time {}", new Object[]{strategy.getId(), holdingStockMap.get(strategy.getId()).getOpenKline().getDataTime()});
                continue;
            }

            Map<String, BigDecimal> sectorValues = dataBroker.getKLineDataByStrategy(strategy, kLine.getDataTime(), holdingStockMap);
            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", JSONObject.toJSONString(sectorValues)});
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);

            LOGGER.info("[{}] [{}] match result is {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", match});
            if (match) {
                if (strategy.isOpenStock()) {
                    HoldingStock holdingStock = new HoldingStock();
                    holdingStock.setOpenKline(kLine);
                    holdingStock.setOpenStrategy(strategy);
                    holdingStock.setOpenStockPrice(kLine.getClose());
                    holdingStockMap.put(strategy.getId(), holdingStock);
                    for (StrategyAction action : strategyActionList) {
                        action.onOpenStock(strategy, kLine);
                    }
                } else {
                    HoldingStock holdingStock = holdingStockMap.get(strategy.getId());
                    for (StrategyAction action : strategyActionList) {
                        //TODO 参数太死
                        action.onCloseStock(holdingStock.getOpenStrategy(), holdingStock.getOpenKline(), strategy, kLine.getClose(), kLine.getDataTime());
                    }
                }


            }
        }
    }

    public void onRealTime(long dataTime, BigDecimal realTimePrice) {
//        Map<String,BigDecimal> sectorValues=new HashMap<>();
//        for(DataManufacturer dataManufacturer : realTimeEventDataManufacturerList){
//            dataManufacturer.manufact(new RealTimePrice(dataTime,realTimePrice,),sectorValues);
//        }
//
//        for (Strategy strategy : strategyList) {
//            if (strategy.isOpenStock()) {
//                continue;
//            }
//
//            if (!holdingStockMap.containsKey(strategy.getCloseStrategyId())) {
//                LOGGER.info("target strategy {} for strategy {} not exists", strategy.getCloseStrategyId(), strategy.getId());
//                continue;
//            }
//
//            BigDecimal openStockPrice = holdingStockMap.get(strategy.getCloseStrategyId()).getOpenKline().getClose();
//            Map<String, BigDecimal> sectorValues = getRealTimeSectorValues(realTimePrice, openStockPrice);
//            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{dataTime, strategy.isGoingLong() ? "long" : "short", JSONObject.toJSONString(sectorValues)});
//            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);
//
//            if (match) {
//                HoldingStock holdingStock = holdingStockMap.remove(strategy.getCloseStrategyId());
//                if(strategyActionList!=null && !strategyActionList.isEmpty()){
//                    for(StrategyAction action:strategyActionList){
//                        action.onCloseStock(holdingStock.getOpenStrategy(), holdingStock.getOpenKline(), strategy, realTimePrice, dataTime);
//                    }
//                }
//
//            }
//        }
    }


    public Map<String, HoldingStock> getHoldingStockMap() {
        return holdingStockMap;
    }


    @Data
    @ToString
    public static class HoldingStock {
        private Strategy openStrategy;
        private KLine openKline;
        private BigDecimal openStockPrice;
    }

}
