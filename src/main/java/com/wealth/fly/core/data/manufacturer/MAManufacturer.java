package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.MACalculator;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.data.manufacturer.interf.DataManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.entity.MAParam;
import com.wealth.fly.core.strategy.criteria.Sector;
import com.wealth.fly.exception.DataInsufficientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MAManufacturer implements DataManufacturer<MAParam> {

    private int maLevel;
    private Sector.SectorType sectorType;
    private DataGranularity dataGranularity;
    private KLineDao kLineDao;

    private MACalculator maCalculator;

    public MAManufacturer(Sector sector, DataGranularity dataGranularity, KLineDao kLineDao) {
        this.sectorType = sector.getType();
        this.maLevel = Integer.valueOf(String.valueOf(sector.getValue()));
        this.dataGranularity = dataGranularity;
        this.kLineDao = kLineDao;
    }

    private BigDecimal initMACalculator(long datatime) {
        String name = dataGranularity + "_" + sectorType.name() + "_" + maLevel;
        maCalculator = new MACalculator(name, maLevel);

        List<KLine> kLineList = kLineDao.getLastKLineLEDataTime(dataGranularity.name(), datatime, maLevel);

        if (kLineList == null || kLineList.size() != maLevel) {
            throw new DataInsufficientException("数据不足:" + name);
        }

        for (KLine kLine : kLineList) {
            BigDecimal maValue = null;
            if (sectorType == Sector.SectorType.KLINE_PRICE_MA || sectorType == Sector.SectorType.REALTIME_PRICE_MA) {
                maValue = kLine.getClose();
            } else if (sectorType == Sector.SectorType.KLINE_VOLUME_MA) {
                maValue = kLine.getVolume();
            } else if (sectorType == Sector.SectorType.KLINE_MACD_MA) {
                maValue = kLine.getMacd();
            }
            maCalculator.push(kLine.getDataTime(), maValue);
        }

        return maCalculator.getLast().getValue();
    }

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }


    @Override
    public void manufact(MAParam maParam, Map<String, BigDecimal> sectorValues) {
        sectorValues.put(sectorType.name() + "_" + maLevel, getMaValue(maParam));
    }

    public BigDecimal getMaValue(MAParam maParam) {
        BigDecimal maValue = null;

        long preDatatime = DateUtil.getPreDateTime(maParam.getDatatime(), dataGranularity);
        if (maCalculator.getCount().intValue() < maLevel || preDatatime != maCalculator.getLast().getValue().longValue()) {
            maValue = initMACalculator(maParam.getDatatime());
        } else {
            if (sectorType.isRealtime()) {
                maValue = maCalculator.replaceLast(maParam.getDatatime(), maParam.getValue());
            } else {
                maValue = maCalculator.push(maParam.getDatatime(), maParam.getValue());
            }
        }
        return maValue;
    }
}
