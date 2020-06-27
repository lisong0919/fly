package com.wealth.fly.core.strategy.criteria;

import java.math.BigDecimal;
import java.util.Map;

public class CompoundCriteriaHandler implements CriteriaHandler {

    @Override
    public boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues) {
        CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;

        if (CompoundCriteria.Operator.AND.equals(compoundCriteria.getOperator())) {
            return executeAndOperatorResult(compoundCriteria, sectorValues);
        }
        if (CompoundCriteria.Operator.OR.equals(compoundCriteria.getOperator())) {
            return executeOrOperatorResult(compoundCriteria, sectorValues);
        }

        throw new RuntimeException("unsupported operator " + compoundCriteria.getOperator());
    }


    private boolean executeAndOperatorResult(CompoundCriteria compoundCriteria, Map<String, BigDecimal> sectorValues) {
        for (Criteria c : compoundCriteria.getCriteriaList()) {
            if (!c.getCriteriaType().getCriteriaHandler().match(c, sectorValues)) {
                return false;
            }
        }
        return true;
    }

    private boolean executeOrOperatorResult(CompoundCriteria compoundCriteria, Map<String, BigDecimal> sectorValues) {
        for (Criteria c : compoundCriteria.getCriteriaList()) {
            if (c.getCriteriaType().getCriteriaHandler().match(c, sectorValues)) {
                return true;
            }
        }
        return false;
    }
}
