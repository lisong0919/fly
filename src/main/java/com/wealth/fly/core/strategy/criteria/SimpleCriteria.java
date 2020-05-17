package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
public class SimpleCriteria implements Criteria{
    private Sector source;
    private Condition condition;
    private Sector target;

}
