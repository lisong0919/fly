package com.wealth.fly.core.dao.impl;

import com.wealth.fly.core.dao.FundingRateDao;
import com.wealth.fly.core.dao.mapper.FundingRateMapper;
import com.wealth.fly.core.model.FundingRate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author : lisong
 * @date : 2024/5/26
 */
@Repository
public class FundingRateDaoImpl implements FundingRateDao {
    @Resource
    private FundingRateMapper fundingRateMapper;

    @Override
    public void save(FundingRate fundingRate) {
        fundingRateMapper.insert(fundingRate);
    }
}
