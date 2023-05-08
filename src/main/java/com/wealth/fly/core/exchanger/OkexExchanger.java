package com.wealth.fly.core.exchanger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.entity.KLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.wealth.fly.core.exception.CancelOrderAlreadyFinishedException;
import com.wealth.fly.core.exception.InsufficientBalanceException;
import com.wealth.fly.core.exception.TPCannotLowerThanMPException;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.model.Order;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class OkexExchanger implements Exchanger {

    @Setter
    @Value("${okex.host}")
    private String host;

    private final String KLINE_PATH = "/api/swap/v3/instruments";
    private final String MARK_PRICE_PATH = "/api/v5/public/mark-price";
    private final String CREATE_ORDER_PATH = "/api/v5/trade/order";
    private final String GET_SINGLE_ORDER_PATH = "/api/v5/trade/order";

    public List<KLine> getKlineData(String currency, Date startTime, Date endTime,
                                    DataGranularity dataGranularity) {
        try {
            String url =
                    host + KLINE_PATH + "/" + currency + "/candles?granularity=" + dataGranularity
                            .getSeconds();

            if (startTime != null) {
                url += "&start=" + transferTimeToOkexStandard(startTime);
            }
            if (endTime != null) {
                url += "&end=" + transferTimeToOkexStandard(endTime);
            }
            String jsonResponse = com.wealth.fly.common.HttpClientUtil.get(url);
            if (StringUtils.isEmpty(jsonResponse)) {
                throw new RuntimeException("获取k线数据失败");
            }
            JSONArray jsonArray = JSONObject.parseArray(jsonResponse);
            List<KLine> kLineList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray data = (JSONArray) jsonArray.get(i);
                Long dateTime = new Long(transferTimeToSystemStandard((String) data.get(0)));

                KLine kLine = new KLine();
                kLine.setDataTime(dateTime);
                kLine.setOpen(new BigDecimal((String) data.get(1)));
                kLine.setHigh(new BigDecimal((String) data.get(2)));
                kLine.setLow(new BigDecimal((String) data.get(3)));
                kLine.setClose(new BigDecimal((String) data.get(4)));
                kLine.setVolume(Long.parseLong((String) data.get(5)));
                kLine.setCurrencyVolume(new BigDecimal((String) data.get(4)));
                kLine.setGranularity(dataGranularity.name());
                kLineList.add(kLine);
            }
            return kLineList;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("获取k线数据失败");
        }

    }

    @Override
    public MarkPrice getMarkPriceByInstId(String instId) throws IOException {
        String url = host + MARK_PRICE_PATH + "?instId=" + instId;

        String res = HttpClientUtil.get(url, "获取标记价格");
        JsonNode jsonNode = JsonUtil.readValue(res);

        checkCode(jsonNode, res);
        MarkPrice markPrice = JsonUtil.getEntity("/data/0", jsonNode, MarkPrice.class);
        if (markPrice != null) {
            long ts = JsonUtil.getLong("/data/0/ts", jsonNode);
            markPrice.setTime(new Date(ts));
        }
        return markPrice;
    }

    @Override
    public String createOrder(Order order) throws IOException {
        String url = host + CREATE_ORDER_PATH;
        String requestBody = JsonUtil.toJSONString(order);

        String response = HttpClientUtil.postBody(url, requestBody, getPostAuthHeaders(CREATE_ORDER_PATH, requestBody), "下单");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);

        //返回订单id
        return JsonUtil.getString("/data/0/ordId", jsonNode);
    }

    @Override
    public void cancelOrder(String instId, String orderId) throws IOException {
        String requestPath = "/api/v5/trade/cancel-order";
        String url = host + requestPath;
        Order order = Order.builder()
                .instId(instId)
                .ordId(orderId)
                .build();
        String requestBody = JsonUtil.toJSONString(order);

        String response = HttpClientUtil.postBody(url, requestBody, getPostAuthHeaders(requestPath, requestBody), "撤单");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);
    }

    @Override
    public String createAlgoOrder(Order order) throws IOException {
        String requestPath = "/api/v5/trade/order-algo";
        String url = host + requestPath;
        String requestBody = JsonUtil.toJSONString(order);

        String response = HttpClientUtil.postBody(url, requestBody, getPostAuthHeaders(requestPath, requestBody), "策略委托下单");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);

        //返回订单id
        return JsonUtil.getString("/data/0/algoId", jsonNode);
    }

    @Override
    public Order getOrder(String instId, String orderId) throws IOException {
        String requestPath = GET_SINGLE_ORDER_PATH + "?instId=" + instId + "&ordId=" + orderId;


        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查订单信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);
        return JsonUtil.getEntity("/data/0", jsonNode, Order.class);
    }

    @Override
    public Order getAlgoOrder(String algoId) throws IOException {
        String requestPath = "/api/v5/trade/order-algo?algoId=" + algoId;

        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查策略委托单信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);
        return JsonUtil.getEntity("/data/0", jsonNode, Order.class);
    }

    public BigDecimal getForceClosePrice(String instId) throws IOException {
        String requestPath = "/api/v5/account/positions?instId=" + instId;
        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查看持仓信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response);
        //TODO 无仓位时会报错
        return new BigDecimal(JsonUtil.getString("/data/0/liqPx", jsonNode));
    }

    private static Map<String, String> getGetAuthHeaders(String requestPath) {
        return getAuthHeaders("GET", requestPath, "");
    }

    private Map<String, String> getPostAuthHeaders(String requestPath, String body) {
        return getAuthHeaders("POST", requestPath, body);
    }

    private static Map<String, String> getAuthHeaders(String method, String requestPath, String body) {

        String secretKey = "BD6FD031EA04652E4F619C100F8306F9";
        String timestamp = getUTCTimestamp();

        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("OK-ACCESS-KEY", "9ca3987e-3f27-4ead-8924-77cccfe0d885");
        authHeaders.put("OK-ACCESS-TIMESTAMP", timestamp);
        authHeaders.put("OK-ACCESS-PASSPHRASE", "5aesyCcvph19ww*iQ98Je*PSHcH6Oi4");

        String toSign = timestamp + method + requestPath + body;

        byte[] signBytes = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey).hmac(toSign.getBytes(StandardCharsets.UTF_8));

        authHeaders.put("OK-ACCESS-SIGN", Base64.encodeBase64String(signBytes));

        return authHeaders;
    }

    private static String getUTCTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    private void checkCode(JsonNode jsonNode, String res) {
        int code = JsonUtil.getInteger("/code", jsonNode);
        if (code != 0) {
            if (code == 1) {
                String codePath = "/data/0/sCode";
                if (JsonUtil.getInteger(codePath, jsonNode) == 51303) {
                    throw new TPCannotLowerThanMPException(JsonUtil.getString("/data/0/sMsg", jsonNode), 51303);
                }
                if (JsonUtil.getInteger(codePath, jsonNode) == 51402) {
                    throw new CancelOrderAlreadyFinishedException(JsonUtil.getString("/data/0/sMsg", jsonNode), 51402);
                }
                if (JsonUtil.getInteger(codePath, jsonNode) == 51008) {
                    throw new InsufficientBalanceException(JsonUtil.getString("/data/0/sMsg", jsonNode), 51008);
                }
            }


            throw new RuntimeException("接口返回码错误,response: " + res);
        }
    }

    private static String transferTimeToSystemStandard(String time) {
        Date date = null;
        try {
            date = new SimpleDateFormat(CommonConstants.OK_DATE_FORMAT).parse(time);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        date = DateUtils.addHours(date, 8);
        return new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(date);
    }

    private static String transferTimeToOkexStandard(Date time) {
        return new SimpleDateFormat(CommonConstants.OK_DATE_FORMAT).format(DateUtils.addHours(time, -8));
    }
}
