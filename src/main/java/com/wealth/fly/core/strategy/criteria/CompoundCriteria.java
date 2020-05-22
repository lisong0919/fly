package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompoundCriteria extends AbstractCriteria implements Criteria {

    private Operator operator;
    private List<Criteria> criteriaList;

    public CompoundCriteria(Operator operator) {
        this.operator = operator;
    }

    public CompoundCriteria(Operator operator, Criteria... criterias) {
        this.operator = operator;
        if (criterias != null) {
            for (Criteria c : criterias) {
                add(c);
            }
        }
    }

    public void add(Criteria criteria) {
        if (criteriaList == null) {
            criteriaList = new ArrayList<>();
        }
        criteriaList.add(criteria);
    }

    @Override
    public CriteriaType getCriteriaType() {
        return CriteriaType.COMPOUND;
    }

    public enum Operator {
        AND,
        OR
    }
}
