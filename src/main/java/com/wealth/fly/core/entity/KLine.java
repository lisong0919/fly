package com.wealth.fly.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("kline")
public class KLine {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer currencyId;
    private Long dataTime;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private Long volume;
    private BigDecimal currencyVolume;
    private String granularity;
    private Date createTime;
    private BigDecimal ema12;
    private BigDecimal ema26;
    private BigDecimal dea9;
    private BigDecimal macd;
}