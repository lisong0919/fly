package com.wealth.fly.core.strategy.criteria.condition;

import java.math.BigDecimal;

public class BehindConditionHandler extends AbstractConditionHandler implements ConditionHandler {
    @Override
    public boolean match(BigDecimal sourceValue, BigDecimal targetValue, Condition condition, boolean goingLong) {
        if (goingLong) {
            return compare(sourceValue, targetValue, condition.getValueType(), condition.getValue()) < 0;
        } else {
            return compare(sourceValue, targetValue, condition.getValueType(), condition.getValue()) > 0;
        }
    }
}
