package com.wealth.fly.core.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Data
public class GoldForkStrategy {
    private String id;
    private String account;
    private String instId;
    private BigDecimal profitPercent;
    private String watchInstId;
}
