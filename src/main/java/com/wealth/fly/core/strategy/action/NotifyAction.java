package com.wealth.fly.core.strategy.action;

import com.wealth.fly.core.SmsUtil;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class NotifyAction implements StrategyAction {

    private static final Logger LOGGER= LoggerFactory.getLogger(NotifyAction.class);

    @Override
    public void onOpenStock(Strategy strategy, KLine kLine) {
        //目前的短信参数不能有特殊符号
        String priceStr = kLine.getClose().toPlainString();
        if (priceStr.contains(".")) {
            priceStr = priceStr.substring(0, priceStr.indexOf("."));
        }
        SmsUtil.sendOpenStockSms(priceStr);
        LOGGER.info("send sms success");
    }

    @Override
    public void onCloseStock(Strategy openStrategy, KLine openKLine, Strategy closeStrategy, BigDecimal closePrice, long closeDataTime) {

    }
}
