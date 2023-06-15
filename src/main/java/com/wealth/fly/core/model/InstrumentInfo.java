package com.wealth.fly.core.model;

import lombok.Data;

/**
 * @author : lisong
 * @date : 2023/5/11
 */
@Data
public class InstrumentInfo {
    /**
     * 产品id， 如 BTC-USD-SWAP
     */
    private String instId;
    /**
     * 标的指数，如 BTC-USD，仅适用于交割/永续/期权
     */
    private String uly;
    /**
     * 交易品种，如 BTC-USD，仅适用于交割/永续/期权
     */
    private String instFamily;

    /**
     * 盈亏结算和保证金币种，如 BTC 仅适用于交割/永续/期权
     */
    private String settleCcy;

    /**
     * 合约面值，仅适用于交割/永续/期权
     */
    private String ctVal;

    /**
     * 合约面值计价币种，仅适用于交割/永续/期权
     */
    private String ctMult;

    /**
     * 期权类型，C或P 仅适用于期权
     */
    private String optType;

    /**
     * 行权价格，仅适用于期权
     */
    private String stk;

    /**
     * 上线日期
     * Unix时间戳的毫秒数格式，如 1597026383085
     */
    private Long listTime;

    /**
     * 交割/行权日期，仅适用于交割 和 期权
     * Unix时间戳的毫秒数格式，如 1597026383085
     */
    private Long expTime;

    /**
     * 产品状态
     * live：交易中
     * suspend：暂停中
     * preopen：预上线，如：交割和期权的新合约在 live 之前，会有 preopen 状态
     * test：测试中（测试产品，不可交易）
     */
    private String state;
}
