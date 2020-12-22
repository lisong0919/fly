package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.exception.DataInsufficientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PreKLineManufacturer implements NewKLineManufacturer {


    private KLine prevKLine;
    private KLineDao kLineDao;
    private DataGranularity dataGranularity;

    public PreKLineManufacturer(DataGranularity dataGranularity, KLineDao kLineDao) {
        this.dataGranularity = dataGranularity;
        this.kLineDao = kLineDao;
    }

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }


    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        long preDataTime= DateUtil.getPreDateTime(kLine.getDataTime(),dataGranularity);

        if (prevKLine == null || preDataTime!=prevKLine.getDataTime()) {
            prevKLine=kLineDao.getKlineByDataTime(dataGranularity.name(),preDataTime);
            if(prevKLine==null){
                throw new DataInsufficientException("prekline 数据不足: "+dataGranularity.name());
            }
        }


        sectorValues.put(Sector.SectorType.KLINE_PREV_CLOSE_PRICE.name(), prevKLine.getClose());
        sectorValues.put(Sector.SectorType.KLINE_PREV_MACD.name(), prevKLine.getMacd());//上一根K线的MACD

        prevKLine = kLine;
    }
}
