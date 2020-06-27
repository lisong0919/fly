package com.wealth.fly.core.strategy.criteria.condition;

import java.math.BigDecimal;

public class LessThanConditionHandler extends AbstractConditionHandler implements ConditionHandler {
    @Override
    public boolean match(BigDecimal sourceValue, BigDecimal targetValue, Condition condition) {
        return compare(sourceValue, targetValue, condition.getValueType(), condition.getValue()) < 0;
    }
}
