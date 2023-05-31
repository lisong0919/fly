package com.wealth.fly.core.model;

import com.wealth.fly.core.constants.ExchangerEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : lisong
 * @date : 2023/5/12
 */
@Data
public class GridStrategy {
    private String instId;
    private BigDecimal minForceClosePrice;
    private ExchangerEnum exchanger;
}
