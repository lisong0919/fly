package com.wealth.fly.core.exchanger;

import com.fasterxml.jackson.databind.JsonNode;
import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.OrderStatus;
import com.wealth.fly.core.constants.TradeMode;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author : lisong
 * @date : 2023/11/25
 */
@Slf4j
public class BinanceExchanger implements Exchanger {

    private static final String HOST = "https://dapi.binance.com";

    private Account account;
    private Map<String, String> headers = new HashMap<>();

    public BinanceExchanger(Account account) {
        this.account = account;
        headers.put("X-MBX-APIKEY", account.getAccessKey());
        headers.put("Content-Type", "application/x-www-form-urlencoded");
    }

    @Override
    public List<KLine> getKlineData(String instId, Date startTime, Date endTime, DataGranularity dataGranularity) {
        return null;
    }

    @Override
    public List<KLine> getHistoryKlineData(String instId, Date startTime, Date endTime, DataGranularity dataGranularity) {
        return null;
    }


    @Override
    public MarkPrice getMarkPriceByInstId(String instId) throws IOException {

        String url = HOST + "/dapi/v1/premiumIndex?symbol=" + instId;
        String response = HttpClientUtil.get(url, "最新现货指数价格和Mark Price");

        MarkPrice markPrice = new MarkPrice();
        JsonNode jsonNode = JsonUtil.readValue(response);

        markPrice.setMarkPx(JsonUtil.getString("/0/markPrice", jsonNode));
        markPrice.setInstId(instId);

        return markPrice;
    }

    @Override
    public String createOrder(Order order) throws IOException {
        return createOrder(order, order.getOrdType().toUpperCase(), null);
    }


    private String createOrder(Order order, String orderType, String stopPrice) throws IOException {
        String url = HOST + "/dapi/v1/order";
        LinkedHashMap<String, Object> request = new LinkedHashMap<>();
        request.put("symbol", order.getInstId());
        request.put("side", order.getSide().toUpperCase());
        request.put("type", orderType);
        request.put("quantity", order.getSz());
        request.put("price", order.getPx());
        request.put("newClientOrderId", order.getClOrdId());
        request.put("workingType", "MARK_PRICE");
        request.put("timeInForce", "GTC");
        request.put("positionSide", order.getPosSide().toUpperCase());
        if (stopPrice != null) {
            request.put("stopPrice", stopPrice);
        }
        request.put("recvWindow", 10000);
        request.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String response = signAndPostRequest(url, request, "下单");
        log.info("binance下单返回结果：" + response);
        JsonNode jsonNode = JsonUtil.readValue(response);
        return JsonUtil.getString("/orderId", jsonNode);
    }


    private String signAndPostRequest(String url, LinkedHashMap<String, Object> request, String desc) throws IOException {
        String signedRequest = generateSignedRequest(request);

        return HttpClientUtil.postBody(url, signedRequest, headers, desc);
    }

    private String signAndGetRequest(String url, String params, String desc) throws IOException {

        String signedRequest = generateSignedRequest(params);

        return HttpClientUtil.get(url + "?" + signedRequest, headers, desc);
    }

    private String signAndDeleteRequest(String url, String params, String desc) throws IOException {

        String signedRequest = generateSignedRequest(params);

        return HttpClientUtil.delete(url + "?" + signedRequest, headers, desc);
    }

    private String generateSignedRequest(LinkedHashMap<String, Object> request) {
        StringBuilder paramStr = new StringBuilder();
        for (String key : request.keySet()) {
            Object value = request.get(key);
            if (value == null) {
                continue;
            }
            paramStr.append("&").append(key).append("=").append(value);
        }
        String tobeSign = paramStr.substring(1, paramStr.length());

        return generateSignedRequest(tobeSign);
    }

    private String generateSignedRequest(String tobeSign) {
        String sign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, account.getSecretKey()).hmacHex(tobeSign.getBytes(StandardCharsets.UTF_8));

        return tobeSign + "&signature=" + sign;
    }


    @Override
    public void cancelOrder(String instId, String orderId) throws IOException {
        String url = HOST + "/dapi/v1/order";
        String params = "symbol=" + instId + "&orderId=" + orderId + "&timestamp=" + System.currentTimeMillis();

        signAndDeleteRequest(url, params, "取消订单");
    }

    @Override
    public String createAlgoOrder(Order order) throws IOException {
        //TODO 这里写死了止盈限价
        order.setPx(order.getTpOrdPx());
        return createOrder(order, "TAKE_PROFIT", order.getTpTriggerPx());
    }

    @Override
    public Order getOrder(String instId, String orderId) throws IOException {
        return getOrder(instId, orderId, null);
    }

    private Order getOrder(String instId, String orderId, String customerOrderId) throws IOException {
        String url = HOST + "/dapi/v1/order";
        String param = "symbol=" + instId + "&timestamp=" + System.currentTimeMillis();
        if (orderId != null) {
            param += "&orderId=" + orderId;
        } else {
            param += "&origClientOrderId=" + customerOrderId;
        }

        String response = signAndGetRequest(url, param, "查询订单");

        JsonNode jsonNode = JsonUtil.readValue(response);
        Order order = new Order();
        order.setInstId(JsonUtil.getString("/symbol", jsonNode));
        order.setOrdId(JsonUtil.getString("/orderId", jsonNode));
        order.setClOrdId(JsonUtil.getString("/clientOrderId", jsonNode));

        order.setSz(JsonUtil.getString("/origQty", jsonNode));

        order.setAccFillSz(JsonUtil.getString("/executedQty", jsonNode));
        order.setAvgPx(JsonUtil.getString("/avgPrice", jsonNode));
        String status = JsonUtil.getString("/status", jsonNode);
        /**
         *
         *         NEW 新建订单
         *         PARTIALLY_FILLED 部分成交
         *         FILLED 全部成交
         *         CANCELED 已撤销
         *         EXPIRED 订单过期(根据timeInForce参数规则)
         */
        order.setState("NEW".equals(status) ? OrderStatus.LIVE : status.toLowerCase());
        order.setFillTime(JsonUtil.getLong("/updateTime", jsonNode));

        return order;
    }

    @Override
    public Order getOrderByCustomerOrderId(String instId, String customerOrderId) throws IOException {
        return getOrder(instId, null, customerOrderId);
    }

    @Override
    public Order getAlgoOrder(String instId, String algoId) throws IOException {
        Order order = getOrder(instId, algoId, null);
        if (order.getState().equals(OrderStatus.FILLED)) {
            //TODO 兼容ok策略单 待重构
            order.setState("effective");
        }

        return order;
    }

    @Override
    public Order getAlgoOrderByCustomerId(String customerAlgoId) throws IOException {
        return null;
    }

    @Override
    public BigDecimal getForceClosePrice(String instId) throws IOException {
        return null;
    }

    @Override
    public List<InstrumentInfo> listInstrumentInfo(String instType, String instFamily) throws IOException {
        return null;
    }

    @Override
    public MaxOpenSize getMaxOpenSize(String instId, TradeMode tdMode) throws IOException {
        return null;
    }
}
