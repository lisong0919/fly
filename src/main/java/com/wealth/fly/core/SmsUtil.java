package com.wealth.fly.core;

import com.ucpaas.restDemo.client.JsonReqClient;

public class SmsUtil {
    private static JsonReqClient jsonReqClient=new JsonReqClient();

    public static void sendOpenStockSms(String closePrice) {
        String sid = "a93319e7d9bd72c8d0041448d658a4a8";
        String token = "94a282b83325dc612114e4ff2264178f";
        String appid = "36c2c513a615444b9e9f1ec9e5175819";
        String templateid = "67283";
        String mobile = "15501162595";

        jsonReqClient.sendSms(sid, token, appid, templateid, closePrice, mobile, null);
    }
}
