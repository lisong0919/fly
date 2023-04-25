package com.wealth.fly.core.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.fly.core.entity.Grid;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
public interface GridDao extends IService<Grid> {
    List<Grid> listGrids(String instId, Integer status, BigDecimal maxBuyPrice, int limit);

    List<Grid> listByStatus(String instId, List<Integer> statusList);

    void updateGridStatus(Integer id, int status);

    void updateGridActive(int id, String sellOrderId,long gridHistoryId);

    void updateGridFinished(int id);

    void updateOrderId(Integer id, String orderId);
}
