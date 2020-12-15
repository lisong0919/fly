package com.wealth.fly.statistic;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class StatisticItem {
    /**
     * 开仓时间
     */
    private Long startDataTime;

    /**
     * 平仓时间
     */
    private Long endDataTime;
    /**
     * 开仓价格
     */
    private BigDecimal startPrice;
    /**
     * 平仓价格
     */
    private BigDecimal endPrice;
    /**
     * 盈利百分比
     */
    private BigDecimal profitPercent;
    /**
     * 距离开仓振幅
     */
    private BigDecimal amplitudeFromOpenPrice;

    /**
     * 距离均线的振幅
     */
    private BigDecimal amplitudeFromMAPrice;

    /**
     * 是否盈利
     */
    private Boolean isWin;

    private Boolean goingLong;
}
