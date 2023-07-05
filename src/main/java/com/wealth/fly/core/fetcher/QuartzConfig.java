package com.wealth.fly.core.fetcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * @author : lisong
 * @date : 2023/6/13
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean tradeStatusFetcherJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(TradeStatusFetcher.class);
        jobDetail.setName("tradeStatusFetcher");
        jobDetail.setDurability(true);
        return jobDetail;
    }

    @Bean
    public CronTriggerFactoryBean tradeStatusFetcherTrigger() {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(tradeStatusFetcherJobDetail().getObject());
        trigger.setCronExpression("*/10 * * * * ?");
        return trigger;
    }

    @Bean
    public CronTriggerFactoryBean KlineDataFetcherTrigger() {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(KlineDataFetcherJobDetail().getObject());
        trigger.setCronExpression("1 */1 * * * ?");
        return trigger;
    }

    @Bean
    public CronTriggerFactoryBean gridStatusFetcherTrigger() {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(gridStatusFetcherJobDetail().getObject());
        trigger.setCronExpression("*/5 * * * * ?");
        return trigger;
    }

    @Bean
    public CronTriggerFactoryBean markPriceFetcherTrigger() {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(markPriceFetcherJobDetail().getObject());
        trigger.setCronExpression("*/6 * * * * ?");
        return trigger;
    }

    @Bean
    public JobDetailFactoryBean KlineDataFetcherJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(KlineDataFetcher.class);
        jobDetail.setName("klineDataFetcherJob");
        jobDetail.setDurability(true);
        return jobDetail;
    }


    @Bean
    public JobDetailFactoryBean gridStatusFetcherJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(GridStatusFetcher.class);
        jobDetail.setName("gridStatusFetcherJob");
        jobDetail.setDurability(true);
        return jobDetail;
    }


    @Bean
    public JobDetailFactoryBean markPriceFetcherJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(MarkPriceFetcher.class);
        jobDetail.setName("markPriceFetcherJob");
        jobDetail.setDurability(true);
        return jobDetail;
    }


}
