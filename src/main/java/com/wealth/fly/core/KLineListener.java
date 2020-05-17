package com.wealth.fly.core;

import com.wealth.fly.core.entity.KLine;

public interface KLineListener {
    void onNewKLine(KLine kLine);
}
