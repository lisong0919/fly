package com.wealth.fly.core.strategy;

import com.wealth.fly.core.strategy.criteria.*;
import org.junit.jupiter.api.Test;

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
        LastNCriteria lastNCriteria1 = new LastNCriteria(2, criteria1, LastNCriteria.MatchType.ONE_MATCH);

        //两个k线的最高或最低价格，都突破价格MA30
        SimpleCriteria criteria2 = new SimpleCriteria();
        criteria2.setSource(new Sector(Sector.SectorType.KLINE_EXTREME_PRICE));
        criteria2.setCondition(new Condition(Condition.ConditionType.BREAK_OUT, Condition.ConditionValueType.ANY, null));
        criteria2.setTarget(new Sector(Sector.SectorType.KLINE_PRICE_MA, 30));
        LastNCriteria lastNCriteria2 = new LastNCriteria(2, criteria2, LastNCriteria.MatchType.ALL_MATCH);

        //均线方向顺势而行
        SimpleCriteria criteria3 = new SimpleCriteria();
        criteria3.setSource(new Sector(Sector.SectorType.KLINE_PRICE_MA_DIRECTION, 30));
        criteria3.setCondition(new Condition(Condition.ConditionType.OBEY, null, null));

        CompoundCriteria finalCriteria = new CompoundCriteria(CompoundCriteria.Operator.AND);
        finalCriteria.add(lastNCriteria1);
        finalCriteria.add(lastNCriteria2);
        finalCriteria.add(criteria3);
    }




}
