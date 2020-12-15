package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
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

        DIF,

        DEA,

        MACD,

        PREV_KLINE_CLOSE_PRICE,

        PREV_KLINE_MACD,

        STOCK_PRICE_OPEN,

        KLINE_PRICE_OPEN,

        KLINE_MAX_PRICE_CHANGE_PERCENT,

        KLINE_PRICE_CHANGE_PERCENT,

        /**
         *  成交量
         */
        KLINE_VOLUME,


        /**
         * 收盘价
         */
        KLINE_PRICE_CLOSE,

        KLINE_PRICE_HIGH,

        KLINE_PRICE_LOW,

        /**
         * 实时价格
         */
        REALTIME_PRICE,

        /**
         * 实时均线价格
         */
        REALTIME_PRICE_MA,
        /**
         * 价格MA
         */
        KLINE_PRICE_MA,

        /**
         * 成交量MA
         */
        KLINE_VOLUME_MA,


        KLINE_MACD_MA


    }
}
