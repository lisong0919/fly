package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.constants.TradeStatus;
import com.wealth.fly.core.dao.TradeDao;
import com.wealth.fly.core.dao.mapper.TradeMapper;
import com.wealth.fly.core.entity.Trade;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/7/5
 */
@Repository
public class TradeDaoImpl extends ServiceImpl<TradeMapper, Trade> implements TradeDao {

    @Override
    public Trade getProcessingTrade(String strategy) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Trade::getStatus, Arrays.asList(TradeStatus.PENDING.getCode(), TradeStatus.OPENED.getCode()));
        queryWrapper.eq(Trade::getStrategy, strategy);
        queryWrapper.orderByDesc(Trade::getId);
        queryWrapper.last("limit 1");

        return baseMapper.selectOne(queryWrapper);
    }

    public boolean save(Trade entity) {
        return baseMapper.insert(entity) >= 1;
    }

    @Override
    public List<Trade> listByStatus(Collection<Integer> statusList) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Trade::getStatus, statusList);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateById(Trade trade) {
        return retBool(baseMapper.updateById(trade));
    }
}
