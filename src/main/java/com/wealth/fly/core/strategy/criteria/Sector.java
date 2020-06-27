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


        KLINE_PRICE_OPEN,

        /**
         *  成交量
         */
        KLINE_VOLUME,


        /**
         * 收盘价
         */
        KLINE_PRICE_CLOSE,

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




    }
}
