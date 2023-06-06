package com.wealth.fly.core.constants;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public class OkexAlgoOrderState {

    /**
     * live：待生效
     * pause：暂停生效
     * partially_effective:部分生效
     * effective： 已生效
     * canceled：已撤销
     * order_failed：委托失败
     */

    public static final String LIVE = "live";
    public static final String EFFECTIVE = "effective";
    public static final String PARTIALLY_EFFECTIVE = "partially_effective";
    public static final String CANCELED="canceled";
    
}
