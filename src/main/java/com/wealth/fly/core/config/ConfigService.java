package com.wealth.fly.core.config;

import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.GoldForkStrategy;
import com.wealth.fly.core.model.GridStrategy;

import java.util.List;

/**
 * @author : lisong
 * @date : 2023/5/11
 */
public interface ConfigService {
    List<Integer> getActiveGridStrategyIds();

    List<GoldForkStrategy> getActiveGoldForkStrategies();

    Account getAccount(String accountId);

    GridStrategy getGridStrategy(Integer strategyId);

    GoldForkStrategy getGoldForkStrategy(String strategyId);

    List<GridStrategy> getActiveGridStrategies();
}
