package com.wealth.fly.core.strategy.criteria;

import java.math.BigDecimal;
import java.util.Map;

public interface CriteriaHandler {

    boolean match(Criteria criteria, Map<String, BigDecimal> sectorValues);
}
