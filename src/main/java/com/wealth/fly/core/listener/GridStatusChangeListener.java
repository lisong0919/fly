package com.wealth.fly.core.listener;

import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.model.Order;

/**
 * @author : lisong
 * @date : 2023/4/25
 */
public interface GridStatusChangeListener {
    /**
     * 网格激活
     *
     * @param grid
     * @param buyOrder
     */
    void onActive(Grid grid, Order buyOrder);


    /**
     * 网格已完成
     *
     * @param grid
     * @param algoOrder
     * @param sellOrder
     */
    void onFinished(Grid grid, Order algoOrder, Order sellOrder);

    /**
     * 网格委托买单撤销
     *
     * @param grid
     * @param buyOrder
     */
    void onCancel(Grid grid, Order buyOrder);

}
