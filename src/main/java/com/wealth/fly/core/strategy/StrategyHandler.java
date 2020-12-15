package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.PostConstruct;

import com.wealth.fly.statistic.SimpleStatisticStrategyAction;
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

    //策略
    private List<Strategy> strategyList;
    private List<StrategyAction> strategyActionList=new ArrayList<>();

    //当前持仓列表
    private Map<String, HoldingStock> holdingStockMap = new HashMap<>();

    //策略执行中需要用到的数据
    private BigDecimal priceMA;
    private BigDecimal volumeMA;
    private BigDecimal macdMA;
    private KLine prevKLine;
    private LinkedList<Map<String, BigDecimal>> lastKlineSectorValuesList = new LinkedList<>();



    public void setStrategyList(List<Strategy> strategyList) {
        this.strategyList = strategyList;
    }

    public void addStrategyAction(StrategyAction strategyAction){
        this.strategyActionList.add(strategyAction);
    }

    @PostConstruct
    public void init() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(CommonConstants.DEFAULT_DATA_GRANULARITY.toString(), CommonConstants.DEFAULT_MA_PRICE_NUM);

        if (kLineList == null || kLineList.isEmpty()) {
            return;
        }

        for (int i = CommonConstants.DEFAULT_MA_PRICE_NUM; i >= 1; i--) {
            priceMA = maHandler.push(MAType.PRICE, kLineList.get(i - 1), CommonConstants.DEFAULT_MA_PRICE_NUM);
        }
        LOGGER.info("init priceMA finished, priceMA-{} is {}", kLineList.get(0).getDataTime(), priceMA);

        for (int i = CommonConstants.DEFAULT_MA_VOLUME_NUM; i >= 1; i--) {
            volumeMA = maHandler.push(MAType.VOLUME, kLineList.get(i - 1), CommonConstants.DEFAULT_MA_VOLUME_NUM);
        }
        LOGGER.info("init volumeMA finished, volumeMA-{} is {}", kLineList.get(0).getDataTime(), volumeMA);

        for (int i = CommonConstants.DEFAULT_MA_MACD_NUM; i >= 1; i--) {
            macdMA = maHandler.push(MAType.MACD, kLineList.get(i - 1), CommonConstants.DEFAULT_MA_MACD_NUM);
        }
        LOGGER.info("init macdMA finished, macdMA-{} is {}", kLineList.get(0).getDataTime(), macdMA);

        dataFetcher.registerKLineListener(this);
    }


    public void onRealTime(long dataTime, BigDecimal realTimePrice) {
        for (Strategy strategy : strategyList) {
            if (strategy.isOpenStock()) {
                continue;
            }

            if (!holdingStockMap.containsKey(strategy.getCloseStrategyId())) {
                LOGGER.info("target strategy {} for strategy {} not exists", strategy.getCloseStrategyId(), strategy.getId());
                continue;
            }

            BigDecimal openStockPrice = holdingStockMap.get(strategy.getCloseStrategyId()).getOpenKline().getClose();
            Map<String, BigDecimal> sectorValues = getRealTimeSectorValues(realTimePrice, openStockPrice);
            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{dataTime, strategy.isGoingLong() ? "long" : "short", JSONObject.toJSONString(sectorValues)});
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);

            if (match) {
                HoldingStock holdingStock = holdingStockMap.remove(strategy.getCloseStrategyId());
                if(strategyActionList!=null && !strategyActionList.isEmpty()){
                    for(StrategyAction action:strategyActionList){
                        action.onCloseStock(holdingStock.getOpenStrategy(), holdingStock.getOpenKline(), strategy, realTimePrice, dataTime);
                    }
                }

            }
        }
    }

    @Override
    public void onNewKLine(KLine kLine) {
        if (!CommonConstants.DEFAULT_DATA_GRANULARITY.name().equals(kLine.getGranularity())) {
            return;
        }

        //更新ma信息
        volumeMA = maHandler.push(MAType.VOLUME, kLine, CommonConstants.DEFAULT_MA_VOLUME_NUM);
        priceMA = maHandler.push(MAType.PRICE, kLine, CommonConstants.DEFAULT_MA_PRICE_NUM);
        macdMA= maHandler.push(MAType.MACD, kLine, CommonConstants.DEFAULT_MA_MACD_NUM);

        if (prevKLine == null) {
            LOGGER.info("pre values not ready.");
            prevKLine = kLine;
            return;
        }

        Map<String, BigDecimal> sectorValues = getCommonSectorValues(kLine, prevKLine);

        synchronized (lastKlineSectorValuesList) {
            if (lastKlineSectorValuesList.size() == CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                lastKlineSectorValuesList.removeFirst();
            }
            lastKlineSectorValuesList.add(sectorValues);
            if (lastKlineSectorValuesList.size() < CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                LOGGER.info("lastKlineSectorValuesList not ready.");
                prevKLine = kLine;
                return;
            }
        }

        setLastKLineSectorValues(sectorValues, kLine);

        for (Strategy strategy : strategyList) {
            if (!strategy.isOpenStock()) {
                continue;
            }
            if (holdingStockMap.containsKey(strategy.getId())) {
                LOGGER.info("exists holding stock for strategy {}, holding time {}", new Object[]{strategy.getId(), holdingStockMap.get(strategy.getId()).getOpenKline().getDataTime()});
                continue;
            }

            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", JSONObject.toJSONString(sectorValues)});
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);

            LOGGER.info("[{}] [{}] match result is {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", match});
            if (match) {
                HoldingStock holdingStock = new HoldingStock();
                holdingStock.setOpenKline(kLine);
                holdingStock.setOpenStrategy(strategy);
                holdingStockMap.put(strategy.getId(), holdingStock);

                //通知已注册action
                if(strategyActionList!=null && !strategyActionList.isEmpty()){
                    for(StrategyAction action:strategyActionList){
                        action.onOpenStock(strategy, kLine);
                    }
                }
            }
        }

        prevKLine = kLine;
    }



    private void setLastKLineSectorValues(Map<String, BigDecimal> orignalSectorValues, KLine kLine) {

        // 最后一个K线不能边遍历边修改，所以要通过kLine创建一个新的sectorValues
        for (int i = 0; i < lastKlineSectorValuesList.size() - 1; i++) {
            Map<String, BigDecimal> lastSectorValues = lastKlineSectorValuesList.get(i);

            for (String key : lastSectorValues.keySet()) {
                if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                    orignalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + (i + 1) + "_" + key, lastSectorValues.get(key));
                }
            }
        }

        Map<String, BigDecimal> lastSectorValues = getCommonSectorValues(kLine, prevKLine);

        for (String key : lastSectorValues.keySet()) {
            if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                orignalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + lastKlineSectorValuesList.size() + "_" + key, lastSectorValues.get(key));
            }
        }
    }


    private Map<String, BigDecimal> getRealTimeSectorValues(BigDecimal realTimePrice, BigDecimal stockOpenPrice) {
        Map sectorValues = new HashMap();
        sectorValues.put(SectorType.REALTIME_PRICE.name(), realTimePrice);
        sectorValues.put(SectorType.KLINE_PRICE_MA.name(), priceMA);
        sectorValues.put(SectorType.STOCK_PRICE_OPEN.name(), stockOpenPrice);

        return sectorValues;
    }

    private Map<String, BigDecimal> getCommonSectorValues(KLine kLine, KLine prevKLine) {
        Map commonSectorValues = new HashMap();
        commonSectorValues.put(SectorType.KLINE_VOLUME.name(), kLine.getVolume());//成交量
        commonSectorValues.put(SectorType.KLINE_PRICE_OPEN.name(), kLine.getOpen());//开盘价
        commonSectorValues.put(SectorType.KLINE_PRICE_CLOSE.name(), kLine.getClose());//收盘价
        commonSectorValues.put(SectorType.KLINE_VOLUME_MA.name(), volumeMA);//成交量MA
        commonSectorValues.put(SectorType.KLINE_PRICE_MA.name(), priceMA);//价格MA
        commonSectorValues.put(SectorType.KLINE_PRICE_HIGH.name(), kLine.getHigh());//最高价
        commonSectorValues.put(SectorType.KLINE_PRICE_LOW.name(), kLine.getLow());//最低价
        commonSectorValues.put(SectorType.KLINE_MAX_PRICE_CHANGE_PERCENT.name(), CommonConstants.MAX_AMPLITUDE);//K线涨跌幅可接受的最大值
        //涨跌幅(开盘与收盘价之间价格浮动百分比)
        commonSectorValues.put(SectorType.KLINE_PRICE_CHANGE_PERCENT.name(), MathUtil.distancePercentInDecimal(kLine.getClose(), kLine.getOpen()));

        for (HoldingStock holdingStock : holdingStockMap.values()) {
            commonSectorValues.put(SectorType.STOCK_PRICE_OPEN.name(), holdingStock.getOpenKline().getClose());
        }

        double diff = MathUtil.caculateDIF(kLine.getEma12().doubleValue(), kLine.getEma26().doubleValue());
        commonSectorValues.put(SectorType.DIF.name(), new BigDecimal(diff));
        commonSectorValues.put(SectorType.DEA.name(), kLine.getDea9());
        commonSectorValues.put(SectorType.MACD.name(), kLine.getMacd());
        commonSectorValues.put(SectorType.PREV_KLINE_CLOSE_PRICE.name(), prevKLine.getClose());
        commonSectorValues.put(SectorType.PREV_KLINE_MACD.name(), prevKLine.getMacd());//上一根K线的MACD

        return commonSectorValues;
    }


    public Map<String, HoldingStock> getHoldingStockMap() {
        return holdingStockMap;
    }

    public BigDecimal getPriceMA() {
        return priceMA;
    }

    @Data
    @ToString
    public static class HoldingStock {
        private Strategy openStrategy;
        private KLine openKline;
    }

}
