package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.data.DataBroker;
import com.wealth.fly.core.entity.CloseStock;
import com.wealth.fly.core.entity.OpenStock;
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

import com.wealth.fly.exception.DataInsufficientException;
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
            try {
                Map<String, BigDecimal> sectorValues = dataBroker.getKLineDataByStrategy(strategy, kLine.getDataTime(), holdingStockMap);
                strategyCheck(kLine.getDataTime(), kLine.getClose(), strategy, sectorValues);
            } catch (DataInsufficientException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private void strategyCheck(long datatime, BigDecimal price, Strategy strategy, Map<String, BigDecimal> sectorValues) {
        if (strategy.isOpenStock() && holdingStockMap.containsKey(strategy.getId())) {
            LOGGER.info("exists holding stock for strategy {}, holding time {}", new Object[]{strategy.getId(), holdingStockMap.get(strategy.getId()).getOpenDataTime()});
            return;
        }

        LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{datatime, strategy.getId(), JSONObject.toJSONString(sectorValues)});
        boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);

        LOGGER.info("[{}] [{}] match result is {}", new Object[]{datatime, strategy.getId(), match});
        if (match) {
            if (strategy.isOpenStock()) {
                HoldingStock holdingStock = new HoldingStock();
                holdingStock.setOpenDataTime(datatime);
                holdingStock.setOpenStrategy(strategy);
                holdingStock.setOpenStockPrice(price);
                holdingStockMap.put(strategy.getId(), holdingStock);
                for (StrategyAction action : strategyActionList) {
                    action.onOpenStock(strategy, new OpenStock(price, datatime));
                }
            } else {
                HoldingStock holdingStock = holdingStockMap.get(strategy.getCloseStrategyId());
                if(holdingStock==null){
                    return;
                }
                for (StrategyAction action : strategyActionList) {
                    CloseStock closeStock = new CloseStock();
                    closeStock.setOpenDataTime(holdingStock.getOpenDataTime());
                    closeStock.setOpenPirce(holdingStock.getOpenStockPrice());
                    closeStock.setCloseDataTime(datatime);
                    closeStock.setClosePrice(price);
                    action.onCloseStock(holdingStock.getOpenStrategy(), strategy, closeStock);
                }
                holdingStockMap.remove(strategy.getCloseStrategyId());
            }
        }
    }

    public void onRealTime(RealTimePrice realTimePrice) {
        for (Strategy strategy : strategyList) {
            try {
                Map<String, BigDecimal> sectorValues = dataBroker.getRealTimeDataByStrategy(strategy, realTimePrice, holdingStockMap);
                strategyCheck(realTimePrice.getDataTime(), realTimePrice.getPrice(), strategy, sectorValues);
            } catch (DataInsufficientException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    public Map<String, HoldingStock> getHoldingStockMap() {
        return holdingStockMap;
    }


    @Data
    @ToString
    public static class HoldingStock {
        private Strategy openStrategy;
        private Long openDataTime;
        private BigDecimal openStockPrice;
    }

}
