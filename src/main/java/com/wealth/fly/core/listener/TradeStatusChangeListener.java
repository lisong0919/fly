package com.wealth.fly.core.listener;

import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.Trade;
import com.wealth.fly.core.model.Order;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
public interface TradeStatusChangeListener {

    /**
     * @param trade
     * @param openOrder
     */
    void onOpen(Trade trade, Order openOrder);

    /**
     * @param trade
     * @param algoOrder
     * @param closeOrder
     */
    void onClose(Trade trade, Order algoOrder, Order closeOrder);

    /**
     * @param trade
     */
    void onCancel(Trade trade);
}
