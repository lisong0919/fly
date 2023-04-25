package com.wealth.fly.core.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.mapper.GridMapper;
import com.wealth.fly.core.entity.Grid;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Repository
public class GridDaoImpl extends ServiceImpl<GridMapper, Grid> implements GridDao {

    @Override
    public List<Grid> listGrids(String instId, Integer status, BigDecimal maxBuyPrice, int limit) {
        LambdaQueryWrapper<Grid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grid::getStatus, status);
        wrapper.eq(Grid::getInstId, instId);
        wrapper.lt(Grid::getBuyPrice, maxBuyPrice);
        wrapper.orderByDesc(Grid::getBuyPrice);
        if (limit != -1) {
            wrapper.last("limit " + limit);
        }

        return list(wrapper);
    }

    @Override
    public List<Grid> listByStatus(String instId, List<Integer> statusList) {
        LambdaQueryWrapper<Grid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grid::getInstId, instId);
        wrapper.in(Grid::getStatus, statusList);
        return list(wrapper);
    }

    @Override
    public void updateGridStatus(Integer id, int status) {
        updateById(Grid.builder().id(id).status(status).build());
    }

    @Override
    public void updateGridActive(int id, String sellOrderId, long gridHistoryId) {
        Grid grid = Grid.builder()
                .id(id)
                .status(GridStatus.ACTIVE.getCode())
                .sellOrderId(sellOrderId)
                .gridHistoryId(gridHistoryId)
                .build();
        updateById(grid);
    }

    @Override
    public void updateGridFinished(int id) {
        Grid grid = Grid.builder()
                .id(id)
                .status(GridStatus.IDLE.getCode())
                .sellOrderId("")
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
