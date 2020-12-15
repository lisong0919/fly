package com.wealth.fly.core.reinforce;

import com.wealth.fly.core.entity.KLine;

import java.math.BigDecimal;
import java.util.Map;

public interface NewKLineDataReinforce extends DataReinforce<KLine>{

    void reinfore(KLine kLine, Map<String, BigDecimal> sectorValues);

}
