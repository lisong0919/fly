package com.wealth.fly.core.exchanger;

import com.wealth.fly.FlyApplication;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.GoldForkStrategy;
import com.wealth.fly.core.model.GridStrategy;

import java.util.HashMap;

/**
 * @author : lisong
 * @date : 2023/6/16
 */
public class ExchangerManager {
    private static HashMap<String, Exchanger> exchangerMap = new HashMap<>();
    private static ConfigService configService;

    public static Exchanger getExchangerByGridStrategy(Integer strategyId) {
        GridStrategy gridStrategy = getConfigService().getGridStrategy(strategyId);
        return getExchangerByAccountId(gridStrategy.getAccount());
    }

    public static Exchanger getExchangerByGoldForkStrategy(String strategyId) {
        GoldForkStrategy strategy = getConfigService().getGoldForkStrategy(strategyId);
        return getExchangerByAccountId(strategy.getAccount());
    }

    public static Exchanger getExchangerByAccountId(String accountId) {
        Exchanger exchanger = exchangerMap.get(accountId);
        if (exchanger == null) {
            synchronized (accountId) {
                Account account = getConfigService().getAccount(accountId);
                exchanger = new OkexExchanger(account);
            }
        }
        return exchanger;
    }

    public static ConfigService getConfigService() {
        if (configService == null) {
            configService = FlyApplication.getApplicationContext().getBean(ConfigService.class);
        }
        return configService;
    }
}
