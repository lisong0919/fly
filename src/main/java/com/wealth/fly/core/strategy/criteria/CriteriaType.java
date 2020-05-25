package com.wealth.fly.core.strategy.criteria;

public enum CriteriaType {
    SIMPLE(new SimpleCriteriaHandler()),
    LAST_N_KLINE(new LastNKlineCriteriaHandler()),
    COMPOUND(new CompoundCriteriaHandler());

    private CriteriaHandler criteriaHandler;

    CriteriaType(CriteriaHandler criteriaHandler) {
        this.criteriaHandler = criteriaHandler;
    }

    public CriteriaHandler getCriteriaHandler() {
        return this.criteriaHandler;
    }
}
