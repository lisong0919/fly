package com.wealth.fly.core.reinforce;

import com.wealth.fly.core.entity.RealTimePrice;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RealtimeDataReinforce implements DataReinforce<RealTimePrice>{


    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void reinfore(RealTimePrice data, Map<String, BigDecimal> sectorValues) {
        sectorValues.put(Sector.SectorType.REALTIME_PRICE.name(), data.getPrice());
        sectorValues.put(Sector.SectorType.KLINE_PRICE_MA.name(), data.getMaPrice());
    }
}
