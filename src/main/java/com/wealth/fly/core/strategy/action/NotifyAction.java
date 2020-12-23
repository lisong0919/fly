package com.wealth.fly.core.strategy.action;

import com.wealth.fly.core.SmsUtil;
import com.wealth.fly.core.entity.CloseStock;
import com.wealth.fly.core.entity.OpenStock;
import com.wealth.fly.core.strategy.Strategy;
import com.wealth.fly.core.strategy.StrategyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NotifyAction implements StrategyAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyAction.class);

    @Override
    public void onOpenStock(Strategy strategy, OpenStock openStock) {
        //目前的短信参数不能有特殊符号
        String priceStr = openStock.getOpenPrice().toPlainString();
        if (priceStr.contains(".")) {
            priceStr = priceStr.substring(0, priceStr.indexOf("."));
        }
        SmsUtil.sendOpenStockSms(priceStr);
        LOGGER.info("send sms success");
    }

    @Override
    public void onCloseStock(Strategy openStrategy, Strategy closeStrategy, CloseStock closeStock) {

    }
}
