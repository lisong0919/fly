package com.wealth.fly.core.dao;

import com.wealth.fly.core.entity.Config;

/**
 * @author : lisong
 * @date : 2023/5/6
 */
public interface ConfigDao {
    Config getConfigByKey(String key);
}
