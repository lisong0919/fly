package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.ucpaas.restDemo.client.JsonReqClient;
import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.SmsUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.CompoundCriteria;
import com.wealth.fly.core.strategy.criteria.LastNKlineCriteria;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import com.wealth.fly.core.strategy.criteria.SimpleCriteria;
import com.wealth.fly.core.strategy.criteria.condition.Condition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrategyHandler implements KLineListener {

    @Autowired
    private KLineDao kLineDao;
    @Autowired
    private MAHandler maHandler;
    @Autowired
    private DataFetcher dataFetcher;

    private BigDecimal priceMA;
    private BigDecimal volumeMA;
    private Map<String, BigDecimal> preLongSectorValues;
    private Map<String, BigDecimal> preShortSectorValues;
    private List<Strategy> strategyList;

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyHandler.class);

    @PostConstruct
    public void init() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(DataGranularity.FIVE_MINUTES.toString(), 30);

        if (kLineList == null || kLineList.isEmpty()) {
            return;
        }

        for (int i = 30; i >= 1; i--) {
            priceMA = maHandler.push(MAType.PRICE, kLineList.get(i - 1), 30);
        }


        LOGGER.info("init priceMA finished, priceMA-{} is {}", kLineList.get(0).getDataTime(), priceMA);

        for (int i = 10; i >= 1; i--) {
            volumeMA = maHandler.push(MAType.VOLUME, kLineList.get(i - 1), 10);
        }

        LOGGER.info("init priceMA finished, volumeMA-{} is {}", kLineList.get(0).getDataTime(), volumeMA);

