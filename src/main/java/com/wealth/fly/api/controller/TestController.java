package com.wealth.fly.api.controller;

import com.wealth.fly.common.DateUtil;
import com.wealth.fly.core.Monitor;
import com.wealth.fly.core.config.ConfigService;
import com.wealth.fly.core.constants.DataGranularity;
import com.wealth.fly.core.constants.GridLogType;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.dao.KLineDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.GridLog;
import com.wealth.fly.core.entity.KLine;
import com.wealth.fly.core.exchanger.Exchanger;
import com.wealth.fly.core.exchanger.ExchangerManager;
import com.wealth.fly.core.model.GridStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class TestController {

    @Resource
    private GridLogDao gridLogDao;
    @Resource
    private GridDao gridDao;
    @Resource
    private KLineDao kLineDao;
    @Resource
    private ConfigService configService;

    private Integer strategyId = 3;


    @RequestMapping("/test")
    public Object proxyOkex() {

        return "test ok...";
    }

    @RequestMapping("/stop")
    public void stop() {
        log.info(">>>>>> stopAll");
        Monitor.stopAll = true;
    }

    @RequestMapping("/open")
    public void open() {
        log.info(">>>>> openAll");
        Monitor.stopAll = false;
    }

    @RequestMapping("/preview")
    public String preview() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        GridStrategy gridStrategy = configService.getGridStrategy(strategyId);

        Exchanger exchanger = ExchangerManager.getExchangerByGridStrategy(gridStrategy.getId());
        String markPrice = exchanger.getMarkPriceByInstId(gridStrategy.getInstId()).getMarkPx();
        List<Grid> activeGridList = gridDao.listByStatusOrderByBuyPrice(Collections.singletonList(GridStatus.ACTIVE.getCode()), strategyId, 1);

        List<Grid> waitingList = gridDao.listGrids(strategyId, new BigDecimal(markPrice), 10);
        List<Grid> pendingGridList = null;
        if (!CollectionUtils.isEmpty(waitingList)) {
            pendingGridList = waitingList.stream()
                    .filter(g -> g.getStatus() == GridStatus.PENDING.getCode().intValue())
                    .collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("markPrice>>>>").append(markPrice).append("<br/>");
        sb.append("最近止盈点>>>>").append(CollectionUtils.isEmpty(activeGridList) ? "无" : activeGridList.get(0).getSellPrice()).append("<br/>");
        sb.append("最近激活点>>>>").append(CollectionUtils.isEmpty(pendingGridList) ? "无" : pendingGridList.get(0).getBuyPrice()).append("<br/>");

        Date now = new Date();
        printMacd(now, DataGranularity.FIFTEEN_MINUTES, sb);
        printMacd(now, DataGranularity.ONE_HOUR, sb);

        sb.append("stopAll>>>>").append(Monitor.stopAll).append("<br/>");
        sb.append("gridStatusLastFetchTime>>>>").append(Monitor.gridStatusLastFetchTime == null ? "无" : sdf.format(Monitor.gridStatusLastFetchTime)).append("<br/>");
        sb.append("markPriceLastFetchTime>>>>").append(Monitor.markPriceLastFetchTime == null ? "无" : sdf.format(Monitor.markPriceLastFetchTime)).append("<br/>");
        sb.append("<br/><br/>").append("==========操作日志=========<br/>");
        List<GridLog> gridLogs = gridLogDao.listRecentLogs(20);
        if (gridLogs != null) {
            for (GridLog gridLog : gridLogs) {
                if (gridLog.getType() == GridLogType.GRID_FINISHED_PROFIT.getCode()) {
                    sb.append("<font color=\"red\">");
                }
                sb.append(sdf.format(gridLog.getCreatedAt()) + ">>>>" + gridLog.getMessage() + "<br/>");
                if (gridLog.getType() == GridLogType.GRID_FINISHED_PROFIT.getCode()) {
                    sb.append("</font>");
                }
            }
        }

        List<Grid> nonIdleList = gridDao.listByStatusOrderByBuyPrice(Arrays.asList(GridStatus.ACTIVE.getCode(), GridStatus.PENDING.getCode()), strategyId, 100);
        sb.append("<br/><br/>").append("==========活跃网格=========<br/>");
        if (nonIdleList != null) {
            for (Grid grid : nonIdleList) {
                sb.append(String.format("%s-%s-%s-%s", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum(), grid.getStatus())).append("<br/>");
            }
        }

        sb.append("<br/><br/>").append("==========排队网格=========<br/>");
        if (waitingList != null) {
            for (Grid grid : waitingList) {
                sb.append(String.format("%s-%s-%s-%s", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum(), grid.getStatus())).append("<br/>");
            }
        }

        return sb.toString();
    }


    private void printMacd(Date now, DataGranularity dataGranularity, StringBuilder sb) {
        Long preDataTime = DateUtil.getLatestKLineDataTime(now, dataGranularity);
        Long prePreDataTime = DateUtil.getPreKLineDataTime(preDataTime, dataGranularity);

        KLine prePreKLine = kLineDao.getKlineByDataTime(dataGranularity.name(), prePreDataTime);
        KLine preKLine = kLineDao.getKlineByDataTime(dataGranularity.name(), preDataTime);

        sb.append("macd >>>" + dataGranularity.name() + " " + (prePreKLine == null ? "" : prePreKLine.getMacd()) + "---" + (preKLine == null ? "" : preKLine.getMacd()));
    }


}
