package com.wealth.fly.core.model;

import lombok.Data;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Data
public class MaxOpenSize {
    private String instId;
    private String ccy;
    private String maxBuy;
    private String maxSell;
}
