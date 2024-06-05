package com.wealth.fly.core.model;

import lombok.Data;

/**
 * @author : lisong
 * @date : 2024/5/26
 */
@Data
public class BAFundingRate {
    private String symbol;
    private Long fundingTime;
    private String fundingRate;
    private String markPrice;
}
