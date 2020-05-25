package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.strategy.criteria.condition.ConditionHandler;

import java.math.BigDecimal;
import java.util.Map;

public class SimpleCriteriaHandler implements CriteriaHandler{

    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues, Map<String, Object> extraParam, boolean goingLong) {
        SimpleCriteria simpleCriteria = (SimpleCriteria) criteria;

        ConditionHandler conditionHandler = simpleCriteria.getCondition().getType().getConditionHandler();
        BigDecimal sourceValue = sectorValues.get(simpleCriteria.getSource().getType());
        BigDecimal targetValue = sectorValues.get(simpleCriteria.getTarget().getType());
        return conditionHandler.match(sourceValue, targetValue, simpleCriteria.getCondition(), goingLong);
    }
}
