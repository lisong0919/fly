package com.wealth.fly.core.strategy.criteria;

import java.math.BigDecimal;
import java.util.Map;

public class CompoundCriteriaHandler implements CriteriaHandler {

    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues, boolean goingLong) {
        CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;

        if (CompoundCriteria.Operator.AND.equals(compoundCriteria.getOperator())) {
            return executeAndOperatorResult(compoundCriteria, sectorValues, goingLong);
        }
        if (CompoundCriteria.Operator.OR.equals(compoundCriteria.getOperator())) {
            return executeOrOperatorResult(compoundCriteria, sectorValues, goingLong);
        }

        throw new RuntimeException("unsupported operator " + compoundCriteria.getOperator());
    }


    private boolean executeAndOperatorResult(CompoundCriteria compoundCriteria, Map<String, BigDecimal> sectorValues, boolean goingLong) {
        for (Criteria c : compoundCriteria.getCriteriaList()) {
            if (!c.getCriteriaType().getCriteriaHandler().match(c, sectorValues, goingLong)) {
                return false;
            }
        }
        return true;
    }

    private boolean executeOrOperatorResult(CompoundCriteria compoundCriteria, Map<String, BigDecimal> sectorValues, boolean goingLong) {
        for (Criteria c : compoundCriteria.getCriteriaList()) {
            if (c.getCriteriaType().getCriteriaHandler().match(c, sectorValues, goingLong)) {
                return true;
            }
        }
        return false;
    }
}
