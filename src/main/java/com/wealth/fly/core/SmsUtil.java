package com.wealth.fly.core;

import com.ucpaas.restDemo.client.JsonReqClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsUtil {
    private static JsonReqClient jsonReqClient = new JsonReqClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsUtil.class);

    public static void sendOpenStockSms(String closePrice) {
        String sid = "a93319e7d9bd72c8d0041448d658a4a8";
        String token = "94a282b83325dc612114e4ff2264178f";
        String appid = "36c2c513a615444b9e9f1ec9e5175819";
        String templateid = "67283";
        String mobile = "15501162595";

        String responseStr = jsonReqClient.sendSms(sid, token, appid, templateid, closePrice, mobile, null);
        LOGGER.info("send sms response is " + responseStr);
    }

    public static void sendGridSms(String msg){
        String sid = "a93319e7d9bd72c8d0041448d658a4a8";
        String token = "94a282b83325dc612114e4ff2264178f";
        String appid = "36c2c513a615444b9e9f1ec9e5175819";
        String templateid = "67283";
        String mobile = "15501162595";

        String responseStr = jsonReqClient.sendSms(sid, token, appid, templateid, msg, mobile, null);
        LOGGER.info("send sms response is " + responseStr);
    }
}
