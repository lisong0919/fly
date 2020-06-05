package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.strategy.criteria.condition.Condition;
import com.wealth.fly.core.strategy.criteria.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StrategyDemo {

    /**
     * 开仓条件示例
     */
    @Test
    public void openStockdemo() {
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
        criteria4.setSource(new Sector(Sector.SectorType.KLINE_PRICE_MA_DIRECTION, 30));
        criteria4.setCondition(new Condition(Condition.ConditionType.FOLLOW, Condition.ConditionValueType.ANY, null));

        //条件5：两个K线涨幅不超过1%
//        SimpleCriteria criteria5=new SimpleCriteria();
        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(criteria1);
        finalCriteria.add(criteria2);
        finalCriteria.add(criteria3);
        finalCriteria.add(criteria4);

        Strategy strategy1=new Strategy();
        strategy1.setCriteria(finalCriteria);
        strategy1.setGoingLong(true);


        Strategy strategy2=new Strategy();
        strategy2.setCriteria(finalCriteria);
        strategy2.setGoingLong(false);

        List<Strategy> strategyList=new ArrayList<>();
        strategyList.add(strategy1);
        strategyList.add(strategy2);

        System.out.println(JSONObject.toJSONString(strategyList));

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
