package com.wealth.fly.core.data.manufacturer;

import com.wealth.fly.common.MathUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.data.manufacturer.interf.NewKLineManufacturer;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.criteria.Sector;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CommonKLineManufacturer implements NewKLineManufacturer {

    @Override
    public List<Sector.SectorType> getDependency() {
        return null;
    }

    @Override
    public void manufact(KLine kLine, Map<String, BigDecimal> sectorValues) {
        sectorValues.put(Sector.SectorType.KLINE_VOLUME.name(), kLine.getVolume());//成交量
        sectorValues.put(Sector.SectorType.KLINE_PRICE_OPEN.name(), kLine.getOpen());//开盘价
        sectorValues.put(Sector.SectorType.KLINE_PRICE_CLOSE.name(), kLine.getClose());//收盘价
        sectorValues.put(Sector.SectorType.KLINE_PRICE_HIGH.name(), kLine.getHigh());//最高价
        sectorValues.put(Sector.SectorType.KLINE_PRICE_LOW.name(), kLine.getLow());//最低价
        sectorValues.put(Sector.SectorType.KLINE_MAX_PRICE_CHANGE_PERCENT.name(), CommonConstants.MAX_AMPLITUDE);//K线涨跌幅可接受的最大值
        //涨跌幅(开盘与收盘价之间价格浮动百分比)
        sectorValues.put(Sector.SectorType.KLINE_PRICE_CHANGE_PERCENT.name(), MathUtil.distancePercentInDecimal(kLine.getClose(), kLine.getOpen()));

        //macd相关
        double diff = MathUtil.caculateDIF(kLine.getEma12().doubleValue(), kLine.getEma26().doubleValue());
        sectorValues.put(Sector.SectorType.KLINE_DIF.name(), new BigDecimal(diff));
        sectorValues.put(Sector.SectorType.KLINE_DEA.name(), kLine.getDea9());
        sectorValues.put(Sector.SectorType.KLINE_MACD.name(), kLine.getMacd());
    }
}
