package com.wealth.fly;

import com.wealth.fly.core.fetcher.KlineDataFetcher;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.JobDetail;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;


@SpringBootApplication(scanBasePackages = {"com.wealth.fly"})
@MapperScan(basePackages = {"com.wealth.fly.core.dao.mapper"})
public class FlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlyApplication.class, args);
    }

    @Bean
    public SimpleTriggerFactoryBean createSimpleTriggerFactoryBean(JobDetail jobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactory
                = new SimpleTriggerFactoryBean();

        simpleTriggerFactory.setJobDetail(jobDetail);
        simpleTriggerFactory.setStartDelay(2000);
        simpleTriggerFactory.setRepeatInterval(30000);
        return simpleTriggerFactory;
    }

    @Bean
    public JobDetailFactoryBean createJobDetailFactoryBean() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(KlineDataFetcher.class);
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

}
