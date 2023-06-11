package com.wealth.fly.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DataGranularity {
    ONE_MINUTE(60,"1m"),
    THREE_MINUTES(180,"3m"),
    FIVE_MINUTES(300,"5m"),
    FIFTEEN_MINUTES(900,"15m"),
    THIRTY_MINUTES(1800,"30m"),
    ONE_HOUR(3600,"1H"),
    TWO_HOUR(7200,"2H"),
    FOUR_HOUR(14400,"4H"),
//    SIX_HOUR(21600),
//    TWELVE_HOUR(43200),
//    ONE_DAY(86400),
//    ONE_WEEK(604800)
    ;


    private int seconds;
    private String key;

}
