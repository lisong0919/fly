package com.wealth.fly;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
@SpringBootApplication(scanBasePackages = {"com.wealth.fly"})
@MapperScan(basePackages = {"com.wealth.fly.core.dao.mapper"})
public class FlyTestApplication {

}
