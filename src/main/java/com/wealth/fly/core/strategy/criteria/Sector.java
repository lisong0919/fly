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
        VOLUME,
        /**
         * K线极端价格：开多时最高价，开空时最低价
         */
        KLINE_EXTREME_PRICE,
        KLINE_CLOSE_PRICE,
        REALTIME_PRICE,
        KLINE_PRICE_MA,
        /**
         * K线MA的方向
         */
        KLINE_PRICE_MA_DIRECTION,
        KLINE_VOLUME_MA;

    }
}
