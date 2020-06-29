package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.DataFetcher;
import com.wealth.fly.core.KLineListener;
import com.wealth.fly.core.MAHandler;
import com.wealth.fly.core.SmsUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.MAType;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.*;
import com.wealth.fly.core.strategy.criteria.Sector.SectorType;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.PostConstruct;

import com.wealth.fly.core.strategy.criteria.condition.Condition;
import com.wealth.fly.statistic.StatisticStrategyAction;
import com.wealth.fly.statistic.StatisticVolumeStrategyAction;
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

//    @Autowired
//    private StatisticVolumeStrategyAction strategyAction;

    @Autowired
    private StatisticStrategyAction strategyAction;


    private BigDecimal priceMA;
    private BigDecimal volumeMA;
    private BigDecimal prevMACD;
    private KLine prevKLine;
    private LinkedList<Map<String, BigDecimal>> lastKlineSectorValuesList = new LinkedList<>();

    private List<Strategy> strategyList;

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyHandler.class);

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
        if (!CommonConstants.DEFAULT_DATA_GRANULARITY.name().equals(kLine.getGranularity())) {
            return;
        }

        //更新ma信息
        volumeMA = maHandler.push(MAType.VOLUME, kLine, 10);
        priceMA = maHandler.push(MAType.PRICE, kLine, CommonConstants.DEFAULT_MA_PRICE_NUM);

        if (volumeMA == null || priceMA == null) {
            setSomePrevValues(kLine);
            LOGGER.info("ma is not ready.");
            return;
        }

        if (prevMACD == null || prevKLine == null) {
            LOGGER.info("pre values not ready.");
            setSomePrevValues(kLine);
            return;
        }


        Map<String, BigDecimal> sectorValues = getCommonSectorValues(kLine, prevKLine);

        synchronized (lastKlineSectorValuesList) {
            if (lastKlineSectorValuesList.size() == CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                lastKlineSectorValuesList.removeFirst();
            }
            lastKlineSectorValuesList.add(sectorValues);
            if (lastKlineSectorValuesList.size() < CommonConstants.DEFAULT_LAST_LINE_SIZE) {
                setSomePrevValues(kLine);
                LOGGER.info("lastKlineSectorValuesList not ready.");
                return;
            }
        }

        setLastKLineSectorValues(sectorValues, kLine);

        for (Strategy strategy : strategyList) {

            LOGGER.info("[{}] [{}] sectorValues: {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", JSONObject.toJSONString(sectorValues)});
            boolean match = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues);

            LOGGER.info("[{}] [{}] match result is {}", new Object[]{kLine.getDataTime(), strategy.isGoingLong() ? "long" : "short", match});
            if (match) {
                strategy.getAction().doAction(strategy, kLine, priceMA, null);
                //目前的短信参数不能有特殊符号
                String priceStr= kLine.getClose().toPlainString();
                if(priceStr.contains(".")){
                    priceStr=priceStr.substring(0,priceStr.indexOf("."));
                }
//                SmsUtil.sendOpenStockSms(priceStr);
                LOGGER.info("send sms success");
            }
        }

        setSomePrevValues(kLine);
    }

    /**
     * 本方法在一个K线处理结束的时候，将本次K线数据设置为prev值
     *
     * @param kLine
     */
    private void setSomePrevValues(KLine kLine) {
        if (prevKLine != null) {
            double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
            double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
            double diff = MathUtil.caculateDIF(ema12, ema26);
            double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);

            prevMACD = new BigDecimal(MathUtil.caculateMACD(diff, dea9));
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


    private Map<String, BigDecimal> getCommonSectorValues(KLine kLine, KLine prevKLine) {
        Map commonSectorValues = new HashMap();
        commonSectorValues.put(SectorType.KLINE_VOLUME.name(), new BigDecimal(kLine.getVolume()));
        commonSectorValues.put(SectorType.KLINE_PRICE_OPEN.name(), kLine.getOpen());
        commonSectorValues.put(SectorType.KLINE_PRICE_CLOSE.name(), kLine.getClose());
        commonSectorValues.put(SectorType.KLINE_VOLUME_MA.name(), volumeMA);
        commonSectorValues.put(SectorType.KLINE_PRICE_MA.name(), priceMA);

        double ema12 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 12, prevKLine.getEma12().doubleValue());
        double ema26 = MathUtil.calculateEMA(kLine.getClose().doubleValue(), 26, prevKLine.getEma26().doubleValue());
        double diff = MathUtil.caculateDIF(ema12, ema26);
        double dea9 = MathUtil.caculateDEA(prevKLine.getDea9().doubleValue(), diff);

        commonSectorValues.put(SectorType.DIF.name(), new BigDecimal(diff));
        commonSectorValues.put(SectorType.DEA.name(), new BigDecimal(dea9));
        commonSectorValues.put(SectorType.MACD.name(), new BigDecimal(MathUtil.caculateMACD(diff, dea9)));
        commonSectorValues.put(SectorType.PREV_KLINE_CLOSE_PRICE.name(), prevKLine.getClose());
        commonSectorValues.put(SectorType.PREV_KLINE_MACD.name(), prevMACD);

        return commonSectorValues;
    }

    public void initStrategyList() {

        Strategy strategy1 = new Strategy();
        strategy1.setCriteria(getClassicMALongCriteria());
        strategy1.setGoingLong(true);
        strategy1.setAction(strategyAction);

        Strategy strategy2 = new Strategy();
        strategy2.setCriteria(getClassicMAShortCriteria());
        strategy2.setGoingLong(false);
        strategy2.setAction(strategyAction);

        strategyList = new ArrayList<>();
        strategyList.add(strategy1);
        strategyList.add(strategy2);

        System.out.println(JSONObject.toJSONString(strategyList));
    }

    public static void main(String[] args) {
        new StrategyHandler().initStrategyList();

    }

    private Criteria getClassicMALongCriteria() {
        //条件1：最后两个K线的成交量，任意一个大于成交量MA10的两倍
        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
        simpleCriteria1.setSource(new Sector(Sector.SectorType.KLINE_VOLUME));
        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, "100"));
        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_VOLUME_MA, 10));

        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
        simpleCriteria2.setSource(new Sector(Sector.SectorType.KLINE_PRICE_OPEN));
        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));

        CompoundCriteria cc1 = new CompoundCriteria(CompoundCriteria.Operator.AND);
        cc1.add(simpleCriteria1);
        cc1.add(simpleCriteria2);

        LastNKlineCriteria lc1 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, cc1, LastNKlineCriteria.MatchType.ONE_MATCH);
        lc1.setDescription("条件1：放量上涨");


        //条件2：突破MA30
        SimpleCriteria simpleCriteria3 = new SimpleCriteria();
        simpleCriteria3.setSource(new Sector(SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria3.setTarget(new Sector(SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria4 = new SimpleCriteria();
        simpleCriteria4.setSource(new Sector(SectorType.KLINE_PRICE_OPEN));
        simpleCriteria4.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria4.setTarget(new Sector(SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        CompoundCriteria cc2 = new CompoundCriteria(CompoundCriteria.Operator.AND);
        cc2.add(simpleCriteria3);
        cc2.add(simpleCriteria4);

        LastNKlineCriteria lc2 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, cc2, LastNKlineCriteria.MatchType.FIRST_MATCH);
        lc2.setDescription("条件2：突破均线");


        //条件3: 站稳均线
        SimpleCriteria simpleCriteria5 = new SimpleCriteria();
        simpleCriteria5.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria5.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria5.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));
        LastNKlineCriteria lc3 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, simpleCriteria5, LastNKlineCriteria.MatchType.ALL_MATCH);
        lc3.setDescription("条件3: 站稳均线");

        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lc1);
        finalCriteria.add(lc2);
        finalCriteria.add(lc3);

        return finalCriteria;
    }


    private Criteria getClassicMAShortCriteria() {
        //条件1：最后两个K线的成交量，任意一个大于成交量MA10的两倍
        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
        simpleCriteria1.setSource(new Sector(Sector.SectorType.KLINE_VOLUME));
        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, "100"));
        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_VOLUME_MA, 10));

        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
        simpleCriteria2.setSource(new Sector(Sector.SectorType.KLINE_PRICE_OPEN));
        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));

        CompoundCriteria cc1 = new CompoundCriteria(CompoundCriteria.Operator.AND);
        cc1.add(simpleCriteria1);
        cc1.add(simpleCriteria2);

        LastNKlineCriteria lc1 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, cc1, LastNKlineCriteria.MatchType.ONE_MATCH);
        lc1.setDescription("条件1：放量下跌");


        //条件2：2个K线中任意一个突破MA30
        SimpleCriteria simpleCriteria3 = new SimpleCriteria();
        simpleCriteria3.setSource(new Sector(SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria3.setTarget(new Sector(SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria4 = new SimpleCriteria();
        simpleCriteria4.setSource(new Sector(SectorType.KLINE_PRICE_OPEN));
        simpleCriteria4.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria4.setTarget(new Sector(SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        CompoundCriteria cc2 = new CompoundCriteria(CompoundCriteria.Operator.AND);
        cc2.add(simpleCriteria3);
        cc2.add(simpleCriteria4);

        LastNKlineCriteria lc2 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, cc2, LastNKlineCriteria.MatchType.FIRST_MATCH);
        lc2.setDescription("条件2:突破均线");



        SimpleCriteria simpleCriteria5 = new SimpleCriteria();
        simpleCriteria5.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria5.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria5.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));
        LastNKlineCriteria lc3 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, simpleCriteria5, LastNKlineCriteria.MatchType.ALL_MATCH);
        lc3.setDescription("条件3: 站稳均线");

        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lc1);
        finalCriteria.add(lc2);
        finalCriteria.add(lc3);

        return finalCriteria;
    }


}
