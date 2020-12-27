package com.wealth.fly.core.constants;

import java.math.BigDecimal;

public class CommonConstants {
    public static final String SYSTEM_DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String OK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String LAST_KLINE_PARAM = "LAST_KLINE_PARAM";



    public static final int DEFAULT_LAST_LINE_SIZE = 2;
    public static final int DEFAULT_MA_PRICE_NUM = 60;
    public static final int DEFAULT_MA_VOLUME_NUM = 10;
    public static final int DEFAULT_MA_MACD_NUM = 10;


    public static final String WIN_PERCENT = "0.025";
    public static final String MISS_PERCENT = "0.01";
    public static final BigDecimal MAX_AMPLITUDE = new BigDecimal("0.04");


    public static final BigDecimal PROFIT_PERCENT = new BigDecimal("0.04");
    public static final BigDecimal FLOAT_PERCENT = new BigDecimal("0.001");


    //新策略
    public static final DataGranularity DEFAULT_DATA_GRANULARITY = DataGranularity.FIFTEEN_MINUTES;
    public static final int MACD_LAST_LINE_SIZE = 7;
    public static final int MACD_MA_NUM = 20;
    public static final String MACD_WIN_PERCENT = "0.003";
    public static final String MACD_MISS_PERCENT = "0.02";

}