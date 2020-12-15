package com.wealth.fly.core.strategy;

import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.strategy.criteria.*;
import com.wealth.fly.core.strategy.criteria.LastNKlineCriteria.MatchType;
import com.wealth.fly.core.strategy.criteria.condition.Condition;

import java.math.BigDecimal;

public class StrategyFactory {

//    public static Criteria getClassicMACDLongOpenCriteria(){
//        //条件1：最后两个K线的，任意一个大于成交量MA10的两倍
//        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
//        simpleCriteria1.setSource(new Sector(Sector.SectorType.MACD));
//        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, "100"));
//        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_VOLUME_MA, 10));
//
//        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
//        simpleCriteria2.setSource(new Sector(Sector.SectorType.KLINE_PRICE_OPEN));
//        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
//        simpleCriteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
//
//        CompoundCriteria cc1 = new CompoundCriteria(CompoundCriteria.Operator.AND);
//        cc1.add(simpleCriteria1);
//        cc1.add(simpleCriteria2);
//
//        LastNKlineCriteria lc1 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, cc1, LastNKlineCriteria.MatchType.ONE_MATCH);
//        lc1.setDescription("条件1：放量上涨");
//
//    }

    public static Criteria getClassicMALongCloseCriteria() {
        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
        simpleCriteria1.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        String missPercent = new BigDecimal(CommonConstants.MISS_PERCENT).multiply(new BigDecimal(100)).toPlainString();
        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.PERCENT, missPercent));
        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
        simpleCriteria2.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        String winPercent = new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(100)).toPlainString();
        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, winPercent));
        simpleCriteria2.setTarget(new Sector(Sector.SectorType.STOCK_PRICE_OPEN));


//        SimpleCriteria simpleCriteria3 = new SimpleCriteria();
//        simpleCriteria3.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
//        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.PERCENT, winPercent));
//        simpleCriteria3.setTarget(new Sector(Sector.SectorType.STOCK_PRICE_OPEN));

        CompoundCriteria compoundCriteria = new CompoundCriteria(CompoundCriteria.Operator.OR);
        compoundCriteria.add(simpleCriteria1);
        compoundCriteria.add(simpleCriteria2);
//        compoundCriteria.add(simpleCriteria3);

        return compoundCriteria;
    }


    public static Criteria getClassicMAShortCloseCriteria() {
        SimpleCriteria simpleCriteria1 = new SimpleCriteria();
        simpleCriteria1.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        String missPercent = new BigDecimal(CommonConstants.MISS_PERCENT).multiply(new BigDecimal(100)).toPlainString();
        simpleCriteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, missPercent));
        simpleCriteria1.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria2 = new SimpleCriteria();
        simpleCriteria2.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        String winPercent = new BigDecimal(CommonConstants.WIN_PERCENT).multiply(new BigDecimal(100)).toPlainString();
        simpleCriteria2.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.PERCENT, winPercent));
        simpleCriteria2.setTarget(new Sector(Sector.SectorType.STOCK_PRICE_OPEN));


//        SimpleCriteria simpleCriteria3 = new SimpleCriteria();
//        simpleCriteria3.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
//        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, winPercent));
//        simpleCriteria3.setTarget(new Sector(Sector.SectorType.STOCK_PRICE_OPEN));

        CompoundCriteria compoundCriteria = new CompoundCriteria(CompoundCriteria.Operator.OR);
        compoundCriteria.add(simpleCriteria1);
        compoundCriteria.add(simpleCriteria2);
//        compoundCriteria.add(simpleCriteria3);

        return compoundCriteria;
    }


    public static Criteria getClassicMALongCriteria() {
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
        simpleCriteria3.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria3.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria4 = new SimpleCriteria();
        simpleCriteria4.setSource(new Sector(Sector.SectorType.KLINE_PRICE_OPEN));
        simpleCriteria4.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria4.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


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

        //条件4：涨跌幅限制
        SimpleCriteria simpleCriteria6 = new SimpleCriteria();
        simpleCriteria6.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CHANGE_PERCENT));
        simpleCriteria6.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria6.setTarget(new Sector(Sector.SectorType.KLINE_MAX_PRICE_CHANGE_PERCENT));
        LastNKlineCriteria lc4 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, simpleCriteria6, LastNKlineCriteria.MatchType.ALL_MATCH);


        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lc1);
        finalCriteria.add(lc2);
        finalCriteria.add(lc3);
        finalCriteria.add(lc4);

        return finalCriteria;
    }


    public static Criteria getClassicMAShortCriteria() {
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
        simpleCriteria3.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria3.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria3.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


        SimpleCriteria simpleCriteria4 = new SimpleCriteria();
        simpleCriteria4.setSource(new Sector(Sector.SectorType.KLINE_PRICE_OPEN));
        simpleCriteria4.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria4.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, CommonConstants.DEFAULT_MA_PRICE_NUM));


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



        //条件4：涨跌幅限制
        SimpleCriteria simpleCriteria6 = new SimpleCriteria();
        simpleCriteria6.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CHANGE_PERCENT));
        simpleCriteria6.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.ANY, null));
        simpleCriteria6.setTarget(new Sector(Sector.SectorType.KLINE_MAX_PRICE_CHANGE_PERCENT));
        LastNKlineCriteria lc4 = new LastNKlineCriteria(CommonConstants.DEFAULT_LAST_LINE_SIZE, simpleCriteria6, LastNKlineCriteria.MatchType.ALL_MATCH);

        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lc1);
        finalCriteria.add(lc2);
        finalCriteria.add(lc3);
        finalCriteria.add(lc4);

        return finalCriteria;
    }
}
