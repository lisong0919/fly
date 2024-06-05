package com.wealth.fly.core.exchanger;

import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.constants.TradeMode;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public interface Exchanger {

    List<KLine> getKlineData(String instId, Date startTime, Date endTime, DataGranularity dataGranularity);

    List<KLine> getHistoryKlineData(String instId, Date startTime, Date endTime,
                                    DataGranularity dataGranularity);

    MarkPrice getMarkPriceByInstId(String instId) throws IOException;

    String createOrder(Order order) throws IOException;

    void cancelOrder(String instId, String orderId) throws IOException;

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

    Order getOrderByCustomerOrderId(String instId, String customerOrderId) throws IOException;


    /**
     * 查策略委托单信息
     *
     * @param instId
     * @param algoId
     * @return
     * @throws IOException
     */
    Order getAlgoOrder(String instId, String algoId) throws IOException;

    /**
     * 查策略委托单信息
     *
     * @param customerAlgoId
     * @return
     * @throws IOException
     */
    Order getAlgoOrderByCustomerId(String customerAlgoId) throws IOException;

    /**
     * 查询强平价格
     *
     * @param instId
     * @return
     * @throws Exception
     */
    BigDecimal getForceClosePrice(String instId) throws IOException;

    /**
     * 获取交易产品基础信息
     *
     * @param instType
     * @param instFamily
     * @return
     * @throws IOException
     */
    List<InstrumentInfo> listInstrumentInfo(String instType, String instFamily) throws IOException;

    /**
     * 获取最大可开仓信息
     *
     * @param instId
     * @param tdMode
     * @return
     */
    MaxOpenSize getMaxOpenSize(String instId, TradeMode tdMode) throws IOException;


    List<FundingRate> listFundingRateHistory(String symbol, Date startTime, Date endTime, Integer limit) throws IOException;

}
