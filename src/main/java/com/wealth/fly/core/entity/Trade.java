package com.wealth.fly.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trade {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String strategy;

    private String instId;

    private Integer status;


    private BigDecimal planOpenSize;


    private BigDecimal planOpenPrice;

    private BigDecimal actualOpenPrice;

    private BigDecimal actualClosePrice;

    /**
     * 收益
     */
    private BigDecimal profit;


    /**
     * 交易方向
     * long 或 short
     */
    private String posSide;

    /**
     * 止盈触发价
     */
    private BigDecimal triggerWinPrice;

    /**
     * 止盈委托价
     */
    private BigDecimal planWinPrice;

    /**
     * 止损触发价
     */
    private BigDecimal triggerMissPrice;

    /**
     * 止损委托价
     */
    private BigDecimal planMissPrice;

    private String openOrderId;

    private String closeOrderId;

    /**
     * 止盈止损策略订单号
     */
    private String algoOrderId;


    /**
     * 开仓手续费币种
     */
    private String openFeeCcy;

    /**
     * 开仓手续费
     */
    private BigDecimal openFee;

    /**
     * 平仓手续费币种
     */
    private String closeFeeCcy;

    /**
     * 平仓手续费
     */
    private BigDecimal closeFee;

    private Date openTime;

    private Date closeTime;

    private Date cancelTime;

    private Date createdAt;

    private Date updatedAt;
}
