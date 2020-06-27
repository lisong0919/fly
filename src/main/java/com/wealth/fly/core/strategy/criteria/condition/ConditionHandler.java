package com.wealth.fly.core.strategy.criteria.condition;

import java.math.BigDecimal;

public interface ConditionHandler {

    /**
     * @param sourceValue
     * @param targetValue
     * @param condition
     * @return
     */
    boolean match(BigDecimal sourceValue, BigDecimal targetValue, Condition condition);
}
