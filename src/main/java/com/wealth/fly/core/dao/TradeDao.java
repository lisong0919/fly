package com.wealth.fly.core.dao;

import com.wealth.fly.core.entity.Trade;

import java.util.Collection;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
public interface TradeDao {
    Trade getProcessingTrade(String strategy);

    boolean save(Trade trade);

    List<Trade> listByStatus(Collection<Integer> statusList);

    boolean updateById(Trade trade);
}
