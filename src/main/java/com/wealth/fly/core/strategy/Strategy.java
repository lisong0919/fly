package com.wealth.fly.core.strategy;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.Event;
import com.wealth.fly.core.strategy.criteria.Criteria;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Strategy {
    private String id;
    private Criteria criteria;
    private int currencyId;
    private DataGranularity dataGranularity;
    private boolean goingLong;
    private boolean openStock;
    private String closeStrategyId;
    private List<Event> triggerEventList;


}
