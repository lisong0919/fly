package com.wealth.fly.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : lisong
 * @date : 2024/5/25
 */
@Data
public class FundingRate {
    private String symbol;
    private String exchanger;
    private Long fundingTime;
    private BigDecimal fundingRate;
    private BigDecimal markPrice;
    private Date createTime;
    private Date modifyTime;
}
