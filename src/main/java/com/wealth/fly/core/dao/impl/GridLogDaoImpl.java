package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.dao.mapper.GridLogMapper;
import com.wealth.fly.core.entity.GridHistory;
import com.wealth.fly.core.entity.GridLog;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/26
 */
@Repository
public class GridLogDaoImpl extends ServiceImpl<GridLogMapper, GridLog> implements GridLogDao {
    @Override
    public boolean save(GridLog entity) {
        return retBool(baseMapper.insert(entity));
    }

    @Override
    public List<GridLog> listRecentLogs(Integer strategyId, int limit) {
        LambdaQueryWrapper<GridLog> wrapper = new LambdaQueryWrapper<>();
        if (strategyId != null) {
            wrapper.eq(GridLog::getStrategyId, strategyId);
        }
        wrapper.orderByDesc(GridLog::getId);
        wrapper.last("limit " + limit);
        return baseMapper.selectList(wrapper);
    }
}
