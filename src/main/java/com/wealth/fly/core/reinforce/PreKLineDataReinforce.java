package com.wealth.fly.core.reinforce;

import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PreKLineDataReinforce implements NewKLineDataReinforce{

    private KLine prevKLine;

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void reinfore(KLine kLine, Map<String, BigDecimal> sectorValues) {
        if(prevKLine==null){
            return;
        }
        sectorValues.put(Sector.SectorType.PREV_KLINE_CLOSE_PRICE.name(), prevKLine.getClose());
        sectorValues.put(Sector.SectorType.PREV_KLINE_MACD.name(), prevKLine.getMacd());//上一根K线的MACD

        prevKLine=kLine;
    }
}
