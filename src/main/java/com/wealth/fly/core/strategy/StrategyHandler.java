package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.ucpaas.restDemo.client.JsonReqClient;
import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.SmsUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Criteria;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

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

    private BigDecimal priceMA;
    private BigDecimal volumeMA;
    private Map<String, BigDecimal> preLongSectorValues;
    private Map<String, BigDecimal> preShortSectorValues;
    private List<Strategy> strategyList;

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyHandler.class);

    @PostConstruct
    public void init() {
        List<KLine> kLineList = kLineDao.getLastKLineByGranularity(DataGranularity.FIVE_MINUTES.toString(), 30);

        for (int i = 30; i >= 1; i--) {
            priceMA = maHandler.push(MAType.PRICE, kLineList.get(i - 1), 30);
        }

        for (int i = 10; i >= 1; i++) {
            volumeMA = maHandler.push(MAType.VOLUME, kLineList.get(i - 1), 10);
        }

        try {
            String config = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("strategy.json"));
            strategyList = JSONObject.parseArray(config, Strategy.class);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void onNewKLine(KLine kLine) {
        if (!DataGranularity.FIVE_MINUTES.equals(kLine.getGranularity())) {
            return;
        }
        BigDecimal prePriceMA = priceMA;
        //更新ma信息
        volumeMA = maHandler.push(MAType.VOLUME, kLine, 30);
        priceMA = maHandler.push(MAType.PRICE, kLine, 10);
        boolean priceMaIncrease = priceMA.compareTo(prePriceMA) > 0;

        Map<String, BigDecimal> longSectorValues = getLongSectorValues(kLine, priceMaIncrease);
        Map<String, BigDecimal> shortSectorValues = getShortSectorValues(kLine, priceMaIncrease);

        boolean isFirstNewLineEvent = preLongSectorValues == null || preShortSectorValues == null;
        preLongSectorValues = longSectorValues;
        preShortSectorValues = shortSectorValues;

        if (isFirstNewLineEvent) {
            return;
        }

        for (Strategy strategy : strategyList) {
            Map<String, BigDecimal> sectorValues = strategy.isGoingLong() ? longSectorValues : shortSectorValues;
            setLastKLineSectorValues(kLine, sectorValues, priceMaIncrease, strategy.isGoingLong());
            LOGGER.info("sectorValues: " + sectorValues);
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues, strategy.isGoingLong());

            LOGGER.info("match result is " + match);
            if (match) {
                SmsUtil.sendOpenStockSms(String.valueOf(kLine.getClose()));
                LOGGER.info("send sms success");
            }
        }
    }

    private void setLastKLineSectorValues(KLine newKLine, Map<String, BigDecimal> originalSectorValues, boolean priceMaIncrease, boolean isGoingLong) {
        Map<String, BigDecimal> lastSectorValues = isGoingLong ? getLongSectorValues(newKLine, priceMaIncrease) : getShortSectorValues(newKLine, priceMaIncrease);
        for (String key : lastSectorValues.keySet()) {
            originalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + 1 + "_" + key, lastSectorValues.get(key));
        }

        Map<String, BigDecimal> preSectorValues = isGoingLong ? preLongSectorValues : preShortSectorValues;
        for (String key : preSectorValues.keySet()) {
            originalSectorValues.put(CommonConstants.LAST_KLINE_PARAM + "_" + 2 + "_" + key, preSectorValues.get(key));
        }
    }

    private Map<String, BigDecimal> getLongSectorValues(KLine kLine, boolean priceMaIncrease) {
        Map longSectorValues = new HashMap();
        longSectorValues.putAll(getCommonSectorValues(kLine));
        longSectorValues.put(SectorType.KLINE_POSITIVE_PRICE.name(), kLine.getHigh());
        longSectorValues.put(SectorType.KLINE_NEGATIVE_PRICE.name(), kLine.getLow());
        longSectorValues.put(SectorType.KLINE_PRICE_MA_DIRECTION.name(), priceMaIncrease ? Condition.ConditionType.FOLLOW : Condition.ConditionType.AGAINST);

        return longSectorValues;
    }

    private Map<String, BigDecimal> getShortSectorValues(KLine kLine, boolean priceMaIncrease) {
        Map shortSectorValues = new HashMap();
        shortSectorValues.putAll(getCommonSectorValues(kLine));
        shortSectorValues.put(SectorType.KLINE_POSITIVE_PRICE.name(), kLine.getLow());
        shortSectorValues.put(SectorType.KLINE_NEGATIVE_PRICE.name(), kLine.getHigh());
        shortSectorValues.put(SectorType.KLINE_PRICE_MA_DIRECTION.name(), priceMaIncrease ? Condition.ConditionType.AGAINST : Condition.ConditionType.FOLLOW);
        return shortSectorValues;
    }

    private Map<String, BigDecimal> getCommonSectorValues(KLine kLine) {
        Map commonSectorValues = new HashMap();
        commonSectorValues.put(SectorType.KLINE_VOLUME.name(), kLine.getVolume());
        commonSectorValues.put(SectorType.KLINE_PRICE_CLOSE.name(), kLine.getClose());
        commonSectorValues.put(SectorType.KLINE_VOLUME_MA.name(), volumeMA);
        commonSectorValues.put(SectorType.KLINE_PRICE_MA.name(), priceMA);

        return commonSectorValues;
    }

    public static void main(String[] args) {
        KLine lastKline = new KLine();
        lastKline.setGranularity(DataGranularity.FIVE_MINUTES.name());

        KLine kLine = new KLine();
        kLine.setGranularity(DataGranularity.ONE_DAY.name());
        List<KLine> lastKlineList = new ArrayList<>();
        lastKlineList.add(lastKline);
        lastKlineList.add(kLine);
        lastKline = kLine;

        System.out.println(lastKline);
        System.out.println(lastKlineList);

    }

}
