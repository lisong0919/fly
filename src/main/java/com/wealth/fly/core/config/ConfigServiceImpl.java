package com.wealth.fly.core.config;

import com.wealth.fly.common.JsonUtil;
import com.wealth.fly.core.dao.ConfigDao;
import com.wealth.fly.core.entity.Config;
import com.wealth.fly.core.model.Account;
import com.wealth.fly.core.model.GridStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
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
            return null;
        }
        return Arrays.stream(config.getValue().split(",")).map(Integer::valueOf).collect(Collectors.toList());
    }

    @Override
    public Account getAccount(String accountId) {
        Config config = configDao.getConfigByKey(accountId);

        return JsonUtil.readValue(config.getValue(), Account.class);
    }

    @Override
    public GridStrategy getGridStrategy(Integer strategyId) {
        Config config = configDao.getConfigByKey("grid.strategy." + strategyId);
        GridStrategy gridStrategy= JsonUtil.readValue(config.getValue(), GridStrategy.class);
        gridStrategy.setId(strategyId);
        return gridStrategy;
    }
}
