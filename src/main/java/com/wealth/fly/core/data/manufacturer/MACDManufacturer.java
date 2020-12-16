package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MACDManufacturer implements NewKLineManufacturer {

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }


    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        double diff = MathUtil.caculateDIF(kLine.getEma12().doubleValue(), kLine.getEma26().doubleValue());
        sectorValues.put(Sector.SectorType.DIF.name(), new BigDecimal(diff));
        sectorValues.put(Sector.SectorType.DEA.name(), kLine.getDea9());
        sectorValues.put(Sector.SectorType.MACD.name(), kLine.getMacd());
    }
}
