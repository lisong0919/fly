package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PreKLineManufacturer implements NewKLineManufacturer {

    private KLine prevKLine;

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }


    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        if(prevKLine==null){
            //TODO 从数据库查
            return;
        }
        sectorValues.put(Sector.SectorType.PREV_KLINE_CLOSE_PRICE.name(), prevKLine.getClose());
        sectorValues.put(Sector.SectorType.PREV_KLINE_MACD.name(), prevKLine.getMacd());//上一根K线的MACD

        prevKLine=kLine;
    }
}
