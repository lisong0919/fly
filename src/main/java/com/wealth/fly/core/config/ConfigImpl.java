package com.wealth.fly.core.config;

import com.wealth.fly.core.dao.ConfigDao;
import com.wealth.fly.core.entity.Config;
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
public class ConfigImpl implements IConfig {
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
}
