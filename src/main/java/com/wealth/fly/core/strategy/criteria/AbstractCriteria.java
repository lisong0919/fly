package com.wealth.fly.core.strategy.criteria;

public abstract class AbstractCriteria implements  Criteria {
    private String description;

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
