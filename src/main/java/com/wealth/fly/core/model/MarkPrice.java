package com.wealth.fly.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Data
public class MarkPrice {
    private String instId;
    private String instType;
    private String markPx;
    private Date time;
}
