package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.mapper.GridMapper;
import com.wealth.fly.core.entity.Grid;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Repository
public class GridDaoImpl extends ServiceImpl<GridMapper, Grid> implements GridDao {

    @Override
    public boolean save(Grid entity) {
        return retBool(baseMapper.insert(entity));
    }

    @Override
    public List<Grid> listGrids(Integer strategy, BigDecimal maxBuyPrice, int limit) {
        LambdaQueryWrapper<Grid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grid::getStrategy, strategy);
        wrapper.lt(Grid::getBuyPrice, maxBuyPrice);
        wrapper.orderByDesc(Grid::getBuyPrice);
        if (limit != -1) {
            wrapper.last("limit " + limit);
        }

        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Grid> listByStatusOrderByBuyPrice(List<Integer> statusList, Integer strategyId, int limit) {
        LambdaQueryWrapper<Grid> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Grid::getStatus, statusList);
        wrapper.orderByAsc(Grid::getBuyPrice);
        if (strategyId != null) {
            wrapper.eq(Grid::getStrategy, strategyId);
        }
        if (limit != -1) {
            wrapper.last("limit " + limit);
        }

        return baseMapper.selectList(wrapper);
    }

    @Override
    public void updateGridStatus(Integer id, int status) {
        updateById(Grid.builder().id(id).status(status).build());
    }

    @Override
    public void updateGridActive(int id, String algoOrderId, long gridHistoryId) {
        Grid grid = Grid.builder()
                .id(id)
                .status(GridStatus.ACTIVE.getCode())
                .algoOrderId(algoOrderId)
                .gridHistoryId(gridHistoryId)
                .build();
        updateById(grid);
    }

    @Override
    public void updateGridFinished(int id) {
        Grid grid = Grid.builder()
                .id(id)
                .status(GridStatus.IDLE.getCode())
                .algoOrderId("")
                .buyOrderId("")
                .gridHistoryId(0L)
                .build();
        updateById(grid);
    }

    @Override
    public void updateOrderId(Integer id, String orderId) {
        updateById(Grid.builder().id(id).buyOrderId(orderId).build());
    }
}
