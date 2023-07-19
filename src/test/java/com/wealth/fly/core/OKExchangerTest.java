package com.wealth.fly.core;

import com.wealth.fly.FlyTestApplication;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.OkexExchanger;
import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.InstrumentInfo;
import com.wealth.fly.core.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public class OKExchangerTest {

    private OkexExchanger exchanger;

    @BeforeEach
    public void init() {
        Account account = new Account();
        account.setAccessKey("0d62d81d-b736-4136-9b16-21a262e5a50b");
        account.setSecretKey("4EA4B3F8D39EBBA03695C980232A957D");
        account.setPassphrase("AiqGdD1fBYl981NbGv*TjWWLILINsn0y");
        exchanger = new OkexExchanger(account);
    }

    @Test
    public void testGetOrder() throws IOException {
        System.out.println(exchanger.getOrder("ETH-USD-230630", "583658033688322050"));
    }

    @Test
    public void testGetAlgoOrder() throws IOException {
        System.out.println(exchanger.getAlgoOrder("583643121104691203"));
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
    public void listInstrumentInfo() throws IOException {
        List<InstrumentInfo> instrumentInfoList = exchanger.listInstrumentInfo("OPTION", "ETH-USD");
        System.out.println(JsonUtil.toJSONString(instrumentInfoList));
    }

    @Test
    public void getFundingRate() throws IOException {
        String responseStr = HttpClientUtil.get("https://www.okx.com/api/v5/public/funding-rate-history?instId=ETH-USDT-SWAP");
    }

    @Test
    public void testGetKLineData() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2023-07-19 10:00:00");
        Date endTime = sdf.parse("2023-07-19 20:00:00");

        List<KLine> kLineList = exchanger.getHistoryKlineData("BTC-USDT-SWAP", startTime, endTime, DataGranularity.FOUR_HOUR);
        System.out.println(JsonUtil.toJSONString(kLineList));
    }

    @Test
    public void testGetForceClosePrice() throws IOException {
        System.out.println(exchanger.getForceClosePrice("ETH-USD-230929"));
    }
}
