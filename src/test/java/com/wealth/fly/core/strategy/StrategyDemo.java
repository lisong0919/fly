package com.wealth.fly.core.strategy;

import com.alibaba.fastjson.JSONObject;
import com.wealth.fly.core.strategy.criteria.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class StrategyDemo {

    /**
     * 开仓条件示例
     */
    @Test
    public void openStockdemo() {
        //条件1：最后两个K线的成交量，任意一个大于成交量MA10的两倍
        SimpleCriteria criteria1 = new SimpleCriteria();
        criteria1.setSource(new Sector(Sector.SectorType.VOLUME));
        criteria1.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, 200));
        criteria1.setTarget(new Sector(Sector.SectorType.KLINE_VOLUME_MA, 10));
        LastNKlineCriteria lastNCriteria1 = new LastNKlineCriteria(2, criteria1, LastNKlineCriteria.MatchType.ONE_MATCH);

        //两个k线的价格，都突破价格MA30
        SimpleCriteria criteria2 = new SimpleCriteria();
        criteria2.setSource(new Sector(Sector.SectorType.KLINE_EXTREME_PRICE));
        criteria2.setCondition(new Condition(Condition.ConditionType.BREAK_OUT, Condition.ConditionValueType.ANY, null));
        criteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        LastNKlineCriteria lastNCriteria2 = new LastNKlineCriteria(2, criteria2, LastNKlineCriteria.MatchType.ALL_MATCH);

        //均线方向顺势而行
        SimpleCriteria criteria3 = new SimpleCriteria();
        criteria3.setSource(new Sector(Sector.SectorType.KLINE_PRICE_MA_DIRECTION, 30));
        criteria3.setCondition(new Condition(Condition.ConditionType.FOLLOW, null, null));

        //单次涨跌幅度未超过千五
//        SimpleCriteria criteria4=new SimpleCriteria();


        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lastNCriteria1);
        finalCriteria.add(lastNCriteria2);
        finalCriteria.add(criteria3);


    }

    @Test
    public void closeStockDemo() {
        //实时价格破价格MA30的千分之五
        SimpleCriteria criteria1 = new SimpleCriteria();
        criteria1.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        criteria1.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        criteria1.setCondition(new Condition(Condition.ConditionType.BREAK_DOWN, Condition.ConditionValueType.PERCENT, new BigDecimal(0.5)));
        criteria1.setDescription("实时价格破价格MA30的千分之五");

        //连续三次破均线
        SimpleCriteria simpleCriteria = new SimpleCriteria();
        simpleCriteria.setSource(new Sector(Sector.SectorType.REALTIME_PRICE));
        simpleCriteria.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        simpleCriteria.setCondition(new Condition(Condition.ConditionType.BREAK_DOWN, Condition.ConditionValueType.ANY, null));
        LastNKlineCriteria criteria2 = new LastNKlineCriteria(3, simpleCriteria, LastNKlineCriteria.MatchType.ALL_MATCH);
        criteria2.setDescription("连续三次破均线");

        //盈利百分之一
        SimpleCriteria criteria3 = new SimpleCriteria();
        criteria3.setSource(new Sector(Sector.SectorType.PROFIT));
        criteria3.setCondition(new Condition(Condition.ConditionType.GREAT_THAN, Condition.ConditionValueType.PERCENT, new BigDecimal(1)));
        criteria3.setDescription("盈利百分之一");

        //亏损百分之一
        SimpleCriteria criteria4 = new SimpleCriteria();
        criteria4.setSource(new Sector(Sector.SectorType.PROFIT));
        criteria4.setCondition(new Condition(Condition.ConditionType.LESS_THAN, Condition.ConditionValueType.PERCENT, new BigDecimal(-1)));
        criteria4.setDescription("亏损百分之一");

        CompoundCriteria compoundCriteria=new CompoundCriteria(CompoundCriteria.Operator.OR);
        compoundCriteria.add(criteria1);
        compoundCriteria.add(criteria2);
        compoundCriteria.add(criteria3);
        compoundCriteria.add(criteria4);
        compoundCriteria.setDescription("平仓条件");

        System.out.println(JSONObject.toJSONString(compoundCriteria));
    }


}
