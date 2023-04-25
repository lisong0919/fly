package com.wealth.fly.core.listener;

import com.wealth.fly.core.model.MarkPrice;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
public interface MarkPriceListener {
    void onNewMarkPrice(MarkPrice markPrice);
}
