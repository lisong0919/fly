package com.wealth.fly.core.reinforce;

import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DataReinforce<T> {

    List<Sector.SectorType> getDependency();

    void reinfore(T data, Map<String, BigDecimal> sectorValues);
}
