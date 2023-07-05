package com.wealth.fly.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lisong
 * @date : 2023/4/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    private String instId;

    private String ordId;

    private String clOrdId;
    /**
     * 交易模式
     * 保证金模式：isolated：逐仓 ；cross：全仓
     * 非保证金模式：cash：非保证金
     */
    private String tdMode;

    /**
     * 保证金币种，仅适用于单币种保证金模式下的全仓币币杠杆订单
     */
    private String ccy;

    /**
     * 订单方向
     * buy：买， sell：卖
     */
    private String side;

    /**
     * 持仓方向，单向持仓模式下此参数非必填，如果填写仅可以选择net；在双向持仓模式下必填，且仅可选择 long 或 short。
     * 双向持仓模式下，side和posSide需要进行组合
     * 开多：买入开多（side 填写 buy； posSide 填写 long ）
     * 开空：卖出开空（side 填写 sell； posSide 填写 short ）
     * 平多：卖出平多（side 填写 sell；posSide 填写 long ）
     * 平空：买入平空（side 填写 buy； posSide 填写 short ）
     */
    private String posSide;

    /**
     * 普通订单类型
     * market：市价单
     * limit：限价单
     * post_only：只做maker单
     * fok：全部成交或立即取消
     * ioc：立即成交并取消剩余
     * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
     * <p>
     * <p>
     * <p>
     * 策略订单类型
     * conditional：单向止盈止损
     * oco：双向止盈止损
     * trigger：计划委托
     * move_order_stop：移动止盈止损
     * iceberg：冰山委托
     * twap：时间加权委托
     */
    private String ordType;

    /**
     * 交易数量，表示要购买或者出售的数量。
     * 当币币/币币杠杆以限价买入和卖出时，指交易货币数量。
     * 当币币/币币杠杆以市价买入时，指计价货币的数量。
     * 当币币/币币杠杆以市价卖出时，指交易货币的数量。
     * 当交割、永续、期权买入和卖出时，指合约张数。
     */
    private String sz;

    /**
     * 委托价格，仅适用于limit、post_only、fok、ioc类型的订单
     */
    private String px;

    /**
     * 收益，适用于有成交的平仓订单，其他情况均为0
     */
    private String pnl;

    /**
     * 累计成交数量
     * 对于币币和杠杆，单位为交易货币，如 BTC-USDT, 单位为 BTC；对于市价单，无论tgtCcy是base_ccy，还是quote_ccy，单位均为交易货币；
     * 对于交割、永续以及期权，单位为张。
     */
    private String accFillSz;

    /**
     * 成交均价，如果成交数量为0，该字段也为""
     */
    private String avgPx;

    /**
     * 订单状态
     * canceled：撤单成功
     * live：等待成交
     * partially_filled：部分成交
     * filled：完全成交
     */
    private String state;

    /**
     * 杠杆倍数，0.01到125之间的数值，仅适用于 币币杠杆/交割/永续
     */
    private String lever;

    /**
     * 止盈触发价，如果填写此参数，必须填写 止盈委托价
     */
    private String tpTriggerPx;

    /**
     * 止盈委托价，如果填写此参数，必须填写 止盈触发价
     * 委托价格为-1时，执行市价止盈
     */
    private String tpOrdPx;


    /**
     * 止盈触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     * 默认为last
     */
    private String tpTriggerPxType;


    /**
     * 交易手续费币种
     */
    private String feeCcy;

    /**
     * 手续费与返佣
     * 对于币币和杠杆，为订单交易累计的手续费，平台向用户收取的交易手续费，为负数。如： -0.01
     * 对于交割、永续和期权，为订单交易累计的手续费和返佣
     */
    private String fee;

    /**
     * 订单状态更新时间，Unix时间戳的毫秒数格式，如：1597026383085
     */
    private Long uTime;

    /**
     * 订单创建时间，Unix时间戳的毫秒数格式， 如 ：1597026383085
     */
    private Long cTime;


    private String tag;

    /**
     * 策略委托单ID，策略订单触发时有值，否则为""
     */
    private String algoId;

    /**
     * 最新成交时间
     */
    private Long fillTime;

    /**
     * 下单附带止盈止损时，客户自定义的策略订单ID
     * 字母（区分大小写）与数字的组合，可以是纯字母、纯数字且长度要在1-32位之间。
     * 订单完全成交，下止盈止损委托单时，该值会传给algoClOrdId
     */
    private String attachAlgoClOrdId;

    /**
     * 止损触发价，如果填写此参数，必须填写 止损委托价
     */
    private String slTriggerPx;

    /**
     * 止损委托价，如果填写此参数，必须填写 止损触发价
     * 委托价格为-1时，执行市价止损
     */
    private String slOrdPx;
    /**
     * 止损触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     * 默认为last
     */
    private String slTriggerPxType;

}
