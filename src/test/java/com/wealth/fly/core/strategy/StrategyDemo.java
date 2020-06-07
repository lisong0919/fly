package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.strategy.criteria.condition.Condition;
import com.wealth.fly.core.strategy.criteria.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyDemo {

    @Test
    public void openShortStockTest(){



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
        strategy1.setGoingLong(false);


        Map<String, BigDecimal> sectorValues = new HashMap<>();

        sectorValues.put("KLINE_VOLUME", new BigDecimal("14647"));
        sectorValues.put("KLINE_PRICE_CLOSE", new BigDecimal("9659.9"));
        sectorValues.put("KLINE_VOLUME_MA", new BigDecimal("24635.20"));
        sectorValues.put("KLINE_PRICE_MA", new BigDecimal("9684.75"));
        sectorValues.put("KLINE_NEGATIVE_PRICE", new BigDecimal("9670"));
        sectorValues.put("KLINE_POSITIVE_PRICE", new BigDecimal("9655"));
        sectorValues.put("KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal("1"));
        sectorValues.put("KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));


        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA", new BigDecimal("9684.75"));

        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_CLOSE", new BigDecimal("9659.9"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_POSITIVE_PRICE", new BigDecimal("9655"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal("1"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_NEGATIVE_PRICE", new BigDecimal("9670"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_VOLUME", new BigDecimal("14647"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_VOLUME_MA", new BigDecimal("24635.20"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));


        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA", new BigDecimal("9685.20"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_VOLUME", new BigDecimal("58529"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_NEGATIVE_PRICE", new BigDecimal("9691.6"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_CLOSE", new BigDecimal("9662.5"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_POSITIVE_PRICE", new BigDecimal("9630"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal("1"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_VOLUME_MA", new BigDecimal("24141.30"));


        boolean res = strategy1.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy1.getCriteria(), sectorValues, false);

//        boolean res = criteria1.getCriteriaType().getCriteriaHandler().match(criteria1, sectorValues, false);
//        boolean res = criteria2.getCriteriaType().getCriteriaHandler().match(criteria2, sectorValues, false);
//        boolean res = criteria3.getCriteriaType().getCriteriaHandler().match(criteria3, sectorValues, false);
//        boolean res = criteria4.getCriteriaType().getCriteriaHandler().match(criteria4, sectorValues, false);

        System.out.println(">>>>>>>>>>" + res);
    }


    @Test
    public void openLongStockTest() {

        Strategy strategy = getStrategy(true);

        Map<String, BigDecimal> sectorValues = new HashMap<>();
        sectorValues.put("KLINE_VOLUME_MA", new BigDecimal("8421.70"));
        sectorValues.put("KLINE_PRICE_MA", new BigDecimal("9602.77"));
        sectorValues.put("KLINE_VOLUME", new BigDecimal(5837));
        sectorValues.put("KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal("1"));//均线拐头向上
        sectorValues.put("KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));//均线拐头向上
        sectorValues.put("KLINE_PRICE_CLOSE", new BigDecimal("9600.6"));
        sectorValues.put("KLINE_NEGATIVE_PRICE", new BigDecimal("9595.5"));
        sectorValues.put("KLINE_POSITIVE_PRICE", new BigDecimal("9602"));

        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_VOLUME_MA", new BigDecimal("9768.70"));//成交量
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_VOLUME", new BigDecimal("19537.5"));//成交量是MA的2倍
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA", new BigDecimal("9602.54"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_CLOSE", new BigDecimal("9602.55"));//站上MA30


        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_NEGATIVE_PRICE", new BigDecimal("9581.2"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_POSITIVE_PRICE", new BigDecimal("9602.54"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));
        sectorValues.put("LAST_KLINE_PARAM_2_KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal("1"));


        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA", new BigDecimal("9602.77"));//均线
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_NEGATIVE_PRICE", new BigDecimal("9602.76"));//突破均线--下方
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_POSITIVE_PRICE", new BigDecimal("9602.78"));//突破均线--上方
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_VOLUME", new BigDecimal("5837"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_CLOSE", new BigDecimal("9600.6"));

        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_VOLUME_MA", new BigDecimal("8421.70"));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA_DIRECTION_BEGIN", new BigDecimal(1));
        sectorValues.put("LAST_KLINE_PARAM_1_KLINE_PRICE_MA_DIRECTION_END", new BigDecimal("2"));


        boolean res = strategy.getCriteria().getCriteriaType().getCriteriaHandler().match(strategy.getCriteria(), sectorValues, true);

//        boolean res = criteria1.getCriteriaType().getCriteriaHandler().match(criteria1, sectorValues, true);
//        boolean res = criteria2.getCriteriaType().getCriteriaHandler().match(criteria2, sectorValues, true);
//        boolean res = criteria3.getCriteriaType().getCriteriaHandler().match(criteria3, sectorValues, true);
//        boolean res = criteria4.getCriteriaType().getCriteriaHandler().match(criteria4, sectorValues, true);

        System.out.println(">>>>>>>>>>" + res);
    }


    private Strategy getStrategy(boolean goingLong) {
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
        strategy1.setGoingLong(goingLong);

        return strategy1;
    }



    @Test
    public void closeStockDemo() {
        //实时价格破价格MA30的千分之五
        SimpleCriteria criteria1 = new SimpleCriteria();
        criteria1.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        criteria1.setTarget(new Sector(Sector.SectorType.REALTIME_PRICE_MA, 30));
        criteria1.setCondition(new Condition(Condition.ConditionType.BEHIND, Condition.ConditionValueType.PERCENT, "0.5"));
        criteria1.setDescription("实时价格破价格MA30的千分之五");

        //连续三次破均线
        SimpleCriteria simpleCriteria = new SimpleCriteria();
        simpleCriteria.setSource(new Sector(Sector.SectorType.KLINE_PRICE_CLOSE));
        simpleCriteria.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        simpleCriteria.setCondition(new Condition(Condition.ConditionType.BEHIND, Condition.ConditionValueType.ANY, null));
        LastNKlineCriteria criteria2 = new LastNKlineCriteria(3, simpleCriteria, LastNKlineCriteria.MatchType.ALL_MATCH);
        criteria2.setDescription("连续三次破均线");

        //盈利百分之一
        SimpleCriteria criteria3 = new SimpleCriteria();
        criteria3.setSource(new Sector(Sector.SectorType.PROFIT));
        criteria3.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, "1"));
        criteria3.setDescription("盈利百分之一");

        //亏损百分之一
        SimpleCriteria criteria4 = new SimpleCriteria();
        criteria4.setSource(new Sector(Sector.SectorType.PROFIT));
        criteria4.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.PERCENT, "-1"));
        criteria4.setDescription("亏损百分之一");

        CompoundCriteria compoundCriteria = new CompoundCriteria(CompoundCriteria.Operator.OR);
        compoundCriteria.add(criteria1);
        compoundCriteria.add(criteria2);
        compoundCriteria.add(criteria3);
        compoundCriteria.add(criteria4);
        compoundCriteria.setDescription("平仓条件");

        System.out.println(JSONObject.toJSONString(compoundCriteria));
    }


}
