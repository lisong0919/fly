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
        /**
         *  成交量
         */
        KLINE_VOLUME,
        /**
         * K线阳性价格：开多时最高价，开空时最低价
         */
        KLINE_POSITIVE_PRICE,

        /**
         * K线阴性价格：开多时为最低价，开空时最高价
         */
        KLINE_NEGATIVE_PRICE,

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
         * K线MA的方向
         */
        KLINE_PRICE_MA_DIRECTION_BEGIN,

        KLINE_PRICE_MA_DIRECTION_END,
        /**
         * 成交量MA
         */
        KLINE_VOLUME_MA,

        /**
         * 收益
         */
        PROFIT,

        LAST_KLINE_CLOSE_PRICE,

        FIRST_KLINE_OPEN_PRICE,

        KLINE_PRICE_OPEN

    }
}
