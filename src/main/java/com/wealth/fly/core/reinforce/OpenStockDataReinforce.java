package com.wealth.fly.core.reinforce;


import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class OpenStockDataReinforce implements DataReinforce<OpenStock>{

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void reinfore(OpenStock data, Map<String, BigDecimal> sectorValues) {

        sectorValues.put(Sector.SectorType.STOCK_PRICE_OPEN.name(), data.getOpenPrice());
    }
}
