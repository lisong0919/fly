package com.wealth.fly.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author : lisong
 * @date : 2022/10/10
 */
@Slf4j
public class JsonUtil {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    static {
        DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //不认识的属性不报错
        DEFAULT_OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);//空json对象序列化不报错
    }


    public static String toJSONString(Object obj) {
        try {
            return DEFAULT_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析json失败，原值：" + obj);
        }
    }

    public static JsonNode readValue(String jsonString) {
        try {
            return DEFAULT_OBJECT_MAPPER.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析json失败，原值：" + jsonString);
        }
    }

    public static <T> T readValue(String jsonString, Class<T> valueType) {
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(jsonString, valueType);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析json失败，原值：" + jsonString);
        }
    }

    /**
     * @param path     https://www.rfc-editor.org/rfc/rfc6901
     * @param jsonNode
     * @return
     */
    public static String getString(String path, JsonNode jsonNode) {
        return jsonNode.at(path).asText();
    }

    /**
     * @param path     https://www.rfc-editor.org/rfc/rfc6901
     * @param jsonNode
     * @return
     */
    public static Long getLong(String path, JsonNode jsonNode) {
        return jsonNode.at(path).asLong();
    }

    /**
     * @param path     https://www.rfc-editor.org/rfc/rfc6901
     * @param jsonNode
     * @return
     */
    public static Integer getInteger(String path, JsonNode jsonNode) {
        return jsonNode.at(path).asInt();
    }

    public static BigDecimal getBigDecimal(String path, JsonNode jsonNode) {
        return new BigDecimal(jsonNode.at(path).asText());
    }

    public static <T> T getEntity(String path, JsonNode jsonNode, Class<T> tClass) {
        String text = jsonNode.at(path).toPrettyString();
        return readValue(text, tClass);
    }


    public static <T> List<T> getEntityList(String path, JsonNode jsonNode, Class<T> tClass) {
        Iterator<JsonNode> iterator = jsonNode.at(path).elements();

        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            list.add(readValue(node.toPrettyString(), tClass));
        }
        return list;
    }


    public static void main(String[] args) {
        String str = "{\"code\":\"0\",\"data\":[{\"accFillSz\":\"2\",\"algoClOrdId\":\"\",\"algoId\":\"\",\"avgPx\":\"1824.37\",\"cTime\":\"1682412675507\",\"cancelSource\":\"\",\"cancelSourceReason\":\"\",\"category\":\"normal\",\"ccy\":\"\",\"clOrdId\":\"\",\"fee\":\"-0.0000054813442449\",\"feeCcy\":\"ETH\",\"fillPx\":\"1824.37\",\"fillSz\":\"2\",\"fillTime\":\"1682412675508\",\"instId\":\"ETH-USD-230630\",\"instType\":\"FUTURES\",\"lever\":\"2\",\"ordId\":\"571015379842478080\",\"ordType\":\"limit\",\"pnl\":\"0\",\"posSide\":\"long\",\"px\":\"1825.2\",\"quickMgnType\":\"\",\"rebate\":\"0\",\"rebateCcy\":\"ETH\",\"reduceOnly\":\"false\",\"side\":\"buy\",\"slOrdPx\":\"\",\"slTriggerPx\":\"\",\"slTriggerPxType\":\"\",\"source\":\"\",\"state\":\"filled\",\"sz\":\"2\",\"tag\":\"12345\",\"tdMode\":\"cross\",\"tgtCcy\":\"\",\"tpOrdPx\":\"2000\",\"tpTriggerPx\":\"2000\",\"tpTriggerPxType\":\"mark\",\"tradeId\":\"3236034\",\"uTime\":\"1682412675510\"}],\"msg\":\"\"}";
        JsonNode jsonNode = JsonUtil.readValue(str);
        System.out.println(JsonUtil.getEntityList("/data", jsonNode, Order.class));
    }
}
