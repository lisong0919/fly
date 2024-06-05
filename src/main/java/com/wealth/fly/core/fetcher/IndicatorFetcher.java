package com.wealth.fly.core.fetcher;

import com.wealth.fly.core.dao.FundingRateDao;
import com.wealth.fly.core.exchanger.BinanceExchanger;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : lisong
 * @date : 2024/5/26
 */
@Component
@Slf4j
public class IndicatorFetcher extends QuartzJobBean {

    @Value("${ba.account.default}")
    private String baDefaultAccount;

    @Resource
    private FundingRateDao fundingRateDao;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Exchanger exchanger =
                ExchangerManager.getExchangerByAccountId(baDefaultAccount);
    }

}
