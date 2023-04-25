package com.wealth.fly.core.listener;

import com.wealth.fly.core.entity.KLine;

public interface KLineListener {
    void onNewKLine(KLine kLine);
}
