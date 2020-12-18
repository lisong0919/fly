package com.wealth.fly.core.strategy.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@EqualsAndHashCode
public class Sector {
    private SectorType type;
    private Object value;

    public Sector() {
    }

    public Sector(SectorType type) {
        this(type, null);
    }

    public Sector(SectorType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public enum SectorType {

        KLINE_PRICE_OPEN,

        KLINE_PRICE_CLOSE,

        KLINE_PRICE_HIGH,

        KLINE_PRICE_LOW,

        KLINE_VOLUME,

        KLINE_PRICE_MA,

        KLINE_VOLUME_MA,

        KLINE_DIF,

        KLINE_DEA,

        KLINE_MACD,

        KLINE_MACD_MA,

        KLINE_MAX_PRICE_CHANGE_PERCENT,

        KLINE_PRICE_CHANGE_PERCENT,

        KLINE_PREV_CLOSE_PRICE,

        KLINE_PREV_MACD,


        REALTIME_PRICE,

        REALTIME_PRICE_MA,

        STOCK_PRICE_OPEN

    }


    public static void main(String[] args) {
        Sector sector1=new Sector(SectorType.KLINE_DEA,3);
        Sector sector2=new Sector(SectorType.KLINE_DEA,4);

        HashSet<Sector> hashSet=new HashSet<>();
        hashSet.add(sector1);
        hashSet.add(sector2);
        System.out.println(hashSet);
    }

//    @Override
//    public int hashCode(){
//
//    }
}
