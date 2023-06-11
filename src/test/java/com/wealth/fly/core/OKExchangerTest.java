package com.wealth.fly.core;

import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.OkexExchanger;
import com.wealth.fly.core.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public class OKExchangerTest {
    private OkexExchanger exchanger;

    @BeforeEach
    public void init() {
        exchanger = new OkexExchanger();
        exchanger.setHost("https://www.okx.com");
    }

    @Test
    public void testGetOrder() throws IOException {
        System.out.println(exchanger.getOrder("ETH-USD-230630", "571015379842478080"));
    }

    @Test
    public void testGetAlgoOrder() throws IOException {
        System.out.println(exchanger.getAlgoOrder("571087136158887936"));
    }

    @Test
    public void testCreateAlgoOrder() throws IOException {
        Order order = Order.builder()
                .instId("ETH-USD-230630")
                .tdMode("cross")
                .side("sell")
                .posSide("long")
                .ordType("conditional")
                .sz("2")
                .tag("12345")
                .tpTriggerPx("1987")
                .tpTriggerPxType("mark")
                .tpOrdPx("1987")
                .build();
        System.out.println(">>>>>>>>" + exchanger.createAlgoOrder(order));
    }

    @Test
    public void testCreateOrder() throws IOException {
        Order order = Order.builder()
                .instId("ETH-USD-230630")
                .tdMode("cross")
                .side("buy")
                .posSide("long")
                .ordType("limit")
                .sz("2")//数量
                .px("1825.2")
                .tag("12345")
                .tpTriggerPx("2000")
                .tpOrdPx("2000")
                .tpTriggerPxType("mark")
                .build();
        String orderId = exchanger.createOrder(order);
        System.out.println("订单id》》》》》" + orderId);
    }

    @Test
    public void getFundingRate() throws IOException {
        String responseStr = HttpClientUtil.get("https://www.okx.com/api/v5/public/funding-rate-history?instId=ETH-USDT-SWAP");
    }

    @Test
    public void testGetKLineData() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2023-06-09 00:00:00");
        Date endTime = sdf.parse("2023-06-09 02:00:00");


        List<KLine> kLineList = exchanger.getKlineData("ETH-USDT-SWAP", startTime, endTime, DataGranularity.FOUR_HOUR);
        System.out.println(JsonUtil.toJSONString(kLineList));
    }
}
