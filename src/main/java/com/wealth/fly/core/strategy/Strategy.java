package com.wealth.fly.core.strategy;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.strategy.criteria.Criteria;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Strategy {
    private String id;
    private Criteria criteria;
    private int currencyId;
    private DataGranularity dataGranularity;
    private Action action;
    private boolean goingLong;
    private boolean openStock;
    private String closeStrategyId;


}
