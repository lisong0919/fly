package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.dao.ConfigDao;
import com.wealth.fly.core.dao.mapper.ConfigMapper;
import com.wealth.fly.core.entity.Config;
import org.springframework.stereotype.Repository;

/**
 * @author : lisong
 * @date : 2023/5/6
 */
@Repository
public class ConfigDaoImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigDao {
    @Override
    public Config getConfigByKey(String key) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, key);
        wrapper.orderByDesc(Config::getUpdatedAt);
        wrapper.last("limit 1");

        return getBaseMapper().selectOne(wrapper);
    }
}
