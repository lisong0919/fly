package com.wealth.fly.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@AllArgsConstructor
@Getter
public enum GridStatus {
    /**
     * 空闲，无操作
     */
    IDLE(0),
    /**
     * 已委托，已挂单
     */
    PENDING(1),
    /**
     * 已激活，已成交
     */
    ACTIVE(2);

    private Integer code;
}
