package com.wealth.fly.core.config;

import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.dao.ConfigDao;
import com.wealth.fly.core.entity.Config;
import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.GoldForkStrategy;
import com.wealth.fly.core.model.GridStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : lisong
 * @date : 2023/5/11
 */
@Component
public class ConfigServiceImpl implements ConfigService {
    @Resource
    private ConfigDao configDao;

    @Override
    public List<Integer> getActiveGridStrategies() {
        Config config = configDao.getConfigByKey("grid.active.strategies");
        if (StringUtils.isBlank(config.getValue())) {
            return Collections.emptyList();
        }
        return Arrays.stream(config.getValue().split(",")).map(Integer::valueOf).collect(Collectors.toList());
    }

    @Override
    public List<GoldForkStrategy> getActiveGoldForkStrategies() {
        Config config = configDao.getConfigByKey("goldfork.active.strategies");
        if (StringUtils.isBlank(config.getValue())) {
            return Collections.emptyList();
        }

        List<GoldForkStrategy> res = new ArrayList<>();
        for (String strategyId : config.getValue().split(",")) {
            GoldForkStrategy goldForkStrategy = getGoldForkStrategy(strategyId);
            if (goldForkStrategy != null) {
                res.add(goldForkStrategy);
            }
        }
        return res;
    }

    @Override
    public Account getAccount(String accountId) {
        Config config = configDao.getConfigByKey(accountId);

        return JsonUtil.readValue(config.getValue(), Account.class);
    }

    @Override
    public GridStrategy getGridStrategy(Integer strategyId) {
        Config config = configDao.getConfigByKey("grid.strategy." + strategyId);
        GridStrategy gridStrategy = JsonUtil.readValue(config.getValue(), GridStrategy.class);
        gridStrategy.setId(strategyId);
        return gridStrategy;
    }

    @Override
    public GoldForkStrategy getGoldForkStrategy(String strategyId) {
        Config config = configDao.getConfigByKey("goldfork.strategy." + strategyId);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            return null;
        }
        GoldForkStrategy goldForkStrategy = JsonUtil.readValue(config.getValue(), GoldForkStrategy.class);
        goldForkStrategy.setId(strategyId);
        return goldForkStrategy;
    }
}
