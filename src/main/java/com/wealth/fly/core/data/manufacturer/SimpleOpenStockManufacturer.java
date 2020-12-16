package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.core.data.manufacturer.interf.OpenStockManufacturer;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SimpleOpenStockManufacturer implements OpenStockManufacturer {
    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void manufact(OpenStock openStock, Map<String, BigDecimal> sectorValues) {
        sectorValues.put(Sector.SectorType.STOCK_PRICE_OPEN.name(), openStock.getOpenPrice());
    }
}
