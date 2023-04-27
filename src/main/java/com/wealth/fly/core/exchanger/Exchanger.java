package com.wealth.fly.core.exchanger;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.model.MarkPrice;
import com.wealth.fly.core.model.Order;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public interface Exchanger {

    List<KLine> getKlineData(String currency, Date startTime, Date endTime, DataGranularity dataGranularity);

    MarkPrice getMarkPriceByInstId(String instId) throws IOException;

    String createOrder(Order order) throws IOException;

    /**
     * 策略委托下单
     *
     * @param order
     * @return
     * @throws IOException
     */
    String createAlgoOrder(Order order) throws IOException;

    /**
     * 查订单
     *
     * @param instId
     * @param orderId
     * @return
     * @throws IOException
     */
    Order getOrder(String instId, String orderId) throws IOException;


    /**
     * 查策略委托单信息
     *
     * @param algoId
     * @return
     * @throws IOException
     */
    Order getAlgoOrder(String algoId) throws IOException;


    /**
     * 查询强平价格
     *
     * @param instId
     * @return
     * @throws Exception
     */
    BigDecimal getForceClosePrice(String instId) throws IOException;
}
