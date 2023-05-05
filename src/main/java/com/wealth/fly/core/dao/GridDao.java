package com.wealth.fly.core.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.fly.core.entity.Grid;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
public interface GridDao{
    boolean save(Grid entity);

    List<Grid> listGrids(Integer strategy, BigDecimal maxBuyPrice, int limit);

    List<Grid> listByStatus(List<Integer> statusList);

    void updateGridStatus(Integer id, int status);

    void updateGridActive(int id, String algoOrderId,long gridHistoryId);

    void updateGridFinished(int id);

    void updateOrderId(Integer id, String orderId);
}
