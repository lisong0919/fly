package com.wealth.fly.core.data.manufacturer.interf;


import com.wealth.fly.core.data.manufacturer.interf.DataManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface OpenStockManufacturer extends DataManufacturer<OpenStock> {


    void manufact(OpenStock openStock, Map<String, BigDecimal> sectorValues);

}
