package com.wealth.fly;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = {"com.wealth.fly"})
@MapperScan(basePackages = {"com.wealth.fly.core.dao.mapper"})
public class FlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlyApplication.class, args);
    }

}
