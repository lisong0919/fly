package com.wealth.fly.core.strategy.criteria.condition;

import java.math.BigDecimal;

public class AbstractConditionHandler {

    private static final BigDecimal PERCENT = new BigDecimal(100);

    public int compare(BigDecimal sourceValue, BigDecimal targetValue, Condition.ConditionValueType conditionValueType, String conditionValue) {
        if (Condition.ConditionValueType.ANY.equals(conditionValueType)) {
            return sourceValue.compareTo(targetValue);
        }
        if (Condition.ConditionValueType.EXACT.equals(conditionValueType)) {
            return sourceValue.subtract(targetValue).compareTo(new BigDecimal(conditionValue));
        }
        if (Condition.ConditionValueType.PERCENT.equals(conditionValueType)) {
            return sourceValue.subtract(targetValue).multiply(PERCENT).divide(targetValue, 2, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal(conditionValue));
        }

        throw new RuntimeException("unsupport condition value type:" + conditionValueType);
    }
}
