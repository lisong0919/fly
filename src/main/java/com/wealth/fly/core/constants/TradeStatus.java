package com.wealth.fly.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@AllArgsConstructor
@Getter
public enum TradeStatus {
    /**
     * 已委托，已挂单
     */
    PENDING(1),
    /**
     * 已开仓
     */
    OPENED(2),

    /**
     * 已平仓
     */
    CLOSED(3),
    /**
     * 已取消
     */
    CANCEL(4);

    private Integer code;
}
