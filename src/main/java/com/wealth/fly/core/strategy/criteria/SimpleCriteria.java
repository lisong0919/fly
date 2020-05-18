package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
public class SimpleCriteria extends AbstractCriteria implements Criteria {
    private Sector source;
    private Condition condition;
    private Sector target;

    @Override
    public CriteriaType getCriteriaType() {
        return CriteriaType.SIMPLE;
    }
}
