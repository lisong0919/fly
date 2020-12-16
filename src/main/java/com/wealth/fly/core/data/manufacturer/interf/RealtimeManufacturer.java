package com.wealth.fly.core.data.manufacturer.interf;

import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.entity.RealTimePrice;
import java.math.BigDecimal;
import java.util.Map;

public interface RealtimeManufacturer extends DataManufacturer<RealTimePrice> {

    void manufact(RealTimePrice realTimePrice, Map<String, BigDecimal> sectorValues);


}
