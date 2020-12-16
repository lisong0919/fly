package com.wealth.fly.core.data.manufacturer.interf;

import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface DataManufacturer<T> {

    List<Sector.SectorType> getDependency();

    void manufact(T material, Map<String, BigDecimal> sectorValues);


}
