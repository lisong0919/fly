package com.wealth.fly.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.exchanger.BinanceExchanger;
import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author : lisong
 * @date : 2023/11/25
 */
public class BinanceExchangerTest {

    private BinanceExchanger exchanger;

    @BeforeEach
    public void init() {
        Account account = new Account();
        account.setType("binance");
        account.setAccessKey("D3v8nDdedqHDB7N2g5UvAjC0Mq8rkQjXR3oYZpeSFKT3To8vu2VRF6dbjLC3pWv0");
        account.setSecretKey("neGGfdhPVOehafjM4MeZ8bIHKy5Lxva46PYGDUytRsx2hWYGd0b1uMFAzXreZgGm");

        exchanger = new BinanceExchanger(account);
    }

    @Test
    public void testGetMarkPriceByInstId() throws IOException {
        System.out.println(">>>>" + exchanger.getMarkPriceByInstId("BTCUSD_PERP").getMarkPx());
    }

    @Test
    public void createOrderTest() throws IOException {
        //下单
        Order order = Order.builder()
                .instId("BTCUSD_231229")
                .tdMode("cross")
                .side("buy")
                .posSide("long")
                .ordType("limit")
                .sz("10")
                .px("30000")
                .build();
        exchanger.createOrder(order);
    }


}
