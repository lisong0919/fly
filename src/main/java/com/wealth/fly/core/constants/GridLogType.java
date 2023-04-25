package com.wealth.fly.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
@Getter
@AllArgsConstructor
public enum GridLogType {
    CREATE_PENDING_ORDER(1, "开委托单，网格待激活"),
    GRID_ACTIVE(2, "委托单成交，网格被激活"),
    GRID_FINISHED_PROFIT(3, "网格已止盈");

    private int code;
    private String desc;
}