//        try {
//            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("strategy.json");
//            String config = IOUtils.toString(inputStream);
//            strategyList = JSONObject.parseArray(config, Strategy.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//            LOGGER.error(e.getMessage(), e);
//        }
        initStrategyList();

        LOGGER.info("init strategy from strategy.json finished, strategy list is {}", strategyList);

        dataFetcher.registerKLineListener(this);
    }

    @Override
    public void onNewKLine(KLine kLine) {
        if (!DataGranularity.FIVE_MINUTES.name().equals(kLine.getGranularity())) {
            return;
        }
        BigDecimal prePriceMA = priceMA;
        //更新ma信息
        volumeMA = maHandler.push(MAType.VOLUME, kLine, 10);
        priceMA = maHandler.push(MAType.PRICE, kLine, 30);

        if (volumeMA == null || priceMA == null) {
            LOGGER.info("ma is not ready.");
            return;
        }

        boolean priceMaIncrease = priceMA.compareTo(prePriceMA) > 0;

        Map<String, BigDecimal> longSectorValues = getLongSectorValues(kLine, priceMaIncrease);
        Map<String, BigDecimal> shortSectorValues = getShortSectorValues(kLine, priceMaIncrease);

        boolean isFirstNewLineEvent = preLongSectorValues == null || preShortSectorValues == null;


        if (isFirstNewLineEvent) {
            preLongSectorValues = longSectorValues;
            preShortSectorValues = shortSectorValues;
            return;
        }

        for (Strategy strategy : strategyList) {
            Map<String, BigDecimal> sectorValues = strategy.isGoingLong() ? longSectorValues : shortSectorValues;
            setLastKLineSectorValues(kLine, sectorValues, priceMaIncrease, strategy.isGoingLong());
            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", sectorValues});
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues, strategy.isGoingLong());

            LOGGER.info("[{}] [{}] match result is {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", match});
            if (match) {
                //目前的短信参数不能有特殊符号
                String priceStr= kLine.getClose().toPlainString();
                if(priceStr.contains(".")){
                    priceStr=priceStr.substring(0,priceStr.indexOf("."));
                }
                SmsUtil.sendOpenStockSms(priceStr);
                LOGGER.info("send sms success");
            }
        }

        preLongSectorValues = longSectorValues;
        preShortSectorValues = shortSectorValues;
    }

    private void setLastKLineSectorValues(KLine newKLine, Map<String, BigDecimal> originalSectorValues, boolean priceMaIncrease, boolean isGoingLong) {
        Map<String, BigDecimal> lastSectorValues = isGoingLong ? getLongSectorValues(newKLine, priceMaIncrease) : getShortSectorValues(newKLine, priceMaIncrease);
        for (String key : lastSectorValues.keySet()) {
            if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                originalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + 1 + "_" + key, lastSectorValues.get(key));
            }
        }

        Map<String, BigDecimal> preSectorValues = isGoingLong ? preLongSectorValues : preShortSectorValues;
        for (String key : preSectorValues.keySet()) {
            if (!key.startsWith(CommonConstants.LAST_KLINE_PARAM)) {
                originalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + 2 + "_" + key, preSectorValues.get(key));
            }
        }
    }

    private Map<String, BigDecimal> getLongSectorValues(KLine kLine, boolean priceMaIncrease) {
        Map longSectorValues = new HashMap();
        longSectorValues.putAll(getCommonSectorValues(kLine, priceMaIncrease));
        longSectorValues.put(SectorType.KLINE_POSITIVE_PRICE.name(), kLine.getHigh());
        longSectorValues.put(SectorType.KLINE_NEGATIVE_PRICE.name(), kLine.getLow());

        return longSectorValues;
    }

    private Map<String, BigDecimal> getShortSectorValues(KLine kLine, boolean priceMaIncrease) {
        Map shortSectorValues = new HashMap();
        shortSectorValues.putAll(getCommonSectorValues(kLine, priceMaIncrease));
        shortSectorValues.put(SectorType.KLINE_POSITIVE_PRICE.name(), kLine.getLow());
        shortSectorValues.put(SectorType.KLINE_NEGATIVE_PRICE.name(), kLine.getHigh());

        return shortSectorValues;
    }

    private Map<String, BigDecimal> getCommonSectorValues(KLine kLine, boolean priceMaIncrease) {
        Map commonSectorValues = new HashMap();
        commonSectorValues.put(SectorType.KLINE_VOLUME.name(), new BigDecimal(kLine.getVolume()));
        commonSectorValues.put(SectorType.KLINE_PRICE_CLOSE.name(), kLine.getClose());
        commonSectorValues.put(SectorType.KLINE_VOLUME_MA.name(), volumeMA);
        commonSectorValues.put(SectorType.KLINE_PRICE_MA.name(), priceMA);
        commonSectorValues.put(SectorType.KLINE_PRICE_MA_DIRECTION_BEGIN.name(), priceMaIncrease ? new BigDecimal(1) : new BigDecimal(2));
        commonSectorValues.put(SectorType.KLINE_PRICE_MA_DIRECTION_END.name(), priceMaIncrease ? new BigDecimal(2) : new BigDecimal(1));

        return commonSectorValues;
    }

    public void initStrategyList() {
        //条件1：最后两个K线的成交量，任意一个大于成交量MA10的两倍
        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
        simpleCriteria1.setSource(new Sector(Sector.SectorType.KLINE_VOLUME));
        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, "100"));
        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_VOLUME_MA, 10));
        LastNKlineCriteria criteria1 = new LastNKlineCriteria(2, simpleCriteria1, LastNKlineCriteria.MatchType.ONE_MATCH);
        criteria1.setDescription("条件1: 两个K线，任意一个成交量大于成交量MA10的两倍");


        //条件2：2个K线中任意一个突破MA30
        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
        simpleCriteria2.setSource(new Sector(Sector.SectorType.KLINE_NEGATIVE_PRICE));
        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.BEHIND, Condition.ConditionValueType.ANY, null));
        simpleCriteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        simpleCriteria2.setDescription("负面价格落后于MA30");

        SimpleCriteria simpleCriteria3 = new SimpleCriteria();
        simpleCriteria3.setSource(new Sector(Sector.SectorType.KLINE_POSITIVE_PRICE));
        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.BEYOND, Condition.ConditionValueType.ANY, null));
        simpleCriteria3.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        simpleCriteria3.setDescription("正面价格超越MA30");
        LastNKlineCriteria criteria2 = new LastNKlineCriteria(2, new CompoundCriteria(CompoundCriteria.Operator.AND, simpleCriteria2, simpleCriteria3), LastNKlineCriteria.MatchType.ONE_MATCH);
        criteria2.setDescription("条件2:两个K线中任意一个穿过MA30");

        //条件3: 两个K线中，任意一个站上价格MA30
        SimpleCriteria simpleCriteria4 = new SimpleCriteria();
        simpleCriteria4.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria4.setCondition(new Condition(Condition.ConditionType.BEYOND, Condition.ConditionValueType.ANY, null));
        simpleCriteria4.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        LastNKlineCriteria criteria3 = new LastNKlineCriteria(2, simpleCriteria4, LastNKlineCriteria.MatchType.ONE_MATCH);
        criteria3.setDescription("条件3: 两个K线中，任意一个站上价格MA30");

        //条件4：均线方向顺势而行
        SimpleCriteria criteria4 = new SimpleCriteria();
        criteria4.setSource(new Sector(Sector.SectorType.KLINE_PRICE_MA_DIRECTION_BEGIN, 30));
        criteria4.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA_DIRECTION_END, 30));
        criteria4.setCondition(new Condition(Condition.ConditionType.FOLLOW, Condition.ConditionValueType.ANY, null));

        //条件5：两个K线涨幅不超过1%
//        SimpleCriteria criteria5=new SimpleCriteria();
        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(criteria1);
        finalCriteria.add(criteria2);
        finalCriteria.add(criteria3);
        finalCriteria.add(criteria4);

        Strategy strategy1 = new Strategy();
        strategy1.setCriteria(finalCriteria);
        strategy1.setGoingLong(true);


        Strategy strategy2 = new Strategy();
        strategy2.setCriteria(finalCriteria);
        strategy2.setGoingLong(false);

        strategyList = new ArrayList<>();
        strategyList.add(strategy1);
        strategyList.add(strategy2);
    }

}
