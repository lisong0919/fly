package com.wealth.fly.core.data.manufacturer.interf;

import com.wealth.fly.core.data.manufacturer.interf.DataManufacturer;
import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;
import java.util.Map;

public interface NewKLineManufacturer extends DataManufacturer<KLine> {

    void manufact(KLine kLine, Map<String, BigDecimal> sectorValues);
}
