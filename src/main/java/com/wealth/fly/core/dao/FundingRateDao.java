package com.wealth.fly.core.dao;

import com.wealth.fly.core.model.FundingRate;

/**
 * @author : lisong
 * @date : 2024/5/26
 */
public interface FundingRateDao {
    void save(FundingRate fundingRate);

    FundingRate getLatestOne(String exchanger,String symbol);
}
