package com.wealth.fly.core.fetcher;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.dao.FundingRateDao;
import com.wealth.fly.core.exchanger.BinanceExchanger;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.model.FundingRate;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

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

    private List<String> baInsts = Arrays.asList(new String[]{"BTCUSD_PERP", "ETHUSD_PERP"});

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Exchanger baExchanger =
                ExchangerManager.getExchangerByAccountId(baDefaultAccount);


        for (String baInst : baInsts) {
            FundingRate latest = fundingRateDao.getLatestOne(baExchanger.getExchangerKey(), baInst);
            Date startTime = latest == null ? null : DateUtil.parseStandardTime(latest.getFundingTime() + 1);
            Date endTime = new Date();

            List<FundingRate> fundingRates = null;
            try {
                fundingRates = baExchanger.listFundingRateHistory(baInst, startTime, endTime, 100);
            } catch (IOException e) {
                log.error("获取币安费率失败 " + e.getMessage(), e);
            }
            if (fundingRates != null) {
                for (FundingRate fundingRate : fundingRates) {
                    fundingRateDao.save(fundingRate);
                }
            }
        }


    }

}
