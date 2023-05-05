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
 * @date : 2023/4/24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Grid {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String instId;

    private Integer strategy;

    private String strategyDesc;

    private String weight;

    private String buyPrice;

    private String sellPrice;

    private String num;

    private Integer status;

    private String buyOrderId;

    private String algoOrderId;

    private Long gridHistoryId;

    private Date createdAt;

    private Date updatedAt;
}
