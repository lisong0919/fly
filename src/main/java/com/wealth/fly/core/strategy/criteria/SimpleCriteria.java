package com.wealth.fly.core.strategy.criteria;

import com.wealth.fly.core.strategy.criteria.condition.Condition;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SimpleCriteria extends AbstractCriteria implements Criteria {
    private Sector source;
    private Condition condition;
    private Sector target;

    @Override
    public CriteriaType getCriteriaType() {
        return CriteriaType.SIMPLE;
    }
}
