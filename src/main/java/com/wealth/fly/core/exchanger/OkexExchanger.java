package com.wealth.fly.core.exchanger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.wealth.fly.common.HttpClientUtil;
import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.constants.CommonConstants;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.TradeMode;
import com.wealth.fly.core.entity.KLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.wealth.fly.core.exception.CancelOrderAlreadyFinishedException;
import com.wealth.fly.core.exception.InsufficientBalanceException;
import com.wealth.fly.core.exception.TPCannotLowerThanMPException;
import com.wealth.fly.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class OkexExchanger implements Exchanger {

    private static final String host = "https://www.okx.com";
    private static final String KLINE_PATH = "/api/v5/market/candles";
    private static final String MARK_PRICE_PATH = "/api/v5/public/mark-price";
    private static final String CREATE_ORDER_PATH = "/api/v5/trade/order";
    private static final String GET_SINGLE_ORDER_PATH = "/api/v5/trade/order";


    private Account account;

    public OkexExchanger(Account account) {
        this.account = account;
    }

    public List<KLine> getHistoryKlineData(String instId, Date startTime, Date endTime,
                                           DataGranularity dataGranularity) {
        String url =
                host + "/api/v5/market/history-candles?instId=" + instId + "&bar=" + dataGranularity.getKey() + "&limit=300";
        return internalGetKlineData(url, startTime, endTime, dataGranularity);
    }

    public List<KLine> getKlineData(String instId, Date startTime, Date endTime,
                                    DataGranularity dataGranularity) {
        String url =
                host + KLINE_PATH + "?instId=" + instId + "&bar=" + dataGranularity.getKey() + "&limit=300";
        return internalGetKlineData(url, startTime, endTime, dataGranularity);
    }


    private List<KLine> internalGetKlineData(String url, Date startTime, Date endTime,
                                             DataGranularity dataGranularity) {
        try {

            if (startTime != null) {
                url += "&before=" + (startTime.getTime() - 1000);
            }
            if (endTime != null) {
                url += "&after=" + (endTime.getTime() + 1000);
            }
            String jsonResponse = HttpClientUtil.get(url);
            if (StringUtils.isEmpty(jsonResponse)) {
                throw new RuntimeException("获取k线数据失败");
            }

            List<KLine> kLineList = new ArrayList<>();
            JsonNode jsonNode = JsonUtil.readValue(jsonResponse);
            checkCode(jsonNode, jsonResponse, url, null);

            JSONObject jsonObject = JSON.parseObject(jsonResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray data = (JSONArray) jsonArray.get(i);

                if ("0".equals(data.get(8).toString())) {
                    continue;
                }

                KLine kLine = new KLine();
                Long dateTime = new Long(transferTimeToSystemStandard((String) data.get(0)));
                kLine.setDataTime(dateTime);
                kLine.setOpen(new BigDecimal((String) data.get(1)));
                kLine.setHigh(new BigDecimal((String) data.get(2)));
                kLine.setLow(new BigDecimal((String) data.get(3)));
                kLine.setClose(new BigDecimal((String) data.get(4)));
                kLine.setVolume(new BigDecimal((String) data.get(5)).longValue());

                kLine.setCurrencyVolume(new BigDecimal((String) data.get(6)));
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

        checkCode(jsonNode, res, url, null);
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
        checkCode(jsonNode, response, url, requestBody);

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
        checkCode(jsonNode, response, url, requestBody);
    }

    @Override
    public String createAlgoOrder(Order order) throws IOException {
        String requestPath = "/api/v5/trade/order-algo";
        String url = host + requestPath;
        String requestBody = JsonUtil.toJSONString(order);

        String response = HttpClientUtil.postBody(url, requestBody, getPostAuthHeaders(requestPath, requestBody), "策略委托下单");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, url, requestBody);

        //返回订单id
        return JsonUtil.getString("/data/0/algoId", jsonNode);
    }

    @Override
    public Order getOrder(String instId, String orderId) throws IOException {
        return getOrder(instId, orderId, null);
    }

    @Override
    public Order getOrderByCustomerOrderId(String instId, String customerOrderId) throws IOException {
        return getOrder(instId, null, customerOrderId);
    }

    private Order getOrder(String instId, String orderId, String customerOrderId) throws IOException {
        String requestPath = GET_SINGLE_ORDER_PATH + "?instId=" + instId;
        if (!StringUtils.isEmpty(orderId)) {
            requestPath += "&ordId=" + orderId;
        }
        if (!StringUtils.isEmpty(customerOrderId)) {
            requestPath += "&clOrdId=" + customerOrderId;
        }

        String url = host + requestPath;
        String response = HttpClientUtil.get(url, getGetAuthHeaders(requestPath), "查订单信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, url, null);
        return JsonUtil.getEntity("/data/0", jsonNode, Order.class);
    }

    public Order getAlgoOrderByCustomerId(String customerAlgoId) throws IOException {
        String requestPath = "/api/v5/trade/order-algo?algoClOrdId=" + customerAlgoId;

        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查策略委托单信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, host + requestPath, null);
        return JsonUtil.getEntity("/data/0", jsonNode, Order.class);
    }

    @Override
    public Order getAlgoOrder(String instId, String algoId) throws IOException {
        String requestPath = "/api/v5/trade/order-algo?algoId=" + algoId;

        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查策略委托单信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, host + requestPath, null);
        return JsonUtil.getEntity("/data/0", jsonNode, Order.class);
    }

    public BigDecimal getForceClosePrice(String instId) throws IOException {
        String requestPath = "/api/v5/account/positions?instId=" + instId;
        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "查看持仓信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, host + requestPath, null);

        List<AccountPosition> accountPositions = JsonUtil.getEntityList("/data", jsonNode, AccountPosition.class);

        if (CollectionUtils.isEmpty(accountPositions)) {
            return new BigDecimal("0");
        }

        for (AccountPosition accountPosition : accountPositions) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(accountPosition.getLiqPx()) && accountPosition.getPos() > 0) {
                return new BigDecimal(accountPosition.getLiqPx());
            }
        }

        return null;
    }

    public List<InstrumentInfo> listInstrumentInfo(String instType, String instFamily) throws IOException {
        String requestPath = "/api/v5/public/instruments?instType=" + instType;
        if (StringUtils.isNotBlank(instFamily)) {
            requestPath += "&instFamily=" + instFamily;
        }
        String response = HttpClientUtil.get(host + requestPath, null, "获取交易产品基础信息");

        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, host + requestPath, null);

        return JsonUtil.getEntityList("/data", jsonNode, InstrumentInfo.class);
    }

    @Override
    public MaxOpenSize getMaxOpenSize(String instId, TradeMode tdMode) throws IOException {
        String requestPath = "/api/v5/account/max-size?instId=" + instId + "&tdMode=" + tdMode.name();
        String response = HttpClientUtil.get(host + requestPath, getGetAuthHeaders(requestPath), "获取最大可开仓数量");
        JsonNode jsonNode = JsonUtil.readValue(response);
        checkCode(jsonNode, response, host + requestPath, null);

        return JsonUtil.getEntity("/data/0", jsonNode, MaxOpenSize.class);
    }

    @Override
    public List<FundingRate> listFundingRateHistory(String symbol, Date startTime, Date endTime, Integer limit) throws IOException {
        return null;
    }


    private Map<String, String> getGetAuthHeaders(String requestPath) {
        return getAuthHeaders("GET", requestPath, "");
    }

    private Map<String, String> getPostAuthHeaders(String requestPath, String body) {
        return getAuthHeaders("POST", requestPath, body);
    }

    private Map<String, String> getAuthHeaders(String method, String requestPath, String body) {

        String secretKey = account.getSecretKey();
        String timestamp = getUTCTimestamp();

        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("OK-ACCESS-KEY", account.getAccessKey());
        authHeaders.put("OK-ACCESS-TIMESTAMP", timestamp);
        authHeaders.put("OK-ACCESS-PASSPHRASE", account.getPassphrase());

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

    private void checkCode(JsonNode jsonNode, String res, String url, String requestBody) {
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

            throw new RuntimeException("接口返回码错误,url:" + url + ",requestBody:" + requestBody + ",response: " + res);
        }
    }

    private static String transferTimeToSystemStandard(String timeStamp) {
        return new SimpleDateFormat(CommonConstants.SYSTEM_DATE_FORMAT).format(new Date(Long.parseLong(timeStamp)));
    }

    private static String transferTimeToOkexStandard(Date time) {
        return new SimpleDateFormat(CommonConstants.OK_DATE_FORMAT).format(DateUtils.addHours(time, -8));
    }
}
