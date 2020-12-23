package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.core.data.manufacturer.interf.RealtimeManufacturer;
import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SimpleRealtimeManufacturer implements RealtimeManufacturer {
    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void manufact(RealTimePrice realTimePrice, Map<String, BigDecimal> sectorValues) {
        sectorValues.put(Sector.SectorType.REALTIME_PRICE.name(), realTimePrice.getPrice());
    }
}
