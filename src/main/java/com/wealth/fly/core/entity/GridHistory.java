package com.wealth.fly.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GridHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer gridId;
    private String instId;
    private String buyOrderId;
    private String sellOrderId;
    private String algoOrderId;

    /**
     * 数量
     */
    private String num;

    /**
     * 金额
     */
    private String usdtAmount;

    /**
     * 币的数量
     */
    private String currencyAmount;

    private String buyPrice;

    private String sellPrice;

    private String buyFee;

    private String sellFee;

    private String feeCcy;

    private String orderProfit;

    private String gridProfit;

    private String orderProfitPercent;

    private String gridProfitPercent;

    private Date pendingTime;

    private Date buyTime;

    private Date sellTime;

    private Date createdAt;

    private Date updatedAt;
}
