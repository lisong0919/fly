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
                .instId("ETHUSD_231229")
                .tdMode("cross")
                .side("buy")
                .posSide("long")
                .ordType("limit")
                .sz("2")
                .px("2040")
                .build();
        exchanger.createOrder(order);
    }

    @Test
    public void getOrderTest() throws IOException {
        Order order = exchanger.getAlgoOrder("ETHUSD_231229", "1117277842");
        System.out.println(JsonUtil.toJSONString(order));
    }

    @Test
    public void cancelOrderTest() throws IOException {
        exchanger.cancelOrder("ETHUSD_231229", "1117277842");
    }

    @Test
    public void createAlgoOrderTest() throws IOException {
        //下止盈策略单
        Order order = Order.builder()
                .instId("ETHUSD_231229")
                .tdMode("cross")
                .side("sell")
                .posSide("long")
                .ordType("conditional")
                .sz("2")
                .tpTriggerPx("2100")
                .tpTriggerPxType("mark")
                .tpOrdPx("2100")
                .build();
        exchanger.createAlgoOrder(order);
    }

}
