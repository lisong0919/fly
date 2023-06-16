package com.wealth.fly;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication(scanBasePackages = {"com.wealth.fly"})
@MapperScan(basePackages = {"com.wealth.fly.core.dao.mapper"})
public class FlyApplication {

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(FlyApplication.class, args);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
