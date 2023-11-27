package com.wealth.fly.core.constants;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public class OrderStatus {
    /**
     * 订单状态
     * canceled：撤单成功
     * live：等待成交
     * partially_filled：部分成交
     * filled：完全成交
     */

    public static final String CANCELED = "canceled";
    public static final String LIVE = "live";
    public static final String PARTIALLY_FILLED = "partially_filled";
    public static final String FILLED = "filled";


}
